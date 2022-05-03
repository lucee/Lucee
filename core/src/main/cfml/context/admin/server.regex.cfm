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
					action="updateRegex"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					regexType="#form.type#"
					
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
			
				<cfadmin 
					action="updateRegex"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					regexType=""
					
					remoteClients="#request.getRemoteClients()#">
				
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
			<cfset error.cfcatch=cfcatch>
		</cfcatch>
	</cftry>
</cfif>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfadmin 
	action="getRegex"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="regex">


<!---
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
---->
<cfscript>
	stText.regex.descr="Settings for regular expressions (regex).";
	stText.regex.type="Dialect (Engine)";
	stText.regex.typeDesc="Which regular expression dialect should be used.";
	stText.regex.type_java="Modern";
	stText.regex.type_perl="Classic (CFML Default)";
	stText.regex.typedesc_java="Modern type is the dialect used by Java itself.";
	stText.regex.typedesc_perl="Classic type is the Perl5 dialect used by CFML traditionally.";
</cfscript>
<!--- 
Error Output --->
<cfset printError(error)>

<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>
	<div class="pageintro">#stText.regex.descr#</div>

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				
				<tr>
					<th scope="row">#stText.regex.type#</th>
					<td>
						<div class="comment">#stText.regex.typeDesc#</div>
						<cfif hasAccess>
							<ul class="radiolist">

								<cfloop list="java,perl" item="_type">
								<li>
									<label>
										<input class="radio" type="radio" name="type" value="#_type#"<cfif regex.type EQ _type> checked="checked"</cfif>>
										<b>#stText.regex['type_'&_type]#</b>
									</label>
									<div class="comment">#stText.regex['typedesc_'&_type]#</div>
								</li>
								</cfloop>
								
							</ul>
						<cfelse>
							<input type="hidden" name="type" value="#regex.type#">
							<b>#stText.regex['type_'&regex.type]#</b><br />
							<div class="comment">#stText.regex['typedesc_'&regex.type]#</div>
						</cfif>
						
						<cfsavecontent variable="codeSample">
							this.regex.type = "#regex.type#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
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
	</cfformClassic>
</cfoutput>