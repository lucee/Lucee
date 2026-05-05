<cfset error.message="">
<cfset error.detail="">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="setting"
	secValue="yes">

<cfadmin 
	action="getScope"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="scope">

<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfif hasAccess>
	<cftry>
		<cfswitch expression="#form.mainAction#">
		<!--- UPDATE --->
			<cfcase value="#stText.Buttons.Update#">
				<cfadmin 
					action="updateScope"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					sessionType="#form.sessionType#"
					localScopeMode="#form.localScopeMode#"
					scopeCascadingType="#form.scopeCascading#"
					cascadeToResultset="#isDefined("form.cascadeToResultset") and form.cascadeToResultset#"
					mergeUrlForm="#isDefined("form.mergeUrlForm") and form.mergeUrlForm#"
					formUrlAsStruct="#isDefined("form.formUrlAsStruct") and form.formUrlAsStruct#"
					
					clientTimeout="#CreateTimeSpan(form.clientTimeout_days,form.clientTimeout_hours,form.clientTimeout_minutes,form.clientTimeout_seconds)#"
					sessionTimeout="#CreateTimeSpan(form.sessionTimeout_days,form.sessionTimeout_hours,form.sessionTimeout_minutes,form.sessionTimeout_seconds)#"
					applicationTimeout="#CreateTimeSpan(form.applicationTimeout_days,form.applicationTimeout_hours,form.applicationTimeout_minutes,form.applicationTimeout_seconds)#"
					sessionManagement="#isDefined("form.sessionManagement") and form.sessionManagement#"
					clientManagement="#isDefined("form.clientManagement") and form.clientManagement#"
					clientCookies="#isDefined("form.clientCookies") and form.clientCookies#"
					domaincookies="#isDefined("form.domaincookies") and form.domaincookies#"
					sessionStorage="#form.sessionStorage#"
					clientStorage="#form.clientStorage#"
					cgiScopeReadOnly="#isDefined("form.cgiReadonly") and form.cgiReadonly#"
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				<cfadmin 
					action="updateScope"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					sessionType=""
					localScopeMode=""
					scopeCascadingType=""
					cascadeToResultset=""
					mergeUrlForm=""
					formUrlAsStruct=""
					sessionTimeout=""
					applicationTimeout=""
					sessionManagement=""
					clientManagement=""
					clientCookies=""
					domaincookies=""
					clientTimeout=""
					sessionStorage=""
					clientStorage=""
					cgiScopeReadOnly=""
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
			<cfset error.cfcatch=cfcatch>
		</cfcatch>
	</cftry>
</cfif>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>

<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>


	<div class="pageintro">
		<cfif request.adminType EQ "server">
			#stText.Scopes.Server#
		<cfelse>
			#stText.Scopes.Web#
		</cfif>
	</div>

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>

				<!---
				Session Type---->
				<tr>
					<th scope="row">#stText.Scopes.SessionType#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="sessionType" 
							value="#scope.sessionType#"
							access="#hasAccess#"
							description="#stText.Scopes.SessionTypeDescription#"
							br=true>
							<select name="sessionType" class="small">
								<option value="application" <cfif scope.sessionType EQ "application">selected</cfif>>#stText.Scopes.SessionType_application#</option>
								<option value="jee" <cfif scope.sessionType EQ "jee">selected</cfif>>#stText.Scopes.SessionType_jee#</option>
							</select>
						</cfmodule>
					 </td>
				</tr>

				<!--- 
				Merge URL and Form --->
				<tr>
					<th scope="row">#stText.Scopes.mergeUrlForm#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="mergeUrlForm" 
							value="#scope.mergeFormAndUrl#"
							access="#hasAccess#"
							description="#stText.Scopes.mergeUrlFormDescription#"
							sp=false>
							<input type="checkbox" class="checkbox" name="mergeUrlForm" value="yes" 
							<cfif scope.mergeFormAndUrl>checked</cfif>>
						</cfmodule>
					</td>
				</tr>

				<!---
					Whether or not to merge form and url variables into structs --->
					<tr>
						<th scope="row">#stText.Scopes.formUrlAsStruct#</th>
						<td>
							<cfmodule template="systemSetting.cfm" 
							name="formUrlAsStruct" 
							value="#scope.formUrlAsStruct#"
							access="#hasAccess#"
							description="#stText.Scopes.formUrlAsStructDescription#"
							sp=true>
								<input type="checkbox" class="checkbox" name="formUrlAsStruct" value="yes" 
								<cfif scope.formUrlAsStruct>checked</cfif>>
							</cfmodule>
						</td>
					</tr>


				<!--- Session Management --->
				<tr>
					<th scope="row">#stText.Scopes.SessionManagement#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="sessionManagement" 
							value="#scope.sessionManagement#"
							access="#hasAccess#"
							description="#stText.Scopes.sessionManagementDescription#"
							sp=true>
							<input type="checkbox" class="checkbox" name="sessionManagement" value="yes" 
								<cfif scope.SessionManagement>checked</cfif>>
						</cfmodule>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Scopes.ClientManagement#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="clientManagement" 
							value="#scope.clientManagement#"
							access="#hasAccess#"
							description="#stText.Scopes.clientManagementDescription#"
							sp=true>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="clientManagement" value="yes" 
							<cfif scope.clientManagement>checked</cfif>>
						<cfelse>
							<b>#iif(scope.clientManagement,de('Yes'),de('No'))#</b>
						</cfif>
						</cfmodule>
					</td>
				</tr>
				<!--- Domain Cookies --->
				<tr>
					<th scope="row">#stText.Scopes.DomainCookies#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="domainCookies" 
							value="#scope.domainCookies#"
							access="#hasAccess#"
							description="#stText.Scopes.domainCookiesDescription#"
							sp=true>
							<input type="checkbox" class="checkbox" name="domainCookies" value="yes" 
							<cfif scope.domainCookies>checked</cfif>>
						</cfmodule>
					</td>
				</tr>
				<!--- Client Cookies --->
				<tr>
					<th scope="row">#stText.Scopes.ClientCookies#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="clientCookies" 
							value="#scope.clientCookies#"
							access="#hasAccess#"
							description="#stText.Scopes.clientCookiesDescription#"
							sp=true>
							<input type="checkbox" class="checkbox" name="clientCookies" value="yes" 
							<cfif scope.clientCookies>checked</cfif>>
						</cfmodule>
					</td>
				</tr>		
			
			<!--- CGI readonly --->
				<tr>
					<th scope="row">#stText.Scopes.cgiReadOnly#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="cgiScopeReadOnly" 
							value="#scope.cgiReadonly#"
							access="#hasAccess#"
							description="#stText.Scopes.cgiReadOnlyDesc#"
							descOnTop=true
							sp=true>
							<ul class="radiolist">
								<li>
									<!--- readonly --->
									<label>
										<input class="radio" type="radio" name="cgiReadonly" value="true"<cfif scope.cgiReadonly> checked="checked"</cfif>>
										<b>#stText.Scopes.cgiReadOnlyTrue#</b>
									</label>
									<div class="comment">#stText.scopes.cgiReadOnlyTrueDesc#</div>
								</li>
								<li>
									<!--- writable --->
									<label>
										<input class="radio" type="radio" name="cgiReadonly" value="false"<cfif !scope.cgiReadonly> checked="checked"</cfif>>
										<b>#stText.Scopes.cgiReadOnlyFalse#</b>
									</label>
									<div class="comment">#stText.scopes.cgiReadOnlyFalseDesc#
									</div>
								</li>
							</ul>
						</cfmodule>
					</td>
				</tr>

				
				<!--- Session Timeout --->
				<tr>
					<th scope="row">#stText.Scopes.SessionTimeout#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="sessionTimeout" 
							value="#scope.sessionTimeout#"
							access="#hasAccess#"
							description="#stText.Scopes.SessionTimeoutDescription#"
							sp=true>
						<cfset timeout=scope.sessionTimeout>
						<table class="maintbl autowidth">
							<thead>
								<tr>
									<th>#stText.General.Days#</td>
									<th>#stText.General.Hours#</td>
									<th>#stText.General.Minutes#</td>
									<th>#stText.General.Seconds#</td>
								</tr>
							</thead>
							<tbody>
									<tr>
										<td><cfinputClassic type="text" name="sessionTimeout_days" value="#scope.sessionTimeout_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#Session#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="sessionTimeout_hours" value="#scope.sessionTimeout_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#Session#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="sessionTimeout_minutes" value="#scope.sessionTimeout_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#Session#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="sessionTimeout_seconds" value="#scope.sessionTimeout_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#Session#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
							</tbody>
						</table>
						</cfmodule>
					</td>
				</tr>
				<!--- Application Timeout --->
				<tr>
					<th scope="row">#stText.Scopes.ApplicationTimeout#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="applicationTimeout" 
							value="#scope.applicationTimeout#"
							access="#hasAccess#"
							description="#stText.Scopes.ApplicationTimeoutDescription#"
							sp=true>
						<cfset timeout=scope.applicationTimeout>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</td>
									<th>#stText.General.Hours#</td>
									<th>#stText.General.Minutes#</td>
									<th>#stText.General.Seconds#</td>
								</tr>
							</thead>
							<tbody>
									<tr>
										<td><cfinputClassic type="text" name="applicationTimeout_days" value="#scope.applicationTimeout_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="applicationTimeout_hours" value="#scope.applicationTimeout_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="applicationTimeout_minutes" value="#scope.applicationTimeout_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="applicationTimeout_seconds" value="#scope.applicationTimeout_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#application#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
							</tbody>
						</table>
						</cfmodule>
					</td>
				</tr>
				<!--- Client Timeout --->
				<tr>
					<th scope="row">#stText.Scopes.ClientTimeout#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="clientTimeout" 
							value="#scope.clientTimeout#"
							access="#hasAccess#"
							description="#stText.Scopes.ClientTimeoutDescription#"
							sp=true>
						<cfset timeout=scope.clientTimeout>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</td>
									<th>#stText.General.Hours#</td>
									<th>#stText.General.Minutes#</td>
									<th>#stText.General.Seconds#</td>
								</tr>
							</thead>
							<tbody>
								<cfif hasAccess>
									<tr>
										<td><cfinputClassic type="text" name="clientTimeout_days" value="#scope.clientTimeout_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#client#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="clientTimeout_hours" value="#scope.clientTimeout_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#client#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="clientTimeout_minutes" value="#scope.clientTimeout_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#client#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="clientTimeout_seconds" value="#scope.clientTimeout_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#client#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td align="center"><b>#scope.clientTimeout_day#</b></td>
										<td align="center"><b>#scope.clientTimeout_hour#</b></td>
										<td align="center"><b>#scope.clientTimeout_minute#</b></td>
										<td align="center"><b>#scope.clientTimeout_second#</b></td>
									</tr>
								</cfif>
							</tbody>
						</table>
						</cfmodule>
					</td>
				</tr>
				
				<cfset stText.Scopes.SessionStorageDesc="Default Storage for Session, possible values are:<br>
						- memory: the data are only in the memory, so in fact no persistent storage<br>
						- file: the data are stored in the local filesystem<br>
						- &lt;cache-name&gt;: name of a cache instance that has ""Storage"" enabled<br>
						- &lt;datasource-name&gt;: name of a datasource instance that has ""Storage"" enabled">
				<cfset stText.Scopes.ClientStorageDesc="Default Storage for Session, possible values are:<br>
						- memory: the data are only in the memory, so in fact no persistent storage<br>
						- file: the data are stored in the local filesystem<br>
						- cookie: the data are stored in the users cookie<br>
						- &lt;cache-name&gt;: name of a cache instance that has ""Storage"" enabled<br>
						- &lt;datasource-name&gt;: name of a datasource instance that has ""Storage"" enabled">
							
				
				<!--- session storage --->
				<tr>
					<th scope="row">#stText.Scopes.sessionStorage#</th>
					<td>
						<cfadmin 
						action="getDatasources"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						returnVariable="datasourcesQuery">
						<cfset datasources = ValueArray(datasourcesQuery.name)>
						
						<cfadmin 
						action="getCacheConnections"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						returnVariable="cacheConnectionsQuery">
						<cfset cacheConnections = ValueArray(cacheConnectionsQuery.name)>
						
						<cfmodule template="systemSetting.cfm" 
							name="sessionStorage" 
							value="#scope.sessionStorage#"
							access="#hasAccess#"
							description="#stText.Scopes.SessionStorageDesc#"
							br=true
							sp=true>
						<select name="sessionStorage" class="medium">
							<option value="memory" <cfif scope.sessionStorage EQ "memory">selected</cfif>>#ucFirst(stText.Scopes.memory)#</option>
							<option value="file" <cfif scope.sessionStorage EQ "file">selected</cfif>>#ucFirst(stText.Scopes.file)#</option>
							<cfloop from="1" to="#arrayLen(cacheConnections)#" index="key">
								<cfif key EQ 1>
									<optgroup label="Cache">
								</cfif>
								<option value="#cacheConnections[key]#" <cfif scope.sessionStorage EQ cacheConnections[key]>selected</cfif>>cache: #cacheConnections[key]#</option>
								<cfif key EQ arrayLen(cacheConnections)>
									</optgroup>
								</cfif>
							</cfloop>
							<cfloop from="1" to="#arrayLen(datasources)#" index="key">
								<cfif key EQ 1>
									<optgroup label="Datasources">
								</cfif>
								<option value="#datasources[key]#" <cfif scope.sessionStorage EQ datasources[key]>selected</cfif>>dsn: #datasources[key]#</option>
								<cfif key EQ arrayLen(datasources)>
									</optgroup>
								</cfif>
							</cfloop>
						</select>
						</cfmodule>
					</td>
				</tr>
				
				<!--- client storage --->
				<tr>
					<th scope="row">#stText.Scopes.clientStorage#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="clientStorage" 
							value="#scope.clientStorage#"
							access="#hasAccess#"
							description="#stText.Scopes.ClientStorageDesc#"
							br=true
							sp=true>
						<select name="clientStorage" class="medium">
							<option value="memory" <cfif scope.clientStorage EQ "memory">selected</cfif>>#ucFirst(stText.Scopes.memory)#</option>
							<option value="file" <cfif scope.clientStorage EQ "file">selected</cfif>>#ucFirst(stText.Scopes.file)#</option>
							<option value="cookie" <cfif scope.clientStorage EQ "cookie">selected</cfif>>#ucFirst(stText.Scopes.cookie)#</option>
							<cfloop from="1" to="#arrayLen(cacheConnections)#" index="key">
								<cfif key EQ 1>
									<optgroup label="Cache">
								</cfif>
								<option value="#cacheConnections[key]#" <cfif scope.clientStorage EQ cacheConnections[key]>selected</cfif>>cache: #cacheConnections[key]#</option>
								<cfif key EQ arrayLen(cacheConnections)>
									</optgroup>
								</cfif>
							</cfloop>
							<cfloop from="1" to="#arrayLen(datasources)#" index="key">
								<cfif key EQ 1>
									<optgroup label="Datasources">
								</cfif>
								<option value="#datasources[key]#" <cfif scope.clientStorage EQ datasources[key]>selected</cfif>>dsn: #datasources[key]#</option>
								<cfif key EQ arrayLen(datasources)>
									</optgroup>
								</cfif>
							</cfloop>
						</select>
						</cfmodule>
					</td>
				</tr>

				<!--- Local Mode --->
				<tr>
					<th scope="row">#stText.Scopes.LocalMode#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="localScopeMode" 
							value="#scope.LocalMode#"
							access="#hasAccess#"
							description="#stText.Scopes.localmodeDesc#"
							br=true
							sp=true
							descOnTop=true>
							<ul class="radiolist">
								<li>
									<!--- modern --->
									<label>
										<input class="radio" type="radio" name="localScopeMode" value="modern"<cfif scope.LocalMode EQ "modern"> checked="checked"</cfif>>
										<b>#stText.Scopes.LocalModeModern#</b>
									</label>
									<div class="comment">#stText.scopes.localmodeModernDesc#</div>
								</li>
								<li>
									<!--- classic --->
									<label>
										<input class="radio" type="radio" name="localScopeMode" value="classic"<cfif scope.LocalMode EQ "classic"> checked="checked"</cfif>>
										<b>#stText.Scopes.LocalModeClassic#</b>
									</label>
									<div class="comment">#stText.scopes.localmodeClassicDesc#</div>
								</li>
							</ul>
						</cfmodule>
					</td>
				</tr>

				<!--- scope cascading --->
				<tr>
					<th scope="row">#stText.Scopes.Cascading#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="scopeCascading" 
							value="#scope.scopeCascadingType#"
							access="#hasAccess#"
							description="#stText.Scopes.CascadingDescription#"
							br=true
							sp=true
							descOnTop=false>
						<cfset type=scope.scopeCascadingType>
							<select name="scopeCascading" class="medium">
								<option value="strict" <cfif type EQ "strict">selected</cfif>>#ucFirst(stText.Scopes.Strict)#</option>
								<option value="small" <cfif type EQ "small">selected</cfif>>#ucFirst(stText.Scopes.Small)#</option>
								<option value="standard" <cfif type EQ "standard">selected</cfif>>#ucFirst(stText.Scopes.Standard)#</option>
							</select>
						</cfmodule>
					</td>
				</tr>
				<!--- cascade to result --->
				<tr>
					<th scope="row">#stText.Scopes.CascadeToResultSet#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="cascadeToResultset" 
							value="#scope.allowImplicidQueryCall#"
							access="#hasAccess#"
							description="#stText.Scopes.CascadeToResultSetDescription#"
							br=false
							sp=true
							descOnTop=false>
							<input class="checkbox" type="checkbox" class="checkbox" name="cascadeToResultset" value="yes" <cfif scope.allowImplicidQueryCall>checked</cfif>>
						</cfmodule>
						<div class="comment">#stText.Scopes.CascadeToResultSetDescription#</div>
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
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif not request.singleMode and request.adminType EQ "web">
								<input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#">
							</cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>

</cfoutput>