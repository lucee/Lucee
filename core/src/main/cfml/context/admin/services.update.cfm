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
 ---><cfif request.admintype EQ "web"><cflocation url="#request.self#" addtoken="no"></cfif>
<cfinclude template="ext.functions.cfm">

<cfparam name="url.action2" default="none">
<cfset error.message="">
<cfset error.detail="">

<cftry>
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
	<cfcase value="run">
		<cfsetting requesttimeout="10000">
		<cfadmin
			action="runUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			remoteClients="#request.getRemoteClients()#">
	</cfcase>

	<cfcase value="remove">
		<cfadmin
			action="removeUpdate"
            onlyLatest="#StructKeyExists(form,'latest')#"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			remoteClients="#request.getRemoteClients()#">
	</cfcase>

	<!---<cfcase value="downUp">
		<cfsetting requesttimeout="100000">
		<cfadmin
			action="changeVersionTo"
            version="#form.version#"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			remoteClients="#request.getRemoteClients()#">
	</cfcase>--->



</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>

<!---
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!---
Error Output --->
<cfset printError(error)>


<cfadmin
			action="listPatches"
			returnvariable="patches"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#">

<cftry>
	<cfadmin
			action="getMinVersion"
			returnvariable="minVersion"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#">
    <cfcatch>
    	<cfset minVersion=createObject('java','lucee.VersionInfo').getIntVersion().toString()>
    </cfcatch>
</cftry>
<!----
<cfadmin
			action="needNewJars"
			returnvariable="needNewJars"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#">
because this is only about optional updates, we do this only in background from now
---->
<cfset needNewJars=false>

<cfscript>
include "services.update.functions.cfm";

// get info for the update location



	curr=server.lucee.version;
	updateData=getAvailableVersion();
	if(updateData.provider.location EQ "http://release.lucee.org"){
		version = "release";
	} else if( updateData.provider.location EQ "http://snapshot.lucee.org" ){
		version = "snapShot";
	} else{
		version = "custom";
	}
	realaseStruct = application.updateProvider.RELEASES;
	snapShotsStruct = application.updateProvider.snapShot;
	realaseArray = [];
	snapShotsArry = [];
	customArray = [];
	if(realaseStruct.type == "warning" || realaseStruct.code != 200 || !structKeyExists(realaseStruct, "otherversions")){
		error.message = "For Release: "& realaseStruct.message;
		printError(error);
	}else{
		realaseArray = qrySort(realaseStruct.otherversions);
	}
	if(snapShotsStruct.type == "warning" || snapShotsStruct.code != 200 || !structKeyExists(snapShotsStruct, "otherversions")){
		error.message = "For snapShot: " &snapShotsStruct.message;
		printError(error);
		snapShotsArry = [];
	}else{
		snapShotsArry = qrySort(snapShotsStruct.otherversions);
	}
	if(version == 'custom'){
		if(!structKeyExists(updateData, "otherVersions")|| isNull(updateData.port) || updateData.port==400){
			if(isNull(updateData.port) || updateData.port==404){
				error.message="Could not reach update provider [#updateData.provider.location#].";
			}
			else if(updateData.port!=200){
				error.message="Invalid response from [#updateData.provider.location#]." ;
			}
			printError(error);
		}else{
			customArray = qrySort(updateData.otherVersions);
		}
	}

	hasAccess=1;
	hasUpdate=structKeyExists(updateData,"available");
	private function qrySort( arry ){
		error.message="";
		try {
			updateData.qryOtherVersions=queryNew('version,versionSortable');
			queryAddRow(updateData.qryOtherVersions,arry.len());
			loop array=arry item="v" index="i" {
				updateData.qryOtherVersions.version[i]=v;
				updateData.qryOtherVersions.versionSortable[i]=toVersionSortable(v);
			}
			querySort(updateData.qryOtherVersions,'versionSortable','desc');
			arr = [];
			for(qry in updateData.qryOtherVersions){
				if(toVersionSortable(qry.version) LTE toVersionSortable(server.lucee.version)){
					break;
				}
				arr.append(qry.version);
			}
		}catch (any e){
			error.message=cfcatch.message;
			error.detail=cfcatch.Detail;
			error.exception = cfcatch;
		}
		printError(error);
		return arr;
	}
</cfscript>



<cfoutput>

	<div class="pageintro">#stText.services.update.desc#</div>

	<!--- Settings --->
	<h2>#stText.services.update.setTitle#</h2>
	<div class="itemintro">#stText.services.update.setDesc#</div>
	<cfformClassic onerror="customError" action="#go(url.action,"settings")#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.services.update.provider#</th>
					<td>
						<cfif hasAccess>
							<cfset isCustom=true>
							<ul class="radiolist" id="updatelocations">
								<!--- Release --->
								<li>
									<label>
										<input type="checkbox" id="sp_radio_custom" name="location"<cfif  version EQ 'custom'> checked</cfif> value="cutsomVersion" />
										<input type="hidden" value="#updateData.provider.location#" name="updatedInfo">
										<b>#stText.services.update.location_custom#</b>
									</label>
									<input id="customtextinput" type="text" class="text" name="locationCustom" size="40" value="<cfif  version EQ 'custom'>#updateData.provider.location#</cfif>" disabled>
									<div class="comment">#stText.services.update.location_customDesc#</div>
									<cfif version EQ 'custom'>
										<cfhtmlbody>
											<script type="text/javascript">
												$( '##customURL input' ).attr( 'disabled', false);
											</script>
										</cfhtmlbody>
									</cfif>
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
	</cfformClassic>


	<cfformClassic onerror="customError">
		<table class="maintbl">
			<tbody>
			<tr>
				<th scope="row">Lucee Update Provider</th>
				<td>
				<ul class="radiolist">
					<li>
						<label>
							<input type="radio" class="radio" name="location" value="releaseVersion" <cfif version eq 'release'>checked</cfif> />
							<b>#stText.services.update.location_release#</b>
						</label>
						<div class="comment">#stText.services.update.location_releaseDesc#</div>
					</li>
					<!--- Snapshot --->
					<li>
						<label>
							<input type="radio" class="radio" name="location" value="snapShotVersion" <cfif version eq 'snapShot'>checked</cfif> />
							<b>#stText.services.update.location_snapshot#</b>
						</label>
						<div class="comment">#stText.services.update.location_snapshotDesc#</div>
					</li>
					<!--- custom --->
					<li>
						<label>
							<input type="radio" class="radio" id="customURL" name="location" disabled value="cutsomVersion"<cfif version eq 'custom'> checked</cfif>  />
							<b>custom version</b>
						</label>
						<div class="comment">#stText.services.update.location_snapshotDesc#</div>
					</li>
				</ul>
				</td>
			</tr>
		</table>
	</cfformClassic>

	<!---
For testing
<cfset updatedata.changeLog={
	"331":"cached query not disconnect from life query",
	"LDEV-327":"add frontend for request Queue"

	}>--->




<cfscript>

stText.services.update.downUpTitle="Update Lucee Version";
stText.services.update.downUpSub="Current version: {version}";
stText.services.update.downUpDesc="Upgrade or downgrade your current version.";
stText.services.update.downgradeTo="Downgrade to";
stText.services.update.updateTo="Upgrade to";
stText.services.update.downup="update";


stText.services.update.downUpDesc=replace(stText.services.update.downUpDesc,'{version}',server.lucee.version);
</cfscript>

	<!--- downgrade/upgrade --->
	<cfif structKeyExists(updateData,"qryOtherVersions") && updateData.qryotherVersions.recordcount>
		<h2>#stText.services.update.downUpTitle#</h2>
		<div id="updateInfoDesc" style="text-align: center;"></div>
		<div class="itemintro">#stText.services.update.downUpDesc#</div>
		<span id="errorMessage"></span>
		<form method="post">
			<table class="maintbl">
				<tbody>
					<tr>
						<td>
							<cfset currVS=toVersionSortable(server.lucee.version)>
							<cfset minVS=toVersionSortable(minVersion)>
							
							<p>#replace(stText.services.update.downUpSub,'{version}',"<b>"&server.lucee.version&"</b>") #</p>
							<cfset versions = {}>
							<cfset versions.releaseVersion = 0>
							<cfset versions.snapShotVersion = 0>
							<cfset versions.cutsomVersion = 0>
							<cfif len(realaseArray)>
								<cfset versions.releaseVersion =  len(realaseArray)>
							</cfif>
							<cfif len(snapShotsArry)>
								<cfset versions.snapShotVersion =  len(snapShotsArry)>
							</cfif>
							<cfif len(customArray)>
								<cfset versions.cutsomVersion =  len(customArray)>
							</cfif>
							<cfset sezJSON = serializeJSON(versions)>
							<input type="hidden" value='#sezJSON#' id="versionsLen">
							<select name="version" id="version"  class="large" style="margin-top:8px">
								<option>select</option>
								<cfif len(realaseArray)>
									<cfloop array="#realaseArray#" index="i">
										<option class="releaseVersion" value="#i#">#i#</option>
									</cfloop>
								</cfif>
								<cfloop array="#snapShotsArry#" index="i">
									<option class="snapShotVersion" value="#i#">#i#</option>
								</cfloop>
								<cfif version eq 'custom' and len(customArray)>
									<cfloop array="#customArray#" index="i">
										<option class="cutsomVersion" value="#i#">#i#</option>
									</cfloop>
								</cfif>
							</select>
							<input type="button" class="button submit" name="mainAction" value="#stText.services.update.downup#"
							 onclick="changeVersion(this)">

						</td>
					</tr>
				</tbody>
			</table>
		</form>
	</cfif>




	<!---
	Info --->
	<cfif hasUpdate>
		<cfscript>
			// Jira
			jira=stText.services.update.jira;
			jira=replace(jira,'{a}','<a href="http://issues.lucee.org/" target="_blank">');
			jira=replace(jira,'{/a}','</a>');
		</cfscript>
		<h2>#stText.services.update.infoTitle#</h2>
		<div class="text">
			#updatedata.message#
		</div>
		<div style="overflow:auto;height:200px;border-style:solid;border-width:1px;padding:10px">
<pre><cfif isStruct(updateData.changelog)><cfloop list="#listSort(structKeyList(updateData.changelog),'textnocase')#" item="key"><!---
			---><cfif findNoCase("LDEV",key)><a target="_blank" href="http://issues.lucee.org/browse/#key#">#key#</a><cfelse><a target="_blank" href="https://bitbucket.org/lucee/lucee/issue/#key#">###key#</a></cfif> - #updateData.changelog[key]#
</cfloop></cfif></pre></div>
		#jira#
	<cfelse>
		<h2>#stText.services.update.infoTitle#</h2>
		<div class="text">#updateData.message#</div>
	</cfif>

<!--- 
	<cfif hasUpdate>
		run update
		<h2>#stText.services.update.exe#</h2>
		<div class="itemintro">#stText.services.update.exeDesc#</div>
		<cfformClassic onerror="customError" action="#go(url.action,"Run")#" method="post">
			<table class="maintbl">
				<tbody>
					<cfmodule template="remoteclients.cfm" colspan="1">
				</tbody>
				<tfoot>
					<tr>
						<td>
							<input type="submit" class="bs button submit" name="mainAction" value="#stText.services.update.exeRun#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif> --->

	<cfhtmlbody>
		<script type="text/javascript">
			$(document).ready(function(){
				lenghtVersion();
				if($('##sp_radio_custom').prop("checked")){
					$( '##customtextinput' ).attr( 'disabled', false);
					$('##customURL').attr( 'disabled', false);
				}
			});

			$('##sp_radio_custom').change(function(){
				if($(this).prop("checked")){
					$( '##customtextinput' ).attr( 'disabled', false);
				} else{
					$( '##customtextinput' ).attr( 'disabled', true);
				}
			});

			$('.radio').click(function(){
				lenghtVersion();
			});
			function lenghtVersion(){
				var val = $("input[type='radio']:checked").val();
				var json = $('##versionsLen').val();
				var obj = $.parseJSON(json);
				var len = obj[val.toUpperCase()];
				if(len == 0){
					$('##version').prop('disabled', true);
					$('##errorMessage').html('<div class="error" id="errorMsg"><span>No update ' + val + ' available to your version</span></dib>')
				} else{
					$('##version').prop('disabled', false);
					$('##errorMsg').html('');
					$('##errorMessage').html('');
				}
				$('##version option:selected').prop('selected', false);
				$('##version option').hide();
				$('##version option.'+val).show();
			}
			var submitted = false;
			function changeVersion(field) {
				field.disabled = true;
				submitted = true;
				var versionField=field.form.version;
				var value = versionField.options[versionField.selectedIndex].value;
				//alert(value);
				url='changeto.cfm?#session.urltoken#&adminType=#request.admintype#&version='+value;
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
