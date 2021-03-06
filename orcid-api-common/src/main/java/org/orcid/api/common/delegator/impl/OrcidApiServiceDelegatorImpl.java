/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.api.common.delegator.impl;

import static org.orcid.api.common.OrcidApiConstants.STATUS_OK_MESSAGE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import org.orcid.api.common.delegator.OrcidApiServiceDelegator;
import org.orcid.api.common.exception.OrcidNotFoundException;
import org.orcid.core.adapter.Jpa2JaxbAdapter;
import org.orcid.core.exception.OrcidSearchException;
import org.orcid.core.manager.ClientDetailsManager;
import org.orcid.core.manager.OrcidProfileManager;
import org.orcid.core.manager.OrcidSearchManager;
import org.orcid.core.security.DeprecatedException;
import org.orcid.core.security.visibility.aop.VisibilityControl;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidSearchResult;
import org.orcid.jaxb.model.message.OrcidSearchResults;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * This class will retrieve {@link OrcidProfile}s and return them for use in the
 * Tier 1 API. Its is worth noting that this will not return
 * {@link OrcidProfile}s that have not been confirmed, but it does this by
 * checking the status on the object rather than at database level.
 * <p/>
 * 
 * @author Declan Newman (declan) Date: 02/03/2012
 */
@Component("orcidApiServiceDelegator")
public class OrcidApiServiceDelegatorImpl implements OrcidApiServiceDelegator {

    @Resource(name = "orcidProfileManager")
    private OrcidProfileManager orcidProfileManager;

    @Resource(name = "orcidSearchManager")
    private OrcidSearchManager orcidSearchManager;

    @Resource
    private ClientDetailsManager clientDetailsManager;

    @Resource
    private Jpa2JaxbAdapter jpa2JaxbAdapter;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrcidApiServiceDelegatorImpl.class);

    /**
     * @return Plain text message indicating health of service
     */
    public Response viewStatusText() {
        return Response.ok(STATUS_OK_MESSAGE).build();
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the profile's bio
     * details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @VisibilityControl
    public Response findBioDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedOrcidBio(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    @Override
    @VisibilityControl
    public Response findBioDetailsFromPublicCache(String orcid) {
        try {
            OrcidMessage orcidMessage = orcidSearchManager.findPublicProfileById(orcid);
            if (orcidMessage != null) {
                OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
                if (orcidProfile != null) {
                    orcidProfile.downgradeToBioOnly();
                }
            }
            return getOrcidMessageResponse(orcidMessage, orcid);
        } catch (OrcidSearchException e) {
            LOGGER.warn("Error searching, so falling back to DB", e);
            return findBioDetails(orcid);
        }
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the profile's
     * external identifier details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @VisibilityControl
    public Response findExternalIdentifiers(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedExternalIdentifiers(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    @Override
    @VisibilityControl
    public Response findExternalIdentifiersFromPublicCache(String orcid) {
        try {
            OrcidMessage orcidMessage = orcidSearchManager.findPublicProfileById(orcid);
            if (orcidMessage != null) {
                OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
                if (orcidProfile != null) {
                    orcidProfile.downgradeToExternalIdentifiersOnly();
                }
            }
            return getOrcidMessageResponse(orcidMessage, orcid);
        } catch (OrcidSearchException e) {
            LOGGER.warn("Error searching, so falling back to DB", e);
            return findExternalIdentifiers(orcid);
        }
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with all of the profile's
     * details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @VisibilityControl
    public Response findFullDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedOrcidProfile(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    @Override
    @VisibilityControl
    public Response findFullDetailsFromPublicCache(String orcid) {
        try {
            OrcidMessage orcidMessage = orcidSearchManager.findPublicProfileById(orcid);
            return getOrcidMessageResponse(orcidMessage, orcid);
        } catch (OrcidSearchException e) {
            LOGGER.warn("Error searching, so falling back to DB", e);
            return findFullDetails(orcid);
        }
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the affiiation
     * details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @VisibilityControl
    public Response findAffiliationsDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedAffiliations(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    @Override
    @VisibilityControl
    public Response findAffiliationsDetailsFromPublicCache(String orcid) {
        try {
            OrcidMessage orcidMessage = orcidSearchManager.findPublicProfileById(orcid);
            if (orcidMessage != null) {
                OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
                if (orcidProfile != null) {
                    orcidProfile.downgradeToAffiliationsOnly();
                }
            }
            return getOrcidMessageResponse(orcidMessage, orcid);
        } catch (OrcidSearchException e) {
            LOGGER.warn("Error searching, so falling back to DB", e);
            return findAffiliationsDetails(orcid);
        }
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the grants details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @VisibilityControl
    public Response findFundingDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedFundings(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    @Override
    @VisibilityControl
    public Response findFundingDetailsFromPublicCache(String orcid) {
        try {
            OrcidMessage orcidMessage = orcidSearchManager.findPublicProfileById(orcid);
            if (orcidMessage != null) {
                OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
                if (orcidProfile != null) {
                    orcidProfile.downgradeToFundingsOnly();
                }
            }
            return getOrcidMessageResponse(orcidMessage, orcid);
        } catch (OrcidSearchException e) {
            LOGGER.warn("Error searching, so falling back to DB", e);
            return findAffiliationsDetails(orcid);
        }
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the work details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @VisibilityControl
    public Response findWorksDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedOrcidWorks(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    @Override
    @VisibilityControl
    public Response findWorksDetailsFromPublicCache(String orcid) {
        try {
            OrcidMessage orcidMessage = orcidSearchManager.findPublicProfileById(orcid);
            if (orcidMessage != null) {
                OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
                if (orcidProfile != null) {
                    orcidProfile.downgradeToWorksOnly();
                }
            }
            return getOrcidMessageResponse(orcidMessage, orcid);
        } catch (OrcidSearchException e) {
            LOGGER.warn("Error searching, so falling back to DB", e);
            return findWorksDetails(orcid);
        }
    }

    @Override
    public Response redirectClientToGroup(String clientId) {
        ClientDetailsEntity clientDetails = clientDetailsManager.findByClientId(clientId);
        if (clientDetails == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String groupOrcid = clientDetails.getGroupProfile().getId();
        URI groupUri;
        try {
            groupUri = new URI(jpa2JaxbAdapter.getOrcidIdBase(groupOrcid).getUri());
            return Response.seeOther(groupUri).build();
        } catch (URISyntaxException e) {
            LOGGER.error("Problem redirecting to group: {}", groupOrcid, e);
            return Response.serverError().build();
        }
    }

    /**
     * See {@link OrcidApiServiceDelegator}{@link #searchByQuery(Map)}
     */
    @Override
    @VisibilityControl
    public Response searchByQuery(Map<String, List<String>> queryMap) {
        OrcidMessage orcidMessage = orcidSearchManager.findOrcidsByQuery(queryMap);
        List<OrcidSearchResult> searchResults = orcidMessage.getOrcidSearchResults() != null ? orcidMessage.getOrcidSearchResults().getOrcidSearchResult() : null;
        List<OrcidSearchResult> filteredResults = new ArrayList<OrcidSearchResult>();
        OrcidSearchResults orcidSearchResults = new OrcidSearchResults();
        if (searchResults != null) {
            orcidSearchResults.setNumFound(orcidMessage.getOrcidSearchResults().getNumFound());
            if (searchResults.size() > 0) {
                for (OrcidSearchResult searchResult : searchResults) {
                    OrcidSearchResult filteredSearchResult = new OrcidSearchResult();
                    OrcidProfile filteredProfile = new OrcidProfile();
                    filteredSearchResult.setRelevancyScore(searchResult.getRelevancyScore());
                    filteredProfile.setOrcid(searchResult.getOrcidProfile().getOrcid());
                    filteredProfile.setOrcidId(searchResult.getOrcidProfile().getOrcidId());
                    filteredProfile.setOrcidIdentifier(searchResult.getOrcidProfile().getOrcidIdentifier());
                    filteredProfile.setOrcidBio(searchResult.getOrcidProfile().getOrcidBio());
                    filteredSearchResult.setOrcidProfile(filteredProfile);
                    filteredResults.add(filteredSearchResult);
                }
            }
        }
        orcidSearchResults.getOrcidSearchResult().addAll(filteredResults);
        return getOrcidSearchResultsResponse(orcidSearchResults, queryMap.toString());
    }

    /**
     * Method to perform the mundane task of checking for null and returning the
     * response with an OrcidMessage entity
     * 
     * @param profile
     * @param requestedOrcid
     * @return
     */
    private Response getOrcidMessageResponse(OrcidProfile profile, String requestedOrcid) {

        if (profile == null) {
            throw new OrcidNotFoundException("ORCID " + requestedOrcid + " not found");
        }

        profile.setOrcidInternal(null);
        OrcidMessage orcidMessage = new OrcidMessage(profile);

        return Response.ok(orcidMessage).build();
    }

    private Response getOrcidMessageResponse(OrcidMessage orcidMessage, String requestedOrcid) {
        boolean isProfileDeprecated = false;
        if (orcidMessage == null) {
            throw new OrcidNotFoundException("ORCID " + requestedOrcid + " not found");
        }
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        if (orcidProfile != null) {
            orcidProfile.setOrcidInternal(null);
            // If profile is deprecated
            if (orcidMessage.getOrcidProfile().getOrcidDeprecated() != null) {
                isProfileDeprecated = true;
            }
        }

        Response response = null;

        if (isProfileDeprecated) {
            // TODO: internationalize these messages
            throw new DeprecatedException("This account is deprecated. Please refer to account: "
                    + orcidProfile.getOrcidDeprecated().getPrimaryRecord().getOrcidIdentifier().getUri());
        } else {
            response = Response.ok(orcidMessage).build();
        }

        return response;
    }

    private Response getOrcidSearchResultsResponse(OrcidSearchResults orcidSearchResults, String query) {

        if (orcidSearchResults != null) {
            OrcidMessage orcidMessage = new OrcidMessage();
            orcidMessage.setOrcidSearchResults(orcidSearchResults);
            return Response.ok(orcidMessage).build();
        } else {
            throw new OrcidNotFoundException("No search results found using query " + query);
        }
    }
}
