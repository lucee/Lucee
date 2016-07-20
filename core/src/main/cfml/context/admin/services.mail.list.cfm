


<cfoutput>
	
	<!--- Mail Settings
	@todo help text --->
	
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>
	
	<h2>#stText.Mail.Settings#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.mail.DefaultEncoding#</th>
					<td>
						<cfif hasAccess>
							<cfinputClassic type="text" name="defaultencoding" value="#mail.defaultEncoding#" class="medium" required="no" message="#stText.mail.missingEncoding#">
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
							<cfinputClassic type="text" name="logFile" value="#mail.strlogfile#" required="no" class="xlarge"
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
							<cfinputClassic type="text" name="spoolInterval" value="#mail.spoolInterval#" validate="integer" class="number" required="no">
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
							<cfinputClassic type="text" name="timeout" value="#mail.timeout#" validate="integer" class="number" required="no"> #stText.mail.seconds#
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
	</cfformClassic>








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
		
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th width="3%">
						<input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)" />
					</th>
					<th>#stText.Mail.Server#</th>
					<!--- <th>#stText.Mail.Password#</th> --->
					<th>#stText.Mail.Port#</th>
					<th>#stText.Mail.Username#</th>
					<th>#stText.Mail.tls#</th>
					<th>#stText.Mail.ssl#</th>
					<th>#stText.Mail.life#<br><span class="comment">dd:hh:mm:ss</span></th>
					<th>#stText.Mail.idle#<br><span class="comment">dd:hh:mm:ss</span></th>
					<th></th>
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
							<input type="hidden" name="username_#ms.currentrow#" value="#ms.username#">
							<input type="hidden" name="password_#ms.currentrow#" value="#variables.stars#">
							<input type="hidden" name="port_#ms.currentrow#" value="#ms.port#">


							#ms.hostName#
						</td>
						
						<!--- port --->
						<td>
							#ms.port#
						</td>
						<!--- username --->
						<td>
							#ms.username#
						</td>
						<!--- tls --->
						<td>
							#yesNoFormat(ms.tls)#
						</td>
						<!--- ssl --->
						<td>
							#yesNoFormat(ms.ssl)#
						</td>
						<!--- life --->
						<td><cfset sct=toTSStruct(ms.life)>
							#fill(sct.days)#:#fill(sct.hours)#:#fill(sct.minutes)#:#fill(sct.seconds)#
						</td>
						<!--- life --->
						<td><cfset sct=toTSStruct(ms.idle)>
							#fill(sct.days)#:#fill(sct.hours)#:#fill(sct.minutes)#:#fill(sct.seconds)#
						</td>


						<!--- edit --->
						<td>
							#renderEditButton("#request.self#?action=#url.action#&action2=edit&row=#ms.currentrow#")#
						</td>
					</tr>
				</cfloop>
				
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
							<!--- <input type="submit" class="bm button submit" name="subAction" value="#stText.Buttons.Update#"> --->
							<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="br button submit" name="subAction" value="#stText.Buttons.Delete#">
						</td>	
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>







<!--- NEW Server --->	
	<cfset data={hostName:"",port:"",username:"",life:60,idle:10}>
	<h2>#stText.mail.createnewMailServerConn#</h2>
	<p>#stText.mail.createnewMailServerConnDesc#</p>
	<cfinclude template="services.mail.form.cfm">





</cfoutput>