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
 
<div class="more-info">
<!-- ng-mouseleave="closeMoreInfo(affiliation.putCode.value)"  --> 
<!--

	<a class="glyphicon glyphicon-plus-sign grey" ng-mouseenter="moreInfoMouseEnter(affiliation.putCode.value,$event);" ng-click="toggleClickMoreInfo(affiliation.putCode.value)"></a>	
	<div class="popover bottom more-info-container">
		<div class="arrow"></div>	
		<div class="lightbox-container">
			<div class="ie7fix">
		 -->	
			
			<div class="row bottomBuffer"></div>
			<div class="row bottomBuffer" ng-show="affiliation.affiliationName.value"
				ng-cloak>
				<div ng-show="affiliation.affiliationName.value"
				ng-cloak></div>
				<div class="col-md-12">
					<strong ng-show="affiliation.affiliationType.value == 'education'"><@orcid.msg 'manual_affiliation_form_contents.labelinstitution'/></strong>
					<strong ng-show="affiliation.affiliationType.value == 'employment'"><@orcid.msg 'manual_affiliation_form_contents.labelinstitutionemployer'/></strong>
					<strong ng-show="affiliation.affiliationType.value != 'education' && affiliation.affiliationType.value != 'employment'"><@orcid.msg 'manual_affiliation_form_contents.labelname'/></strong>
					<div ng-bind="affiliation.affiliationName.value"></div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.city.value"
				ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelcity'/></strong>
					<div ng-bind="affiliation.city.value"></div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.region.value"
				ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelregion'/></strong>
					<div ng-bind="affiliation.region.value"></div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.country.value"
				ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelcountry'/></strong>
					<div ng-bind="affiliation.country.value"></div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.departmentName.value"
				ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labeldepartment'/></strong>
					<div ng-bind="affiliation.departmentName.value"></div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.roleTitle.value"
				ng-cloak>
				<div class="col-md-12">
					<strong ng-show="affiliation.affiliationType.value == 'education'"><@orcid.msg 'manual_affiliation_form_contents.labeldegreetitle'/></strong>
					<strong ng-show="affiliation.affiliationType.value != 'education'"><@orcid.msg 'manual_affiliation_form_contents.labelroletitle'/></strong>
					<div ng-bind="affiliation.roleTitle.value"></div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.affiliationType.value"
				ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelaffiliationtype'/></strong>
					<div ng-bind="affiliation.affiliationType.value"></div>
				</div>
			</div>			
			<div class="row bottomBuffer" ng-show="affiliation.startDate.year" ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelStartDate'/></strong>
					<div>
						<span ng-show="affiliation.startDate.year">{{affiliation.startDate.year}}</span><span ng-show="affiliation.startDate.month">-{{affiliation.startDate.month}}</span><span ng-show="affiliation.startDate.day && affiliation.startDate.month">-{{affiliation.startDate.day}}</span>
					</div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.endDate.year" ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelEndDate'/></strong>
					<div>
						<span ng-show="affiliation.endDate.year">{{affiliation.endDate.year}}</span><span ng-show="affiliation.endDate.month">-{{affiliation.endDate.month}}</span><span ng-show="affiliation.endDate.day && affiliation.endDate.month">-{{affiliation.endDate.day}}</span>						
					</div>
				</div>
			</div>
			<div class="row bottomBuffer" ng-show="affiliation.sourceName"
				ng-cloak>
				<div class="col-md-12">
					<strong><@orcid.msg 'manual_affiliation_form_contents.labelsource'/></strong>
					<div ng-bind="affiliation.sourceName"></div>
				</div>
			</div>
			<!-- 
			</div>
		</div>
	</div>
	-->
</div>
 