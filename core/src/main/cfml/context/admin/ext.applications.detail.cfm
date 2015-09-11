<cfset stText.ext.minLuceeVersion="Lucee Version">
<cfset stText.ext.minLuceeVersionDesc="Minimal Lucee Version needed for this Extension.">
<cfset stText.ext.toSmallVersion="You need at least the Lucee {version} to install this Extension">


<cfset available=getDataByid(url.id,getExternalData(providerURLs))>
<cfset installed=getDataByid(url.id,extensions)>

<!--- get informatioj to the provider of this extension --->
<cfif !isNull(available.provider)>
	<cfset provider=getProviderInfo(available.provider).meta>
</cfif>

<cfset isInstalled=installed.count() GT 0><!--- if there are records it is installed --->
<cfset hasExternalInfo=available.count() GT 0>

<cfset hasUpdate=false>
<cfif isInstalled && hasExternalInfo>
	<cfset app=available>
	<cfset hasUpdate=installed.version LT available.version>
<cfelseif hasExternalInfo>
	<cfset app=available>
<cfelse>
	<cfset app=installed>
</cfif>

<cfoutput>
	<!--- title and description --->
	<div class="modheader">
		<h2>#app.name# (#isInstalled?stText.ext.installed:stText.ext.notInstalled#)</h2>
		#replace(replace(trim(app.description),'<','&lt;',"all"), chr(10),"<br />","all")#
		<br /><br />
	</div>

	
					
	<table class="contentlayout">
		<tbody>
			<tr>
				<!--- image --->
				<td valign="top" style="width:200px;">
					<cfif !isNull(app.image)>
						<cfset dn=getDumpNail(app.image,400,400)>
						<div style="width:100%;overflow:auto;">
							<img src="#dn#" alt="#stText.ext.extThumbnail#" />
						</div>
					</cfif>
				</td>
				<td valign="top">
					<table class="maintbl">
						<tbody>
							<!--- Extension Version --->
							<cfif isInstalled>
								<tr>
									<th scope="row">#stText.ext.installedVersion#</th>
									<td>#installed.version#</td>
								</tr>
								<tr>
									<th scope="row">Type</th>
									<td>#installed.trial?"Trial":"Full"# Version</td>
								</tr>

							<cfelse>
								<tr>
									<th scope="row">#stText.ext.availableVersion#</th>
									<td>#available.version#
								</td>
								</tr>
							</cfif>
							
							<!--- price --->
							<cfif !isNull(available.price) && len(trim(available.price))>
								<tr>
									<th scope="row">#stText.ext.price#</th>
									<td><cfif available.price GT 0>#available.price# <cfif !isNull(available.currency)>#available.currency#<cfelse>USD</cfif><cfelse>#stText.ext.free#</cfif></td>
								</tr>
							</cfif>
							<!--- category --->
							<cfif !isNull(available.category) && len(trim(available.category))>
								<tr>
									<th scope="row">#stText.ext.category#</th>
									<td>#available.category#</td>
								</tr>
							</cfif>
							<!--- author --->
							<cfif !isNull(available.author) && len(trim(available.author))>
								<tr>
									<th scope="row">#stText.ext.author#</th>
									<td>#available.author#</td>
								</tr>
							</cfif>
							<!--- created --->
							<cfif !isNull(available.created) && len(trim(available.created))>
								<tr>
									<th scope="row">#stText.ext.created#</th>
									<td>#LSDateFormat(available.created)#</td>
								</tr>
							</cfif>
							<!--- id --->
							<tr>
								<th scope="row">Id</th>
								<td>#app.id#</td>
							</tr>
							
							<!--- provider --->
							<cfif !isNull(provider.title) && len(trim(provider.title))>
								<tr>
									<th scope="row">#stText.ext.provider#</th>
									<td><cfif !isNull(provider.url)><a href="#provider.url#" target="_blank"></cfif>#provider.title#<cfif !isNull(provider.url)></a></cfif></td>
								</tr>
							</cfif>
							<!--- bundles --->
							<cfset stText.ext.reqbundles="Required Bundles (Jars)">
							<cfif isInstalled && !isNull(installed.bundles) && installed.bundles.recordcount()>
								<tr>
									<th scope="row">#stText.ext.reqbundles#</th>
									<td>
										<cfloop query="#installed.bundles#">
											- #installed.bundles.name# (#installed.bundles.version#)<br />
										</cfloop>
									</td>
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
		<cfset updateAvailableDesc=replace(stText.ext.updateAvailableDesc,'{installed}',installed.version)>
		<cfset updateAvailableDesc=replace(updateAvailableDesc,'{update}',available.version)>
		<!--- #updateAvailableDesc#--->
		
		<table class="maintbl autowidth">
			<tbody>
				<tr>
					<th scope="row">#stText.ext.installedVersion#</td>
					<td>#installed.version#</td>
				</tr>
				<tr>
					<th scope="row">#stText.ext.availableVersion#</td>
					<td>#available.version#</td>
				</tr>
			</tbody>
		</table>
		
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="id" value="#url.id#">
			<input type="hidden" name="provider" value="#available.provider#">
			<cfset _trial=false>
			<cfif isDefined('app.trial') and isBoolean(app.trial)>
				<cfset _trial=app.trial>
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
			<input type="hidden" name="id" value="#url.id#">
			<input type="hidden" name="provider" value="#isNull(available.provider)?"":available.provider#">
			

			<cfif isDefined('app.trial') and isBoolean(app.trial)>
				<cfset _trial=app.trial>
			</cfif>
			<cfif !isNull(available.provider)>
			<cfif _trial>
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.installFull#">
			<cfelse>
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.update#">
			</cfif>
			</cfif>

			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.uninstall#">
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.cancel#">
		</cfform>
	<cfelse>
	

		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="id" value="#url.id#">
			<cfif isDefined('app.minCoreVersion') and (app.minCoreVersion GT server.lucee.version)>
				<div class="error">#replace(stText.ext.toSmallVersion,'{version}',app.minCoreVersion,'all')#</div>
			<cfelse>
				<input type="hidden" name="provider" value="#available.provider#">
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

