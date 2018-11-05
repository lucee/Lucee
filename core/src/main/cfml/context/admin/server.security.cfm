<cfset error.message="">
<cfset error.detail="">

<cfadmin 
	action="getSecurity"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="security">

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
					action="updateSecurity"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					varUsage="#form.varUsage#"
					remoteClients="#request.getRemoteClients()#">
			
			</cfcase>
			<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				
				<cfadmin 
					action="updateSecurity"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					varUsage=""
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

<!--- 
Error Output --->
<cfset printError(error)>
<cfscript>


	stText.security.desc="All settings that concerns securiry in Lucee.";
	stText.security.varUsage="Variable Usage in Queries";
	stText.security.varUsageDesc="With this setting you can control how Lucee handles variables used within queries.";

	stText.security.varUsageIgnore="Allow variables within a query";
	stText.security.varUsageWarn="Add a warning to debug output";
	stText.security.varUsageError="Throw an exception";

</cfscript>
<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>
	
	<div class="pageintro">#stText.security.desc#</div>
	
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				
				<!--- Web --->
				<tr>
					<th scope="row">#stText.security.varUsage#</th>
					<td>
						<cfif hasAccess>
							<select name="varUsage">
								<cfloop list="ignore,warn,error" item="type">
									<option <cfif type EQ security.varusage> selected="selected"</cfif> value="#type#">#stText.security["varUsage"&type]#</option>
								</cfloop>
							</select>
						<cfelse>
							<input type="hidden" name="varUsage" value="#security.varusage#">
							<b>#security.varusage#</b>
						</cfif>
						<div class="comment">#stText.security.varUsageDesc#</div>
						<cfsavecontent variable="codeSample">
							this.query.variableUsage="#security.varusage#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample)>
					</td>
				</tr>

			</tbody>
		
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input class="bl button submit" type="submit" name="mainAction" value="#stText.Buttons.Update#">
							<input class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>