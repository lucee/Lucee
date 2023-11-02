<cfoutput>
<!--- <h2>#stText.Mail.Settings#</h2> --->
<h2>Send test email</h2>
<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&row=#url.row#" method="post">
	<!--- <input type="hidden" name="mainAction" value="#stText.Buttons.Setting#">
	<input type="hidden" name="mainAction" value="#stText.Buttons.Setting#"> --->
	<table class="maintbl">
		<tbody>
			<tr>
				<th scope="row">To Email</th>
				<td>
					<cfinputClassic type="text" name="toMail" value="" class="medium" required="yes" message="Please enter a valid email, to where you need to send the test email.">
				</td>
			</tr>
			<tr>
				<th scope="row">From Email</th>
				<td>
					<cfinputClassic type="text" name="fromMail" value="" class="medium" required="yes" message="Please enter a valid email.">
				</td>
			</tr>
		</tbody>
		<tfoot>
			<tr>
				<td colspan="2"><cfoutput>
					<input type="submit" class="button submit" name="mainAction" value="Send test mail">
				</cfoutput></td>
			</tr>
		</tfoot>
	</table>
</cfformClassic>
</cfoutput>