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
					loginRememberme="#structKeyExists(form,"remembermeEnable") and form.remembermeEnable#"
					loginCaptcha="#structKeyExists(form,"loginCaptcha") and form.loginCaptcha#"
					loginDelay="#form.delay#">

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
							<cfmodule template="systemSetting.cfm" 
								name="loginCaptcha" 
								value="#settings.captcha#"
								access="#hasAccess#"
								description="#stText.Login.useCaptchaDesc#"
								br=false
								sp=true
								descOnTop=true>
								<cfinputClassic type="checkbox" class="checkbox" name="loginCaptcha" checked="#settings.captcha#" value="true">
							</cfmodule>
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