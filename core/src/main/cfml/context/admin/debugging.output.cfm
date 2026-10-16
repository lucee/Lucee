<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfadmin
	action="getMonitoring"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="_mon">

<cfadmin
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="debugging"
	secValue="yes">
<cfset hasAccess=access>


<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
				<cfadmin action="updateMonitoring"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					monitoring="#{
						"showDebug":form.monitoring_showDebug?:false,
						"showMetric":form.monitoring_showMetric?:false,
						"showDoc":form.monitoring_showDoc?:false,
						"showTest":form.monitoring_showTest?:false
					}#">
		</cfcase>
		<cfcase value="#stText.Buttons.resetServerAdmin#">

				<cfadmin action="updateMonitoring"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					monitoring="#{}#">
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
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfset stText.debug.settings.generalYes="Lucee logs debug information you have checked below.">
<cfset stText.debug.settings.generalNo="Lucee does not log any debug information at all.">
	<cfset stText.Debug.showDesc="Enable or disable various outputs related to system performance and diagnostics. ">
	<cfset stText.Debug.showDebug="Debugging">
	<cfset stText.Debug.showMetric="Metrics">
	<cfset stText.Debug.showDoc="Documentation">
	<cfset stText.Debug.showDebugDesc="Configure and view detailed information about execution times, database query performance, template processing, variable access, exception handling, and thread information.">
	<cfset stText.Debug.showMetricDesc=" Monitor key performance indicators and system metrics to ensure optimal performance and identify potential issues.">
	<cfset stText.Debug.showDocDesc="Access and manage documentation and reference materials for better understanding and troubleshooting.">


<cfoutput>
	<!--- Error Output--->
	<cfset printError(error)>

	#stText.Debug.showDesc#

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
		<table class="maintbl autowidth">
			
			<tbody>

										<cfloop list="debug,metric,doc" item="item">
										<tr>
											<th scope="row">#stText.debug["show"&item]#</th>
											<td>
												<cfmodule template="systemSetting.cfm"
													name="monitoring_show#ucFirst(item)#" 
													value="#_mon[item]#"
													access="#hasAccess#"
													description="#stText.debug["show"&item&"Desc"]#"
													br=false
													sp=true
													descOnTop=false>
												<cfset lbl = _mon[item] ? stText.general.yes : stText.general.no>
												<input type="checkbox" class="checkbox" name="monitoring_show#ucFirst(item)#" value="true" #_mon[item] ? 'checked="checked"' : ''#>
												</cfmodule>
											</td>
										</tr>
										</cfloop>
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
							<input type="reset" class="<cfif request.adminType EQ 'web'>bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#" onclick="return sp_clicked(event)" id="resetBtn">
							<cfif not request.singleMode && request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>

</cfoutput>