<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfadmin
	action="getDebugEntry"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="debug">


<cfadmin
	action="getDebug"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="_debug">

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
				<!---
				<cfdump var="#isDefined('form.monitoring_debuggingDatabase') && form.monitoring_debuggingDatabase#">
				<cfdump var="#form#" abort>--->
			<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debuggingDatabase="#form.debuggingDatabase?:false#"
					debuggingException="#form.debuggingException?:false#"
					debuggingTracing="#form.debuggingTracing?:false#"
					debuggingDump="#form.debuggingDump?:false#"
					debuggingTimer="#form.debuggingTimer?:false#"
					debuggingImplicitAccess="#form.debuggingImplicitAccess?:false#"
					debuggingQueryUsage="#form.debuggingQueryUsage?:false#"
					debuggingTemplate="#form.debuggingTemplate?:false#"
					debuggingThread="#form.debuggingThread?:false#">
		</cfcase>
		<cfcase value="#stText.Buttons.resetServerAdmin#">

				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debuggingDatabase=""
					debuggingException=""
					debuggingTracing=""
					debuggingDump=""
					debuggingTimer=""
					debuggingImplicitAccess=""
					debuggingQueryUsage=""
					debuggingTemplate=""
					debuggingThread="">
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

<cfset querySort(debug,"id")>
<cfset qryWeb=queryNew("id,label,iprange,type,custom,readonly,driver")>
<cfset qryServer=queryNew("id,label,iprange,type,custom,readonly,driver")>


<cfset stText.debug.settings.generalYes="Lucee logs debug information you have checked below.">
<cfset stText.debug.settings.generalNo="Lucee does not log any debug information at all.">


<cfhtmlbody>
	<script type="text/javascript">
		function sp_clicked(event)
		{
			var iscustom = $('#sp_radio_debug')[0].checked;
			var tbl = $('#debugoptionstbl').css('opacity', (iscustom ? 1:.5));
			var inputs = $('input', tbl).prop('disabled', !iscustom);
			if(event !== undefined && $(event.target).attr('id') === 'resetBtn'){
				$('#debugoptionstbl').css('opacity',.5);
				$('input', tbl).prop('disabled', true);
			}
		}
		$(function(){
			$('#sp_options input.radio').bind('click change', sp_clicked);
			sp_clicked();
		});
	</script>
</cfhtmlbody>

<cfoutput>
	<cfset stText.Debug.settingsDesc="On this page, you can configure the specific information that Lucee should log during a request. Please note that enabling extensive logging can impact performance, as logging operations require additional processing time.">
	<!--- Error Output--->
	<cfset printError(error)>

	#stText.Debug.settingsDesc#

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
		<table class="maintbl autowidth">


			<tbody>
							
								

										<cfloop list="template,database,exception,tracing,dump,timer,implicitAccess,thread" item="item">
										<cfset cfgKey = "debugging" & ucFirst(item)>
										<tr>
											<th scope="row">#stText.debug.settings[item]#</th>
											<td>
												<cfset lbl = _debug[cfgKey] ? stText.general.yes : stText.general.no>
												
												<cfmodule template="systemSetting.cfm"
													name="#cfgKey#" 
													propName="monitoring_#cfgKey#"
													value="#_debug[cfgKey]#"
													access="#hasAccess#"
													description="#stText.debug.settings[item&"Desc"]#"
													br=false
													sp=true
													descOnTop=false>
													<label>
														<input type="checkbox" class="checkbox" name="#cfgKey#" value="true"  <cfif item EQ "database">id="sp_radio_qu"</cfif> #_debug[cfgKey] ? 'checked="checked"' : ''#>
													</label>
												</cfmodule>
												<cfif structKeyExists(stText.debug.settings, item&"Alert")>
													<div class="err">
														<b >#stText.debug.settings[item&"Alert"]#</b>
													</div>
												</cfif>

												<cfif item EQ "database">
												<table class="maintbl autowidth" id="debugoptionqutbl">
												<tbody>
													<tr>
														<th scope="row">#stText.debug.settings.queryUsage#</th>
														<td>
															<cfmodule template="systemSetting.cfm"
																name="debuggingQueryUsage" 
																propName="monitoring_debuggingQueryUsage"
																value="#_debug.debuggingQueryUsage#"
																access="#hasAccess#"
																description="#stText.debug.settings["queryUsageDesc"]#"
																br=false
																sp=true
																descOnTop=false>#_debug.debuggingQueryUsage#
															<cfset lbl = _debug.debuggingQueryUsage ? stText.general.yes : stText.general.no>
																<label><input type="checkbox" class="checkbox" 
																	name="debuggingQueryUsage" 
																	value="true" #_debug.debuggingQueryUsage ? 'checked="checked"' : ''#>
																</label>
															</cfmodule>
														</td>
													</tr>
												</table>
												</cfif>
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