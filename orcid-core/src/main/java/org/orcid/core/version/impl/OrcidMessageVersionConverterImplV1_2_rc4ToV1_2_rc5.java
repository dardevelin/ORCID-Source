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
import org.orcid.jaxb.model.message.ExternalIdentifier;
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
public class OrcidMessageVersionConverterImplV1_2_rc4ToV1_2_rc5 implements OrcidMessageVersionConverter {

    private static final String FROM_VERSION = "1.2_rc4";
    private static final String TO_VERSION = "1.2_rc5";

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
        if (orcidProfile != null)
            if (orcidProfile.getOrcidBio() != null)
                if (orcidProfile.getOrcidBio().getExternalIdentifiers() != null)
                    for (ExternalIdentifier externalIdentifier: orcidProfile.getOrcidBio().getExternalIdentifiers().getExternalIdentifier()) { 
                        externalIdentifier.setExternalIdOrcid(externalIdentifier.getExternalIdSource());
                        externalIdentifier.setExternalIdSource(null);
                    }
    }
    
    private void upgradeProfile(OrcidProfile orcidProfile) {
        if (orcidProfile != null)
            if (orcidProfile.getOrcidBio() != null)
                if (orcidProfile.getOrcidBio().getExternalIdentifiers() != null)
                    for (ExternalIdentifier externalIdentifier: orcidProfile.getOrcidBio().getExternalIdentifiers().getExternalIdentifier()) { 
                        externalIdentifier.setExternalIdSource(externalIdentifier.getExternalIdOrcid());
                        externalIdentifier.setExternalIdOrcid(null);
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