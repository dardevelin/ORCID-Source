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
package org.orcid.core.version.impl;

import org.orcid.core.version.OrcidMessageVersionConverter;
import org.orcid.jaxb.model.message.ActivitiesVisibilityDefault;
import org.orcid.jaxb.model.message.DeveloperToolsEnabled;
import org.orcid.jaxb.model.message.Funding;
import org.orcid.jaxb.model.message.FundingList;
import org.orcid.jaxb.model.message.OrcidActivities;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.Preferences;
import org.orcid.jaxb.model.message.WorkVisibilityDefault;

/**
 * 
 * @author rcpeters
 * 
 */
public class OrcidMessageVersionConverterImplV1_2_rc3ToV1_2_rc4 implements OrcidMessageVersionConverter {

    private static final String FROM_VERSION = "1.2_rc3";
    private static final String TO_VERSION = "1.2_rc4";

    @Override
    public String getFromVersion() {
        return FROM_VERSION;
    }

    @Override
    public String getToVersion() {
        return TO_VERSION;
    }

    @Override
    public OrcidMessage downgradeMessage(OrcidMessage orcidMessage) {
        if (orcidMessage == null) {
            return null;
        }
        orcidMessage.setMessageVersion(FROM_VERSION);
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        downgradeProfile(orcidProfile);
        downgradeSearchResults(orcidMessage);

        return orcidMessage;
    }

    private void downgradeProfile(OrcidProfile orcidProfile) {
        if (orcidProfile != null) {
            if (orcidProfile.getOrcidHistory() != null) {
                if (orcidProfile.getOrcidHistory().getVerifiedEmail() != null) {
                   orcidProfile.getOrcidHistory().setVerifiedEmail(null);
                }
                if (orcidProfile.getOrcidHistory().getVerifiedPrimaryEmail() != null) {
                    orcidProfile.getOrcidHistory().setVerifiedPrimaryEmail(null);
                }
            }
            if (orcidProfile.getOrcidInternal() != null) {
                // earlier versions of the XSD don't have GroupOrcidIdentifier 
                if (orcidProfile.getOrcidInternal().getReferredBy() != null) {
                    orcidProfile.getOrcidInternal().setReferredBy(null);
                }
                
                // earlier versions of the XSD don't have 
                if(orcidProfile.getOrcidInternal().getPreferences() != null && orcidProfile.getOrcidInternal().getPreferences().getDeveloperToolsEnabled() != null) {
                    orcidProfile.getOrcidInternal().getPreferences().setDeveloperToolsEnabled(null);
                }
                
            }

            if (orcidProfile.getOrcidInternal() != null) {
                if (orcidProfile.getOrcidInternal().getPreferences() != null) {
                    Preferences prefs = orcidProfile.getOrcidInternal().getPreferences();
                    if (prefs.getActivitiesVisibilityDefault() != null && prefs.getActivitiesVisibilityDefault().getValue() != null) {
                        prefs.setWorkVisibilityDefault(new WorkVisibilityDefault(prefs.getActivitiesVisibilityDefault().getValue()));
                        prefs.setActivitiesVisibilityDefault(null);
                    }
                }
            }
            
            //Previews versions doesnt have organization defined funding type
            if(orcidProfile.getOrcidActivities() != null) {
                OrcidActivities activities = orcidProfile.getOrcidActivities();
                if(activities.getFundings() != null) {
                    FundingList fundingList = activities.getFundings();
                    for(Funding funding : fundingList.getFundings()) {
                        funding.setOrganizationDefinedFundingType(null);
                    }
                }
                
            }
        }
    }
    
    private void upgradeProfile(OrcidProfile orcidProfile) {
        if (orcidProfile != null) {
            if (orcidProfile.getOrcidInternal() != null) {
                if (orcidProfile.getOrcidInternal().getPreferences() != null) {
                    Preferences prefs = orcidProfile.getOrcidInternal().getPreferences();
                    if (prefs.getWorkVisibilityDefault() != null && prefs.getWorkVisibilityDefault().getValue() != null) {
                        prefs.setActivitiesVisibilityDefault(new ActivitiesVisibilityDefault(prefs.getWorkVisibilityDefault().getValue()));
                        prefs.setWorkVisibilityDefault(null);
                    }
                    
                    if(prefs.getDeveloperToolsEnabled() == null) {
                        DeveloperToolsEnabled dtn = new DeveloperToolsEnabled(false);                        
                        prefs.setDeveloperToolsEnabled(dtn);
                    }
                }
            }
        }
    }


    private void downgradeSearchResults(OrcidMessage orcidMessage) {
        // downgrade search
    }

    @Override
    public OrcidMessage upgradeMessage(OrcidMessage orcidMessage) {
        if (orcidMessage == null) {
            return null;
        }
        orcidMessage.setMessageVersion(TO_VERSION);
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        upgradeProfile(orcidProfile);

        return orcidMessage;
    }    
}
