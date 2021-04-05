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
					localMode="#form.localMode#"
					scopeCascadingType="#form.scopeCascadingType#"
					allowImplicidQueryCall="#isDefined("form.allowImplicidQueryCall") and form.allowImplicidQueryCall#"
					mergeFormAndUrl="#isDefined("form.mergeFormAndUrl") and form.mergeFormAndUrl#"
					
					
					
					clientTimeout="#CreateTimeSpan(form.client_days,form.client_hours,form.client_minutes,form.client_seconds)#"
					sessionTimeout="#CreateTimeSpan(form.session_days,form.session_hours,form.session_minutes,form.session_seconds)#"
					applicationTimeout="#CreateTimeSpan(form.application_days,form.application_hours,form.application_minutes,form.application_seconds)#"
					sessionManagement="#isDefined("form.sessionManagement") and form.sessionManagement#"
					clientManagement="#isDefined("form.clientManagement") and form.clientManagement#"
					clientCookies="#isDefined("form.clientCookies") and form.clientCookies#"
					domaincookies="#isDefined("form.domaincookies") and form.domaincookies#"
					sessionStorage="#form.sessionStorage#"
					clientStorage="#form.clientStorage#"
					cgiReadonly="#isDefined("form.cgiReadonly") and form.cgiReadonly#"
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				<cfadmin 
					action="updateScope"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					sessionType=""
					localMode=""
					scopeCascadingType=""
					allowImplicidQueryCall=""
					mergeFormAndUrl=""
					sessionTimeout=""
					applicationTimeout=""
					sessionManagement=""
					clientManagement=""
					clientCookies=""
					domaincookies=""
					clientTimeout=""
					sessionStorage=""
					clientStorage=""
					cgiReadonly=""
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
						<cfif hasAccess>
							<select name="sessionType" class="small">
								<option value="application" <cfif scope.sessionType EQ "application">selected</cfif>>#stText.Scopes.SessionType_application#</option>
								<option value="jee" <cfif scope.sessionType EQ "jee">selected</cfif>>#stText.Scopes.SessionType_jee#</option>
							</select>
						<cfelse>
							<b>#scope.sessionType#</b>
						</cfif>
						<div class="comment">#stText.Scopes.SessionTypeDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.sessionType = "#scope.sessionType#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					 </td>
				</tr>

				<!--- 
				Merge URL and Form --->
				<tr>
					<th scope="row">#stText.Scopes.mergeUrlForm#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="mergeFormAndUrl" value="yes" 
							<cfif scope.mergeFormAndUrl>checked</cfif>>
						<cfelse>
							<b>#iif(scope.mergeFormAndUrl,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Scopes.mergeUrlFormDescription#</div>
					</td>
				</tr>

				<!--- Session Management --->
				<tr>
					<th scope="row">#stText.Scopes.SessionManagement#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="sessionManagement" value="yes" 
							<cfif scope.SessionManagement>checked</cfif>>
						<cfelse>
							<b>#iif(scope.sessionManagement,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Scopes.SessionManagementDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.sessionManagement = #scope.sessionManagement#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Scopes.ClientManagement#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="clientManagement" value="yes" 
							<cfif scope.clientManagement>checked</cfif>>
						<cfelse>
							<b>#iif(scope.clientManagement,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Scopes.ClientManagementDescription#</div>

						<cfsavecontent variable="codeSample">
							this.clientManagement = #scope.clientManagement#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<!--- Domain Cookies --->
				<tr>
					<th scope="row">#stText.Scopes.DomainCookies#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="domainCookies" value="yes" 
							<cfif scope.domainCookies>checked</cfif>>
						<cfelse>
							<b>#iif(scope.domainCookies,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Scopes.DomainCookiesDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.setDomainCookies = #scope.domainCookies#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<!--- Client Cookies --->
				<tr>
					<th scope="row">#stText.Scopes.ClientCookies#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="clientCookies" value="yes" 
							<cfif scope.clientCookies>checked</cfif>>
						<cfelse>
							<b>#iif(scope.clientCookies,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Scopes.ClientCookiesDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.setClientCookies = #scope.clientCookies#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>		
			
			<!--- CGI readonly --->
				<tr>
					<th scope="row">#stText.Scopes.cgiReadOnly#</th>
					<td>
						
						<!--- <div class="warning nofocus">
					This feature is experimental (set to "writable").
					If you have any problems while using this functionality,
					please post the bugs and errors in our
					<a href="https://issues.lucee.org" target="_blank">bugtracking system</a>. 
				</div>---><div class="comment">#stText.scopes.cgiReadOnlyDesc#</div>
						<cfif hasAccess>
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
						<cfelse>
							<input type="hidden" name="cgiReadonly" value="#scope.cgiReadonly#">
							<b>#stText.Scopes["cgiReadOnly"& scope.LocalMode]#</b><br />
							<div class="comment">#stText.Scopes["cgiReadOnly"& scope.LocalMode&"desc"]#</div>
						</cfif>
						
						<cfsavecontent variable="codeSample">
							this.cgiReadOnly = #scope.cgiReadOnly#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>

				
				<!--- Session Timeout --->
				<tr>
					<th scope="row">#stText.Scopes.SessionTimeout#</th>
					<td>
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
								<cfif hasAccess>
									<tr>
										<td><cfinputClassic type="text" name="session_days" value="#scope.sessionTimeout_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#Session#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="session_hours" value="#scope.sessionTimeout_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#Session#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="session_minutes" value="#scope.sessionTimeout_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#Session#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="session_seconds" value="#scope.sessionTimeout_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#Session#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td align="center"><b>#scope.sessionTimeout_day#</b></td>
										<td align="center"><b>#scope.sessionTimeout_hour#</b></td>
										<td align="center"><b>#scope.sessionTimeout_minute#</b></td>
										<td align="center"><b>#scope.sessionTimeout_second#</b></td>
									</tr>
								</cfif>
							</tbody>
						</table>
						<div class="comment">#stText.Scopes.SessionTimeoutDescription#</div>

						<cfsavecontent variable="codeSample">
							this.sessionTimeout = createTimeSpan( #scope.sessionTimeout_day#, #scope.sessionTimeout_hour#, #scope.sessionTimeout_minute#, #scope.sessionTimeout_second# );
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<!--- Application Timeout --->
				<tr>
					<th scope="row">#stText.Scopes.ApplicationTimeout#</th>
					<td>
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
								<cfif hasAccess>
									<tr>
										<td><cfinputClassic type="text" name="application_days" value="#scope.applicationTimeout_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="application_hours" value="#scope.applicationTimeout_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="application_minutes" value="#scope.applicationTimeout_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="application_seconds" value="#scope.applicationTimeout_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#application#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td align="center"><b>#scope.applicationTimeout_day#</b></td>
										<td align="center"><b>#scope.applicationTimeout_hour#</b></td>
										<td align="center"><b>#scope.applicationTimeout_minute#</b></td>
										<td align="center"><b>#scope.applicationTimeout_second#</b></td>
									</tr>
								</cfif>
							</tbody>
						</table>
						<div class="comment">#stText.Scopes.ApplicationTimeoutDescription#</div>

						<cfsavecontent variable="codeSample">
							this.applicationTimeout = createTimeSpan( #scope.applicationTimeout_day#, #scope.applicationTimeout_hour#, #scope.applicationTimeout_minute#, #scope.applicationTimeout_second# );
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<!--- Client Timeout --->
				<tr>
					<th scope="row">#stText.Scopes.ClientTimeout#</th>
					<td>
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
										<td><cfinputClassic type="text" name="client_days" value="#scope.clientTimeout_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#client#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="client_hours" value="#scope.clientTimeout_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#client#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="client_minutes" value="#scope.clientTimeout_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#client#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="client_seconds" value="#scope.clientTimeout_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#client#stText.Scopes.TimeoutEndValue#"></td>
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
						<div class="comment">#stText.Scopes.ClientTimeoutDescription#</div>
					
						<cfsavecontent variable="codeSample">
							this.clientTimeout = createTimeSpan( #scope.clientTimeout_day#, #scope.clientTimeout_hour#, #scope.clientTimeout_minute#, #scope.clientTimeout_second# );
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				
				<cfset stText.Scopes.SessionStorageDesc="Default Storage for Session, possible values are:<br>
						- memory: the data are only in the memory, so in fact no persistent storage<br>
						- file: the data are stored in the local filesystem<br>
						- cookie: the data are stored in the users cookie<br>
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
						
						<select name="sessionStorage" class="medium">
							<option value="memory" <cfif scope.sessionStorage EQ "memory">selected</cfif>>#ucFirst(stText.Scopes.memory)#</option>
							<option value="file" <cfif scope.sessionStorage EQ "file">selected</cfif>>#ucFirst(stText.Scopes.file)#</option>
							<option value="cookie" <cfif scope.sessionStorage EQ "cookie">selected</cfif>>#ucFirst(stText.Scopes.cookie)#</option>
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
						<!--- <cfinputClassic type="text" name="sessionStorage" value="#scope.sessionStorage#"> --->
						<div class="comment">#stText.Scopes.sessionStorageDesc#</div>

						<cfsavecontent variable="codeSample">
							this.sessionStorage = "#scope.sessionStorage#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				
				<!--- client storage --->
				<tr>
					<th scope="row">#stText.Scopes.clientStorage#</th>
					<td>
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
						<!--- <cfinputClassic type="text" name="clientStorage" value="#scope.clientStorage#"> --->
						<div class="comment">#stText.Scopes.clientStorageDesc#</div>

						<cfsavecontent variable="codeSample">
							this.clientStorage = "#scope.clientStorage#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
<!---
			</tbody>
		</table>

		<h3>#stText.general.dialect.cfml#</h3>
		<div class="itemintro">#stText.general.dialect.cfmlDesc#</div>
		
		<table class="maintbl">
			<tbody>
--->




				<!--- Local Mode --->
				<tr>
					<th scope="row">#stText.Scopes.LocalMode#</th>
					<td>
						<div class="comment">#stText.scopes.localmodeDesc#</div>
						<cfif hasAccess>
							<ul class="radiolist">
								<li>
									<!--- modern --->
									<label>
										<input class="radio" type="radio" name="LocalMode" value="modern"<cfif scope.LocalMode EQ "modern"> checked="checked"</cfif>>
										<b>#stText.Scopes.LocalModeModern#</b>
									</label>
									<div class="comment">#stText.scopes.localmodeModernDesc#</div>
								</li>
								<li>
									<!--- classic --->
									<label>
										<input class="radio" type="radio" name="LocalMode" value="classic"<cfif scope.LocalMode EQ "classic"> checked="checked"</cfif>>
										<b>#stText.Scopes.LocalModeClassic#</b>
									</label>
									<div class="comment">#stText.scopes.localmodeClassicDesc#</div>
								</li>
							</ul>
						<cfelse>
							<input type="hidden" name="localMode" value="#scope.LocalMode#">
							<b>#stText.Scopes["LocalMode"& scope.LocalMode]#</b><br />
							<div class="comment">#stText.Scopes["LocalMode"& scope.LocalMode&"desc"]#</div>
						</cfif>
						
						<cfsavecontent variable="codeSample">
							this.localMode = "#scope.LocalMode#"; // or "#scope.localMode=="modern"?"classic":"modern"#"
// or as part of a function declaration
function test() localMode="#scope.LocalMode#" {}
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>

				<!--- scope cascading --->
				<tr>
					<th scope="row">#stText.Scopes.Cascading#</th>
					<td>
						<cfset type=scope.scopeCascadingType>
						<cfif hasAccess>
							<select name="scopeCascadingType" class="medium">
								<option value="strict" <cfif type EQ "strict">selected</cfif>>#ucFirst(stText.Scopes.Strict)#</option>
								<option value="small" <cfif type EQ "small">selected</cfif>>#ucFirst(stText.Scopes.Small)#</option>
								<option value="standard" <cfif type EQ "standard">selected</cfif>>#ucFirst(stText.Scopes.Standard)#</option>
							</select>
						<cfelse>
							<b>#ucFirst(type)#</b>
						</cfif>
						<div class="comment">#stText.Scopes.CascadingDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.scopeCascading = "#type#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample)>
					</td>
				</tr>
				<!--- cascade to result --->
				<tr>
					<th scope="row">#stText.Scopes.CascadeToResultSet#</th>
					<td>
						<cfif hasAccess>
							<input class="checkbox" type="checkbox" class="checkbox" name="allowImplicidQueryCall" value="yes" <cfif scope.allowImplicidQueryCall>checked</cfif>>
						<cfelse>
							<b>#iif(scope.allowImplicidQueryCall,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Scopes.CascadeToResultSetDescription#</div>


						<cfsavecontent variable="codeSample">
							this.searchResults = #trueFalseFormat(scope.allowImplicidQueryCall)#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample)>
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
							<cfif request.adminType EQ "web">
								<input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#">
							</cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
<!--- Tip
<div class="tip">
	#stText.settings.appcfcdesc#:
	<pre>	this.sessionType="#scope.sessionType#"; // or "#scope.sessionType=="application"?"jee":"application"#"
	this.SessionManagement=#scope.sessionManagement#; // or #scope.sessionManagement?false:true#
	this.clientManagement=#scope.clientManagement#; // or #scope.clientManagement?false:true#
	this.setDomainCookies=#scope.domainCookies#; // or #scope.domainCookies?false:true#
	this.setClientCookies=#scope.clientCookies#; // or #scope.clientCookies?false:true#
	this.localMode="#scope.LocalMode#"; // or "#scope.localMode=="modern"?"classic":"modern"#"
	this.sessionTimeout=createTimeSpan(#scope.sessionTimeout_day#,#scope.sessionTimeout_hour#,#scope.sessionTimeout_minute#,#scope.sessionTimeout_second#);
	this.applicationTimeout=createTimeSpan(#scope.applicationTimeout_day#,#scope.applicationTimeout_hour#,#scope.applicationTimeout_minute#,#scope.applicationTimeout_second#);
	this.clientTimeout=createTimeSpan(#scope.clientTimeout_day#,#scope.clientTimeout_hour#,#scope.clientTimeout_minute#,#scope.clientTimeout_second#);
	this.sessionStorage="#scope.sessionStorage#";
	this.clientStorage="#scope.clientStorage#";</pre></div> --->
</cfoutput>