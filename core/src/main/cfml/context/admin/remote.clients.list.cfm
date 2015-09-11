<cfadmin 
	action="getRemoteClients"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="clients">

<cfparam name="url.row" default="0">
<cfif url.row GT 0 and url.row LTE clients.recordcount>
	<cfset form.mainAction=stText.Buttons.verify>
	<cfset form['url_'&url.row]=clients.url[url.row]>
	<cfset form['row_'&url.row]=url.row>
	
</cfif>
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="remote"
	secValue="yes">

<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.urls=toArrayFromForm("url")>
			<cfset data.rows=toArrayFromForm("row")>
			
			<cfloop index="idx" from="1" to="#arrayLen(data.urls)#">
				<cfif isDefined("data.rows[#idx#]") and data.urls[idx] NEQ "">
					<cfadmin 
						action="removeRemoteClient"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						url="#data.urls[idx]#">
					
				</cfif>
			</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.verify#">
        	
				<cfset data.urls=toArrayFromForm("url")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.urls)#">
					<cfif isDefined("data.rows[#idx#]") and data.urls[idx] NEQ "">
						<cfadmin 
							action="getRemoteClient"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							url="#data.urls[idx]#"
							returnVariable="rclient">
						
						<cftry>
             
							<cfadmin 
								action="verifyRemoteClient"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								attributeCollection="#rclient#">
								<cfset stVeritfyMessages["#data.urls[idx]#"].Label = "OK">
							<cfcatch>
								<!--- <cfset error.message=error.message&data.names[idx]&": "&cfcatch.message&"<br>"> --->
								<cfset stVeritfyMessages[data.urls[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.urls[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
					</cfif>
				</cfloop>
				
		</cfcase>
		<cfcase value="#stText.Buttons.Update#">
			
			<cfadmin 
				action="updatePSQ"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				psq="#structKeyExists(form,"psq") and form.psq#">
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
<!--- 
Error Output --->
<cfset printError(error)>



<cfadmin 
    action="getRemoteClientUsage"
    type="#request.adminType#"
    password="#session["password"&request.adminType]#"
    
    returnVariable="usage">

<!--- 
list all mappings and display necessary edit fields --->

<cfoutput>
	<div class="pageintro">#stText.remote.desc#</div>
	
	<cfif clients.recordcount>
		<h2>#stText.remote.listClients#</h2>
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%">
							<input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)">
						</th>
						<th>#stText.remote.label#</th>
						<cfloop query="usage">
							<th>#usage.displayname#</th>
						</cfloop>
						<th>#stText.Settings.DBCheck#</th>
						<th width="3%"></th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="clients">
						<cfset css=iif(len(clients.usage),de('Green'),de('Red'))>
						<cfset isOK = not StructKeyExists(stVeritfyMessages, clients.url) or stVeritfyMessages[clients.url].label eq "OK" />
						<!--- and now display --->
						<tr<cfif not isOK> class="notOK"<cfelse> class="OK"</cfif>>
							<td>
								<cfif hasAccess>
									<input type="checkbox" class="checkbox" name="row_#clients.currentrow#" value="#clients.currentrow#">
									<input type="hidden" name="url_#clients.currentrow#" value="#clients.url#">
								</cfif>
								<!--- <input type="hidden" name="password_#clients.currentrow#" value="#clients.Password#">--->
							</td>
							
							<td>#clients.label#</td>
							
							<cfloop query="variables.usage">
								<cfset has=listFindNoCase(clients.usage,variables.usage.code)>
								<td>#YesNoFormat(has)#</td>
							</cfloop>
							
							<cfif StructKeyExists(stVeritfyMessages, clients.url)>
								<td>
									<cfif isOK>
										#stVeritfyMessages[clients.url].label#
									<cfelse>
										<abbr title="#stVeritfyMessages[clients.url].message#">#stVeritfyMessages[clients.url].label#</abbr>
									</cfif>
								</td>
							<cfelse>
								<td>&nbsp;</td>			
							</cfif>
							<td>
								<cfif hasAccess>
									#renderEditButton("#request.self#?action=#url.action#&action2=create&url=#hash(clients.url)#")#
								</cfif>
							</td>
						</tr>
					</cfloop>
				</tbody>
				<cfif hasAccess>
					<tfoot>
						<tr>
							<td colspan="#4+usage.recordcount#">
								<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Verify#">
								<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
								<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.Delete#">
							</td>	
						</tr>
					</tfoot>
				</cfif>
			</table>
		</cfform>
	</cfif>
</cfoutput>

<cfif hasAccess>
	<cfoutput>
		<!--- Create Remote Client --->
		<h2>New remote client</h2>
		<cfform onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
			<input type="submit" class="button submit" name="run" value="#stText.remote.newClient#">
		</cfform>
	</cfoutput>
</cfif>