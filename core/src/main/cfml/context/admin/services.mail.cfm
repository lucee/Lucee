<cfset stText.mail.LogFileDesc="Destination file where the log information get stored.">
<cfset stText.mail.LogLevelDesc="Log level used for the log file.">
<cfset stText.mail.SpoolEnabledDesc="If enabled the mails are sent in a background thread and the main request does not have to wait until the mails are sent.">
<cfset stText.mail.maxThreads="Maximum concurrent threads">
<cfset stText.mail.maxThreadsDesc="This setting can be changed on page ""Services/Taks"". Maximum number of threads that are executed at the same time to send the mails, fewer threads will take longer to send all the mail, more threads will add more load to the system.">
<cfset stText.mail.TimeoutDesc="Time in seconds that the Task Manager waits to send a single mail, when the time is reached the Task Manager stops the thread and the mail gets moved to unsent folder, where the Task Manager will pick it up later to try to send it again.">
<cfset stText.mail.seconds="Seconds">
<!--- 
Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">
<cfparam name="stveritfymessages" default="#struct()#">
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="mail"
	secValue="yes">
	
<cfadmin 
	action="getMailSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mail">
<cfadmin 
	action="getMailServers"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="ms">

<cfscript>
	variables.stars = "*********";
	function toPassword(host,pw, ms)
	{
		var i=1;
		if(arguments.pw EQ variables.stars)
		{
			for(i=arguments.ms.recordcount;i>0;i--)
			{
				if(arguments.host EQ arguments.ms.hostname[i])
					return arguments.ms.password[i];
			}
		}
		return arguments.pw;
	}
</cfscript>

<!--- ACTIONS --->
<cftry>
	<cfswitch expression="#form.mainAction#">
		<!--- Setting --->
		<cfcase value="#stText.Buttons.Setting#">
			<cfif form._mainAction EQ stText.Buttons.update>
				<cfadmin 
					action="updateMailSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					spoolEnable="#isDefined("form.spoolenable") and form.spoolenable#"
					timeout="#form.timeout#"
					defaultEncoding="#form.defaultEncoding#"
					remoteClients="#request.getRemoteClients()#">
			<cfelseif form._mainAction EQ stText.Buttons.resetServerAdmin>
				<!--- reset to server setting --->
				<cfadmin 
					action="updateMailSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					spoolEnable=""
					timeout=""
					defaultEncoding=""
					
					remoteClients="#request.getRemoteClients()#">
			 </cfif>
		</cfcase>
		<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
			<!--- update --->
			<cfif form.subAction EQ "#stText.Buttons.Update#">
							
				<cfset data.hosts=toArrayFromForm("hostname")>
				<cfset data.usernames=toArrayFromForm("username")>
				<cfset data.passwords=toArrayFromForm("password")>
				<cfset data.ports=toArrayFromForm("port")>
				<cfset data.tlss=toArrayFromForm("tls")>
				<cfset data.ssls=toArrayFromForm("ssl")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.ids=toArrayFromForm("id")>
				<cfloop index="idx" from="1" to="#arrayLen(data.hosts)#">
					<cfif isDefined("data.rows[#idx#]") and data.hosts[idx] NEQ "">
						<cfparam name="data.ports[idx]" default="25">
						<cfif trim(data.ports[idx]) EQ "">
							<cfset data.ports[idx]=25>
						</cfif>
						<cfadmin 
							action="updateMailServer"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							hostname="#data.hosts[idx]#"
							dbusername="#data.usernames[idx]#"
							dbpassword="#toPassword(data.hosts[idx],data.passwords[idx], ms)#"
							port="#data.ports[idx]#"
							id="#isDefined("data.ids[#idx#]")?data.ids[idx]:''#"
							tls="#isDefined("data.tlss[#idx#]") and data.tlss[idx]#"
							ssl="#isDefined("data.ssls[#idx#]") and data.ssls[idx]#"
							remoteClients="#request.getRemoteClients()#">
					</cfif>
				</cfloop>
			<!--- delete --->
			<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.hosts=toArrayFromForm("hostname")>
				<!---  @todo
				<cflock type="exclusive" scope="application" timeout="5"></cflock> --->
				<cfset len=arrayLen(data.hosts)>
				<cfloop index="idx" from="1" to="#len#">
					<cfif isDefined("data.rows[#idx#]") and data.hosts[idx] NEQ "">
						<cfadmin 
							action="removeMailServer"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							hostname="#data.hosts[idx]#"
							remoteClients="#request.getRemoteClients()#">
					</cfif>
				</cfloop>
			<cfelseif form.subAction EQ "#stText.Buttons.Verify#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.hosts=toArrayFromForm("hostName")>
				<cfset data.usernames=toArrayFromForm("username")>
				<cfset data.passwords=toArrayFromForm("password")>
				<cfset data.ports=toArrayFromForm("port")>
				<cfset doNotRedirect=true>
				<cfloop index="idx" from="1" to="#arrayLen(data.rows)#">
					<cfif isDefined("data.rows[#idx#]") and isDefined("data.hosts[#idx#]") and data.hosts[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyMailServer"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								hostname="#data.hosts[idx]#"
								port="#data.ports[idx]#"
								mailusername="#data.usernames[idx]#"
								mailpassword="#toPassword(data.hosts[idx],data.passwords[idx], ms)#">
							<cfset stVeritfyMessages[data.hosts[idx]].Label = "OK">
							<cfcatch>
								<cfset stVeritfyMessages[data.hosts[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.hosts[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
					</cfif>
				</cfloop>
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>

<!--- Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and not isDefined('doNotRedirect')>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfoutput>
	<!--- Error Output--->
	<cfset printError(error)>
	
	<!--- Mail Settings
	@todo help text --->
	
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>
	
	<h2>#stText.Mail.Settings#</h2>
	<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.mail.DefaultEncoding#</th>
					<td>
						<cfif hasAccess>
							<cfinput type="text" name="defaultencoding" value="#mail.defaultEncoding#" class="medium" required="no" message="#stText.mail.missingEncoding#">
						<cfelse>
							<input type="hidden" name="defaultencoding" value="#mail.defaultEncoding#">
							<b>#mail.defaultEncoding#</b>
						</cfif>
						<div class="comment">#stText.mail.DefaultEncodingDescription#</div>
					</td>
				</tr>
<!---
				<cfset css = len(mail.logfile) EQ 0 and len(mail.strlogfile) NEQ 0 ? 'Red':'' />
				<tr>
					<th scope="row">#stText.Mail.LogFile#</th>
					<td class="tblContent#css# tooltipMe"<cfif mail.strlogfile neq mail.logfile> title="#mail.strlogfile#:<br>#mail.logfile#"</cfif>>
						<cfif hasAccess>
							<cfinput type="text" name="logFile" value="#mail.strlogfile#" required="no" class="xlarge"
								message="#stText.Mail.LogFileMissing#">
						<cfelse>
							<b>#mail.strlogfile#</b><br>
						</cfif>
						<div class="comment">#stText.mail.LogFileDesc#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Mail.Level#</th>
					<td>
						<cfif hasAccess>
							<cfset levels=array("INFO","DEBUG","WARN","ERROR","FATAL")>
							<select name="logLevel" class="small">
								<cfloop index="idx" from="1" to="#arrayLen(levels)#"><option<cfif levels[idx] EQ mail.logLevel> selected</cfif>>#levels[idx]#</option></cfloop>
							</select>
						<cfelse>
							<b>#mail.logLevel#</b><br>
						</cfif>
						<div class="comment">#stText.mail.LogLevelDesc#</div>
					</td>
				</tr>
--->
				<tr>
					<th scope="row">#stText.Mail.SpoolEnabled#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="spoolEnable" value="yes"<cfif mail.spoolEnable> checked</cfif> /><br>
						<cfelse>
							<b>#iif(mail.spoolEnable,de('Yes'),de('No'))#</b><br>
						</cfif>
						<div class="comment">#stText.mail.SpoolEnabledDesc#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Mail.maxThreads#</th>
					<td>
						<b>#mail.maxThreads#</b>
						<div class="comment">#stText.mail.maxThreadsDesc#</div>
					</td>
				</tr>
				<!---- there is no spooler intervall anymore
				<tr>
					<th scope="row">#stText.Mail.SpoolInterval#</th>
					<td>
						<cfif hasAccess>
							<cfinput type="text" name="spoolInterval" value="#mail.spoolInterval#" validate="integer" class="number" required="no">
						<cfelse>
							<b>#mail.spoolInterval#</b>
						</cfif>
					</td>
				</tr>
				---->
				<tr>
					<th scope="row">#stText.Mail.Timeout#</th>
					<td>
						<cfif hasAccess>
							<cfinput type="text" name="timeout" value="#mail.timeout#" validate="integer" class="number" required="no"> #stText.mail.seconds#
						<cfelse>
							<b>#mail.timeout# #stText.mail.seconds#</b><br>
						</cfif>
						<div class="comment">#stText.mail.TimeoutDesc#</div>
					</td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2"><cfoutput>
							<input type="hidden" name="mainAction" value="#stText.Buttons.Setting#">
							<input type="submit" class="bl button submit" name="_mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="canel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="_mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</cfoutput></td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfform>

	<!--- Existing mailservers --->
	<h2>#stText.Mail.MailServers#</h2>
	<div class="itemintro">#stText.Mail.MailServersDescription#</div>
	
	<!--- show verify messages in a more prominent way --->
	<cfloop collection="#stVeritfyMessages#" item="hostname">
		<cfif stVeritfyMessages[hostname].label eq "OK">
			<div class="message">
				Verification of mail server [#hostname#] was successful.
			</div>
		<cfelse>
			<div class="error">
				<strong>Verification of mail server [#hostname#] failed:</strong>
				<br /><em>#stVeritfyMessages[hostName].message#</em>
			</div>
		</cfif>
	</cfloop>
		
	<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th width="3%">
						<input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)" />
					</th>
					<th>#stText.Mail.Server#</th>
					<th>#stText.Mail.Username#</th>
					<th>#stText.Mail.Password#</th>
					<th>#stText.Mail.Port#</th>
					<th>#stText.Mail.tls#</th>
					<th>#stText.Mail.ssl#</th>
				</tr>
			</thead>
			<tbody>
				<cfloop query="ms">
					<tr>
						<td>
							<input type="hidden" name="id_#ms.currentrow#" value="#hash(ms.hostName&":"&ms.username&":"&ms.password&":"&ms.tls&":"&ms.ssl)#">
							<cfif not ms.readonly>
								<input type="checkbox" class="checkbox" name="row_#ms.currentrow#" value="#ms.currentrow#">
							</cfif>
						</td>
						<!--- host --->
						<td>
							<input type="hidden" name="hostName_#ms.currentrow#" value="#ms.hostName#">
							#ms.hostName#
						</td>
						<!--- username --->
						<td>
							<cfif ms.readonly>
								#ms.username#&nbsp;
							<cfelse>
								<cfinput onKeyDown="checkTheBox(this)" type="text" name="username_#ms.currentrow#" 
									value="#ms.username#" required="no" class="xlarge" message="#stText.Mail.UserNameMissing##ms.currentrow#)">
							</cfif>
						</td>
						<!--- password --->
						<td>
							<cfif ms.readonly>
								***********
							<cfelse>
								<cfinput onKeyDown="checkTheBox(this)" type="password" passthrough='autocomplete="off"'
									onClick="if (this.value=='#variables.stars#')this.value='';" name="password_#ms.currentrow#" value="#variables.stars#" required="no"  
									class="xlarge" message="#stText.Mail.PasswordMissing##ms.currentrow#)">
							</cfif>
						</td>
						<!--- port --->
						<td>
							<cfif ms.readonly>
								#ms.port#
							<cfelse>
								<cfinput onKeyDown="checkTheBox(this)" 
								type="text" name="port_#ms.currentrow#" value="#ms.port#" required="no"  
								class="xlarge" validate="integer" 
								message="#stText.Mail.PortErrorFirst##ms.currentrow##stText.Mail.PortErrorLast#">
							</cfif>
						</td>
						<!--- tls --->
						<td>
							<cfif ms.readonly>
								#ms.tls#
							<cfelse>
								<cfinput onClick="checkTheBox(this)" type="checkbox" class="checkbox" name="tls_#ms.currentrow#"
								value="true" required="no" checked="#ms.tls#">
							</cfif>
						</td>
						<!--- ssl --->
						<td>
							<cfif ms.readonly>
								#ms.ssl#
							<cfelse>
								<cfinput onClick="checkTheBox(this)" type="checkbox" class="checkbox" name="ssl_#ms.currentrow#"
								value="true" required="no" checked="#ms.ssl#">
							</cfif>
						</td>
					</tr>
				</cfloop>
				<cfif hasAccess>
					<tr>
						<td>
							<input type="hidden" name="id_#ms.recordcount+1#" value="new">
							<input type="checkbox" class="checkbox linecb" name="row_#ms.recordcount+1#" value="0">
						</td>
						<td>
							<cfinput onKeyDown="checkTheBox(this)"  
							type="text" name="hostName_#ms.recordcount+1#" value="" required="no" class="xlarge">
						</td>
						<td>
							<cfinput onKeyDown="checkTheBox(this)" type="text" name="username_#ms.recordcount+1#" value="" required="no" class="xlarge">
						</td>
						<td>
							<cfinput onKeyDown="checkTheBox(this)" type="password" name="password_#ms.recordcount+1#" passthrough='autocomplete="off"' value="" required="no" class="xlarge">
						</td>
						<td>
							<cfinput onKeyDown="checkTheBox(this)" 
								type="text" name="port_#ms.recordcount+1#" value="" required="no" validate="integer" 
								message="Value for Port (Row #ms.recordcount+1#) must be of type number" class="xlarge">
						</td>
						<td>
							<cfinput onClick="checkTheBox(this)" type="checkbox" class="checkbox" name="tls_#ms.recordcount+1#" value="true" required="no">
						</td>
						<td>
							<cfinput onClick="checkTheBox(this)" type="checkbox" class="checkbox" name="ssl_#ms.recordcount+1#" value="true" required="no">
						</td>
					</tr>
				</cfif>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="7" line=true>
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="7">
							<input type="hidden" name="mainAction" value="#stText.Buttons.Update#">
							<input type="submit" class="bl button submit" name="subAction" value="#stText.Buttons.Verify#">
							<input type="submit" class="bm button submit" name="subAction" value="#stText.Buttons.Update#">
							<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="br button submit" name="subAction" value="#stText.Buttons.Delete#">
						</td>	
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfform>
</cfoutput>