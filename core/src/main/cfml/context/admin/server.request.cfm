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
					AllowURLRequestTimeout="#structKeyExists(form,'requestTimeoutInURL') and form.requestTimeoutInURL#"
					requestTimeout="#CreateTimeSpan(form.requestTimeout_span_days,form.requestTimeout_span_hours,form.requestTimeout_span_minutes,form.requestTimeout_span_seconds)#"
					requestTimeoutConcurrentRequestThreshold="#form.requestTimeout_concurrentrequestthreshold?:0#"
					requestTimeoutCPUThreshold="#form.requestTimeout_cputhreshold?:0#"
					requestTimeoutMemoryThreshold="#form.requestTimeout_memorythreshold?:0#"
					>

				<cfif request.admintype =="server">
					<cfscript>
						if(structKeyExists(form,'requestQueueTimeout_days')) {
							timeoutMS=
								(form.requestQueueTimeout_seconds*1000)+
								(form.requestQueueTimeout_minutes*60*1000)+
								(form.requestQueueTimeout_hours*60*60*1000)+
								(form.requestQueueTimeout_days*60*60*24*1000);
						}
						else timeoutMS="";// emty string==removed
					</cfscript>


					<cfadmin
					action="updateQueueSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					requestQueueEnable="#structKeyExists(form,'requestQueueEnable') and form.requestQueueEnable#"
					requestQueueMax="#structKeyExists(form,'ConcurrentRequestMax')?form.ConcurrentRequestMax:""#"
					requestQueueTimeout="#timeoutMS#"
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

					requestQueueMax=""
					requestQueueTimeout=""
					requestQueueEnable=""
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

					listenerType="#form.listenerType#"
					listenerMode="#form.listenerMode#"
					listenerSingleton="#form.listenerSingleton?:false#"
					applicationPathTimeout="#CreateTimeSpan(form.applicationPathTimeout_days?:0,form.applicationPathTimeout_hours?:0,form.applicationPathTimeout_minutes?:0,form.applicationPathTimeout_seconds?:0)#"
					
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
					listenerSingleton=""
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
						<cfmodule template="systemSetting.cfm" 
							name="scriptProtect" 
							value="#appSettings.scriptProtect#"
							access="#hasAccess#"
							description=""
							br=true
							sp=true
							descOnTop=false>
						
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
						</cfmodule>
					</td>
				</tr>
				</tbody>
				<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction1" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif not request.singleMode and request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction1" value="#stText.Buttons.resetServerAdmin#"></cfif>
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
						<cfmodule template="systemSetting.cfm" 
							name="requestTimeout_span" 
							value="#appSettings.requestTimeout#"
							access="#hasAccess#"
							description="#stText.application.RequestTimeoutDescription#"
							br=true
							sp=true
							descOnTop=true>
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
								<tr>
									<td><cfinputClassic type="text" name="requestTimeout_span_days" value="#appSettings.requestTimeout_day#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
									<td><cfinputClassic type="text" name="requestTimeout_span_hours" value="#appSettings.requestTimeout_hour#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
									<td><cfinputClassic type="text" name="requestTimeout_span_minutes" value="#appSettings.requestTimeout_minute#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
									<td><cfinputClassic type="text" name="requestTimeout_span_seconds" value="#appSettings.requestTimeout_second#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
								</tr>
							</tbody>

						</table>
						</cfmodule>
					</td>
				</tr>
				<!--- request timeout url --->
				<tr>
					<th scope="row">#stText.application.AllowURLRequestTimeout#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="requestTimeoutInURL" 
							value="#appSettings.AllowURLRequestTimeout#"
							access="#hasAccess#"
							description="#stText.application.AllowURLRequestTimeoutDesc#"
							br=false
							sp=false
							descOnTop=false>
							<input type="checkbox" name="requestTimeoutInURL" value="true" class="checkbox"
							<cfif appSettings.AllowURLRequestTimeout>  checked="checked"</cfif>>
						</cfmodule>
					</td>
				</tr>
				<!--- allow request timeout
				<tr>
					<th scope="row">#stText.application.AllowRequestTimeout#</th>
					<td>
						<div class="comment">#stText.application.AllowRequestTimeoutDesc#</div>
						<cfset renderSettings( "requestTimeout",true)>
					</td>
				</tr> --->
				<!--- concurrentrequestthreshold --->
				<tr>
					<th scope="row">#stText.application.concurrentrequestthreshold#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="requestTimeout_concurrentrequestthreshold" 
							value="#appSettings.RequestTimeoutConcurrentRequestThreshold#"
							access="#hasAccess#"
							description="#stText.application.concurrentrequestthresholdDesc#"
							br=false
							sp=false
							descOnTop=true>
						
							<cfinputClassic type="text" name="requestTimeout_concurrentrequestthreshold" value="#appSettings.RequestTimeoutConcurrentRequestThreshold?:0#"
									validate="integer" id="RequestTimeoutConcurrentRequestThreshold">
						
						</cfmodule>
					</td>
				</tr>
<!---
						sct.set("RequestTimeoutConcurrentRequestThreshold", Caster.toDouble(config.getRequestTimeoutConcurrentRequestThreshold()));
		sct.set("RequestTimeoutCPUThreshold", Caster.toDouble(config.getRequestTimeoutCPUThreshold()));
		sct.set("RequestTimeoutMemoryThreshold", Caster.toDouble(config.getRequestTimeoutMemoryThreshold()));
--->
				<!--- cputhreshold --->
				<tr>
					<th scope="row">#stText.application.cputhreshold#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="requestTimeout_cputhreshold" 
							value="#decimalFormat(appSettings.RequestTimeoutCPUThreshold?:0)#"
							access="#hasAccess#"
							description="#stText.application.cputhresholdDesc#"
							br=false
							sp=false
							descOnTop=true>
						
							<cfinputClassic type="text" name="requestTimeout_cputhreshold" value="#decimalFormat(appSettings.RequestTimeoutCPUThreshold?:0)#"
									 id="RequestTimeoutCPUThreshold">
						
						</cfmodule>
					</td>
				</tr>
				<!--- memorythreshold --->
				<tr>
					<th scope="row">#stText.application.memorythreshold#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="requestTimeout_memorythreshold" 
							value="#decimalFormat(appSettings.RequestTimeoutMemoryThreshold?:0)#"
							access="#hasAccess#"
							description="#stText.application.memorythresholdDesc#"
							br=false
							sp=false
							descOnTop=true>
						
							<cfinputClassic type="text" name="requestTimeout_memorythreshold" value="#decimalFormat(appSettings.RequestTimeoutMemoryThreshold?:0)#"
									 id="RequestTimeoutMemoryThreshold">
						
						</cfmodule>
					</td>
				</tr>

				

			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction1" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif not request.singleMode and request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction1" value="#stText.Buttons.resetServerAdmin#"></cfif>
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
						<cfmodule template="systemSetting.cfm" 
							name="requestQueueEnable" 
							value="#queueSettings.requestQueueEnable#"
							access="#hasAccess#"
							description="#stText.application.ConcurrentRequestEnableDesc#"
							br=false
							sp=false
							descOnTop=false>
						<span id="ConcurrentRequestEnableSpan">
							<input type="checkbox" name="requestQueueEnable" value="true" class="checkbox"
							<cfif queueSettings.requestQueueEnable>  checked="checked"</cfif>>
						
						</span>
</cfmodule>
					</td>
				</tr>

				<tr>
					<th scope="row">#stText.application.ConcurrentRequestMax#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="requestQueueMax" 
							value="#queueSettings.requestQueueMax#"
							access="#hasAccess#"
							description="#stText.application.ConcurrentRequestMaxDesc#"
							br=false
							sp=false
							descOnTop=false>
							<cfinputClassic type="text" name="ConcurrentRequestMax" value="#queueSettings.requestQueueMax#"
									class="number" required="yes" validate="integer" id="ConcurrentRequestMax"
									message="#stText.application.ConcurrentRequestMaxError#">

						</cfmodule>
					</td>
				</tr>


				<tr>
					<th scope="row">#stText.application.ConcurrentRequestTimeout#</th>
					<td>
						<cfscript>
							seconds=int(queueSettings.requestQueueTimeout/1000);
							minutes=int(seconds/60);
							seconds-=minutes*60;
							hours=int(minutes/60);
							minutes-=hours*60;
							days=int(hours/24);
							hours-=days*24;

							ts=createTimespan(days,hours,minutes,seconds);
						</cfscript>
						<cfmodule template="systemSetting.cfm" 
							name="requestQueueTimeout" 
							value="#ts#"
							access="#hasAccess#"
							description="#stText.application.ConcurrentRequestTimeoutDesc#"
							br=false
							sp=false
							descOnTop=true>
							
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
								<tr>
									<td><cfinputClassic type="text" name="requestQueueTimeout_days" value="#days#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
									<td><cfinputClassic type="text" name="requestQueueTimeout_hours" value="#hours#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
									<td><cfinputClassic type="text" name="requestQueueTimeout_minutes" value="#minutes#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
									<td><cfinputClassic type="text" name="requestQueueTimeout_seconds" value="#seconds#"
										class="number" required="yes" validate="integer"
										message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
								</tr>
							</tbody>
							</table>
						</cfmodule>
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
							<cfif not request.singleMode and request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction1" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
</cfif>

	</cfformClassic>

	<h2>#stText.application.listener#</h2>
	<div class="itemintro">#stText.application.listenerDescription#</div>
	<cfscript>
		stText.application.singleton="Singleton";
		stText.application.singletonDesc="When enabled, Lucee loads the Application.cfc component only once when the application context starts or when the component file changes. 
		This reduces overhead for applications that follow best practices of containing logic within lifecycle methods. When disabled (default), Application.cfc is loaded for every request.";
	</cfscript>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- singleton --->
				<tr>
					<th scope="row">#stText.application.singleton#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="listenerSingleton" 
							value="#listener.singleton?:false#"
							access="#hasAccess#"
							description="#stText.application.singletonDesc#"
							br=false
							sp=false
							descOnTop=false>
						
						<span id="singleton">
							<input type="checkbox" name="listenerSingleton" value="true" class="checkbox"
							<cfif (listener.singleton?:false)>  checked="checked"</cfif>>
						</span>
						</cfmodule>
					</td>
				</tr>

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
						<cfmodule template="systemSetting.cfm" 
							name="listenerType" 
							value="#listener.type#"
							access="#hasAccess#"
							description=""
							br=false
							sp=false
							descOnTop=true>
						
							<ul class="radiolist">
								<cfloop index="key" list="none,classic,modern,mixed">
									<li>
										<label>
											<input type="radio" class="radio" name="listenerType" value="#key#" <cfif listener.type EQ key>checked="checked"</cfif>>
											<b>#stText.application['listenerType_' & key]#</b>
										</label>
										<div class="comment">#stText.application['listenerTypeDescription_' & key]#</div>
									</li>
								</cfloop>
							</ul>
						</cfmodule>
					</td>
				</tr>

				<!--- listener mode --->
				<tr>
					<th>#stText.application.listenerMode#
					</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="listenerMode" 
							value="#listener.mode#"
							access="#hasAccess#"
							description="#stText.application.listenerModeDescription#"
							br=false
							sp=false
							descOnTop=true>
						
							<ul class="radiolist">
								<cfloop index="key" list="curr2root,currorroot,root,current">
									<li>
										<label>
											<input type="radio" class="radio" name="listenerMode" value="#key#" <cfif listener.mode EQ key>checked="checked"</cfif>>
											<b>#stText.application['listenerMode_' & key]#</b>
										</label>
										<div class="comment">#stText.application['listenerModeDescription_' & key]#</div>
									</li>
								</cfloop>
							</ul>
						</cfmodule>
					</td>
				</tr>
<cfset stText.application.appPathEnvVar="This can also be defined using an environment variable as follows">
<cfset stText.application.appPathTimeout="Timeout for the Application Path Cache">
<cfset stText.application.appPathTimeoutDesc="If set to greater than 0, Lucee will cache the Path to the Application.[cfc|cfm] file to use for that time. So Lucee does not search the Application.cfc with every request. If set to 0, the cache is disabled. ">


				<tr>
					<th scope="row">#stText.application.appPathTimeout#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="applicationPathTimeout" 
							value="#appSettings.applicationPathTimeout#"
							access="#hasAccess#"
							description="#stText.application.appPathTimeoutDesc#"
							br=false
							sp=false
							descOnTop=true>
						
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
										<td><cfinputClassic type="text" name="applicationPathTimeout_days" value="#appSettings.applicationPathTimeout_day#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="applicationPathTimeout_hours" value="#appSettings.applicationPathTimeout_hour#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="applicationPathTimeout_minutes" value="#appSettings.applicationPathTimeout_minute#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="applicationPathTimeout_seconds" value="#appSettings.applicationPathTimeout_second#"
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
						</cfmodule>
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
							<cfif not request.singleMode and request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction2" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>