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

<cfparam name="url.action2" default="none">
<cfset error.message="">
<cfset error.detail="">

<cftry>
<cfswitch expression="#url.action2#">
	<cfcase value="settings">
    	<cfif not len(form.location)>
        	<cfset form.location=form.locationCustom>
        </cfif>

		<cfadmin
			action="UpdateUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"

			updateType="#form.type#"
			updateLocation="#form.location#"
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
</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
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

hasAccess=1;
hasUpdate=structKeyExists(updateData,"available");

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
										<input type="radio" class="radio" name="location" value="http://release.lucee.org"<cfif updateData.provider.location EQ 'http://release.lucee.org'> <cfset isCustom=false>checked="checked"</cfif> />
										<b>#stText.services.update.location_release#</b>
									</label>
									<div class="comment">#stText.services.update.location_releaseDesc#</div>
								</li>
								<!--- Snapshot --->
								<li>
									<label>
										<input type="radio" class="radio" name="location" value="http://snapshot.lucee.org"<cfif updateData.provider.location EQ 'http://snapshot.lucee.org'> <cfset isCustom=false>checked="checked"</cfif> />
										<b>#stText.services.update.location_snapshot#</b>
									</label>
									<div class="comment">#stText.services.update.location_snapshotDesc#</div>
								</li>
								<li>
									<label>
										<input type="radio" class="radio" id="sp_radio_custom" name="location"<cfif isCustom> checked="checked"</cfif> value="" />
										<b>#stText.services.update.location_custom#</b>
									</label>
									<input id="customtextinput" type="text" class="text" name="locationCustom" size="40" value="<cfif isCustom>#updateData.provider.location#</cfif>">
									<div class="comment">#stText.services.update.location_customDesc#</div>

									<cfsavecontent variable="headText">
										<script type="text/javascript">
											function sp_clicked()
											{
												var iscustom = $('##sp_radio_custom')[0].checked;
												$('##customtextinput').css('opacity', (iscustom ? 1:.5)).prop('disabled', !iscustom);
											}
											$(function(){
												$('##updatelocations input.radio').bind('click change', sp_clicked);
												sp_clicked();
											});
										</script>
									</cfsavecontent>
									<cfhtmlhead text="#headText#" />
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

	<!---
For testing
<cfset updatedata.changeLog={
	"331":"cached query not disconnect from life query",
	"LDEV-327":"add frontend for request Queue"

	}>--->

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
<pre><cfloop list="#listSort(structKeyList(updateData.changelog),'textnocase')#" item="key"><!---
			---><cfif findNoCase("LDEV",key)><a target="_blank" href="http://issues.lucee.org/browse/#key#">#key#</a><cfelse><a target="_blank" href="https://bitbucket.org/lucee/lucee/issue/#key#">###key#</a></cfif> - #updateData.changelog[key]#
</cfloop></pre></div>
		#jira#
	<cfelse>
		<h2>#stText.services.update.infoTitle#</h2>
		<div class="text">#updateData.message#</div>
	</cfif>


	<cfif hasUpdate>
		<!--- run update --->
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
	<cfelseif needNewJars>
		<h2>#stText.services.update.lib#</h2>
		<div class="itemintro">#stText.services.update.libDesc#</div>
		<cfformClassic onerror="customError" action="#go(url.action,"updateJars")#" method="post">
			<table class="maintbl">
				<tbody>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="button submit" name="mainAction" value="#stText.services.update.lib#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>

	<!--- remove update --->
	<cfset size=arrayLen(patches)>
	<cfif size>
		<h2>#stText.services.update.remove#</h2>
		<div class="itemintro">#stText.services.update.removeDesc#</div>
		<cfformClassic onerror="customError" action="#go(url.action,"Remove")#" method="post">
			<table class="maintbl">
				<thead>
					<tr>
						<th>#stText.services.update.patch#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop index="i" from="1" to="#size#">
						<tr>
							<td>#patches[i]#</td>
						</tr>
						<cfset version=patches[i]>
					</cfloop>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</tbody>
				<tfoot>
					<tr>
						<td>
							<input type="submit" class="button submit" name="mainAction" value="#stText.services.update.removeRun#">
							<input type="submit" class="button submit" name="latest" value="#replace(stText.services.update.removeLatest,'{version}',version)#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>
</cfoutput>
