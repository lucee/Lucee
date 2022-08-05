<cfset error.message="">
<cfset error.detail="">

<cfadmin
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="setting"
	secValue="yes">

<cfadmin
	action="getApplicationSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="appSettings">
<cfif request.admintype =="server">
	<cfadmin
		action="getQueueSetting"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="queueSettings">
</cfif>
<cfadmin
	action="getApplicationListener"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="listener">

<!---
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction1" default="none">
<cfparam name="form.mainAction2" default="none">
<cfparam name="form.subAction" default="none">

<cfif hasAccess>
	<cftry>
	<!--- generell --->
		<cfswitch expression="#form.mainAction1#">
		<!--- UPDATE --->
			<cfcase value="#stText.Buttons.Update#">

				<cfif form.scriptProtect EQ "custom">
					<cfparam name="form.scriptProtect_custom" default="none">
					<cfset form.scriptProtect=form.scriptProtect_custom>
				</cfif>

				<cfadmin
					action="updateApplicationSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					scriptProtect="#form.scriptProtect#"
					AllowURLRequestTimeout="#structKeyExists(form,'AllowURLRequestTimeout') and form.AllowURLRequestTimeout#"
					requestTimeout="#CreateTimeSpan(form.request_days,form.request_hours,form.request_minutes,form.request_seconds)#"
					remoteClients="#request.getRemoteClients()#">

				<cfif request.admintype =="server">
					<cfscript>
						if(structKeyExists(form,'timeout_days')) {
							timeoutMS=
								(form.timeout_seconds*1000)+
								(form.timeout_minutes*60*1000)+
								(form.timeout_hours*60*60*1000)+
								(form.timeout_days*60*60*24*1000);
						}
						else timeoutMS="";// emty string==removed
					</cfscript>


					<cfadmin
					action="updateQueueSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					enable="#structKeyExists(form,'ConcurrentRequestEnable') and form.ConcurrentRequestEnable#"
					max="#structKeyExists(form,'ConcurrentRequestMax')?form.ConcurrentRequestMax:""#"
					timeout="#timeoutMS#"
					remoteClients="#request.getRemoteClients()#">
				</cfif>

			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">

				<cfadmin
					action="updateApplicationSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					scriptProtect=""
					AllowURLRequestTimeout=""
					requestTimeout=""
					applicationPathTimeout=""
					
					remoteClients="#request.getRemoteClients()#">
				<cfif request.admintype =="server">
					<cfadmin
					action="updateQueueSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					max=""
					timeout=""
					enable=""
					remoteClients="#request.getRemoteClients()#">
				</cfif>

			</cfcase>
		</cfswitch>

	<!--- listener --->
		<cfswitch expression="#form.mainAction2#">
		<!--- UPDATE --->
			<cfcase value="#stText.Buttons.Update#">
				<cfadmin
					action="updateApplicationListener"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					listenerType="#form.type#"
					listenerMode="#form.mode#"
					applicationPathTimeout="#CreateTimeSpan(form.apppath_days?:0,form.apppath_hours?:0,form.apppath_minutes?:0,form.apppath_seconds?:0)#"
					
					remoteClients="#request.getRemoteClients()#">

			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">

				<cfadmin
					action="updateApplicationListener"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					listenerType=""
					listenerMode=""
					applicationPathTimeout=""

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

<!--- script to enable/disable script-protect 'custom' checkboxes --->
<cfhtmlbody>

	<script type="text/javascript">
		function sp_clicked()
		{
			var iscustom = $('#sp_radio_custom')[0].checked;
			var tbl = $('#customoptionstbl').css('opacity', (iscustom ? 1:.5));
			var inputs = $('input', tbl).prop('disabled', !iscustom);
			if (!iscustom)
			{
				inputs.prop('checked', false);
			}
		}
		$(function(){
			$('#sp_options input.radio').bind('click change', sp_clicked);
			sp_clicked();
		});


		function concurrent()
		{
			var isChecked = $('#ConcurrentRequestEnableSpan input.checkbox')[0].checked;
			$('#ConcurrentRequestMax').css('opacity', (isChecked ? 1:.5));
			$('#ConcurrentRequestTimeout').css('opacity', (isChecked ? 1:.5));


			$('#ConcurrentRequestMax').prop('disabled', !isChecked);
			$('#ConcurrentRequestTimeout input').prop('disabled', !isChecked);
		}
		$(function(){
			$('#ConcurrentRequestEnableSpan input.checkbox').bind('click change', concurrent);
			concurrent();
		});


	</script>
</cfhtmlbody>

<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>

	<div class="pageintro">#stText.request.description#
	</div>

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">




		<!--- script-protect --->
		<h2>#stText.application.scriptProtect#</h2>
		<div class="itemintro">#stText.application.scriptProtectDescription#</div>
		<table class="maintbl">
			<tbody>
				<tr>
					<td>
						<cfif hasAccess>
							<cfset isNone=appSettings.scriptProtect EQ  "none">
							<cfset isAll=appSettings.scriptProtect EQ  "all">
							<cfset isCustom=not isNone and not isAll>
							<ul class="radiolist" id="sp_options">
								<li>
									<label>
										<input type="radio" class="radio" name="scriptProtect" value="none" <cfif isNone>checked="checked"</cfif>>
										<b>none</b>
									</label>
									<div class="comment">#stText.application.scriptProtectNone#</div>
								</li>
								<li>
									<label>
										<input type="radio" class="radio" name="scriptProtect" id="sp_radio_custom" value="custom" <cfif isCustom>checked="checked"</cfif>>
										<b>custom:</b>
									</label>
									<div class="comment">#stText.application.scriptProtectCustom#</div>
									<table class="maintbl autowidth" id="customoptionstbl">
										<thead>
											<tr>
												<th>cgi</th>
												<th>cookie</th>
												<th>form</th>
												<th>url</th>
											</tr>
										</thead>
										<tbody>
											<tr>
												<td><input type="checkbox" class="checkbox" name="scriptProtect_custom"
												<cfif ListFindNoCase(appSettings.scriptProtect,'cgi')> checked="checked"</cfif> value="cgi"></td>
												<td><input type="checkbox" class="checkbox" name="scriptProtect_custom"
												<cfif ListFindNoCase(appSettings.scriptProtect,'cookie')> checked="checked"</cfif> value="cookie"></td>
												<td><input type="checkbox" class="checkbox" name="scriptProtect_custom"
												<cfif ListFindNoCase(appSettings.scriptProtect,'form')> checked="checked"</cfif> value="form"></td>
												<td><input type="checkbox" class="checkbox" name="scriptProtect_custom"
												<cfif ListFindNoCase(appSettings.scriptProtect,'url')> checked="checked"</cfif> value="url"></td>
											</tr>
										</tbody>
									</table>
								</li>
								<li>
									<label>
										<input type="radio" class="radio" name="scriptProtect" value="all" <cfif isAll>checked="checked"</cfif>>
										<b>all</b>
									</label>
									<div class="comment">#stText.application.scriptProtectAll#</div>
								</li>
							</ul>
						<cfelse>
							<!---<input type="hidden" name="scriptProtect" value="#appSettings.scriptProtect#">--->
							<b>#appSettings.scriptProtect#</b>
						</cfif>
<cfsavecontent variable="codeSample">
	this.scriptprotect="#appSettings.scriptProtect#";
</cfsavecontent>
					</td>
				</tr>
				</tbody>
				<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction1" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction1" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>


				<!--- request timeout --->
				<h2>#stText.application.RequestTimeout#</h2>
				<div class="itemintro">#stText.application.RequestTimeoutDesc#</div>
		<table class="maintbl">
				<tbody>
				<!--- request timeout time --->
				<tr>
					<th scope="row">#stText.application.RequestTimeoutTime#</th>
					<td>
						<cfset timeout=appSettings.requestTimeout>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>
								<cfif hasAccess>
									<tr>
										<td><cfinputClassic type="text" name="request_days" value="#appSettings.requestTimeout_day#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="request_hours" value="#appSettings.requestTimeout_hour#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="request_minutes" value="#appSettings.requestTimeout_minute#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="request_seconds" value="#appSettings.requestTimeout_second#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td class="right"><b>#appSettings.requestTimeout_day#</b></td>
										<td class="right"><b>#appSettings.requestTimeout_hour#</b></td>
										<td class="right"><b>#appSettings.requestTimeout_minute#</b></td>
										<td class="right"><b>#appSettings.requestTimeout_second#</b></td>
									</tr>
								</cfif>
							</tbody>

						</table>
						<div class="comment">#stText.application.RequestTimeoutDescription#</div>


<cfsavecontent variable="codeSample">
	<cfset total=
		appSettings.requestTimeout_second +
		(appSettings.requestTimeout_minute*60) +
		(appSettings.requestTimeout_hour*3600) +
		(appSettings.requestTimeout_day*3600*24)>
	this.requestTimeout=createTimeSpan(#appSettings.requestTimeout_day#,#appSettings.requestTimeout_hour#,#appSettings.requestTimeout_minute#,#appSettings.requestTimeout_second#);
</cfsavecontent>
<cfset renderCodingTip( codeSample)>

					</td>
				</tr>
				<!--- request timeout url --->
				<tr>
					<th scope="row">#stText.application.AllowURLRequestTimeout#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" name="AllowURLRequestTimeout" value="true" class="checkbox"
							<cfif appSettings.AllowURLRequestTimeout>  checked="checked"</cfif>>
						<cfelse>
							<!---<input type="hidden" name="AllowURLRequestTimeout" value="#appSettings.AllowURLRequestTimeout#">--->
							<b>#yesNoFormat(appSettings.AllowURLRequestTimeout)#</b>
						</cfif>
						<div class="comment">#stText.application.AllowURLRequestTimeoutDesc#</div>
					</td>
				</tr>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction1" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction1" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>


<cfif request.admintype =="server">
				<!--- Maximal Concurrent Request --->
				<h2>#stText.application.ConcurrentRequest#</h2>
				<div class="itemintro">#stText.application.ConcurrentRequestDesc#</div>
		<table class="maintbl">
			<tbody>

				<tr>
					<th scope="row">#stText.application.ConcurrentRequestEnable#</th>
					<td>
						<span id="ConcurrentRequestEnableSpan"><cfif hasAccess>
							<input type="checkbox" name="ConcurrentRequestEnable" value="true" class="checkbox"
							<cfif queueSettings.enable>  checked="checked"</cfif>>
						<cfelse>
							<b>#yesNoFormat(queueSettings.enable)#</b>
						</cfif>
						<div class="comment">#stText.application.ConcurrentRequestEnableDesc#</div></span>
					</td>
				</tr>

				<tr>
					<th scope="row">#stText.application.ConcurrentRequestMax#</th>
					<td>
						<cfif hasAccess>
							<cfinputClassic type="text" name="ConcurrentRequestMax" value="#queueSettings.max#"
									class="number" required="yes" validate="integer" id="ConcurrentRequestMax"
									message="#stText.application.ConcurrentRequestMaxError#">

						<cfelse>
							<b>#yesNoFormat(queueSettings.max)#</b>
						</cfif>
						<div class="comment">#stText.application.ConcurrentRequestMaxDesc#</div>
					</td>
				</tr>


				<tr>
					<th scope="row">#stText.application.ConcurrentRequestTimeout#</th>
					<td>
						<cfif hasAccess>
							<!---<cfinputClassic type="text" name="ConcurrentRequestTimeout" value="#queueSettings.timeout#"
									class="number" required="yes" validate="integer"  id="ConcurrentRequestTimeoutOld"
									message="#stText.application.ConcurrentRequestTimeoutError#">--->

							<cfscript>
								seconds=int(queueSettings.timeout/1000);
								minutes=int(seconds/60);
								seconds-=minutes*60;
								hours=int(minutes/60);
								minutes-=hours*60;
								days=int(hours/24);
								hours-=days*24;
							</cfscript>
							<table class="maintbl" style="width:auto" id="ConcurrentRequestTimeout">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>
								<cfif hasAccess>


									<tr>
										<td><cfinputClassic type="text" name="timeout_days" value="#days#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="timeout_hours" value="#hours#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="timeout_minutes" value="#minutes#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="timeout_seconds" value="#seconds#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td class="right"><b>#days#</b></td>
										<td class="right"><b>#hours#</b></td>
										<td class="right"><b>#minutes#</b></td>
										<td class="right"><b>#seconds#</b></td>
									</tr>
								</cfif>
							</tbody>

						</table>




						<cfelse>
							<b>#yesNoFormat(queueSettings.timeout)#</b>
						</cfif>
						<div class="comment">#stText.application.ConcurrentRequestTimeoutDesc#</div>
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
							<input type="submit" class="bl button submit" name="mainAction1" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction1" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
</cfif>

	</cfformClassic>

	<h2>#stText.application.listener#</h2>
	<div class="itemintro">#stText.application.listenerDescription#</div>

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- listener type --->
				<tr>
					<th scope="row">
						#stText.application.listenerType#
						<cfif hasAccess>
							<!--- PK: disabled, because it only said "please select an option"
							<div class="comment">#stText.application.listenerTypeDescription#</div>
							--->
						</cfif>
					</th>
					<td>
						<cfif hasAccess>
							<ul class="radiolist">
								<cfloop index="key" list="none,classic,modern,mixed">
									<li>
										<label>
											<input type="radio" class="radio" name="type" value="#key#" <cfif listener.type EQ key>checked="checked"</cfif>>
											<b>#stText.application['listenerType_' & key]#</b>
										</label>
										<div class="comment">#stText.application['listenerTypeDescription_' & key]#</div>
									</li>
								</cfloop>
							</ul>
						<cfelse>
							<!---<input type="hidden" name="type" value="#listener.type#">--->
							<b>#listener.type#</b>
							<div class="comment">#stText.application['listenerTypeDescription_' & listener.type]#</div>
						</cfif>
					</td>
				</tr>

				<!--- listener mode --->
				<tr>
					<th>#stText.application.listenerMode#
						<cfif hasAccess>
							<div class="comment">#stText.application.listenerModeDescription#</div>
						</cfif>
					</th>
					<td>
						<cfif hasAccess>
							<ul class="radiolist">
								<cfloop index="key" list="curr2root,currorroot,root,curr">
									<li>
										<label>
											<input type="radio" class="radio" name="mode" value="#key#" <cfif listener.mode EQ key>checked="checked"</cfif>>
											<b>#stText.application['listenerMode_' & key]#</b>
										</label>
										<div class="comment">#stText.application['listenerModeDescription_' & key]#</div>
									</li>
								</cfloop>
							</ul>
						<cfelse>
							<!---<input type="hidden" name="type" value="#listener.mode#">--->
							<b>#listener.mode#</b>
							<div class="comment">#stText.application['listenerModeDescription_' & listener.mode]#</div>
						</cfif>
					</td>
				</tr>
<cfset stText.application.appPathEnvVar="This can also be defined using an environment variable as follows">
<cfset stText.application.appPathTimeout="Timeout for the Application Path Cache">
<cfset stText.application.appPathTimeoutDesc="If set to greater than 0 Lucee will cache the Path to the Application.[cfc|cfm] file to use for that time. So Lucee does not search the Application.cfc with every request. If set to 0 the cache is disabled. ">


				<tr>
					<th scope="row">#stText.application.appPathTimeout#</th>
					<td>
						<cfset timeout=appSettings.requestTimeout>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>
								<cfif hasAccess>
									<tr>
										<td><cfinputClassic type="text" name="apppath_days" value="#appSettings.applicationPathTimeout_day#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="apppath_hours" value="#appSettings.applicationPathTimeout_hour#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="apppath_minutes" value="#appSettings.applicationPathTimeout_minute#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="apppath_seconds" value="#appSettings.applicationPathTimeout_second#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td class="right"><b>#appSettings.applicationPathTimeout_day#</b></td>
										<td class="right"><b>#appSettings.applicationPathTimeout_hour#</b></td>
										<td class="right"><b>#appSettings.applicationPathTimeout_minute#</b></td>
										<td class="right"><b>#appSettings.applicationPathTimeout_second#</b></td>
									</tr>
								</cfif>
							</tbody>

						</table>
						<div class="comment">#stText.application.appPathTimeoutDesc#</div>


<cfsavecontent variable="codeSample">
	LUCEE_APPLICATION_PATH_CACHE_TIMEOUT=60000
</cfsavecontent>
<cfset renderCodingTip( codeSample,stText.application.appPathEnvVar)>

					</td>
				</tr>




				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="3">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction2" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction2" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>