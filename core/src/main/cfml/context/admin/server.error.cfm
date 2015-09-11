<cfset error.message="">
<cfset error.detail="">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="setting"
	secValue="yes">

<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfif hasAccess>
	<cftry>
		<cfswitch expression="#form.mainAction#">
		<!--- UPDATE --->
			<cfcase value="#stText.Buttons.Update#">
			
				<cfadmin 
					action="updateError"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					template500="#form["errorTemplate_"&form.errtype500&500]#"
					template404="#form["errorTemplate_"&form.errtype404&404]#"
					statuscode="#isDefined('form.doStatusCode')#"
					
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
			
				<cfadmin 
					action="updateError"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					template500=""
					template404=""
					statuscode=""
					
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
		</cfcatch>
	</cftry>
</cfif>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfadmin 
	action="getError"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="err">



<cfsavecontent variable="headText">
	<script type="text/javascript">
		function disableField(oField,statusCode) {
			var oForm=oField.form;
			
			if (oField.value == 'Select') {
				oForm["errorTemplate_File"+statusCode].disabled   = true;
				oForm["errorTemplate_Select"+statusCode].disabled = false;
			} 
			else {
				oForm["errorTemplate_File"+statusCode].disabled   = false;
				oForm["errorTemplate_Select"+statusCode].disabled = true;
			}
		}
	</script>
</cfsavecontent>
<cfhtmlhead text="#headText#" />

<!--- 
Error Output --->
<cfset printError(error)>

<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>
	<div class="pageintro">#stText.err.descr#</div>

	<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<cfloop list="500,404" index="statusCode">
					<cfset css=iif(len(err.templates[statusCode]) EQ 0 and len(err.templates[statusCode]) NEQ 0,de('Red'),de(''))>
					<tr>
						<th scope="row">#stText.err.errorTemplate[statusCode]#</th>
						<td class="tblContent#css#" title="#err.str[statusCode]##chr(10)##err.str[statusCode]#">
							<cfif LCase(left(err.templates[statusCode], 41)) eq "#cgi.context_path#/lucee/admin/templates/error/">
								<cfset bDisableFile = True>
							<cfelse>
								<cfset bDisableFile = False>
							</cfif>
							<cfif hasAccess>
								<cfif structKeyExists(session,"passwordserver")>
									<cfdirectory action="LIST" directory="../templates/error/" name="err_templates" serverpassword="#session.passwordserver#">
								<cfelse>
									<cftry>
										<cfdirectory action="LIST" directory="../templates/error/" name="err_templates">
										<cfcatch type="security">
											<cfadmin 
												action="getErrorList"
												type="#request.adminType#"
												password="#session["password"&request.adminType]#"
												returnVariable="err_templates">			
										</cfcatch>		
									</cftry>
								</cfif>
								<cfset isFromTemplate=false>
								<cfset path=GetDirectoryFromPath(mid(GetDirectoryFromPath(cgi.SCRIPT_NAME),1,len(GetDirectoryFromPath(cgi.SCRIPT_NAME))-1))>
								
								<cfloop query="err_Templates">
									<cfif err.templates[statusCode] EQ expandPath(path&"templates/error/" & err_Templates.Name)>
										<cfset isFromTemplate=true>
									</cfif>
								</cfloop>
								<ul class="radiolist">
									<li>
										<input type="radio" class="radio" name="errType#statusCode#" value="Select" onclick="disableField(this,#statusCode#)" <cfif isFromTemplate>checked</cfif>>
										<select name="errorTemplate_Select#statusCode#" id="errorTemplate_Select#statusCode#" <cfif not isFromTemplate>disabled</cfif> class="medium">
											<cfloop query="err_Templates">
												<cfif mid(err_Templates.Name,1,1) EQ "." or err_Templates.type EQ "dir"><cfcontinue></cfif>
												<cfset sName = path&"templates/error/" & err_Templates.Name>
												<option value="#sName#"<cfif expandPath(sName) eq err.templates[statusCode]> selected</cfif>>#err_Templates.Name#</option>
											</cfloop>
										</select>
									</li>
									<li>
										<input type="radio" class="radio" name="errType#statusCode#" value="File" onclick="disableField(this,#statusCode#)" <cfif not isFromTemplate>checked</cfif>>
										<input type="text" name="errorTemplate_File#statusCode#" value="#err.str[statusCode]#" id="errorTemplate_File[statusCode]" <cfif isFromTemplate>disabled</cfif> class="large">
									</li>
								</ul>
								<div class="comment">#stText.err.errorTemplateDescription[statusCode]#</div>
							<cfelse>
								<b>#err.str[statusCode]#</b>
								<!---<input type="hidden" name="errorTemplate#statusCode#" value="#err.str[statusCode]#">--->
							</cfif>
						</td>
					</tr>
				</cfloop>
				<tr>
					<th scope="row">#stText.err.errorStatusCode#</th>
					<td>
						<cfif hasAccess>
						<input class="checkbox" type="checkbox" name="doStatusCode" value="yes" <cfif err.doStatusCode>checked</cfif>>
						<cfelse>
						<b>#YesNoFormat(err.doStatusCode)#</b><br />
						</cfif>
						<div class="comment">#stText.err.errorStatusCodeDescription#</div><br>
						
					</td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web">
								<input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#">
							</cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfform>
</cfoutput>