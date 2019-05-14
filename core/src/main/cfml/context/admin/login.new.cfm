<cfparam name="cookie.lucee_admin_lang" default="en">
<cfset session.lucee_admin_lang = cookie.lucee_admin_lang>
<cfparam name="languages" default="#{en:'English',de:'Deutsch'}#">

<cfoutput>
	<cfformClassic name="login" action="#request.self#" method="post">
		<table class="maintbl" style="width:300px">
			<tbody>
				<tr>
					<th scope="row" class="right" nowrap="nowrap">#stText.login.Password#</th>
					<td><cfinputClassic type="password" name="new_password" id="new_password" value="" passthrough='autocomplete="off"'
						class="xlarge" required="yes" message="#stText.login.PasswordMissing#" />
					</td>
				</tr>
				<tr>
					<th scope="row" class="right" nowrap="nowrap">#stText.login.RetypePassword#</th>
					<td><cfinputClassic type="password" name="new_password_re" value="" passthrough='autocomplete="off"'
						class="xlarge" required="yes" message="#stText.login.RetypePasswordMissing#" />
					</td>
				</tr>
				<cfset f="">
				<cfloop collection="#languages#" item="key">
					<cfif f == "" || (key == session.lucee_admin_lang)>
						<cfset f = key>
					</cfif>
				</cfloop>
				<tr>
					<th scope="row" class="right" nowrap="nowrap">#stText.login.language#</th>
					<td>
						<select name="lang" class="xlarge">
							<cfloop collection="#languages#" item="key">
								<option value="#key#" <cfif key == session.lucee_admin_lang>selected</cfif>>#languages[key]#</option>
							</cfloop>
						</select>
					</td>
				</tr>
				<tr>
					<th scope="row" class="right" nowrap="nowrap">#stText.login.rememberMe#</th>
					<td>
						<select name="rememberMe" class="xlarge">
							<cfloop list="s,d,ww,m,yyyy" index="i">
								<option value="#i#"<cfif (i == form.rememberMe)> selected</cfif>>#stText.Login[i]#</option>
							</cfloop>
						</select>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2" class="right"><input class="button submit" type="submit" name="submit" value="#stText.Buttons.Submit#"></td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>

<cfhtmlbody>

	<script type="text/javascript">
		$(function() {
			$("#new_password").focus();
		});
	</script>

</cfhtmlbody>