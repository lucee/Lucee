<cfoutput>
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
</cfoutput>