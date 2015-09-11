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
 ---><cfset stText.ext.minLuceeVersion="Lucee Version">
<cfset stText.ext.minLuceeVersionDesc="Minimal Lucee Version needed for this Extension.">
<cfset stText.ext.toSmallVersion="You need at least the Lucee {version} to install this Extension">
<cfset detail=getDetailByUid(url.uid)>

<cfset isInstalled=structKeyExists(detail,'installed')>
<cfif isInstalled>
	<cfset app=detail.installed>
    <cfset hasUpdate=updateAvailable(detail.installed)>
<cfelse>
	<cfset app=detail.data>
    <cfset hasUpdate=false>
</cfif>

<cfif arrayLen(detail.all) GT 0>
	<cfset info=detail.data.info>
	<cfloop array="#detail.all#" index="ap">
    	<cfif ap.provider EQ app.provider>
        	<cfset info=ap.info>
        </cfif>
    </cfloop>
<cfelse>
	<cfset info={title:""}>
</cfif>

<cfoutput query="app">
	<!--- Info --->
	<div class="modheader">
		<h2>#app.label# (#iif(isInstalled,de(stText.ext.installed),de(stText.ext.notInstalled))#)</h2>
		#replace(replace(trim(app.description),'<','&lt;',"all"), chr(10),"<br />","all")#
		<br /><br />
	</div>
	<table class="contentlayout">
		<tbody>
			<tr>
				<td valign="top" <cfif len(app.video)>style="width:320px;"<cfelse>style="width:200px;"</cfif>>
					<cfif isValid('url', app.image)>
						<div style="width:100%;overflow:auto;">
							<img src="#app.image#" alt="#stText.ext.extThumbnail#" />
						</div>
					</cfif>
					<cfif len(app.video)>
						<cfset attrs = {bgcolor="##595F73", fgcolor="##DFE9F6"} />
						<cfif isValid('url', app.image)>
							<cfset attrs.preview = app.image />
						</cfif>
						<br /><br />
						<cfvideoplayer attributeCollection="#attrs#" video="#app.video#" width="320" height="256">
					</cfif>
				</td>
				<td valign="top">
					<table class="maintbl">
						<tbody>
							<!--- Extension Version --->
							<cfif isInstalled>
								<tr>
									<th scope="row">#stText.ext.installedVersion#</th>
									<td>#app.version#<cfif app.codename neq ""> (#stText.ext.codename#: <em>#app.codename#</em>)</cfif></td>
								</tr>
							<cfelse>
								<tr>
									<th scope="row">#stText.ext.availableVersion#</th>
									<td>#app.version#<cfif app.codename neq ""> (#stText.ext.codename#: <em>#app.codename#</em>)</cfif></td>
								</tr>
							</cfif>
							<!--- Lucee Version
							<cfif isDefined('app.minCoreVersion') and len(trim(app.minCoreVersion))>
							<tr>
								<th scope="row">
								#stText.ext.minLuceeVersion#<br>
								<span class="comment">#stText.ext.minLuceeVersionDesc#</span></th>
								<td>#app.minCoreVersion#</td>
							</tr>
							</cfif> --->
							<!--- price --->
							<cfif isDefined('app.price') and len(trim(app.price))>
								<tr>
									<th scope="row">#stText.ext.price#</th>
									<td><cfif app.price GT 0>#app.price# <cfif structKeyExists(app,"currency")>#app.currency#<cfelse>USD</cfif><cfelse>#stText.ext.free#</cfif></td>
								</tr>
							</cfif>
							<!--- category --->
							<cfif len(trim(app.category))>
								<tr>
									<th scope="row">#stText.ext.category#</th>
									<td>#app.category#</td>
								</tr>
							</cfif>
							<!--- author --->
							<cfif len(trim(app.author))>
								<tr>
									<th scope="row">#stText.ext.author#</th>
									<td>#app.author#</td>
								</tr>
							</cfif>
							<!--- created --->
							<cfif len(trim(app.created))>
								<tr>
									<th scope="row">#stText.ext.created#</th>
									<td>#LSDateFormat(app.created)#</td>
								</tr>
							</cfif>
							<!--- provider --->
							<cfif len(trim(info.title))>
								<tr>
									<th scope="row">#stText.ext.provider#</th>
									<td><a href="#info.url#" target="_blank">#info.title#</a></td>
								</tr>
							</cfif>
							<!--- documentation --->
							<cfif len(trim(app.documentation))>
								<tr>
									<th scope="row">#stText.ext.documentation#</th>
									<td><a href="#app.documentation#" target="_blank">#replace(replace(app.documentation,'http://',''),'https://','')#</a></td>
								</tr>
							</cfif>
							<!--- support --->
							<cfif len(trim(app.support))>
								<tr>
									<th scope="row">#stText.ext.support#</th>
									<td><a href="#app.support#" target="_blank">#replace(replace(app.support,'http://',''),'https://','')#</a></td>
								</tr>
							</cfif>
							<!--- forum --->
							<cfif len(trim(app.forum))>
								<tr>
									<th scope="row">#stText.ext.forum#</th>
									<td><a href="#app.forum#" target="_blank">#replace(replace(app.forum,'http://',''),'https://','')#</a></td>
								</tr>
							</cfif>
							<!--- mailinglist --->
							<cfif len(trim(app.mailinglist))>
								<tr>
									<th scope="row">#stText.ext.mailinglist#</th>
									<td><a href="#app.mailinglist#" target="_blank">#replace(replace(app.mailinglist,'http://',''),'https://','')#</a></td>
								</tr>
							</cfif>
						</tbody>
					</table>
				</td>
			</tr>
		</tbody>
	</table>
	<br />
	<!--- Update --->
	<cfif isInstalled and hasUpdate>
		<h2>#stText.ext.updateAvailable#</h2>
		<cfset updateAvailableDesc=replace(stText.ext.updateAvailableDesc,'{installed}',app.version)>
		<cfset updateAvailableDesc=replace(updateAvailableDesc,'{update}',detail.data.version)>
		<!--- #updateAvailableDesc#--->
		
		<table class="maintbl autowidth">
			<tbody>
				<tr>
					<th scope="row">#stText.ext.installedVersion#</td>
					<td>#detail.installed.version#</td>
				</tr>
				<tr>
					<th scope="row">#stText.ext.availableVersion#</td>
					<td>#detail.data.version#</td>
				</tr>
				<!---<tr>
					<td colspan="2">
					<textarea cols="80" rows="20">TODO get Update info</textarea>
					
					</td>
				</tr>--->
			</tbody>
		</table>
		
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="uid" value="#url.uid#">
			
			<cfset _trial=false>
			<cfif isDefined('app.trial') and isBoolean(app.trial)>
				<cfset _trial=isBoolean(app.trial)>
			<cfelseif app.id EQ detail.data.id and isDefined('detail.data.trial') and isBoolean(detail.data.trial)>
				<cfset _trial=detail.data.trial>
			</cfif>
			
			<cfif _trial>
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.updateTrial#">
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.updateFull#">
			<cfelse>
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.update#">
			</cfif>
			
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.uninstall#">
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.cancel#">
		</cfform>
		
	<!--- Install --->
	<cfelseif isInstalled and not hasUpdate>
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="uid" value="#url.uid#">
			
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.uninstall#">
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.cancel#">
		</cfform>
	<cfelse>
	

		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="uid" value="#url.uid#">
			<cfif isDefined('app.minCoreVersion') and (app.minCoreVersion GT server.lucee.version)>
				<div class="error">#replace(stText.ext.toSmallVersion,'{version}',app.minCoreVersion,'all')#</div>
			<cfelse>
				<cfif isDefined('app.trial') and isBoolean(app.trial) and app.trial EQ true>
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.installTrial#">
				<cfif true or !isDefined('app.disablefull') or app.disablefull NEQ true><input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.installFull#"></cfif>
				<cfelse>
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.install#">
				</cfif>
			</cfif>
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.cancel#">
		</cfform>
	</cfif>
</cfoutput>