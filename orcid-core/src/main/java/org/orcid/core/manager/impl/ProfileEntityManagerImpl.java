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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.orcid.core.manager.ProfileEntityManager;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidType;
import org.orcid.persistence.dao.ProfileDao;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 2011-2012 ORCID
 * 
 * <p>
 * 
 * @author Declan Newman (declan) Date: 10/02/2012
 */
@Service("profileEntityManager")
public class ProfileEntityManagerImpl implements ProfileEntityManager {

    @Resource
    private ProfileDao profileDao;

    @Override
    public ProfileEntity findByOrcid(String orcid) {
        return profileDao.find(orcid);
    }

    @Override
    public boolean orcidExists(String orcid) {
        return profileDao.orcidExists(orcid);
    }

    @Override
    public boolean hasBeenGivenPermissionTo(String giverOrcid, String receiverOrcid) {
        return profileDao.hasBeenGivenPermissionTo(giverOrcid, receiverOrcid);
    }

    public boolean existsAndNotClaimedAndBelongsTo(String messageOrcid, String clientId) {
        return profileDao.existsAndNotClaimedAndBelongsTo(messageOrcid, clientId);
    }

    @Override
    public Long getConfirmedProfileCount() {
        return profileDao.getConfirmedProfileCount();
    }

    /**
     * Updates a profile with the given OrcidProfile object
     * 
     * @param orcidProfile
     *            The object that will be used to update the database profile
     * @return true if the profile was successfully updated on database, false
     *         otherwise
     * */
    @Override
    public boolean updateProfile(OrcidProfile orcidProfile) {
        ProfileEntity profile = generateProfileEntityWithBio(orcidProfile);
        return profileDao.updateProfile(profile);
    }

    /**
     * Updates a profile entity object on database.
     * 
     * @param profile
     *            The profile object to update
     * @return true if the profile was successfully updated.
     * */
    @Override
    public boolean updateProfile(ProfileEntity profile) {
        return profileDao.updateProfile(profile);
    }

    /**
     * Generate a ProfileEntity object with the bio information populated from
     * the info that comes from the OrcidProfile parameter
     * 
     * @param orcidProfile
     * @return A Profile Entity containing the bio information that comes in the
     *         OrcidProfile parameter
     * */
    private ProfileEntity generateProfileEntityWithBio(OrcidProfile orcidProfile) {
        ProfileEntity profile = new ProfileEntity();
        profile.setCreditName(orcidProfile.getOrcidBio().getPersonalDetails().getCreditName().getContent());
        profile.setFamilyName(orcidProfile.getOrcidBio().getPersonalDetails().getFamilyName().getContent());
        profile.setGivenNames(orcidProfile.getOrcidBio().getPersonalDetails().getGivenNames().getContent());
        profile.setBiography(orcidProfile.getOrcidBio().getBiography().getContent());
        profile.setIso2Country(orcidProfile.getOrcidBio().getContactDetails().getAddress().getCountry().getValue());
        profile.setBiographyVisibility(orcidProfile.getOrcidBio().getBiography().getVisibility());
        profile.setKeywordsVisibility(orcidProfile.getOrcidBio().getKeywords().getVisibility());
        profile.setResearcherUrlsVisibility(orcidProfile.getOrcidBio().getResearcherUrls().getVisibility());
        profile.setOtherNamesVisibility(orcidProfile.getOrcidBio().getPersonalDetails().getOtherNames().getVisibility());
        profile.setCreditNameVisibility(orcidProfile.getOrcidBio().getPersonalDetails().getCreditName().getVisibility());
        profile.setProfileAddressVisibility(orcidProfile.getOrcidBio().getContactDetails().getAddress().getCountry().getVisibility());
        profile.setId(orcidProfile.getOrcidIdentifier().getPath());
        return profile;
    }

    /**
     * Deprecates a profile
     * 
     * @param deprecatedProfile
     *            The profile that want to be deprecated
     * @param primaryProfile
     *            The primary profile for the deprecated profile
     * @return true if the account was successfully deprecated, false otherwise
     * */
    @Override
    public boolean deprecateProfile(ProfileEntity deprecatedProfile, ProfileEntity primaryProfile) {
        boolean result = profileDao.deprecateProfile(deprecatedProfile.getId(), primaryProfile.getId());
        if (result)
            profileDao.refresh(deprecatedProfile);
        return result;
    }

    /**
     * Return the list of profiles that belongs to the provided OrcidType
     * 
     * @param type
     *            OrcidType that indicates the profile type we want to fetch
     * @return the list of profiles that belongs to the specified type
     * */
    @Override
    public List<ProfileEntity> findProfilesByOrcidType(OrcidType type) {
        if (type == null)
            return new ArrayList<ProfileEntity>();
        return profileDao.findProfilesByOrcidType(type);
    }

    /**
     * Enable developer tools
     * 
     * @param profile
     *            The profile to update
     * @return true if the developer tools where enabled on that profile
     * */
    @Override
    public boolean enableDeveloperTools(OrcidProfile profile) {
        boolean result = profileDao.updateDeveloperTools(profile.getOrcidIdentifier().getPath(), true);
        return result;
    }

    /**
     * Disable developer tools
     * 
     * @param profile
     *            The profile to update
     * @return true if the developer tools where disabeled on that profile
     * */
    @Override
    public boolean disableDeveloperTools(OrcidProfile profile) {
        boolean result = profileDao.updateDeveloperTools(profile.getOrcidIdentifier().getPath(), false);
        return result;
    }

    /**
     * ORCID SOCIAL PROJECT
     * */
    @Value("${org.orcid.core.baseUri:http://orcid.org}")
    private String baseUri;
    private Twitter twitter = null;
    private RequestToken requestToken = null;
    private String authUrl = null;
    private static String KEY = "soCTKWWByfjq91SxuaQRh4Gnk";
    private static String SECRET = "sjtMHV2myGQ6qZAoKROoKaNfvRFvyDtIuGn0cKdy5h0RQ55NPM";
    
    /**
     * 
     * */
    private void init() throws TwitterException {
        twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(KEY, SECRET);
        requestToken = twitter.getOAuthRequestToken();
        authUrl = requestToken.getAuthorizationURL();
    }

    /**
     * 
     * */
    public String getAuthUrl() {
        if (twitter == null) {
            try {
                init();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return authUrl;
    }

    /**
     * 
     * */
    public void processTwitterNotifications() throws TwitterException {
        if (twitter == null) {
            try {
                init();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        List<ProfileEntity> profiles = this.getAllProfilesToTweet();

        for (ProfileEntity profile : profiles) {
            String token = profile.getTwitterToken();
            String secret = profile.getTwitterSecret();

            ConfigurationBuilder cb = new ConfigurationBuilder();

            cb.setDebugEnabled(true);

            cb.setOAuthConsumerKey(KEY);
            cb.setOAuthConsumerSecret(SECRET);
            cb.setOAuthAccessToken(token);
            cb.setOAuthAccessTokenSecret(secret);

            TwitterFactory tf = new TwitterFactory(cb.build());

            Twitter twitter = tf.getInstance();
            
            String url = baseUri + '/' + profile.getId();
           
            
            twitter.updateStatus("I just updated my orcid profile! Checkit out at " + url );

        }
    }

    /**
     * 
     * */
    public boolean enableTwitter(String orcid, String token, String secret) throws TwitterException {
        AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, secret);

        String s1 = accessToken.getToken();
        String s2 = accessToken.getTokenSecret();

        return profileDao.enableTwitter(orcid, s1, s2);
    }

    public String getTwitterKey(String orcid) {
        return profileDao.getTwitterKey(orcid);
    }

    public boolean disableTwitter(String orcid) {
        return profileDao.disableTwitter(orcid);
    }

    public List<ProfileEntity> getAllProfilesToTweet() {
        return profileDao.getAllProfilesToTweet();
    }
}
