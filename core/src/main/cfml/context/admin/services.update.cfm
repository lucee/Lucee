<!---
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 --->


 <cftry>
<cfswitch expression="#url.action2#">
	<cfcase value="settings">
		<cfif !structKeyExists(form, "location") OR !structKeyExists(form, "locationCustom")>
			<cfset form.locationCustom = "http://release.lucee.org">
		</cfif>
		<cfadmin
			action="UpdateUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"

			updateType="#form.type#"
			updateLocation="#form.locationCustom#"
			remoteClients="#request.getRemoteClients()#">
	</cfcase>
</cfswitch>
<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>
 <cfif request.admintype EQ "web"><cflocation url="#request.self#" addtoken="no"></cfif>
<cfset error.message="">
<cfset error.detail="">

 <cfscript>

	max=100;
	count=0;
	while(!structKeyExists(application.luceeUpdateProvider, "versions") && count++ < max ) {
	   sleep(100);
	}
	if(count==max) throw "failed to load ..."

	include template="ext.functions.cfm";
	include template="services.update.functions.cfm";
	stText.services.update.downUpTitle="Update Lucee Version";
	stText.services.update.luceeProvider = "Lucee Update Provider";
	stText.services.update.downUpSub="Current version: {version}";
	stText.services.update.downUpDesc="Upgrade or downgrade your current version.";
	stText.services.update.downgradeTo="Downgrade to";
	stText.services.update.updateTo="Upgrade to";
	stText.services.update.downup="update";
	stText.services.update.custom="Custom";
	stText.services.update.customDesc="Versions came from custom URL given by the user";
	stText.services.update.downUpDesc=replace(stText.services.update.downUpDesc,'{version}',server.lucee.version);


		luceeProvider = application.luceeUpdateProvider;
		if(isNull(luceeProvider.versions.otherVersions) || luceeProvider.type == 'warning'){
			error.message = "Couldn't able to reach the server. Please try after some times";
			result.otherVersions = [];
		} else{
			result = luceeProvider.versions;
		}
		updateData=getAvailableVersion();
		if(updateData.provider.location EQ "http://release.lucee.org"){
			version = "release";
		} else if( updateData.provider.location EQ "http://snapshot.lucee.org" ){
			version = "snapShot";
		} else{
			version = "custom";
		}
		versionsStr = {};
		versionsStr.snapShot = {};
		versionsStr.pre_Release= {};
		versionsStr.release = {};
		if(version eq 'custom'){
			versionsStr.custom = {};
		}
		for(type in versionsStr){
			versionsStr[type].upgrade = [];
			versionsStr[type].downgrade = [];
		}
		if(version eq 'custom' && structKeyExists(updateData, "otherVersions") && Len(updateData.otherVersions)){
			for(versions in updateData.otherVersions){
				if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.custom.downgrade, versions);
				}else{
					arrayPrepend(versionsStr.custom.upgrade, versions);
				}
			}
		}
		minVersion=createObject('java','lucee.VersionInfo').getIntVersion().toString();
		minVs = toVersionSortable(minVersion);
		if(len(result.otherVersions)){
			for(versions in result.otherVersions ){
				if(versions EQ server.lucee.version) cfcontinue;
				if(  toVersionSortable(versions) LTE minVS) cfcontinue;
				if(FindNoCase("SNAPSHOT", versions)){
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.SNAPSHOT.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.SNAPSHOT.upgrade, versions);
					}
				} else if(FindNoCase("BETA", versions)){
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.pre_Release.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.pre_Release.upgrade, versions);
					}
				} else if(FindNoCase("RC", versions)){
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.pre_Release.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.pre_Release.upgrade, versions);
					}
				} else{
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.release.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.release.upgrade, versions);
					}
				}
			}
		}
	printError(error);
</cfscript>
<cfoutput>

<style>
	.btn {
		color:white;
		background-color:##CC0000;
	}
</style>

	<p class="fs">#replace(stText.services.update.downUpSub,'{version}',"<b>"&server.lucee.version&"</b>") #</p>
	
	<h1>#stText.services.update.luceeProvider#</h1>
	<p class="fs leftSpace">#stText.services.update.titleDesc#</p>
	<cfset hiddenFormContents = "" >
	<cfset count = 1>

	<cfloop list="Release,Pre_Release,SnapShot,Custom" index="key">
		<cfif key EQ 'custom' && !structKeyExists(versionsStr, "custom")>
			<cfcontinue>
		</cfif>
		<span><input <cfif count EQ 1>
		class="bl button alignLeft" <cfelseif count EQ StructCount(versionsStr)> class="br button" <cfelse> class="bm button" </cfif>  name="changeConnection" id="btn_#UcFirst(Lcase(key))#" value="#key#" onclick="enableVersion('#UcFirst(Lcase(key))#');"  type="button"></span>
		<cfsavecontent variable="tmpContent">
			<div id="div_#UcFirst(Lcase(key))#" class="topBottomSpace">
				<div class="whitePanel">
					<h1 class="">#key#<cfif key EQ "pre_Release"> (<span>#stText.services.update.alphaBetaRelease#</span>)</cfif></h1>
					<div class="itemintro">#stText.services.update[key&"Desc"]#</div>
					<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
						<div>
							<h3 class="pdTop">#stText.services.update.upgrade# :</h3>
							<cfif len(versionsStr[key].upgrade)>
								<select name="UPDATE_#key#"  class="large">
									<cfloop array="#versionsStr[key].upgrade#" index="i">
										<option value="#i#">#i#</option>
									</cfloop>
								</select>
								<input type="button" class="smBtn" id="btn_UPDATE_#key#"  onclick="changeVersion(this, UPDATE_#key#)" name="mainAction" value="#stText.menu.services.update#">
							<cfelse>
								<div class="alertMsg">#replace("#stText.services.update.alertMessage#","update","#key#&nbsp#Lcase(stText.services.update.upgrade)#")#</div>
							</cfif>
						</div>
						<div>
							<h3 class="pdTop">#stText.services.update.downgrade# :</h3>	
							<cfif len(versionsStr[key].downgrade)>
								<select name="DOWNGRADE_#key#"  class="large">
									<cfloop array="#versionsStr[key].downgrade#" index="i">
										<option value="#i#">#i#</option>
									</cfloop>
								</select>
								<input type="button" class="smBtn" id="btn_DOWNGRADE_#key#" onclick="changeVersion(this, DOWNGRADE_#key#)" name="mainAction" value="#stText.services.update.downgrade#">
							<cfelse>
								<div class="alertMsg">#replace("#stText.services.update.alertMessage#","update","#key#&nbsp#Lcase(stText.services.update.downgrade)#")#</div>
							</cfif>
						</div>
					</cfformClassic>
				</div>
			</div>
		</cfsavecontent>
		<cfset hiddenFormContents &= tmpContent>
		<cfset count = count+1>
	</cfloop>

	<div id="updateInfoDesc" style="text-align: center;"></div>
	<div id="group_Connection">
		<input type="hidden" value='#UcFirst(Lcase(version))#' id="versionsLen">
		#hiddenFormContents#
	</div>

	<!--- for custom --->
	<cfformClassic onerror="customError" action="#go(url.action,"settings")#" method="post">
		<h1>#stText.services.update.customProvider#</h1>
		<table class="maintbl alignLeft"> 
			<tbody>
				<tr>
					<th scope="row">#stText.services.update.provider#</th>
					<td>
						<ul class="radiolist" id="updatelocations">
							<!--- Release --->
							<li>
								<label>
									<input type="checkbox" id="sp_radio_custom" name="location"<cfif  version EQ 'custom'> checked</cfif> value="cutsomVersion" />
									<input type="hidden" value="#updateData.provider.location#" name="updatedInfo">
									<b>#stText.services.update.location_custom#</b>
								</label>
								<input id="customtextinput" type="text" class="text" name="locationCustom" size="40" value="<cfif  version EQ 'custom'>#updateData.provider.location#</cfif>" disabled>
								<div class="comment">#replace("#stText.services.update.location_customDesc#","{url}","<a href=""http://docs.lucee.org"">http://docs.lucee.org</a>")#</div>
								<cfif version EQ 'custom'>
									<cfhtmlbody>
										<script type="text/javascript">
											$( '##customtextinput' ).attr( 'disabled', false);
										</script>
									</cfhtmlbody>
								</cfif>
							</li>
						</ul>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.services.update.type#</th>
					<td>
						<select name="type">
							<option value="manual" <cfif updateData.provider.type EQ "manual">selected</cfif>>#stText.services.update.type_manually#</option>
							<option value="auto" <cfif updateData.provider.type EQ "auto">selected</cfif>>#stText.services.update.type_auto#</option>
						</select>
						<div class="comment">#stText.services.update.typeDesc#</div>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
						<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>

	<cfhtmlbody>
		<script type="text/javascript">
			$(document).ready(function(){
				if($('##sp_radio_custom').prop("checked")){
					$( '##customtextinput' ).attr( 'disabled', false);
					$('##customURL').attr( 'disabled', false);
				}
				var version = $('##versionsLen').val();
				enableVersion(version);
				$("##btn_"+version).addClass("btn");
			});

			function enableVersion(v){
				$("##group_Connection").find('div').each(function(index) {
					var xx = $(this).attr('id');
					$('##'+xx).show();
					if("div_"+v != xx){
						$('##'+xx).hide();
					}
  				});
		  		$(".btn").removeClass('btn');
		  		$("##btn_"+v).addClass("btn");
			}

			function changeVersion(field, frm) {
				submitted = true;
				$('##group_Connection').hide();
				url='changeto.cfm?#session.urltoken#&adminType=#request.admintype#&version='+frm.value;
				$(document).ready(function(){
					$('##updateInfoDesc').html('<img src="../res/img/spinner16.gif.cfm">');
					disableBlockUI=true;

			 		$.get(url, function(response) {
			      		field.disabled = false;

			 			if((response+"").trim()=="")
							window.location=('#request.self#?action=#url.action#'); //$('##updateInfoDesc').html("<p>#stText.services.update.restartOKDesc#</p>");
						else
							$('##updateInfoDesc').html('<div class="error">'+response+'</div>');
							//window.location=('#request.self#?action=#url.action#'); //$('##updateInfoDesc').html(response);
			 		});
				});
			}

			$('##sp_radio_custom').change(function(){
				// enable / disable custom URL field
				if($(this).prop("checked")){
					$( '##customtextinput' ).attr( 'disabled', false);
				} else{
					$( '##customtextinput' ).attr( 'disabled', true);
				}
			});
		</script>
	</cfhtmlbody>
</cfoutput>