


<cfoutput>
	

<!--- NEW Server --->	
	
		<cfif hasAccess>
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="id_#ms.recordcount+1#" value="new">
			<input type="hidden" name="row_#ms.recordcount+1#" value="true" >
			<table class="maintbl">
				<tbody>
					<!--- host --->
					<tr>
						<th scope="row">#stText.Mail.Server#</th>
						<td>
							<cfinput type="text" name="hostName_#ms.recordcount+1#" value="#data.hostName#" required="yes" class="large" message="#stText.Mail.hostnameMissing#">
							<div class="comment">#stText.mail.serverDesc#</div>
						</td>
					</tr>

					<!--- Port --->
					<tr>
						<th scope="row">#stText.Mail.port#</th>
						<td>
							<cfinput type="text" name="port_#ms.recordcount+1#" value="#data.port#" required="yes"
							validate="integer" 
							message="#stText.Mail.PortErrorFirst#">
							<div class="comment">#stText.mail.portDesc#</div>
						</td>
					</tr>

					<!--- Username ---->
					<tr>
						<th scope="row">#stText.Mail.Username#</th>
						<td>
							<cfinput type="text" name="username_#ms.recordcount+1#" value="#data.username#" required="no" class="large"
							message="#stText.Mail.UserNameMissing#">
							<div class="comment">#stText.mail.usernameDesc#</div>
						</td>
					</tr>
					<!--- Password --->
					<tr>
						<th scope="row">#stText.Mail.password#</th>
						<td>
							<cfinput type="password" name="password_#ms.recordcount+1#" value="#isNull(data.password)?"":"*********"#" required="no" class="large"
							message="#stText.Mail.PasswordMissing#">
							<div class="comment">#stText.mail.passwordDesc#</div>
						</td>
					</tr>

					<!--- TLS --->
					<tr>
						<th scope="row">#stText.Mail.tls#</th>
						<td>
							<cfinput class="checkbox" type="checkbox" checked="#!isNull(data.tls) && data.tls#" name="tls_#ms.recordcount+1#"  value="true">
							<div class="comment">#stText.mail.tlsDesc#</div>
						</td>
					</tr>

					<!--- SSL --->
					<tr>
						<th scope="row">#stText.Mail.ssl#</th>
						<td>
							<cfinput class="checkbox" type="checkbox" checked="#!isNull(data.ssl) && data.ssl#" name="ssl_#ms.recordcount+1#" value="true">
							<div class="comment">#stText.mail.sslDesc#</div>
							
						</td>
					</tr>

					<!--- Life Timespan --->
					<tr>
						<th scope="row">#stText.Mail.life#</th>
						<td>
							<cfset data.life=toTSStruct(data.life)>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>
								
									<tr>
										<td><cfinput type="text" name="life_days_#ms.recordcount+1#" value="#data.life.days#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinput type="text" name="life_hours_#ms.recordcount+1#" value="#data.life.hours#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinput type="text" name="life_minutes_#ms.recordcount+1#" value="#data.life.minutes#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinput type="text" name="life_seconds_#ms.recordcount+1#" value="#data.life.seconds#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								
							</tbody>

						</table>
						<div class="comment">#stText.Mail.lifeDesc#</div>

						</td>
					</tr>

					<!--- Idle Timespan --->
					<tr>
						<th scope="row">#stText.Mail.idle#</th>
						<td>

						<cfset data.idle=toTSStruct(data.idle)>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>
								
									<tr>
										<td><cfinput type="text" name="idle_days_#ms.recordcount+1#" value="#data.idle.days#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinput type="text" name="idle_hours_#ms.recordcount+1#" value="#data.idle.hours#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinput type="text" name="idle_minutes_#ms.recordcount+1#" value="#data.idle.minutes#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinput type="text" name="idle_seconds_#ms.recordcount+1#" value="#data.idle.seconds#" 
											class="number" required="yes" validate="integer" 
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								
							</tbody>

						</table>
						<div class="comment">#stText.Mail.idleDesc#</div>
						</td>
					</tr>

				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="hidden" name="mainAction" value="#stText.Buttons.update#">
							<input type="hidden" name="subAction" value="#stText.Buttons.update#">
							<input type="submit" class="bs button submit" name="sdasd" value="#stText.Buttons.save#" />
						</td>
					</tr>
				</tfoot>
			</table>
		</cfform>
	</cfif>



</cfoutput>