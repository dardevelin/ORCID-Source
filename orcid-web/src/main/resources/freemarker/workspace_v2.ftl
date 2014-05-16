<#--

    =============================================================================

    ORCID (R) Open Source
    http://orcid.org

    Copyright (c) 2012-2013 ORCID, Inc.
    Licensed under an MIT-Style License (MIT)
    http://orcid.org/open-source-license

    This copyright and license information (including a link to the full license)
    shall be included in its entirety in all copies or substantial portion of
    the software.

    =============================================================================

-->
<#-- @ftlvariable name="profile" type="org.orcid.jaxb.model.message.OrcidProfile" -->
<@protected nav="record">
<#escape x as x?html>
<#if emailVerified?? && emailVerified>
    <div class="alert alert-success">
        <strong><@spring.message "orcid.frontend.web.email_verified"/></strong>
    </div>
</#if>

<#if invalidVerifyUrl?? && invalidVerifyUrl>
    <div class="alert alert-success">
        <strong><@spring.message "orcid.frontend.web.invalid_verify_link"/></strong>
    </div>
</#if>


<#if invalidOrcid?? && invalidOrcid>
    <div class="alert alert-success">
        <strong><@spring.message "orcid.frontend.web.invalid_switch_orcid"/></strong>
    </div>
</#if>

<div class="row workspace-top public-profile">

	<#-- hidden divs that trigger angular -->
	<#if RequestParameters['recordClaimed']??>
	    <div ng-controller="ClaimThanks" style="display: hidden;"></div>	    
	<#elseif !Session.CHECK_EMAIL_VALIDATED?exists && !inDelegationMode>
    	<div ng-controller="VerifyEmailCtrl" style="display: hidden;"></div>
	</#if>

    <div class="col-md-3 lhs left-aside">
    	<div class="workspace-profile">
            <#include "includes/id_banner.ftl"/>
	       	<div class="other-names-box">
		       	<div ng-controller="OtherNamesCtrl" class="other-names-controller">
		        	<div>
		        	   <strong><@orcid.msg 'workspace.Alsoknownas'/></strong>
		        	   <span ng-hide="showEdit == true" ng-click="openEdit()">
		        	      <span class="glyphicon glyphicon-pencil edit-other-names edit-option" title=""></span><br />
		        	      <span ng-repeat="otherNames in otherNamesForm.otherNames" ng-cloak>
		        	         {{ $last?otherNames.value:otherNames.value+ ", "}}
		        	      </span>
		        	   </span>
		        	   <div ng-show="showEdit == true" ng-cloak class="other-names-edit">
		        	      <@orcid.privacyToggle  angularModel="otherNamesForm.visibility.visibility"
				             questionClick="toggleClickPrivacyHelp()"
				             clickedClassCheck="{'popover-help-container-show':privacyHelp==true}" 
				             publicClick="setPrivacy('PUBLIC', $event)" 
	                 	     limitedClick="setPrivacy('LIMITED', $event)" 
	                 	     privateClick="setPrivacy('PRIVATE', $event)" />
		        	   
		        	      <div ng-repeat="otherNames in otherNamesForm.otherNames">
		        	          <input type="text" ng-model="otherNames.value"></input>
		        	          <a ng-click="deleteKeyword(otherNames)" class="glyphicon glyphicon-trash grey"></a>
		        	          <br />
		        	          <span class="orcid-error" ng-show="otherNames.url.errors.length > 0">
							     <div ng-repeat='error in otherNames.url.errors' ng-bind-html="error"></div>
						      </span>
		        	          <span class="orcid-error" ng-show="otherNames.name.errors.length > 0">
							     <div ng-repeat='error in otherNames.name.errors' ng-bind-html="error"></div>
						      </span>
		        	      </div>
		        	      <a class="glyphicon glyphicon-plus" ng-click="addNew()"></a><br />
		        	      <button class="btn btn-primary" ng-click="setOtherNamesForm()"><@spring.message "freemarker.btnsavechanges"/></button>
		        	      <button class="btn" ng-click="close()"><@spring.message "freemarker.btncancel"/></button>
		        	   </div> 
		           </div>
		       	</div>
	       	</div>
            
            
            
            
            <div ng-controller="CountryCtrl" class="country-controller">
	        	<strong><@orcid.msg 'public_profile.labelCountry'/></strong>
	            <span class="glyphicon glyphicon-pencil edit-country edit-option" ng-click="openEdit()" title="" ng-hide="showEdit == true"></span>
	            <br />   
                <span ng-hide="showEdit == true" ng-click="toggleEdit()">
	                <span ng-show="countryForm != null && countryForm.iso2Country != null" ng-bind="countryForm.iso2Country.value">
	                </span>
	            </span>
               
               <div ng-show="showEdit == true" ng-cloak class="country-edit">
               	  <@orcid.privacyToggle  angularModel="countryForm.profileAddressVisibility.visibility"
			         questionClick="toggleClickPrivacyHelp()"
			         clickedClassCheck="{'popover-help-container-show':privacyHelp==true}" 
			         publicClick="setPrivacy('PUBLIC', $event)" 
                 	     limitedClick="setPrivacy('LIMITED', $event)" 
                 	     privateClick="setPrivacy('PRIVATE', $event)" />
                  
                  <select id="country" name="country" ng-model="countryForm.iso2Country.value">
		    			<option value=""><@orcid.msg 'org.orcid.persistence.jpa.entities.CountryIsoEntity.empty' /></option>
						<#list isoCountries?keys as key>
							<option value="${key}">${isoCountries[key]}</option>
						</#list>
				  </select>				  
				  
	             <button class="btn btn-primary" ng-click="setCountryForm()"><@spring.message "freemarker.btnsavechanges"/></button>
		         <button class="btn" ng-click="close()"><@spring.message "freemarker.btncancel"/></button>
	              			  
				</div>
				
            </div>
            
	       	<div class="keyword-box">
		       	<div ng-controller="KeywordsCtrl" class="keywords-controller">
		        	<div>
		        	   <strong><@orcid.msg 'public_profile.labelKeywords'/></strong>
		        	   <span ng-hide="showEdit == true" ng-click="openEdit()">
		        	      <span class="glyphicon glyphicon-pencil edit-keywords edit-option" title=""></span><br />
		        	      <span ng-repeat="keyword in keywordsForm.keywords" ng-cloak>
		        	         {{ $last?keyword.value:keyword.value+ ", "}}
		        	      </span>
		        	   </span>
		        	   <div ng-show="showEdit == true" ng-cloak class="keywords-edit">
		        	      <@orcid.privacyToggle  angularModel="keywordsForm.visibility.visibility"
				             questionClick="toggleClickPrivacyHelp()"
				             clickedClassCheck="{'popover-help-container-show':privacyHelp==true}" 
				             publicClick="setPrivacy('PUBLIC', $event)" 
	                 	     limitedClick="setPrivacy('LIMITED', $event)" 
	                 	     privateClick="setPrivacy('PRIVATE', $event)" />
		        	   
		        	      <div ng-repeat="keyword in keywordsForm.keywords">
		        	          <input type="text" ng-model="keyword.value"></input>
		        	          <a ng-click="deleteKeyword(keyword)" class="glyphicon glyphicon-trash grey"></a>
		        	          <br />
		        	          <span class="orcid-error" ng-show="keyword.url.errors.length > 0">
							     <div ng-repeat='error in keyword.url.errors' ng-bind-html="error"></div>
						      </span>
		        	          <span class="orcid-error" ng-show="keyword.name.errors.length > 0">
							     <div ng-repeat='error in keyword.name.errors' ng-bind-html="error"></div>
						      </span>
		        	      </div>
		        	      <a class="glyphicon glyphicon-plus" ng-click="addNew()"></a><br />
		        	      <button class="btn btn-primary" ng-click="setKeywordsForm()"><@spring.message "freemarker.btnsavechanges"/></button>
		        	      <button class="btn" ng-click="close()"><@spring.message "freemarker.btncancel"/></button>
		        	   </div> 
		           </div>
		       	</div>
	       	</div>
	       	
	       	<div class="websites-box">
		       	<div ng-controller="WebsitesCtrl" class="websites-controller">
		        	<div>
		        	   <strong><@orcid.msg 'public_profile.labelWebsites'/></strong>
		        	   <span ng-hide="showEdit == true">
		        	      <span class="glyphicon glyphicon-pencil edit-websites edit-option" ng-click="openEdit()" title=""></span><br />
		        	      <div ng-repeat="website in websitesForm.websites" ng-cloak>
		        	         <a href="{{website.url.value}}" target="_blank" rel="nofollow">{{website.name.value != null? website.name.value : website.url.value}}</a>
		        	      </div>
		        	   </span>
		        	   <div ng-show="showEdit == true" ng-cloak class="websites-edit">
		        	      <@orcid.privacyToggle  angularModel="websitesForm.visibility.visibility"
				             questionClick="toggleClickPrivacyHelp()"
				             clickedClassCheck="{'popover-help-container-show':privacyHelp==true}" 
				             publicClick="setPrivacy('PUBLIC', $event)" 
	                 	     limitedClick="setPrivacy('LIMITED', $event)" 
	                 	     privateClick="setPrivacy('PRIVATE', $event)" />
		        	   
		        	      <div ng-repeat="website in websitesForm.websites" class="mobile-box">
		        	          <input type="text" ng-model="website.name.value" placeholder="${springMacroRequestContext.getMessage("manual_work_form_contents.labeldescription")}"></input>
		        	          <input type="text" ng-model="website.url.value" placeholder="${springMacroRequestContext.getMessage("manual_work_form_contents.labelURL")}"></input>
		        	          <a ng-click="deleteWebsite(website)" class="glyphicon glyphicon-trash grey"></a>
		        	          <br />
		        	          <span class="orcid-error" ng-show="website.url.errors.length > 0">
							     <div ng-repeat='error in website.url.errors' ng-bind-html="error"></div>
						      </span>
		        	          <span class="orcid-error" ng-show="website.name.errors.length > 0">
							     <div ng-repeat='error in website.name.errors' ng-bind-html="error"></div>
						      </span>
		        	      </div>
		        	      <a class="glyphicon glyphicon-plus" ng-click="addNew()"></a><br />
		        	      <button class="btn btn-primary" ng-click="setWebsitesForm()"><@spring.message "freemarker.btnsavechanges"/></button>
		        	      <button class="btn" ng-click="close()"><@spring.message "freemarker.btncancel"/></button>
		        	   </div> 
		           </div>
		       	</div>
	       	</div>
       		<div ng-controller="ExternalIdentifierCtrl" ng-hide="!externalIdentifiersPojo.externalIdentifiers.length" ng-cloak>	       			
       			<p><strong><@orcid.msg 'public_profile.labelOtherIDs'/></strong></p>
       			<div ng-repeat='externalIdentifier in externalIdentifiersPojo.externalIdentifiers'>
		        	<span ng-hide="externalIdentifier.externalIdUrl">{{externalIdentifier.externalIdCommonName.content}} {{externalIdentifier.externalIdReference.content}}</span>
		        	<span ng-show="externalIdentifier.externalIdUrl"><a href="{{externalIdentifier.externalIdUrl.value}}" target="_blank">{{externalIdentifier.externalIdCommonName.content}} {{externalIdentifier.externalIdReference.content}}</a></span>
			   		<a ng-click="deleteExternalIdentifier($index)" class="glyphicon glyphicon-trash grey"></a>       			
       			</div>
			</div>													    
	        <#if RequestParameters['OldPersonal']??>	        
				<p class="hoover-white-fonts">	       
		       		<a href="<@spring.url '/account/manage-bio-settings'/>" id="update-personal-modal-link" class="label btn-primary"><@orcid.msg 'workspace.Update'/></a>
		        </p>
	        </#if>
		</div>
    </div>
    <div class="col-md-9 right-aside">
        <div class="workspace-right">
        	<div class="workspace-inner workspace-header" ng-controller="WorkspaceSummaryCtrl">
                <div class="alert alert-info" ng-show="showAddAlert()" ng-cloak><strong><@orcid.msg 'workspace.addinformationaboutyou'/></strong></div>
                <!-- Summary 
        		<div class="row">
        			<div class="workspace-overview col-md-3 col-sm-3 col-xs-6" id="works-overview">
	        			<a href="#workspace-publications" class="overview-count" ng-click="workspaceSrvc.openWorks()"><span ng-bind="worksSrvc.works.length"></span></a>
	        			<a href="#workspace-publications" class="overview-title" ng-click="workspaceSrvc.openWorks()"><@orcid.msg 'workspace.Works'/></a>
	                    <br />	                    	
	                    <a href="#workspace-publications" class="btn-update no-icon" ng-click="workspaceSrvc.openWorks()"><@orcid.msg 'workspace.view'/></a>	                    
	        		</div>
		            <div class="workspace-overview col-md-3 col-sm-3 col-xs-6" id="educations-overview">
		                <a href="#workspace-educations" class="overview-count" ng-click="workspaceSrvc.openEducation()"><span ng-bind="affiliationsSrvc.educations.length"></span></a>
		                <a href="#workspace-educations" class="overview-title" ng-click="workspaceSrvc.openEducation()"><@orcid.msg 'org.orcid.jaxb.model.message.AffiliationType.education'/></a>
		                <br />
		                <a href="#workspace-educations" class="btn-update no-icon" ng-click="workspaceSrvc.openEducation()"><@orcid.msg 'workspace.view'/></a>
		            </div>
		            <div class="workspace-overview col-md-3 col-sm-3 col-xs-6" id="employments-overview">
		                <a href="#workspace-employments" class="overview-count" ng-click="workspaceSrvc.openEmployment()"><span ng-bind="affiliationsSrvc.employments.length"></span></a>
		                <a href="#workspace-employments" class="overview-title" ng-click="workspaceSrvc.openEmployment()"><@orcid.msg 'org.orcid.jaxb.model.message.AffiliationType.employment'/></a>
		                <br />
		                <a href="#workspace-employments" class="btn-update no-icon" ng-click="workspaceSrvc.openEmployment()"><@orcid.msg 'workspace.view'/></a>
		             </div>
					<div class="workspace-overview col-md-3 col-sm-3 col-xs-6">
        				<a href="#workspace-fundings" class="overview-count" ng-click="workspaceSrvc.openFunding()"><span ng-bind="fundingSrvc.fundings.length"></span></a>
        				<a href="#workspace-fundings" class="overview-title" ng-click="workspaceSrvc.openFunding()"><@orcid.msg 'workspace.Funding'/></a>
        				<br />
        				<a href="#workspace-employments" class="btn-update no-icon" ng-click="workspaceSrvc.openFunding()"><@orcid.msg 'workspace.view'/></a>
        			</div>
	        	</div>
	        	 -->
        	</div>
        	<div class="workspace-accordion" id="workspace-accordion">        		
        		<!-- Personal Information -->
				<div id="workspace-personal" class="workspace-accordion-item workspace-accordion-active" ng-controller="PersonalInfoCtrl">
        			<div class="workspace-accordion-header">
        				<div class="row">
        					<div class="col-md-12 col-sm-12 col-xs-12">
		 			   			<a href="" ng-click="toggleDisplayInfo($event)" class="toggle-text">
		  			   				<i class="glyphicon-chevron-down glyphicon x0" ng-class="{'glyphicon-chevron-right':displayInfo==false}"></i>  			   			
		 			   				<@orcid.msg 'workspace.personal_information'/>
		 			   				<#if RequestParameters['OldPersonal']??>	        
        			   		   			<a href="<@spring.url '/account/manage-bio-settings'/>" id="update-personal-modal-link" class="label btn-primary"><@orcid.msg 'workspace.Update'/></a>        			   		
        			        		</#if>
		 			   			</a>
	 			   			</div>
   			   			</div>
        			</div>
            		<div class="workspace-accordion-content" ng-show="displayInfo">
            			<#include "workspace_personal_v2.ftl"/>
        			</div>
            	</div>
            	<!-- Affiliations / Education  -->
                <#include "workspace_affiliations_body_list_v2.ftl"/>
                <!-- Fundings -->
               	<#include "workspace_fundings_body_list_v2.ftl"/>
		        <!-- Works -->                
                <div id="workspace-publications" class="workspace-accordion-item workspace-accordion-active" ng-controller="WorkCtrl">
                	<div class="workspace-accordion-header">
                		<div class="row">
                			<div class="col-md-2 col-sm-2 col-xs-12">
		                		<div class="work-title" ng-controller="WorkspaceSummaryCtrl">
			                		<a href="" ng-click="workspaceSrvc.toggleWorks($event)" class="toggle-text">
				       			       <i class="glyphicon-chevron-down glyphicon x0" ng-class="{'glyphicon-chevron-right':workspaceSrvc.displayWorks==false}"></i>
				       			       <@orcid.msg 'workspace.Works'/> (<span ng-bind="worksSrvc.works.length"></span>)
				       			    </a>
			       				</div>
			       			</div>	
			       			<div class="col-md-10 col-sm-10 col-xs-12 action-button-bar" ng-show="workspaceSrvc.displayWorks">
			       				 <!--
			       				<div class="sort-menu-container">
			       					 
				       				<a class="action-option manage-button sort-menu" ng-click="">
										<span class="glyphicon glyphicon-sort"></span>							
										<@orcid.msg ''/>Sort Items							
									</a>
									<ul class="sort-menu-options">
										<li><a href="">Title <span class=""></span></a></li>
										<li><a href="" class="checked">Data <span class="glyphicon glyphicon-ok pull-right"></span></a></li>
										<li><a href="">Type <span class=""></span></a></li>
										<li><a href="">Source <span class=""></span></a></li>
									</ul>
									
								</div>
								 -->
		                		<ul class="works-menu">
		                			<!-- 
			        				<li>
			        					<a href="" class="action-option manage-button" ng-click="">
											<span class="glyphicon glyphicon-cog"></span>
											Manage View
										</a>	        				
			        				</li>
			        				 -->
			        				<li>
				        				<a href="" class="action-option manage-button" ng-click="addWorkModal()">
											<span class="glyphicon glyphicon-plus"></span>
											<@orcid.msg 'manual_work_form_contents.add_work_manually'/>
										</a>
			        				</li>
			        				<li>
				        				<a class="action-option manage-button" ng-click="showWorkImportWizard()">
											<span class="glyphicon glyphicon-cloud-upload"></span>							
											<@orcid.msg 'workspace.link_works'/>
										</a>	        				
			        				</li>
								</ul>								
							</div>
						</div>					
					</div>					 
      	            <div ng-show="workspaceSrvc.displayWorks" class="workspace-accordion-content">
	            		<#include "includes/work/add_work_modal_inc.ftl"/>
						<#include "includes/work/del_work_modal_inc.ftl"/>
						<#include "includes/work/body_work_inc_v2.ftl"/>
	            	</div>
	            	 
            	</div>
            	
            	<#--
        		<div id="workspace-fundings" class="workspace-accordion-item">
        			<div class="workspace-accordion-header"><a href="#"><@orcid.msg 'workspace.Grants'/></a></div>
            	</div>
            	
        		<div id="workspace-patents" class="workspace-accordion-item">
        			<div class="workspace-accordion-header"><a href="#"><@orcid.msg 'workspace.Patents'/></a></div>
            	</div>
            	-->
            	
            </div>
        </div>
    </div>    
</div>
</#escape>

<script type="text/ng-template" id="verify-email-modal">	
	<div class="lightbox-container">
		<div class="row">
			<div class="col-md-12 col-xs-12 col-sm-12">
				<h4><@orcid.msg 'workspace.your_primary_email'/></h4>
				<@orcid.msg 'workspace.ensure_future_access'/>
				<br />
				<br />						
				<span class="btn btn-primary" id="modal-close" ng-click="verifyEmail()"><@orcid.msg 'workspace.send_verification'/></span>
				<span class="btn" id="modal-close" ng-click="closeColorBox()"><@orcid.msg 'freemarker.btncancel'/></span>								
			</div>
		</div>		
	</div>		


</script>

<script type="text/ng-template" id="verify-email-modal-sent">
	<div class="lightbox-container">
		<div class="row">
			<div class="col-md-12 col-sm-12 col-xs-12">
				<h4><@orcid.msg 'workspace.sent'/></h4>
				<@orcid.msg 'workspace.check_your_email'/><br />
				<br />
				<span class="btn" ng-click="closeColorBox()"><@orcid.msg 'freemarker.btnclose'/></span>
			</div>
		</div>
	</div>
</script>

<script type="text/ng-template" id="claimed-record-thanks">
	<div class="lightbox-container">
		<div class="row">
			<div class="col-md-12 col-sm-12 col-xs-12">
				<strong><@spring.message "orcid.frontend.web.record_claimed"/></strong><br />
				<br />
				<button class="btn" ng-click="close()"><@spring.message "freemarker.btnclose"/></button>
			</div>
		</div>
	</div>
</script>
	
<script type="text/ng-template" id="claimed-record-thanks-source-grand-read">
	<div class="lightbox-container">
		<div class="row">
			<div class="col-md-12 col-sm-12 col-xs-12">
				<strong><@spring.message "orcid.frontend.web.record_claimed"/></strong><br />
				<br />
				<strong ng-bind="sourceGrantReadWizard.displayName"></strong> <@spring.message "orcid.frontend.web.record_claimed.would_like"/><br />
				<br />
				<button class="btn btn-primary" ng-click="yes()"><@spring.message "orcid.frontend.web.record_claimed.yes_go_to" /></button>
				<button class="btn" ng-click="close()"><@spring.message "orcid.frontend.web.record_claimed.no_thanks" /></button>
			</div>
		</div>
	</div>
</script>

<script type="text/ng-template" id="delete-external-id-modal">
	<div class="lightbox-container">
		<div class="row">
			<div class="col-md-12 col-sm-12 col-xs-12">
				<h3><@orcid.msg 'manage.deleteExternalIdentifier.pleaseConfirm'/> {{removeExternalModalText}} </h3>
				<button class="btn btn-danger" ng-click="removeExternalIdentifier()"><@orcid.msg 'freemarker.btnDelete'/></button> 
				<a ng-click="closeModal()"><@orcid.msg 'freemarker.btncancel'/></a>
			<div>
		<div>
	<div>	
</script>

<script type="text/ng-template" id="import-wizard-modal">
    <#if ((workImportWizards)??)>		
    	<div id="third-parties">
			<div class="ie7fix-inner">
			<div class="row">	
				<div class="col-md-12 col-sm-12 col-xs-12">					
					<a class="btn pull-right close-button" ng-click="closeModal()">X</a>
	           		<h1 class="lightbox-title" style="text-transform: uppercase;"><@orcid.msg 'workspace.link_works'/></h1>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12 col-sm-12 col-xs-12">
	    	    	<div class="justify">
						<p><@orcid.msg 'workspace.LinkResearchActivitiess.description'/></p>
					</div>            	    	           	
    		    	<#list workImportWizards?sort_by("displayName") as thirdPartyDetails>
	        	       	<#assign redirect = (thirdPartyDetails.redirectUris.redirectUri[0].value) >
            	   		<#assign predefScopes = (thirdPartyDetails.redirectUris.redirectUri[0].scopeAsSingleString) >
                   		<strong><a ng-click="openImportWizardUrl('<@spring.url '/oauth/authorize?client_id=${thirdPartyDetails.clientId}&response_type=code&scope=${predefScopes}&redirect_uri=${redirect}'/>')">${thirdPartyDetails.displayName}</a></strong><br />
                 		<div class="justify">
							<p>
								${(thirdPartyDetails.shortDescription)!}
							</p>
						</div>
                   		<#if (thirdPartyDetails_has_next)>
	                      	<hr/>
						</#if>
                		</#list>
				</div>
			</div>                 
            <div class="row footer">
				<div class="col-md-12 col-sm-12 col-xs-12">
					<p>
				   		<strong><@orcid.msg 'workspace.LinkResearchActivitiess.footer.title'/></strong>	    
	        			<@orcid.msg 'workspace.LinkResearchActivitiess.footer.description1'/> <a href="<@orcid.msg 'workspace.LinkResearchActivitiess.footer.description.url'/>"><@orcid.msg 'workspace.LinkResearchActivitiess.footer.description.link'/></a> <@orcid.msg 'workspace.LinkResearchActivitiess.footer.description2'/>
			    	</p>
				</div>
	        </div>
		</div>
		</div>
	</#if>
</script>

<script type="text/ng-template" id="import-funding-modal">
    <#if ((fundingImportWizards)??)>		
    	<div id="third-parties">
			<div class="ie7fix-inner">
			<div class="row">	
				<div class="col-md-12 col-sm-12 col-xs-12">					
					<a class="btn pull-right close-button" ng-click="closeModal()">X</a>
	           		<h1 class="lightbox-title" style="text-transform: uppercase;"><@orcid.msg 'workspace.link_funding'/></h1>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12 col-sm-12 col-xs-12">
	    	    	<div class="justify">
						<p><@orcid.msg 'workspace.LinkResearchActivitiess.description'/></p>
					</div>            	    	           	
    		    	<#list fundingImportWizards?sort_by("displayName") as thirdPartyDetails>
	        	       	<#assign redirect = (thirdPartyDetails.redirectUris.redirectUri[0].value) >
            	   		<#assign predefScopes = (thirdPartyDetails.redirectUris.redirectUri[0].scopeAsSingleString) >
                   		<strong><a ng-click="openImportWizardUrl('<@spring.url '/oauth/authorize?client_id=${thirdPartyDetails.clientId}&response_type=code&scope=${predefScopes}&redirect_uri=${redirect}'/>')">${thirdPartyDetails.displayName}</a></strong><br />
                 		<div class="justify">
							<p>
								${(thirdPartyDetails.shortDescription)!}
							</p>
						</div>
                   		<#if (thirdPartyDetails_has_next)>
	                      	<hr/>
						</#if>
                		</#list>
				</div>
			</div>                 
            <div class="row footer">
				<div class="col-md-12 col-sm-12 col-xs-12">
					<p>
				   		<strong><@orcid.msg 'workspace.LinkResearchActivitiess.footer.title'/></strong>	    
	        			<@orcid.msg 'workspace.LinkResearchActivitiess.footer.description1'/> <a href="<@orcid.msg 'workspace.LinkResearchActivitiess.footer.description.url'/>"><@orcid.msg 'workspace.LinkResearchActivitiess.footer.description.link'/></a> <@orcid.msg 'workspace.LinkResearchActivitiess.footer.description2'/>
			    	</p>
				</div>
	        </div>
		</div>
		</div>
	</#if>
</script>	
</@protected>
