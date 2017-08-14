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


 <!--- <cftry> 
<cfswitch expression="#url.action2#">
	<cfcase value="settings">
		<cfif !structKeyExists(form, "locationCustom") || form.locationCustom eq "" || !structKeyExists(form, "location")>
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
</cftry> --->
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
	include "services.update.functions.cfm";
	stText.services.update.downUpTitle="Update Lucee Version";
	stText.services.update.downUpSub="Current version: {version}";
	stText.services.update.downUpDesc="Upgrade or downgrade your current version.";
	stText.services.update.downgradeTo="Downgrade to";
	stText.services.update.updateTo="Upgrade to";
	stText.services.update.downup="update";
	stText.services.update.downUpDesc=replace(stText.services.update.downUpDesc,'{version}',server.lucee.version);


		http = application.luceeUpdateProvider;
		if(isNull(http.versions.otherVersions) || http.type == 'warning'){
			error.message = "Couldn't able to reach the server. Please try after some times";
			result.otherVersions = [];
		} else{
			result = http.versions;
		}
		versionsStr = {};
		versionsStr.snapShot = {};
		versionsStr.beta= {};
		versionsStr.release = {};
		for(type in versionsStr){
			versionsStr[type].upgrade = [];
			versionsStr[type].downgrade = [];
		}
		if(len(result.otherVersions)){
			for(versions in result.otherVersions ){
				if(FindNoCase("SNAPSHOT", versions)){
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.SNAPSHOT.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.SNAPSHOT.upgrade, versions);
					}
				} else if(FindNoCase("BETA", versions)){
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.BETA.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.BETA.upgrade, versions);
					}
				} else if(FindNoCase("RC", versions)){
					if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.BETA.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.BETA.upgrade, versions);
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


<!--- <cfformClassic onerror="customError"  method="post"> --->
<cfoutput>
	<p class="fs">#stText.services.update.titleDesc#</p>
	<table class="maintbl">
		<tbody>
			<tr>
				<th scope="row">#stText.services.update.snapShot#</th>
				<td>
					<div class="comment">#stText.services.update.snapShotDesc#</div>
				</td>
			</tr>
			<tr>
				<th scope="row">#stText.services.update.beta#</th>
				<td>
					<div class="comment">#stText.services.update.betaDesc#</div>
				</td>
			</tr>
			<tr>
				<th scope="row">#stText.services.update.release#</th>
				<td>
					<div class="comment">#stText.services.update.releaseDesc#</div>
				</td>
			</tr>
		</tbody>
	</table>
	<p class="fs">#replace(stText.services.update.downUpSub,'{version}',"<b>"&server.lucee.version&"</b>") #</p>
	<div id="updateInfoDesc" style="text-align: center;"></div>
	<div class="container">
		<h1>#stText.services.update.upgrade#</h1>
		<form method="post">
			<cfset tmpStr = {}>
			<cfset tmpStr.update_snapshot = len(versionsStr.snapshot.upgrade)>
			<cfset tmpStr.update_release = len(versionsStr.release.upgrade)>
			<cfset tmpStr.update_beta = len(versionsStr.beta.upgrade)>
			<cfset tmpStr.downgrade_snapshot = len(versionsStr.snapshot.downgrade)>
			<cfset tmpStr.downgrade_release = len(versionsStr.release.downgrade)>
			<cfset tmpStr.downgrade_beta = len(versionsStr.beta.downgrade)>
			<cfset sezJson = serializeJSON(tmpStr)>
			<input type="hidden" value='#sezJson#' id="versionsLen">
			<div class="fLeft">
				<h3>#stText.services.update.snapShot#</h3>
				<div>
					<select name="update_snapshot" id="" class="large">
						<cfloop array="#versionsStr.snapshot.upgrade#" index="i">
							<option value="#i#">#i#</option>
						</cfloop>
					</select>
					<input type="button" class="smBtn" id="buttonValue1" onclick="changeVersion(this, update_snapshot)" name="mainAction" value="#stText.menu.services.update#">
				</div>
				<div id="update_snapshot"></div>
			</div>
		</form>
		<form method="post">
			<div class="fLeft">
				<h3>#stText.services.update.beta#</h3>
				<div>
					<select name="update_beta" class="large">
						<cfloop array="#versionsStr.beta.upgrade#" index="i">
							<option value="#i#">#i#</option>
						</cfloop>
					</select>
					<input type="button" class="smBtn" id="buttonValue2" onclick="changeVersion(this)" name="mainAction, update_beta" value="#stText.menu.services.update#">
				</div>
				<div id="update_beta"></div>
			</div>
		</form>
		<form method="post">
			<div class="fLeft">
				<h3>#stText.services.update.release#</h3>
				<div>
					<select name="update_release" class="large">
						<cfloop array="#versionsStr.release.upgrade#" index="i">
							<option value="#i#">#i#</option>
						</cfloop>
					</select>
					<input type="button" class="smBtn" id="buttonValue3" onclick="changeVersion(this, update_release)" name="mainAction" value="#stText.menu.services.update#">
				</div>
				<div id="update_release"></div>
			</div>
		</form>
	</div>
	<div class="pgTop container">
		<h1>#stText.services.update.downgrade#</h1>
		<form method="post">
			<div class="fLeft">
				<!--- SnapShot --->
				<h3>#stText.services.update.snapShot#</h3>
				<div>
					<select name="downgrade_snapshot" class="large">
						<cfloop array="#versionsStr.snapshot.downgrade#" index="i">
							<option value="#i#">#i#</option>
						</cfloop>
					</select>
					<input type="button" class="smBtn" onclick="changeVersion(this, downgrade_snapshot)" id="buttonValue4" name="mainAction" value="#stText.services.update.downgrade#">
				</div>
				<div id="downgrade_snapshot"></div>
			</div>
		</form>
		<form method="post">
			<div class="fLeft">
				<h3>#stText.services.update.beta#</h3>
				<div>
					<select name="downgrade_beta" class="large">
						<cfloop array="#versionsStr.beta.downgrade#" index="i">
							<option value="#i#">#i#</option>
						</cfloop>
					</select>
					<input type="button" class="smBtn" onclick="changeVersion(this, downgrade_beta)" id="buttonValue5" name="mainAction" value="#stText.services.update.downgrade#">
				</div>
				<div id="downgrade_beta"></div>
			</div>
		</form>
		<form method="post">
			<div class="fLeft">
				<h3>#stText.services.update.release#</h3>
				<div>
					<select name="downgrade_release" class="large">
						<cfloop array="#versionsStr.release.downgrade#" index="i">
							<option value="#i#">#i#</option>
						</cfloop>
					</select>
					<input type="button" class="smBtn" onclick="changeVersion(this, downgrade_release)" id="buttonValue6" name="mainAction" value="#stText.services.update.downgrade#">
				</div>
				<div id="downgrade_release"></div>
			</div>
		</form>
	</div>

<!--- for custom --->
<!--- 	<cfformClassic onerror="customError" action="#go(url.action,"settings")#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.services.update.provider#</th>
					<td>
						<cfif hasAccess>
							<cfset isCustom=true>
							<ul class="radiolist" id="updatelocations">
								<!--- custom --->
								<li>
									<label>
										<input type="checkbox" id="sp_radio_custom" name="location"<cfif  version EQ 'custom'>checked</cfif> value="cutsomVersion" />
										<input type="hidden" value="#updateData.provider.location#" name="updatedInfo">
										<b>#stText.services.update.location_custom#</b>
									</label>
									<input id="customtextinput" type="text" class="text" name="locationCustom" size="40" value="<cfif  version EQ 'custom'>#updateData.provider.location#</cfif>" disabled>
									<div class="comment">#stText.services.update.location_customDesc#</div>
								</li>
							</ul>
						<cfelse>
							<b>#updateData.provider.location#</b>
						</cfif>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.services.update.type#</th>
					<td>
						<cfif hasAccess>
							<select name="type">
								<option value="manual" <cfif updateData.provider.type EQ "manual">selected</cfif>>#stText.services.update.type_manually#</option>
								<option value="auto" <cfif updateData.provider.type EQ "auto">selected</cfif>>#stText.services.update.type_auto#</option>
							</select>
						<cfelse>
							<b>#updateData.provider.type#</b>
						</cfif>
						<div class="comment">#stText.services.update.typeDesc#</div>
					</td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic> --->
	<cfhtmlbody>
		<script type="text/javascript">
			$(document).ready(function(){
				lenghtVersion();
			});

			function lenghtVersion(){
				//disable select box while it has no value
				var json = $('##versionsLen').val();
				var obj = $.parseJSON(json);
				$.each(obj, function(k, v) {
					var len = obj[k];
					if(len == 0){
						$("select[name="+ k.toLowerCase() +"]").prop('disabled', true);
						$( "select[name="+ k.toLowerCase() +"]" ).next().prop('disabled', true);
						$("##"+k.toLowerCase()).html('<div class="alertMsg"><span> Currently no  <b>' + k.split('_')[1] + ' </b>' + k.split('_')[0].toLowerCase() + ' available for your version </span></div>');
					}
				});
			}

			function changeVersion(field, frm) {
				submitted = true;
				$('.container').hide();
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
		</script>
	</cfhtmlbody>
</cfoutput>