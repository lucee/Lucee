<!--- <cfset classConfig=createObject("java","lucee.runtime.config.ConfigWeb")>
<cfset STRICT=classConfig.SCOPE_STRICT>
<cfset SMALL=classConfig.SCOPE_SMALL>
<cfset STANDART=classConfig.SCOPE_STANDART> --->
<cfset error.message="">
<cfset error.detail="">
<!--- <cfset hasAccess=securityManager.getAccess("setting") EQ ACCESS.YES>

<cfset hasAccess=securityManagerGet("setting","yes")> --->

<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- save settings --->
		<cfcase value="#stText.Buttons.save#">
			<cfadmin
					action="updateLoginSettings"
					type="#request.adminType#"
					password="#session["password" & request.adminType]#"
					rememberme="#structKeyExists(form,"remembermeEnable") and form.remembermeEnable#"
					captcha="#structKeyExists(form,"captcha") and form.captcha#"
					delay="#form.delay#">

		</cfcase>
	<!--- CHANGE --->
		<cfcase value="#stText.Buttons.Change#">
			<cfif len(form._new_password) LT 6>
				<cfset error.message="#stText.Login.NewTooShort#">
			<cfelseif form._new_password NEQ form._new_password_re>
				<cfset error.message="#stText.Login.UnequalPasswords#">
			<cfelse>
				<cfadmin
					action="updatePassword"
					type="#request.adminType#"
					oldPassword="#form._old_password#"
					newPassword="#form._new_password#">
				<cfset session["password" & request.adminType]=form._new_password>
			</cfif>

		</cfcase>
	<!--- UPDATE Default Password --->
		<cfcase value="#stText.Buttons.Update#">
			<cfif len(form._new_password) LT 6>
				<cfset error.message="#stText.Login.NewTooShort#">
			<cfelse>
				<cfadmin
					action="updateDefaultPassword"
					type="#request.adminType#"
					password="#session["password" & request.adminType]#"
					newPassword="#form._new_password#">
			</cfif>
		</cfcase>

        <cfcase value="#stText.Buttons.delete#">

				<cfadmin
					action="removeDefaultPassword"
					type="#request.adminType#"
					password="#session["password" & request.adminType]#">
		</cfcase>

	<!--- reset individual password --->
		<cfcase value="#stText.Buttons.Reset#">
			<cfif len(form.contextPath)>
				<cfadmin
					action="resetPassword"
					type="#request.adminType#"
					password="#session["password" & request.adminType]#"
					contextPath="#form.contextPath#">
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>


<!---
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!---
Error Output --->
<cfset printError(error)>


<cfadmin
        action="getLoginSettings"
        type="#request.adminType#"
		password="#session["password" & request.adminType]#"
   		returnVariable="settings">


<!--- settings --->
<cfif request.adminType EQ "server">
	<cfoutput>
		<h2>#stText.Login.settings#</h2>
		<cfformClassic action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">#stText.Login.useCaptcha#</th>
						<td>
							<cfinputClassic type="checkbox" class="checkbox" name="captcha" checked="#settings.captcha#" value="true">
							<div class="comment">#stText.Login.useCaptchaDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Login.delay#</th>
						<td>
							<select name="delay"><cfset hasDelay=false>
								<cfloop list="0,1,5,10,30,60" index="i"><option <cfif settings.delay EQ i><cfset hasDelay=true>selected="selected"</cfif>>#i#</option></cfloop>
								<cfif not hasDelay><option selected="selected">#settings.delay#</option></cfif>
							</select> #stText.Login.seconds#
							<div class="comment">#stText.Login.delayDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Login.rememberMeEnable#</th>
						<td>
							<cfinputClassic type="checkbox" class="checkbox" name="remembermeEnable" checked="#settings.rememberme#" value="true">
							<div class="comment">#stText.Login.rememberMeEnableDesc#</div>
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.save#">
							<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfoutput>
</cfif>

<!--- change password --->
<cfoutput>
	<h2>#stText.Login.ChangePassword#</h2>
	<div class="itemintro">#stText.Login.ChangePasswordDescription#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Login.OldPassword#</th>
					<td>
						<cfinputClassic type="password" name="_old_password" value="" passthrough='autocomplete="off"'
						class="medium" required="yes" message="#stText.Login.OldPasswordMissing#">
						<div class="comment">#stText.Login.OldPasswordDescription#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Login.NewPassword#</th>
					<td>
						<cfinputClassic type="password" name="_new_password" value="" passthrough='autocomplete="off"'
						class="medium" required="yes" message="#stText.Login.NewPasswordMissing#">
						<div class="comment">#stText.Login.NewPasswordDescription#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Login.RetypePassword#</th>
					<td>
						<cfinputClassic type="password" name="_new_password_re" value="" passthrough='autocomplete="off"'
						class="medium" required="yes" message="#stText.Login.RetypeNewPasswordMissing#">
						<div class="comment">#stText.Login.RetypeNewPassword#</div>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Change#">
						<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>

<cfif request.adminType EQ "server">
	<cftry>
		<cfset hasDefaultPW=true>
		<cfadmin
			action="getDefaultPassword"
			type="#request.adminType#"
			password="#session["password" & request.adminType]#"
			returnVariable="defaultPassword">
		<cfcatch type="any">
			<cfset hasDefaultPW=false>
		</cfcatch>
	</cftry>
	<!--- Set default password --->
	<cfif hasDefaultPW>
		<cfoutput>
			<h2>#stText.Login.DefaultPassword#</h2>
			<div class="itemintro">#stText.Login.DefaultPasswordDescription#</div>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Login.Password#</th>
							<td>
								<cfinputClassic type="password" id="passEmpty" name="_new_password" value="#defaultPassword#"
								class="medium" required="no" message="#stText.Login.NewPasswordMissing#">
								<div id="messagePass" class="commentError"></div>
								<div class="comment">#stText.Login.NewPasswordDescription#</div>
							</td>
						</tr>
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<input type="submit" class="bl button submit" onclick="return validatePass()" name="mainAction" value="#stText.Buttons.Update#">
								<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.delete#">
								<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>
			</cfformClassic>
		</cfoutput>
	</cfif>

	<cfset hasContextes = true>
	<cftry>
		<cfadmin
			action="getContextes"
			type="#request.adminType#"
			password="#session["password" & request.adminType]#"
			returnVariable="contextes">
		<cfcatch type="application">
			<cfset hasContextes=false>
		</cfcatch>
	</cftry>
	<cfif hasContextes>
		<!--- Reset Password --->
		<cfoutput>
			<h2>#stText.Login.resetWebPW#</h2>
			<div class="itemintro">#stText.Login.resetWebPWDescription#</div>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Login.web#</th>
							<td>
								<cfsilent>
								<cfset size=0>
								<cfset QueryAddColumn(contextes,"text",array())>
								<cfloop query="contextes">
										<cfif len(contextes.label)><cfset path=contextes.label&" ("&contextes.path&")"><cfelse><cfset path=contextes.path></cfif>
										<cfset contextes.text=path>
										<cfif size LT len(path)><cfset size=len(path)></cfif>
								</cfloop>
								</cfsilent>
								<select name="contextPath">
										<option value="">#stText.Login.chooseWebContext#</option>
									<cfloop query="contextes"><option value="#contextes.path#">#contextes.text#</option></cfloop>
								</select>
							</td>
						</tr>
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Reset#">
								<input type="reset" class="br reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>
			</cfformClassic>
		</cfoutput>
	</cfif>
</cfif>