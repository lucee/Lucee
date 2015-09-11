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
 ---><cfsilent>
	<cfset errMsg="">
	
	<!---- load ExtensionManager ---->
	<cfset manager=createObject('component','extension.ExtensionManager')>
    
	<cfif structKeyExists(url, 'uploadExt')>
		<cfset detail = session.uploadExtDetails />
		<cfset appendURL = "&uploadExt=1" />
	<cfelse>
    	<cfset detail=getDetailByUid(url.uid)>
		<cfset appendURL = "" />
	</cfif>
	
	<cfadmin 
		action="getExtensionInfo"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="info">
	<cfset detail.directory=info.directory>

	<!--- create app folder --->
	<cftry>
		<cfset dest=manager.createUIDFolder(url.uid)>
		<cfcatch>
			<cfset dest=createUIDFolder(url.uid)>
		</cfcatch>
	</cftry>
	

	<!--- copy lucee extension package to destination directory --->
	<cfset app=manager.copyAppFile(detail.data,dest,isDefined('url.trial') and isBoolean(url.trial) and url.trial EQ true)>
</cfsilent>

<cfif (not isDefined('app.url') or not len(app.url)) and not FileExists(app.destFile)>
	<cfif app.error>
		<cfset printError(app.message)>
	<cfelse>
		<cfoutput>#app.message#</cfoutput>
	</cfif>
	
	<cfif len(cgi.http_referer)>
		<cfform onerror="customError" action="#cgi.http_referer#" method="post">
			<cfoutput><input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.cancel#"></cfoutput>
		</cfform>
	</cfif>
<cfelse>
	<!---- load license ---->
	<cfset zip="zip://"&app.destFile&"!/">
	<cfset licenseFile=zip&"license.txt">
	<cfif not FileExists(licenseFile)>
		<cflocation url="#request.self#?action=#url.action#&action2=install2&uid=#url.uid#" addtoken="no">
	</cfif>
	<cffile action="read" file="#licenseFile#" variable="license">
	<cfoutput>
        <cfform onerror="customError" action="#request.self#?action=#url.action#&action2=install2&uid=#url.uid##appendURL#" method="post">
			<div class="modheader">
				<h2>#stText.ext.LicenseAgreement#</h2>
				#stText.ext.LicenseAgreementDesc#
			</div>
            <table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">#stText.ext.licenseagreement#</th>
						<td>
							<textarea readonly="readonly" class="licensetext" rows="20">#license#</textarea>
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td>&nbsp;</td>
						<td>
							<span style="float:right">
								<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.dontagree#" />
							</span>
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.agree#" />
						</td>
					</tr>
				</tfoot>
            </table>
        </cfform>
	</cfoutput>
</cfif>