<cfoutput>
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
</cfoutput>