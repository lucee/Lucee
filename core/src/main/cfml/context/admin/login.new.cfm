<cfparam name="cookie.lucee_admin_lang" default="en">
<cfset session.lucee_admin_lang = cookie.lucee_admin_lang>
<cfparam name="languages" default="#{en:'English',de:'Deutsch'}#">
<cfscript>
try{
	path=getPageContext().getConfig().getConfigServerDir().toString();
}
// in case direct java  access is disabled
catch(e) {
	path="/lucee-server/context/";
}
</cfscript>

<cfoutput><table cellpadding="0" cellspacing="0" border="0" width="500" height="120">
		<tr>
			<td align="left">
				For security reasons it is no longer possible to set the inital password here directly.<br>
	<cfif request.adminType == "server">
		You will need to configure a password before you can access the Server Administrator.
		<ul>
		<li>create a file with name <b>password.txt</b> containing your new password under the root Lucee server directory ( /lucee-server/context/password.txt ).</li>
		<li>click "import file", Lucee will then read and configure your new password, afterwards Lucee will automatically delete that file</ul>

		
	<cfelse>
		Please set a password for the <a href="server.cfm">Server Administrator</a> and then this password can be used to log into this Web Administrator as well. After that you can set a custom password inside Web Administrator, if you wish.
	</cfif></td>
		</tr>
		</table>
	<cfif request.adminType == "server">
		<cfformClassic name="login" action="#request.self#" method="post">
			<input type="hidden" name="checkPassword" value="true">
			<input class="button submit" type="submit" name="submit" value="import file">
		</cfformClassic>
	</cfif>
</cfoutput>
<!---
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
				<cfset f = "">
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
								<option value="#key#" <cfif (key == session.lucee_admin_lang)>selected</cfif>>#languages[key]#</option>
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

</cfhtmlbody>--->