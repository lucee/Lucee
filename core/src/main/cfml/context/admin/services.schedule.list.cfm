<cfset error.message="">
<cfset error.detail="">

<!--- 
Defaults ---> 
<cfparam name="session.st.nameFilter" default="">
<cfparam name="session.st.IntervalFilter" default="">
<cfparam name="session.st.urlFilter" default="">
<cfparam name="session.st.sortOrder" default="">
<cfparam name="session.st.sortName" default="">

<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">
<cfset error.message="">

<cffunction name="doFilter" returntype="string" output="false">
	<cfargument name="filter" required="yes" type="string">
	<cfargument name="value" required="yes" type="string">
	<cfargument name="exact" required="no" type="boolean" default="false">
	
	<cfset arguments.filter=replace(arguments.filter,'*','',"all")>
    <cfset arguments.filter=trim(arguments.filter)>
	<cfif not len(arguments.filter)>
		<cfreturn true>
	</cfif>
	<cfif exact>
		<cfreturn filter EQ value>
	<cfelse>
		<cfreturn FindNoCase(filter,value)>
	</cfif>
</cffunction>

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- FILTER --->
		<cfcase value="filter">
			<cfset session.st.nameFilter=form.nameFilter>
			<cfset session.st.IntervalFilter=form.IntervalFilter>
			<cfset session.st.urlFilter=form.urlFilter>
		</cfcase>
	<!--- EXECUTE --->
		<cfcase value="#stText.Buttons.Execute#">
			<cfset data.names=toArrayFromForm("name")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
				<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
				<cfsetting requesttimeout="10000">
					<cfadmin 
						action="schedule" 
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						scheduleAction="run" 
						task="#data.names[idx]#"
						remoteClients="#request.getRemoteClients()#">
				</cfif>
			</cfloop>
		</cfcase>
	<!--- DELETE --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
				<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
				
					<cfadmin 
						action="schedule" 
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						scheduleAction="delete" 
						task="#data.names[idx]#"
						remoteClients="#request.getRemoteClients()#">
				</cfif>
			</cfloop>
		</cfcase>
	<!--- pause --->
		<cfcase value="#stText.Schedule.pause#">
			<cfset data.names=toArrayFromForm("name")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
				<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
				
					<cfadmin 
						action="schedule" 
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						scheduleAction="pause" 
						task="#data.names[idx]#"
						remoteClients="#request.getRemoteClients()#">
				</cfif>
			</cfloop>
		</cfcase>
	<!--- resume --->
		<cfcase value="#stText.Schedule.resume#">
			<cfset data.names=toArrayFromForm("name")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
				<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
				
					<cfadmin 
						action="schedule" 
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						scheduleAction="resume" 
						task="#data.names[idx]#"
						remoteClients="#request.getRemoteClients()#">
				</cfif>
			</cfloop>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>


<!--- set order --->
<cfif isDefined("url.order") and ListFindNoCase("task,interval,url",url.order)>
	<cfif session.st.sortName NEQ url.order>
    	<cfset session.st.sortOrder="">
    </cfif>
	<cfset session.st.sortName=url.order>
   
    <cfif session.st.sortOrder EQ "">
    	<cfset session.st.sortOrder="asc">
    <cfelseif  session.st.sortOrder EQ "asc">
    	<cfset session.st.sortOrder="desc">
    <cfelseif  session.st.sortOrder EQ "desc">
    	<cfset session.st.sortOrder="asc">
    </cfif>
</cfif>


<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfschedule action="list" returnvariable="tasks">
<cfif len(session.st.sortName) and len(session.st.sortOrder)>
	<cfset querysort(tasks, session.st.sortName,session.st.sortOrder)>
</cfif>


<cfoutput>
	<div class="pageintro">
		<cfoutput>#stText.Schedule.Description#</cfoutput>
	</div>

	<!--- Error Output--->
	<cfif error.message NEQ "">
		<cfoutput><div class="error">
			#error.message#<br>
			#error.detail#
		</div></cfoutput>
	</cfif>

	<!--- list all mappings and display necessary edit fields --->
	
	<!--- List --->
	<cfif tasks.recordcount>
		<h2>#stText.Schedule.Detail#</h2>
		<div class="itemintro">
			#stText.Schedule.DetailDescription#
		</div>
		
		<div class="filterform">
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<ul>
					<li>
						<label for="filter">#stText.Schedule.Name#:</label>
						<input type="text" name="nameFilter" class="txt" value="#session.st.nameFilter#" />
					</li>
					<li>
						<label for="filter">#stText.Schedule.interval#:</label>
						<input type="text" name="IntervalFilter" class="txt" value="#session.st.IntervalFilter#" />
					</li>
					<li>
						<label for="filter">#stText.Schedule.URL#:</label>
						<input type="text" name="urlFilter" class="txt" value="#session.st.urlFilter#" />
					</li>
					<li>
						<input type="submit" class="button submit" name="mainAction" value="#stText.buttons.filter#" />
					</li>
				</ul>
				<div class="clear"></div>
			</cfformClassic>
		</div>

		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<cfset sort = "asc">
			<cfif sort EQ 'asc'>
				<cfset sorting = "desc">
			<cfelse>
				<cfset sorting = "asc">
			</cfif>
			
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<cfif structKeyExists(URL, "sort")>
							<cfset sorting = URL.sort EQ 'desc' ? 'asc' : 'desc'>
						</cfif>
						<th width="3%"><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)" /></th>
						<th><a href="#request.self#?action=#url.action#&order=task&sort=#sorting#">#stText.Schedule.Name#
							<cfif session.st.sortName EQ "task" and len(session.st.sortOrder)>
								<img src="../res/img/arrow-#session.st.sortOrder EQ 'asc' ? 'up':'down'#.gif.cfm" hspace="4" vspace="4" border="0">
							</cfif></a></th>
						<th><a href="#request.self#?action=#url.action#&order=interval&sort=#sorting#">#stText.Schedule.Interval#
							<cfif session.st.sortName EQ "interval" and len(session.st.sortOrder)><img src="../res/img/arrow-#session.st.sortOrder EQ 'asc' ? 'up':'down'#.gif.cfm" hspace="4" vspace="2" border="0"></cfif></a></th>

						<th><a href="#request.self#?action=#url.action#&order=url&sort=#sorting#">#stText.Schedule.URL#
							<cfif session.st.sortName EQ "url" and len(session.st.sortOrder)><img src="../res/img/arrow-#session.st.sortOrder EQ 'asc' ? 'up':'down'#.gif.cfm" hspace="4" vspace="2" border="0"></cfif></a></th>

						<th>#stText.Schedule.paused#</th>
						<th width="3%">&nbsp;</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="tasks">
						<cfset urlAndPort=mergeURLAndPort(tasks.url,tasks.port)>
						<cfif isNumeric(tasks.interval)>
							<cfset _int=toStructInterval(tasks.interval)>
							<cfset _intervall="#stText.Schedule.Every# (hh:mm:ss) #two(_int.hour)#:#two(_int.minute)#:#two(_int.second)#">
						<cfelse>
							<cfset _intervall=tasks.interval>
						</cfif>
						<cfif
							doFilter(session.st.nameFilter,tasks.task,false)
							and
							doFilter(session.st.IntervalFilter,_intervall,false)
							and
							doFilter(session.st.urlFilter,urlAndPort,false)
						>
							<!--- and now display  --->
							<tr<cfif tasks.valid and not tasks.paused><!--- class="OK"---><cfelse> class="notOK"</cfif>>
								<td>
									<input type="checkbox" class="checkbox" name="row_#tasks.currentrow#" value="#tasks.currentrow#">
								</td>
								<td>
									<input type="hidden" name="name_#tasks.currentrow#" value="#HTMLEditFormat(tasks.task)#">
									#tasks.task#
								</td>
								<td>#_intervall#</td>
								<td><cfif len(urlAndPort) gt 50><abbr title="#urlAndPort#">#cut(urlAndPort,50)#</abbr><cfelse>#urlAndPort#</cfif></td>
								<td>#YesNoFormat(tasks.paused)#</td>
								<td>
									#renderEditButton("#request.self#?action=#url.action#&action2=edit&task=#hash(tasks.task)#")#
								</td>
							</tr>
						</cfif>
					</cfloop>
					<cfmodule template="remoteclients.cfm" colspan="6" line=true>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="6">
							<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.Execute#">
							<input type="reset" class="bm button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="bm button submit enablebutton" name="mainAction" value="#stText.Buttons.Delete#">
							<input type="submit" class="bm button submit enablebutton" name="mainAction" value="#stText.Schedule.pause#">
							<input type="submit" class="br button submit enablebutton" name="mainAction" value="#stText.Schedule.resume#">
						</td>
					</tr>
				</tfoot>
			 </table>
		</cfformClassic>
	</cfif>

	<!--- Create Task --->
	<h2>#stText.Schedule.CreateTask#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Schedule.Name#</th>
					<td><cfinputClassic type="text" name="name" value="" class="large" required="yes" 
						message="#stText.Schedule.NameMissing#"></td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.URL#</th>
					<td>
						<cfinputClassic type="text" name="url" value="" class="xlarge" required="yes" 
						message="#stText.Schedule.URLMissing#">
						<div class="comment">#stText.Schedule.URLDescription#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.IntervalType#</th>
					<td>
						<select name="interval" class="small">
							<option value="3600">#stText.Schedule.Every# ...</option>
							<option value="once">#stText.Schedule.Once#</option>
							<option value="daily">#stText.Schedule.Daily#</option>
							<option value="weekly">#stText.Schedule.Weekly#</option>
							<option value="monthly">#stText.Schedule.Monthly#</option>
						</select>
						<div class="comment">#stText.Schedule.IntervalTypeDesc#</div>
					</td>
				</tr>
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
									<td><cfinputClassic type="text" name="start_day" value="#two(day(now()))#" class="number" required="yes" validate="integer">&nbsp;</td>
									<td><cfinputClassic type="text" name="start_month" value="#two(month(now()))#" class="number" required="yes" validate="integer">&nbsp;</td>
									<td><cfinputClassic type="text" name="start_year" value="#two(year(now()))#" class="number" required="yes" validate="integer">&nbsp;</td>
								</tr>
							</tbody>
						</table>
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
									<td><cfinputClassic type="text" name="start_hour" value="00" class="number" required="yes" validate="integer">&nbsp;</td>
									<td><cfinputClassic type="text" name="start_minute" value="00" class="number" required="yes" validate="integer">&nbsp;</td>
									<td><cfinputClassic type="text" name="start_second" value="00" class="number" required="yes" validate="integer">&nbsp;</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Schedule.paused#</th>	
					<td><input type="checkbox" class="checkbox" name="paused" value="true" /></td>
				</tr>
				<cfmodule template="remoteclients.cfm" colspan="2">
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="reset" class="bl button reset" name="cancel" value="#stText.Buttons.Cancel#">
						<input type="submit" class="br button submit" name="run" value="#stText.Buttons.Create#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>

<!---
<cfmodule template="log.cfm" name="scheduled-task" title="Log" description="this is ...">
--->