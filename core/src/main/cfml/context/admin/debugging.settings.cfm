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
				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug="#isDefined('form.debug') && form.debug#"
					database="#isDefined('form.database') && form.database#"
					exception="#isDefined('form.exception') && form.exception#"
					tracing="#isDefined('form.tracing') && form.tracing#"
					dump="#isDefined('form.dump') && form.dump#"
					timer="#isDefined('form.timer') && form.timer#"
					implicitAccess="#isDefined('form.implicitAccess') && form.implicitAccess#"
					queryUsage="#isDefined('form.queryUsage') && form.queryUsage#"
					template="#isDefined('form.template') && form.template#"
					thread="#isDefined('form.thread') && form.thread#"


					debugTemplate=""
					remoteClients="#request.getRemoteClients()#">
		</cfcase>
		<cfcase value="#stText.Buttons.resetServerAdmin#">

				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug=""
					database=""
					exception=""
					tracing=""
					dump=""
					timer=""
					implicitAccess=""
					queryUsage=""
					thread=""

					debugTemplate=""
					remoteClients="#request.getRemoteClients()#">
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
						<cfif hasAccess>
							
								

										<cfloop list="template,database,exception,tracing,dump,timer,implicitAccess,thread" item="item">
										<tr>
											<th scope="row">#stText.debug.settings[item]#</th>
											<td>
												<cfset lbl = _debug[item] ? stText.general.yes : stText.general.no>
												<cfif hasAccess>
													<label><input type="checkbox" class="checkbox" name="#item#" value="true"  <cfif item EQ "database">id="sp_radio_qu"</cfif> #_debug[item] ? 'checked="checked"' : ''#>
													#stText.general.enabled#</label>
												<cfelse>
													<b>#_debug[item] ? stText.general.yes : stText.general.no#</b>
													<input type="hidden" name="#item#" value="#_debug[item]#">
												</cfif>
												<cfif structKeyExists(stText.debug.settings, item&"Alert")>
													<div class="comment">
														<b style="color:##bf4f36">#stText.debug.settings[item&"Alert"]#</b>
													</div>
												</cfif>
												<div class="comment">#stText.debug.settings[item&"Desc"]#</div>
												<cfsavecontent variable="codeSample">
													this.monitoring.debugging#ucFirst(item)#=#_debug[item]#;
												</cfsavecontent>
												<cfset renderCodingTip( codeSample )>
												<cfset renderSysPropEnvVar( name:"lucee.monitoring.debugging#ucFirst(item)#",value:_debug[item])>

												<cfif item EQ "database">
												<table class="maintbl autowidth" id="debugoptionqutbl">
												<tbody>
													<tr>
														<th scope="row">#stText.debug.settings.queryUsage#</th>
														<td>
															<cfset lbl = _debug.queryUsage ? stText.general.yes : stText.general.no>
															<cfif hasAccess>
																<label><input type="checkbox" class="checkbox" name="queryUsage" value="true" #_debug.queryUsage ? 'checked="checked"' : ''#>
																#stText.general.enabled#</label>
															<cfelse>
																<b>#_debug.queryUsage ? stText.general.yes : stText.general.no#</b>
																<input type="hidden" name="queryUsage" value="#_debug.queryUsage#">
															</cfif>
															<div class="comment">#stText.debug.settings["queryUsageDesc"]#</div>

															<cfsavecontent variable="cs">
																this.monitoring.debuggingQueryUsage=#_debug.queryUsage#;
															</cfsavecontent>
															<cfset renderCodingTip( cs )>
															<cfset renderSysPropEnvVar( name:"lucee.monitoring.debuggingQueryUsage",value:_debug.queryUsage)>
														</td>
													</tr>
												</table>
												</cfif>
												
												


											</td>
										</tr>
										</cfloop>

								
						<cfelse>
							<!---<input type="hidden" name="scriptProtect" value="#appSettings.scriptProtect#">--->
							<b>#lbl#</b>
							<div class="comment">#_debug.debug?stText.debug.settings.generalYes:stText.debug.settings.generalNo#</div>
							<cfloop list="database,exception,tracing,dump,timer,implicitAccess" item="item">
								<cfif _debug[item]>- #stText.debug.settings[item]#<br></cfif>
							</cfloop>


						</cfif>
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