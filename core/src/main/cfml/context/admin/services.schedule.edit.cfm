<cfscript>
stText.Schedule.unique="Run Exclusive";
stText.Schedule.uniqueDescription="If set run the task only once at time. Every time a task is started, it will check if still a task from previous round is running, if so no new test is started.";


function toFile(path,file) {
	if(len(arguments.path) EQ 0) return arguments.file;
	if(right(arguments.path,1) NEQ server.separator.file) arguments.path=arguments.path&server.separator.file;
	return arguments.path&arguments.file;

}

function translateDateTime(task,dateName,timeName,newName) {
	var sct=struct();
	var d=0;
	// Date
	if(structKeyExists(arguments.task,arguments.dateName) and IsDate(arguments.task[arguments.dateName])) {
		d=arguments.task[arguments.dateName];
		sct.year=year(d);
		sct.month=two(month(d));
		sct.day=two(day(d));
	}
	else {
		sct.year='';
		sct.month='';
		sct.day='';
	}
	// Time
	if(structKeyExists(arguments.task,arguments.timeName) and IsDate(arguments.task[arguments.timeName])) {
		d=arguments.task[arguments.timeName];
		sct.hour=two(hour(d));
		sct.minute=two(minute(d));
		sct.second=two(second(d));
	}
	else {
		sct.hour='';
		sct.minute='';
		sct.second='';
	}
	arguments.task[arguments.newName]=sct;
}

function formBool(formName) {

	return structKeyExists(form,formName) and form[formName];
}
/**
* returns null if string is empty (no return is equal to return null)
*/
function nullIfEmpty(str) {
	str=trim(str);
	if(len(str) GT 0) return str;
}


function _toInt(str) {
	if(isNumeric(str)) return str;
	return 0;
}


</cfscript>

<cfparam name="error" default="#struct(message:"",detail:"")#">

<!---
ACTIONS --->
<cftry>
	<cfif StructKeyExists(form,"url")>
		<cfset sctURL=splitURLAndPort(form.url)>
		<cfset form.url=sctURL.url>
		<cfset form.port=sctURL.port>
		
		<!--- Check Values --->
		<cfif not IsNumeric(form.port)><cfset form.port=-1></cfif>
		<cfif not IsNumeric(form.timeout)><cfset form.timeout=-1></cfif>
		<cfif not IsNumeric(form.proxyport)><cfset form.proxyport=80></cfif>


		<cfif not StructKeyExists(form,"interval")>
			<cfif StructKeyExists(form,"interval_hour")>
				<cfset form.interval=
					(_toInt(form.interval_hour)*3600)+
					(_toInt(form.interval_minute)*60)+
					(_toInt(form.interval_second))>
			<cfelse>
				<cfset form.interval=form._interval>
			</cfif>

		<cfelseif form.interval EQ "every ...">
			<cfset form.interval="3600">
		</cfif>
		<cfif structKeyExists(session,"passwordserver")>
			<cfset variables.passwordserver=session.passwordserver>
		<cfelse>
			<cfset variables.passwordserver="">
		</cfif>
			<cfadmin
				action="schedule"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"

				scheduleAction="update"
				task="#form.name#"
				url="#form.url#"
				port="#form.port#"
				unique="#form.unique?:false#"
				requesttimeout="#form.timeout#"
				username="#nullIfEmpty(form.username)#"
				schedulePassword="#nullIfEmpty(form.password)#"
				userAgent="#nullIfEmpty(form.userAgent)#"
				proxyserver="#nullIfEmpty(form.proxyserver)#"
				proxyport="#form.proxyport#"
				proxyuser="#nullIfEmpty(form.proxyuser)#"
				proxypassword="#nullIfEmpty(form.proxypassword)#"
				publish="#formBool('publish')#"
				resolveurl="#formBool('resolveurl')#"
				startdate="#nullIfNoDate('start')#"
				starttime="#nullIfNoTime('start')#"
				enddate="#nullIfNoDate('end')#"
				endtime="#nullIfNoTime('end')#"
				interval="#nullIfEmpty(form.interval)#"
				file="#nullIfEmpty(form.file)#"
				serverpassword="#variables.passwordserver#"
				remoteClients="#request.getRemoteClients()#"
				>

   <cfif StructKeyExists(form,"paused") and form.paused>
	   	<cfadmin
					action="schedule"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"

					scheduleAction="pause"
					task="#trim(form.name)#"
					remoteClients="#request.getRemoteClients()#">
   <cfelse>
		<cfadmin
						action="schedule"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"

						scheduleAction="resume"
						task="#trim(form.name)#"
						remoteClients="#request.getRemoteClients()#">
	</cfif>

		<cflocation url="#request.self#?action=#url.action#" addtoken="no">
	</cfif>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>
<!---
Error Output--->
<cfset printError(error)>
<cfschedule action="list" returnvariable="tasks" >
<cfset task=struct()>
<cfloop query="tasks">
	<cfif hash((tasks.task)) EQ trim(url.task)>
		<cfset task=queryRow2Struct(tasks,tasks.currentrow)>
	</cfif>
</cfloop>
<cfset task.urlAndPort=mergeURLAndPort(task.url,task.port)>

<cfset translateDateTime(task,"startdate","starttime","start")>
<cfset translateDateTime(task,"enddate","endtime","end")>

<cfoutput>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=#url.action2#&task=#url.task#" method="post">

		<input type="submit" style="display:none;" onclick="return false;" value="dummy button to disable submit on enter">

		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Schedule.Name#</th>
					<td>
						<input type="hidden" name="name" value="#trim(task.task)#">
						#task.task#
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.URL#</th>
					<td>
						<cfinputClassic type="text" name="url" value="#task.urlAndPort#" class="xlarge" required="yes"
						message="#stText.Schedule.URLMissing#">
						<div class="comment">#stText.Schedule.NameDescEdit#</div></td>
				</tr>
				<!---<tr>
					<th scope="row">#stText.Schedule.Port#</th>
					<td>
						<cfinputClassic type="text" name="port" value="#task.port#" class="number" required="no" validate="integer">
						<div class="comment">#stText.Schedule.PortDescription#</div>
					</td>
				</tr> --->
				<tr>
					<th scope="row">#stText.Schedule.Timeout#</th>
					<td>
						<cfinputClassic type="text" name="timeout" value="#task.timeout#" class="number" required="no" validate="integer">
						<div class="comment">#stText.Schedule.TimeoutDescription#</div>
					</td>
				</tr>

				<tr>
					<th scope="row">#stText.Schedule.unique#</th>
					<td>
						<input type="checkbox" class="checkbox" name="unique" value="true" <cfif task.unique?:false>checked</cfif>>
						<div class="comment">#stText.Schedule.uniqueDescription#</div>
					</td>
				</tr>

				<tr>
					<th scope="row">#stText.Schedule.Username#</th>
					<td>
						<cfinputClassic type="text" name="username" value="#task.username#" class="medium"
						required="no">
						<div class="comment">#stText.Schedule.UserNameDescription#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.Password#</th>
					<td>

						<cfinputClassic type="password" name="password" value="#task.password#" class="medium" required="no">
						<div class="comment">#stText.Schedule.PasswordDescription#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.UserAgent#</th>
					<td>
						<cfinputClassic type="text" name="useragent" value="#task.useragent#" class="large" required="no">
						<div class="comment">#stText.Schedule.UserAgentDescription#</div>
					</td>
				</tr>
			</tbody>
		</table>

		<h2>#stText.Schedule.Proxy#</h2>
		<div class="itemintro">#stText.Schedule.ProxyDesc#</div>
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Schedule.Server#</th>
					<td>
						<cfinputClassic type="text" name="proxyserver" value="#task.proxyserver#" class="large" required="no">
						<div class="comment">#stText.Schedule.ProxyServerDesc#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.Port#</th>
					<td><cfinputClassic type="text" name="proxyport" value="#task.proxyport#" class="number" validate="integer" required="no">
						<div class="comment">#stText.Schedule.ProxyPort#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.Username#</th>
					<td>
						<cfinputClassic type="text" name="proxyuser" value="#task.proxyuser#" class="medium" required="no">
						<div class="comment">#stText.Schedule.ProxyUserName#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.Password#</th>
					<td>
						<cfinputClassic type="text" name="proxypassword" value="#task.proxypassword#" class="medium" required="no">
						<div class="comment">#stText.Schedule.ProxyPassword#</div>
					</td>
				</tr>
			</tbody>
		</table>

		<h2>#stText.Schedule.Output#</h2>
		<div class="itemintro">#stText.Schedule.OutputDesc#</div>
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Schedule.Publish#</th>
					<td><input type="checkbox" class="checkbox" name="publish" value="yes" <cfif task.publish>checked</cfif>>
						<div class="comment">#stText.Schedule.StoreResponse#</div></td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.File#</th>
					<td>
						<cfinputClassic type="text" name="file" value="#toFile(task.path,task.file)#" class="large" required="no">
						<div class="comment">#stText.Schedule.FileDescription#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.Resolve_URL#</th>
					<td>
						<input type="checkbox" class="checkbox" name="resolveurl" value="yes" <cfif task.resolveurl>checked</cfif>>
						<div class="comment">#stText.Schedule.ResolveDescription#</div>
					</td>
				</tr>
			</tbody>
		</table>

		<cfif structKeyExists(form, "interval")>
			<a name="here"></a>

			<cfhtmlbody>

			<script type="text/javascript">
				$(function(){ self.location.href = '##here' });
			</script>

			</cfhtmlbody>
		</cfif>
		<h2>#stText.Schedule.ExecutionDate# <cfif isNumeric(task.interval)>(Every...)<cfelse>(#ucFirst(task.interval)#)</cfif></h2>
		<div class="itemintro">
			<cfif isNumeric(task.interval)>
				#stText.Schedule['ExecutionDescEvery']#
			<cfelse>
				#stText.Schedule['ExecutionDesc'& task.interval]#
			</cfif>
			<br />
			#stText.Schedule.CurrentDateTime#&nbsp;
			#dateFormat(now(),'mmmm dd yyyy')# #timeFormat(now(),'HH:mm:ss')# <!---(mmmm dd yyyy HH:mm:ss)--->
		</div><cfset css="color:white;background-color:#request.adminType EQ "web"?'##39c':'##c00'#;">

			<input style="margin-left:0px;#iif(task.interval EQ 'once','css',de(''))#"
					type="submit" class="bl button submit" name="interval" value="once">
			<input style="#iif(task.interval EQ 'daily','css',de(''))#"
					type="submit" class="bm button submit" name="interval" value="daily">
			<input style="#iif(task.interval EQ 'weekly','css',de(''))#"
					type="submit" class="bm button submit" name="interval" value="weekly">
			<input style="#iif(task.interval EQ 'monthly','css',de(''))#"
					type="submit" class="bm button submit" name="interval" value="monthly">
			<input style="#iif(isNumeric(task.interval),'css',de(''))#"
					type="submit" class="br button submit" name="interval" value="every ...">

		<table class="maintbl">
			<tbody>
				<cfswitch expression="#task.interval#">
					<cfcase value="once">
						<tr>
							<th scope="row">#stText.Schedule.ExecuteAt#
								<input type="hidden" name="_interval" value="#task.interval#">
								<input type="hidden" name="end_hour" value="#task.end.hour#">
								<input type="hidden" name="end_minute" value="#task.end.minute#">
								<input type="hidden" name="end_second" value="#task.end.second#">

								<input type="hidden" name="end_day" value="#task.end.day#">
								<input type="hidden" name="end_month" value="#task.end.month#">
								<input type="hidden" name="end_year" value="#task.end.year#">
							</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Day#</th>
											<th>#stText.General.Month#</th>
											<th>#stText.General.Year#</th>
											<th>&nbsp;</th>
											<th>#stText.General.Hour#</th>
											<th>#stText.General.Minute#</th>
											<th>#stText.General.second#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="start_day" value="#task.start.day#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_month" value="#task.start.month#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_year" value="#task.start.year#" class="number" required="yes" validate="integer"></td>
											<td>&nbsp;&nbsp;-&nbsp;&nbsp;</td>
											<td><cfinputClassic type="text" name="start_hour" value="#task.start.hour#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_minute" value="#task.start.minute#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_second" value="#task.start.second#" class="number" required="yes" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule.ExecuteAtDesc#</div>
							</td>
						</tr>
					</cfcase>
					<cfcase value="daily,weekly,monthly">
						<tr>
							<th scope="row">#stText.Schedule.StartsAt#
								<input type="hidden" name="_interval" value="#task.interval#">
								<input type="hidden" name="end_hour" value="#task.end.hour#">
								<input type="hidden" name="end_minute" value="#task.end.minute#">
								<input type="hidden" name="end_second" value="#task.end.second#">
							</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Day#</th>
											<th>#stText.General.Month#</th>
											<th>#stText.General.Year#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="start_day" value="#task.start.day#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_month" value="#task.start.month#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_year" value="#task.start.year#" class="number" required="yes" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule.StartsAtDesc#</div>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Schedule.ExecutionTime#</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Hour#</th>
											<th>#stText.General.Minute#</th>
											<th>#stText.General.second#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="start_hour" value="#task.start.hour#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_minute" value="#task.start.minute#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_second" value="#task.start.second#" class="number" required="yes" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule['ExecutionTimeDesc'& task.interval ]#</div>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Schedule.EndsAt#</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Day#</th>
											<th>#stText.General.Month#</th>
											<th>#stText.General.Year#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="end_day" value="#task.end.day#" class="number" required="no" validate="integer"></td>
											<td><cfinputClassic type="text" name="end_month" value="#task.end.month#" class="number" required="no" validate="integer"></td>
											<td><cfinputClassic type="text" name="end_year" value="#task.end.year#" class="number" required="no" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule['EndsAtDesc'& task.interval ]#</div>
							</td>
						</tr>
					</cfcase>
					<cfdefaultcase>
						<tr>
							<th scope="row">#stText.Schedule.StartDate#</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Day#</th>
											<th>#stText.General.Month#</th>
											<th>#stText.General.Year#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="start_day" value="#task.start.day#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_month" value="#task.start.month#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_year" value="#task.start.year#" class="number" required="yes" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule.StartDateDesc#</div>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Schedule.StartTime#</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Hour#</th>
											<th>#stText.General.Minute#</th>
											<th>#stText.General.second#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="start_hour" value="#task.start.hour#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_minute" value="#task.start.minute#" class="number" required="yes" validate="integer"></td>
											<td><cfinputClassic type="text" name="start_second" value="#task.start.second#" class="number" required="yes" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment important">#stText.Schedule.StartTimeDesc#</div>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Schedule.EndDate#</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Day#</th>
											<th>#stText.General.Month#</th>
											<th>#stText.General.Year#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="end_day" value="#task.end.day#" class="number" required="no" validate="integer"></td>
											<td><cfinputClassic type="text" name="end_month" value="#task.end.month#" class="number" required="no" validate="integer"></td>
											<td><cfinputClassic type="text" name="end_year" value="#task.end.year#" class="number" required="no" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule.endDateDesc#</div>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Schedule.EndTime#</th>
							<td>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Hour#</th>
											<th>#stText.General.Minute#</th>
											<th>#stText.General.second#</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="end_hour" value="#task.end.hour#" class="number" required="no" validate="integer"></td>
											<td><cfinputClassic type="text" name="end_minute" value="#task.end.minute#" class="number" required="no" validate="integer"></td>
											<td><cfinputClassic type="text" name="end_second" value="#task.end.second#" class="number" required="no" validate="integer"></td>
										</tr>
									</tbody>
								</table>
								<div class="comment important">#stText.Schedule.endTimeDesc#</div>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Schedule.Interval#</th>
							<td>
								<cfset interval=toStructInterval(task.interval)>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Hour#s</th>
											<th>#stText.General.Minute#s</th>
											<th>#stText.General.Second#s</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" name="interval_hour" value="#interval.hour#" class="number"
												required="no" validate="integer"
												message="#stText.General.HourError#">
											</td>
											<td><cfinputClassic type="text" name="interval_minute" value="#interval.minute#" class="number"
												required="no" validate="integer"
												message="#stText.General.MinuteError#">
											</td>
											<td><cfinputClassic type="text" name="interval_second" value="#interval.second#" class="number"
												required="no" validate="integer"
												message="#stText.General.SecondError#">
											</td>
										</tr>
									</tbody>
								</table>
								<div class="comment">#stText.Schedule.IntervalDesc#</div>
							</td>
						</tr>
					</cfdefaultcase>
				</cfswitch>
				<tr>
					<th scope="row">#stText.Schedule.paused#</th>
					<td>
						<input type="checkbox" class="checkbox" name="paused" value="true"<cfif task.paused> checked="checked"</cfif> />
						<div class="comment">#stText.Schedule.pauseDesc#</div>
					</td>
				</tr>
				<cfmodule template="remoteclients.cfm" colspan="2">
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="bl button cancel" name="cancel" value="#stText.Buttons.Cancel#">
						<input type="submit" class="br button submit" name="run" value="#stText.Buttons.Update#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>
