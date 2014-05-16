/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.core.manager.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.orcid.core.manager.EncryptionManager;
import org.orcid.core.manager.NotificationManager;
import org.orcid.core.manager.TemplateManager;
import org.orcid.core.profileEvent.ProfileEventConstants;
import org.orcid.jaxb.model.message.ApplicationSummary;
import org.orcid.jaxb.model.message.Applications;
import org.orcid.jaxb.model.message.Delegation;
import org.orcid.jaxb.model.message.DelegationDetails;
import org.orcid.jaxb.model.message.Email;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidType;
import org.orcid.jaxb.model.message.PersonalDetails;
import org.orcid.jaxb.model.message.SendChangeNotifications;
import org.orcid.jaxb.model.message.Source;
import org.orcid.persistence.dao.GenericDao;
import org.orcid.persistence.dao.ProfileDao;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.persistence.jpa.entities.ProfileEventEntity;
import org.orcid.persistence.jpa.entities.ProfileEventType;
import org.orcid.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSender;

/**
 * @author Will Simpson
 */
public class NotificationManagerImpl implements NotificationManager {

    private static final String UPDATE_NOTIFY_ORCID_ORG = "update@notify.orcid.org";

    private static final String SUPPORT_VERIFY_ORCID_ORG = "support@verify.orcid.org";

    private static final String RESET_NOTIFY_ORCID_ORG = "reset@notify.orcid.org";

    private static final String CLAIM_NOTIFY_ORCID_ORG = "claim@notify.orcid.org";
    
    private static final String DEACTIVATE_NOTIFY_ORCID_ORG = "deactivate@notify.orcid.org";
    
    private static final String AMEND_NOTIFY_ORCID_ORG = "amend@notify.orcid.org";
    
    private static final String DELEGATE_NOTIFY_ORCID_ORG = "delegate@notify.orcid.org";
    
    private static final String EMAIL_CHANGED_NOTIFY_ORCID_ORG = "email-changed@notify.orcid.org";
    
    private static final String ACCOUNT_DEPRECATED_NOTIFY_ORCID_ORG = "account-deprecate@notify.orcid.org";

    @Resource
    private MessageSource messages;

    @Resource
    private MailGunManager mailGunManager;

    private MailSender mailSender;

    private String fromAddress;

    private String supportAddress;

    private String LAST_RESORT_ORCID_USER_EMAIL_NAME = "ORCID Registry User";

    private String ORCID_PRIVACY_POLICY_UPDATES = "ORCID - Privacy Policy Updates";

    private URI baseUri;

    private boolean apiRecordCreationEmailEnabled;

    private TemplateManager templateManager;

    private EncryptionManager encryptionManager;
    

    @Resource
    private GenericDao<ProfileEventEntity, Long> profileEventDao;

    @Resource
    private ProfileDao profileDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManagerImpl.class);

    @Required
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Required
    public void setSupportAddress(String supportAddress) {
        this.supportAddress = supportAddress;
    }

    @Required
    public void setBaseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    public boolean isApiRecordCreationEmailEnabled() {
        return apiRecordCreationEmailEnabled;
    }

    public void setApiRecordCreationEmailEnabled(boolean apiRecordCreationEmailEnabled) {
        this.apiRecordCreationEmailEnabled = apiRecordCreationEmailEnabled;
    }

    @Override
    @Required
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Required
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Required
    public void setEncryptionManager(EncryptionManager encryptionManager) {
        this.encryptionManager = encryptionManager;
    }    

    public void setProfileEventDao(GenericDao<ProfileEventEntity, Long> profileEventDao) {
        this.profileEventDao = profileEventDao;
    }

    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    @Override
    public void sendOrcidDeactivateEmail(OrcidProfile orcidToDeactivate, URI baseUri) {
        // Create verification url

        Map<String, Object> templateParams = new HashMap<String, Object>();
        
        String subject = getSubject("email.subject.deactivate", orcidToDeactivate);
        String email = orcidToDeactivate.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
        
        String emailFriendlyName = deriveEmailFriendlyName(orcidToDeactivate);
        templateParams.put("emailName", emailFriendlyName);
        templateParams.put("orcid", orcidToDeactivate.getOrcidIdentifier().getPath());
        templateParams.put("baseUri", baseUri);
        templateParams.put("deactivateUrlEndpoint", "/account/confirm-deactivate-orcid");
        templateParams.put("subject", subject);

        addMessageParams(templateParams, orcidToDeactivate);

        // Generate body from template
        String body = templateManager.processTemplate("deactivate_orcid_email.ftl", templateParams);
        // Generate html from template
        String html = templateManager.processTemplate("deactivate_orcid_email_html.ftl", templateParams);
        
        mailGunManager.sendEmail(DEACTIVATE_NOTIFY_ORCID_ORG, email, subject, body, html);
    }

    // look like the following is our best best for i18n emails
    // http://stackoverflow.com/questions/9605828/email-internationalization-using-velocity-freemarker-templates
    public void sendVerificationEmail(OrcidProfile orcidProfile, URI baseUri, String email) {
        Map<String, Object> templateParams = new HashMap<String, Object>();

        String emailFriendlyName = deriveEmailFriendlyName(orcidProfile);
        templateParams.put("emailName", emailFriendlyName);
        templateParams.put("subject", getSubject("email.subject.verify_reminder", orcidProfile));
        String verificationUrl = createVerificationUrl(email, baseUri);
        templateParams.put("verificationUrl", verificationUrl);
        templateParams.put("orcid", orcidProfile.getOrcidIdentifier().getPath());
        templateParams.put("baseUri", baseUri);

        addMessageParams(templateParams, orcidProfile);

        // Generate body from template
        String body = templateManager.processTemplate("verification_email.ftl", templateParams);
        String htmlBody = templateManager.processTemplate("verification_email_html.ftl", templateParams);
        mailGunManager.sendEmail(SUPPORT_VERIFY_ORCID_ORG, email, getSubject("email.subject.verify_reminder", orcidProfile), body, htmlBody);
    }

    // look like the following is our best best for i18n emails
    // http://stackoverflow.com/questions/9605828/email-internationalization-using-velocity-freemarker-templates
    public boolean sendPrivPolicyEmail2014_03(OrcidProfile orcidProfile, URI baseUri) {
        String email = orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
        Map<String, Object> templateParams = new HashMap<String, Object>();

        String emailFriendlyName = deriveEmailFriendlyName(orcidProfile);
        templateParams.put("emailName", emailFriendlyName);
        if (!orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().isVerified()) {
            String verificationUrl = createVerificationUrl(email, baseUri);
            templateParams.put("verificationUrl", verificationUrl);
        }
        templateParams.put("orcid", orcidProfile.getOrcidIdentifier().getPath());
        templateParams.put("baseUri", baseUri);

        addMessageParams(templateParams, orcidProfile);

        String text = templateManager.processTemplate("priv_policy_upate_2014_03.ftl", templateParams);
        String html = templateManager.processTemplate("priv_policy_upate_2014_03_html.ftl", templateParams);

        return mailGunManager.sendEmail(UPDATE_NOTIFY_ORCID_ORG, email, ORCID_PRIVACY_POLICY_UPDATES, text, html);
    }

    private void addMessageParams(Map<String, Object> templateParams, OrcidProfile orcidProfile) {
        Locale locale = null;
        if (orcidProfile.getOrcidPreferences() != null && orcidProfile.getOrcidPreferences().getLocale() != null) {
            orcidProfile.getOrcidPreferences().getLocale().value();
            locale = LocaleUtils.toLocale(orcidProfile.getOrcidPreferences().getLocale().value());
        } else {
            locale = LocaleUtils.toLocale("en");
        }
        templateParams.put("messages", this.messages);
        templateParams.put("messageArgs", new Object[0]);
        templateParams.put("locale", locale);
    }

    private String getSubject(String code, OrcidProfile orcidProfile) {
        Locale locale = null;
        if (orcidProfile.getOrcidPreferences() != null && orcidProfile.getOrcidPreferences().getLocale() != null) {
            orcidProfile.getOrcidPreferences().getLocale().value();
            locale = LocaleUtils.toLocale(orcidProfile.getOrcidPreferences().getLocale().value());
        } else {
            locale = LocaleUtils.toLocale("en");
        }
        return messages.getMessage(code, null, locale);
    }

    public void sendVerificationReminderEmail(OrcidProfile orcidProfile, String email) {
        Map<String, Object> templateParams = new HashMap<String, Object>();

        String emailFriendlyName = deriveEmailFriendlyName(orcidProfile);
        templateParams.put("emailName", emailFriendlyName);
        String verificationUrl = createVerificationUrl(email, baseUri);
        templateParams.put("verificationUrl", verificationUrl);
        templateParams.put("orcid", orcidProfile.getOrcidIdentifier().getPath());
        templateParams.put("email", email);
        templateParams.put("subject", getSubject("email.subject.verify_reminder", orcidProfile));
        templateParams.put("baseUri", baseUri);

        addMessageParams(templateParams, orcidProfile);

        // Generate body from template
        String body = templateManager.processTemplate("verification_reminder_email.ftl", templateParams);
        String htmlBody = templateManager.processTemplate("verification_reminder_email_html.ftl", templateParams);
        mailGunManager.sendEmail(SUPPORT_VERIFY_ORCID_ORG, email, getSubject("email.subject.verify_reminder", orcidProfile), body, htmlBody);
    }

    public String deriveEmailFriendlyName(OrcidProfile orcidProfile) {
        if (orcidProfile.getOrcidBio() != null && orcidProfile.getOrcidBio().getPersonalDetails() != null) {
            PersonalDetails personalDetails = orcidProfile.getOrcidBio().getPersonalDetails();
            // all this should never be null as given names are required for
            // all...
            if (personalDetails.getGivenNames() != null) {
                String givenName = personalDetails.getGivenNames().getContent();
                String familyName = personalDetails.getFamilyName() != null && !StringUtils.isBlank(personalDetails.getFamilyName().getContent()) ? " "
                        + personalDetails.getFamilyName().getContent() : "";
                return givenName + familyName;
            }
        }

        return LAST_RESORT_ORCID_USER_EMAIL_NAME;
    }

    @Override
    public void sendPasswordResetEmail(String submittedEmail, OrcidProfile orcidProfile, URI baseUri) {

        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        templateParams.put("emailName", deriveEmailFriendlyName(orcidProfile));
        templateParams.put("orcid", orcidProfile.getOrcidIdentifier().getPath());
        templateParams.put("subject", getSubject("email.subject.reset", orcidProfile));
        templateParams.put("baseUri", baseUri);
        // Generate body from template
        String resetUrl = createResetEmail(orcidProfile, baseUri);
        templateParams.put("passwordResetUrl", resetUrl);

        addMessageParams(templateParams, orcidProfile);

        String primaryEmail = orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();

        // Generate body from template
        String body = templateManager.processTemplate("reset_password_email.ftl", templateParams);
        String htmlBody = templateManager.processTemplate("reset_password_email_html.ftl", templateParams);
        mailGunManager.sendEmail(RESET_NOTIFY_ORCID_ORG, primaryEmail, getSubject("email.subject.reset", orcidProfile), body, htmlBody);
    }

    @Override
    public void sendAmendEmail(OrcidProfile amendedProfile, String amenderOrcid) {
        if (amenderOrcid == null) {
            LOGGER.debug("Not sending amend email, because amender is null: {}", amendedProfile);
            return;
        }
        if (amenderOrcid.equals(amendedProfile.getOrcidIdentifier().getPath())) {
            LOGGER.debug("Not sending amend email, because self edited: {}", amendedProfile);
            return;
        }
        SendChangeNotifications sendChangeNotifications = amendedProfile.getOrcidInternal().getPreferences().getSendChangeNotifications();
        if (sendChangeNotifications == null || !sendChangeNotifications.isValue()) {
            LOGGER.debug("Not sending amend email, because option to send change notifications not set to true: {}", amendedProfile);
            return;
        }
        if (OrcidType.ADMIN.equals(profileDao.retrieveOrcidType(amenderOrcid))) {
            LOGGER.debug("Not sending amend email, because modified by admin ({}): {}", amenderOrcid, amendedProfile);
            return;
        }
        
        String subject = getSubject("email.subject.amend", amendedProfile);
        String email = amendedProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
        
        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        templateParams.put("emailName", deriveEmailFriendlyName(amendedProfile));
        templateParams.put("orcid", amendedProfile.getOrcidIdentifier().getPath());
        templateParams.put("amenderName", extractAmenderName(amendedProfile, amenderOrcid));
        templateParams.put("baseUri", baseUri);
        templateParams.put("subject", subject);

        addMessageParams(templateParams, amendedProfile);

        // Generate body from template
        String body = templateManager.processTemplate("amend_email.ftl", templateParams);
        // Generate html from template
        String html = templateManager.processTemplate("amend_email_html.ftl", templateParams);
        
        mailGunManager.sendEmail(AMEND_NOTIFY_ORCID_ORG, email, subject, body, html);
        
    }

    @Override
    public void sendNotificationToAddedDelegate(OrcidProfile orcidUserGrantingPermission, List<DelegationDetails> delegatesGrantedByUser) {
        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        String subject = getSubject("email.subject.added_as_delegate", orcidUserGrantingPermission);
        
        for (DelegationDetails newDelegation : delegatesGrantedByUser) {                       
            ProfileEntity delegateProfileEntity = profileDao.find(newDelegation.getDelegateSummary().getOrcidIdentifier().getPath());
            Boolean sendChangeNotifications = delegateProfileEntity.getSendChangeNotifications();
            if (sendChangeNotifications == null || !sendChangeNotifications) {
                LOGGER.debug("Not sending added delegate email, because option to send change notifications not set to true for delegate: {}", delegateProfileEntity.getId());
                return;
            }
                        
            String grantingOrcidEmail = orcidUserGrantingPermission.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
            String emailNameForDelegate = deriveEmailFriendlyName(delegateProfileEntity);
            String email = delegateProfileEntity.getPrimaryEmail().getId();

            templateParams.put("emailNameForDelegate", emailNameForDelegate);
            templateParams.put("grantingOrcidValue", orcidUserGrantingPermission.getOrcidIdentifier().getPath());
            templateParams.put("grantingOrcidName", deriveEmailFriendlyName(orcidUserGrantingPermission));
            templateParams.put("baseUri", baseUri);
            templateParams.put("grantingOrcidEmail", grantingOrcidEmail);
            templateParams.put("subject", subject);

            addMessageParams(templateParams, orcidUserGrantingPermission);

            // Generate body from template
            String body = templateManager.processTemplate("added_as_delegate_email.ftl", templateParams);
            // Generate html from template
            String html = templateManager.processTemplate("added_as_delegate_email_html.ftl", templateParams);
            
            mailGunManager.sendEmail(DELEGATE_NOTIFY_ORCID_ORG, email, subject, body, html);
        }
    }
    
    @Override
    public void sendEmailAddressChangedNotification(OrcidProfile updatedProfile, Email oldEmail, URI baseUri) {

        // build up old template
        Map<String, Object> templateParams = new HashMap<String, Object>();
        
        String subject = getSubject("email.subject.email_removed", updatedProfile);
        String email = oldEmail.getValue();        
        String emailFriendlyName = deriveEmailFriendlyName(updatedProfile);
        templateParams.put("emailName", emailFriendlyName);
        String verificationUrl = createVerificationUrl(updatedProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue(), baseUri);
        templateParams.put("verificationUrl", verificationUrl);
        templateParams.put("oldEmail", oldEmail.getValue());
        templateParams.put("newEmail", updatedProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue());
        templateParams.put("orcid", updatedProfile.getOrcidIdentifier().getPath());
        templateParams.put("baseUri", baseUri);
        templateParams.put("subject", subject);

        addMessageParams(templateParams, updatedProfile);

        // Generate body from template
        String body = templateManager.processTemplate("email_removed.ftl", templateParams);
        // Generate html from template
        String html = templateManager.processTemplate("email_removed_html.ftl", templateParams);
        
        mailGunManager.sendEmail(EMAIL_CHANGED_NOTIFY_ORCID_ORG, email, subject, body, html);
    }

    @Override
    public void sendApiRecordCreationEmail(String toEmail, OrcidProfile createdProfile) {
        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        templateParams.put("emailName", deriveEmailFriendlyName(createdProfile));
        templateParams.put("orcid", createdProfile.getOrcidIdentifier().getPath());
        templateParams.put("subject", getSubject("email.subject.api_record_creation", createdProfile));
        Source source = createdProfile.getOrcidHistory().getSource();
        templateParams.put("creatorName", source == null ? "" : source.getSourceName().getContent());
        templateParams.put("baseUri", baseUri);
        String verificationUrl = createClaimVerificationUrl(createdProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue(), baseUri);
        templateParams.put("verificationUrl", verificationUrl);

        addMessageParams(templateParams, createdProfile);

        String email = createdProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
        // Generate body from template
        String body = templateManager.processTemplate("api_record_creation_email.ftl", templateParams);
        String htmlBody = templateManager.processTemplate("api_record_creation_email_html.ftl", templateParams);

        // Send message
        if (apiRecordCreationEmailEnabled) {
            mailGunManager.sendEmail(CLAIM_NOTIFY_ORCID_ORG, email, getSubject("email.subject.api_record_creation", createdProfile), body, htmlBody);
        } else {
            LOGGER.debug("Not sending API record creation email, because option is disabled. Message would have been: {}", body);
        }
    }

    @Override
    public void sendClaimReminderEmail(OrcidProfile orcidProfile, int daysUntilActivation) {
        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        templateParams.put("emailName", deriveEmailFriendlyName(orcidProfile));
        String orcid = orcidProfile.getOrcidIdentifier().getPath();
        templateParams.put("orcid", orcid);
        templateParams.put("subject", getSubject("email.subject.claim_reminder", orcidProfile));
        Source source = orcidProfile.getOrcidHistory().getSource();
        templateParams.put("creatorName", source == null ? "" : source.getSourceName().getContent());
        templateParams.put("baseUri", baseUri);
        templateParams.put("daysUntilActivation", daysUntilActivation);
        Email primaryEmail = orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail();
        if (primaryEmail == null) {
            LOGGER.info("Cant send claim reminder email if primary email is null: {}", orcid);
            return;
        }
        String verificationUrl = createClaimVerificationUrl(primaryEmail.getValue(), baseUri);
        templateParams.put("verificationUrl", verificationUrl);

        addMessageParams(templateParams, orcidProfile);

        String email = orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
        // Generate body from template
        String body = templateManager.processTemplate("api_record_creation_email.ftl", templateParams);
        String htmlBody = templateManager.processTemplate("api_record_creation_email_html.ftl", templateParams);

        // Send message
        if (apiRecordCreationEmailEnabled) {
            mailGunManager.sendEmail(CLAIM_NOTIFY_ORCID_ORG, email, getSubject("email.subject.claim_reminder", orcidProfile), body, htmlBody);
            profileEventDao.persist(new ProfileEventEntity(orcid, ProfileEventType.CLAIM_REMINDER_SENT));
        } else {
            LOGGER.debug("Not sending claim reminder email, because API record creation email option is disabled. Message would have been: {}", body);
        }

    }

    /**
     * 
     * 
     * */
    public String deriveEmailFriendlyName(ProfileEntity profileEntity) {
        String result = LAST_RESORT_ORCID_USER_EMAIL_NAME;
        if (profileEntity.getGivenNames() != null) {
            result = profileEntity.getGivenNames();
            if (!StringUtils.isBlank(profileEntity.getFamilyName())) {
                result += " " + profileEntity.getFamilyName();
            }
        }
        return result;
    }

    /**
     * Substitute the message params with his real values
     * @param templateParams
     * @param profileEntity
     * */
    private void addMessageParams(Map<String, Object> templateParams, ProfileEntity profileEntity) {
        Locale locale = null;
        if (profileEntity.getLocale() != null) {
            locale = LocaleUtils.toLocale(profileEntity.getLocale().value());
        } else {
            locale = LocaleUtils.toLocale("en");
        }
        templateParams.put("messages", this.messages);
        templateParams.put("messageArgs", new Object[0]);
        templateParams.put("locale", locale);
    }

    /**
     * Get the subject of a message given a code and a profile entity
     * @param code
     * @param profileEntity
     * */
    private String getSubject(String code, ProfileEntity profileEntity) {
        Locale locale = null;
        if (profileEntity.getLocale() != null) {
            locale = LocaleUtils.toLocale(profileEntity.getLocale().value());
        } else {
            locale = LocaleUtils.toLocale("en");
        }
        return messages.getMessage(code, null, locale);
    }

    @Override
    public void sendProfileDeprecationEmail(ProfileEntity deprecatedProfile, ProfileEntity primaryProfile) {
        // Send email to deprecated account
        sendProfileDeprecationEmailToDeprecatedAccount(deprecatedProfile, primaryProfile);
        // Send email to primary account
        sendProfileDeprecationEmailToPrimaryAccount(deprecatedProfile, primaryProfile);
        // Store deprecation message
        profileEventDao.persist(new ProfileEventEntity(deprecatedProfile.getId(), ProfileEventType.PROFILE_DEPRECATED, String.format(
                ProfileEventConstants.ADMIN_DEPRECATE_ACCOUNT, deprecatedProfile.getId(), primaryProfile.getId())));
    }

    /**
     * Sends an email to the depreciated account owner
     * @param deprecatedProfile
     * @param primaryProfile 
     * */
    private void sendProfileDeprecationEmailToDeprecatedAccount(ProfileEntity deprecatedProfile, ProfileEntity primaryProfile) {
        String subject = getSubject("email.subject.deprecated_profile", deprecatedProfile);
        String email = deprecatedProfile.getPrimaryEmail().getId();
        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        templateParams.put("emailName", deriveEmailFriendlyName(deprecatedProfile));
        templateParams.put("deprecatedAccount", deprecatedProfile.getId());
        templateParams.put("primaryAccount", deprecatedProfile.getId());
        templateParams.put("subject", subject);
        
        addMessageParams(templateParams, deprecatedProfile);

        // Generate body from template
        String body = templateManager.processTemplate("profile_deprecation_deprecated_profile_email.ftl", templateParams);
        // Generate html from template
        String html = templateManager.processTemplate("profile_deprecation_deprecated_profile_email_html.ftl", templateParams);
        
        // Send message
        if (apiRecordCreationEmailEnabled) {
            mailGunManager.sendEmail(ACCOUNT_DEPRECATED_NOTIFY_ORCID_ORG, email, subject, body, html);
            profileEventDao.persist(new ProfileEventEntity(deprecatedProfile.getId(), ProfileEventType.PROFILE_DEPRECATED));
        } else {
            LOGGER.debug("Not sending profile deprecated email, because API record creation email option is disabled. Message would have been: {}", body);
        }
    }

    /**
     * Send an email to the primary account indicating that an account has been deprecated to his account
     * @param deprecatedProfile
     * @param primaryProfile
     * */
    private void sendProfileDeprecationEmailToPrimaryAccount(ProfileEntity deprecatedProfile, ProfileEntity primaryProfile) {
        String subject = getSubject("email.subject.deprecated_profile_primary", primaryProfile);        
        String email = deprecatedProfile.getPrimaryEmail().getId();
        
        // Create map of template params
        Map<String, Object> templateParams = new HashMap<String, Object>();
        templateParams.put("emailName", deriveEmailFriendlyName(primaryProfile));
        templateParams.put("deprecatedAccount", deprecatedProfile.getId());
        templateParams.put("primaryAccount", deprecatedProfile.getId());
        templateParams.put("subject", subject);
        
        addMessageParams(templateParams, deprecatedProfile);

        // Generate body from template
        String body = templateManager.processTemplate("profile_deprecation_primary_profile_email.ftl", templateParams);
        // Generate html from template
        String html = templateManager.processTemplate("profile_deprecation_primary_profile_email_html.ftl", templateParams);
        
        // Send message
        if (apiRecordCreationEmailEnabled) {
            mailGunManager.sendEmail(ACCOUNT_DEPRECATED_NOTIFY_ORCID_ORG, email, subject, body, html);
            profileEventDao.persist(new ProfileEventEntity(deprecatedProfile.getId(), ProfileEventType.PROFILE_DEPRECATED));
        } else {
            LOGGER.debug("Not sending profile deprecated email, because API record creation email option is disabled. Message would have been: {}", body);
        }
    }

    private String extractAmenderName(OrcidProfile orcidProfile, String amenderOrcid) {
        Delegation delegation = orcidProfile.getOrcidBio().getDelegation();
        if (delegation != null && delegation.getGivenPermissionTo() != null && !delegation.getGivenPermissionTo().getDelegationDetails().isEmpty()) {
            for (DelegationDetails delegationDetails : delegation.getGivenPermissionTo().getDelegationDetails()) {
                if (amenderOrcid.equals(delegationDetails.getDelegateSummary().getOrcidIdentifier().getPath())) {
                    return delegationDetails.getDelegateSummary().getCreditName().getContent();
                }
            }
        }
        Applications applications = orcidProfile.getOrcidBio().getApplications();
        if (applications != null && applications.getApplicationSummary() != null && !applications.getApplicationSummary().isEmpty()) {
            for (ApplicationSummary applicationSummary : applications.getApplicationSummary()) {
                if (amenderOrcid.equals(applicationSummary.getApplicationOrcid().getPath())) {
                    return applicationSummary.getApplicationName().getContent();
                }
            }
        }
        return "";
    }

    private String createClaimVerificationUrl(String email, URI baseUri) {
        return createEmailBaseUrl(email, baseUri, "claim");
    }

    public String createVerificationUrl(String email, URI baseUri) {
        return createEmailBaseUrl(email, baseUri, "verify-email");
    }

    private String createResetEmail(OrcidProfile orcidProfile, URI baseUri) {
        String userEmail = orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue();
        XMLGregorianCalendar date = DateUtils.convertToXMLGregorianCalendarNoTimeZoneNoMillis(new Date());
        String resetParams = MessageFormat.format("email={0}&issueDate={1}", new Object[] { userEmail, date.toXMLFormat() });
        return createEmailBaseUrl(resetParams, baseUri, "reset-password-email");
    }

    public String createEmailBaseUrl(String unencryptedParams, URI baseUri, String path) {
        // Encrypt and encode params
        String encryptedUrlParams = encryptionManager.encryptForExternalUse(unencryptedParams);
        String base64EncodedParams = null;
        try {
            base64EncodedParams = Base64.encodeBase64URLSafeString(encryptedUrlParams.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return String.format("%s/%s/%s", baseUri.toString(), path, base64EncodedParams);
    }
}
