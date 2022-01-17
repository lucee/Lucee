<cfset hasAccess=true>
<cfif not isDefined('session.filter')>
	<cfset session.filter.type="">
	<cfset session.filter.name="">
	<cfset session.filter.next="">
	<cfset session.filter.tries="">
</cfif>


<cfadmin 
	action="getTaskSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="settings">
	

<cfparam name="form.mainAction" default="none">
<cfparam name="session.taskRange" default="10">
<cfparam name="form.subAction" default="none">
<cfparam name="url.startrow" default="1">
<cfparam name="url.maxrow" default="100">

<cfparam name="error" default="#struct(message:"",detail:"")#">
<cfset error.message="">
<cfset stVeritfyMessages = StructNew()>
<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- Filter --->
		<cfcase value="#stText.Buttons.filter#">
			
			
			
			<cfset session.filter.type=trim(form.typeFilter)>
			<cfset session.filter.name=trim(form.nameFilter)>
			<cfset session.filter.next=trim(form.nextFilter)>
			<cfset session.filter.tries=trim(form.triesFilter)>
		</cfcase>
	
		<cfcase value="#stText.Buttons.Update#">
			<cfadmin 
					action="updateTaskSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					maxThreads="#form.maxThreads#"
					remoteClients="#request.getRemoteClients()#">
		</cfcase>
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			<cfadmin 
					action="updateTaskSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					maxThreads=""
					remoteClients="#request.getRemoteClients()#">
		</cfcase>
	<!--- EXECUTE --->
		<cfcase value="#stText.Buttons.Execute#">
			<cfset data.ids=toArrayFromForm("id")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
				<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
					<cftry>
						<cfadmin 
							action="executeSpoolerTask"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							id="#data.ids[idx]#">
							<cfset stVeritfyMessages[data.ids[idx]].Label = "OK">
						<cfcatch>
							<cfset stVeritfyMessages[data.ids[idx]].Label = "Error">
							<cfset stVeritfyMessages[data.ids[idx]].message = cfcatch.message>
						</cfcatch>
					</cftry>
				</cfif>
			</cfloop>
		</cfcase>
	<!--- DELETE --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.ids=toArrayFromForm("id")>
			<cfset data.rows=toArrayFromForm("row")>
			
			<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
				<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
					<cfadmin 
						action="removeSpoolerTask"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						id="#data.ids[idx]#">
				</cfif>
			</cfloop>
			<cfif cgi.request_method EQ "POST" and error.message EQ "">
				<cflocation url="#request.self#?action=#url.action#" addtoken="no">
			</cfif>
		</cfcase>
	<!--- DELETE ALL --->
		<cfcase value="#stText.Buttons.DeleteAll#">
			
					<cfadmin 
						action="removeAllSpoolerTask"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#">

			<cfif cgi.request_method EQ "POST" and error.message EQ "">
				<cflocation url="#request.self#?action=#url.action#" addtoken="no">
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>

<!--- Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and not isDefined('doNotRedirect')>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfparam name="url.id" default="0">

<cfadmin 
	action="getSpoolerTasks"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	startrow="#url.startrow#"
	maxrow="#url.maxrow#"
	result="result"
	returnVariable="tasks">

<cffunction name="addZeros" returntype="string" output="false">
	<cfargument name="nbr" required="yes" type="numeric">
	
	<cfif arguments.nbr GT 9>
		<cfreturn  arguments.nbr>
	<cfelse>
		<cfreturn  "0"&arguments.nbr>
	</cfif>
</cffunction>

<cffunction name="toTime" returntype="string" output="false">
	<cfargument name="date" required="yes" type="date">
	<cfargument name="dspMinus" required="no" type="boolean" default="false">
	
	<cfset seconds=DateDiff("s",now(),arguments.date)>
	<cfset str="">
	<cfif seconds LT 0>
		<cfset s=seconds>
		<cfset seconds=(s-s)-s>
		<cfif dspMinus><cfset str="- "></cfif>
	</cfif>
	
	<cfset h=int(seconds/3600)>
	<cfset m=int(seconds/60)-h*60>
	<cfset s=(seconds-h*3600)-m*60>
	<cfreturn "#str##addZeros(h)#:#addZeros(m)#:#addZeros(s)#">
</cffunction>


<cffunction name="inMinutes" returntype="string" output="false">
	<cfargument name="date" required="yes" type="date">
	<cfargument name="dspMinus" required="no" type="boolean" default="false">
	
	<cfreturn DateDiff("m",now(),arguments.date)>
	
	
	
</cffunction>

<cffunction name="doFilter" returntype="string" output="false">
	<cfargument name="filter" required="yes" type="string">
	<cfargument name="value" required="yes" type="string">
	<cfargument name="exact" required="no" type="boolean" default="false">
	
	<cfset arguments.filter=replace(arguments.filter,'*','',"all")>
	<cfif not len(filter)>
		<cfreturn true>
	</cfif>
	
	
	<cfif exact>
		<cfreturn filter EQ value>
	<cfelse>
		<cfreturn FindNoCase(filter,value)>
	</cfif>
</cffunction>
				
<cfset querySort(tasks,"lastExecution","desc")>


<cfoutput>
	<!--- Error Output--->
	<cfif error.message NEQ "">
		<div class="error">
			#error.message#<br>
			#error.detail#
		</div>
	</cfif>
	
	
	
	
	<!--- DETAIL ---->
	<cfif url.id NEQ 0>
		<cfloop query="tasks">
			<cfif url.id EQ tasks.id>
				<cfset css=iif(not tasks.closed,de('Green'),de('Red'))>
				#replace(replace(stText.remote.ot.detailDesc[css],'<tries>',tasks.tries),'<triesleft>',tasks.triesMax-tasks.tries)#
				<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
					<table class="maintbl">
						<tbody>
							<!--- MUST wieso geht hier der direkte aufruf nicht! --->
							<cfset detail=tasks.detail>
							<cfif isDefined("detail.label")>
								<tr>
									<th scope="row">x#stText.remote.ot.name#</th>
									<td class="tblContent#css#">#detail.label#</td>
								</tr>
							</cfif>
							<cfif isDefined("detail.url")>
								<tr>
									<th scope="row">#stText.remote.ot.url#</th>
									<td class="tblContent#css#">#detail.url#</td>
								</tr>
							</cfif>
							<cfif isDefined("detail.action")>
								<tr>
									<th scope="row">#stText.remote.ot.action#</th>
									<td class="tblContent#css#">#detail.action#</td>
								</tr>
							</cfif>
							<tr>
								<th scope="row">#stText.remote.ot.lastExecution#
									<div class="comment" style="color:##DFE9F6">(mm/dd/yyyy HH:mm:ss)</div>
								</th>
								<td class="tblContent#css#">#dateFormat(tasks.lastExecution,'mm/dd/yyyy')# #timeFormat(tasks.lastExecution,'HH:mm:ss')#</td>
							</tr>
							<tr>
								<th scope="row">#stText.remote.ot.nextExecution#
									<div class="comment" style="color:##DFE9F6">(mm/dd/yyyy HH:mm:ss)</div>
								</th>
								<td class="tblContent#css#"><cfif tasks.closed> <center>-</center> <cfelse>
								#dateFormat(tasks.nextExecution,'mm/dd/yyyy')# #timeFormat(tasks.nextExecution,'HH:mm:ss')#</cfif></td>
							</tr>
							
							<tr>
								<th scope="row">#stText.remote.ot.tries#</th>
								<td class="tblContent#css#">#tasks.tries#</td>
							</tr>
							<tr>
								<th scope="row">#stText.remote.ot.triesLeft#</th>
								<cfset tmp=tasks.triesMax-tasks.tries>
								<cfif tmp LT 0><cfset tmp=0></cfif>
								<td class="tblContent#css#">#tmp#</td>
							</tr>
							<tr>
								<th scope="row">#stText.remote.ot.state#</th>
								<td class="tblContent#css#">#iif(tasks.closed,de("Close"),de("Open"))#</td>
							</tr>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="2">
									<input type="hidden" class="checkbox" name="row_#tasks.currentrow#" value="#tasks.currentrow#">
									<input type="hidden" name="id_#tasks.currentrow#" value="#tasks.id#">
									<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="bl button cancel" name="cancel" value="#stText.Buttons.Cancel#">
									<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.Execute#">
									<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.Delete#">
								</td>
							</tr>
						</tfoot>
					</table>
					
					<h3>Task details</h3>
					<table class="maintbl">
						<tbody>
							<cfloop collection="#detail#" item="key">
								<cfif ListFindNoCase("label,url,action",key)>
									<cfcontinue>
								</cfif>
								<tr>
									<th scope="row">#(tasks.type)# #key#</th>
									<td class="tblContent#css#">#replace(detail[key],'<','&lt;','all')#</td>
								</tr>
							</cfloop>
						</tbody>
					</table>

					<h3>#stText.remote.ot.error#</h3>
					<table class="maintbl">
						<thead>
							<tr>
								<th>#stText.remote.ot.exetime#
									<div class="comment">(mm/dd/yyyy HH:mm:ss)</div>
								</th>
								<th>#stText.remote.ot.error#</th>
							</tr>
						</thead>
						<tbody>
							<cfset exp=tasks.exceptions>
							<cfloop collection="#exp#" item="i">
								<tr>
									<td class="tblContent#css#">
										<cfif isDate(exp[i].time) and year(exp[i].time) NEQ 1970>
											#dateFormat(exp[i].time,'mm/dd/yyyy')# #timeFormat(exp[i].time,'HH:mm:ss')#
										<cfelse>-</cfif>
									</td>
									<td class="tblContent#css#">
										<cfif structKeyExists(exp[i],"message")><b>#exp[i].message#</b></cfif>
										<cfif structKeyExists(exp[i],"stacktrace")><pre>#exp[i].stacktrace#</pre></cfif>
									</td>
								</tr>
							</cfloop>
						</tbody>
					</table>
				</cfformClassic>
			</cfif>
		</cfloop>
	<!--- List ---->
	<cfelse>
	
		
	<h2>#stText.remote.Settings.title#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.remote.settings.maxThreads#</th>
					<td>
						<cfif hasAccess>
							<cfinputClassic type="text" name="maxThreads" 
									value="#settings.maxThreads#" validate="integer" class="number" required="no">
						<cfelse>
							<b>#settings.maxThreads#</b><br>
						</cfif>
						
						<div class="comment">#stText.remote.settings.maxThreadsDesc#</div>
					</td>
				</tr>
				
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2"><cfoutput>
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="canel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</cfoutput></td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>

	
	
	
	
	
	
	<h2>#stText.remote.title#</h2>
	
		<cfset types=struct()>
		<cfsilent>
			<cfloop query="tasks">
				<cfset types[tasks.type]="">
			</cfloop>
			<cfset types=StructKeyArray(types)>
		</cfsilent>
		<div class="pageintro">#stText.remote.ot.overviewDesc#</div>
		
		<!--- show verify result --->
		<cfloop collection="#stVeritfyMessages#" item="id">
			<cfif stVeritfyMessages[id].label eq "OK">
				<div class="message">Verified OK</div>
			<cfelse>
				<div class="error">
					#stVeritfyMessages[id].label#:<br />
					#stVeritfyMessages[id].message#
				</div>
			</cfif>
		</cfloop>

		<!--- 0 records ---->
		<cfif result.open+result.closed gt 0>
			<!--- Todo: better styled paging --->
			<cfsavecontent variable="browse">
				<cfset to=url.startrow+url.maxrow-1>
				<cfif to GT result.open+result.closed>
					<cfset to=result.open+result.closed>
				</cfif>
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td width="100">
							<cfif url.startrow GT 1><a href="#request.self#?action=#url.action#&startrow=#url.startrow-url.maxrow#" class="comment"><img src="../res/img/arrow-left.jpg.cfm" title="Previous" width="25" height="25"
							 border= "5px solid ##555;" hspace="4"></a><cfelse>&nbsp;</cfif>
						</td>
						<td style="text-align:center"><b>#url.startrow# #stText.remote.to# #to# #stText.remote.from# #result.open+result.closed#</b></td>
						<td width="100" style="text-align:right">
							<cfif to LT result.open+result.closed><a href="#request.self#?action=#url.action#&startrow=#url.startrow+url.maxrow#" class="comment"><img src="../res/img/arrow-right.jpg.cfm" title="Next" width="25" height="25"></a><cfelse>&nbsp;</cfif>
						</td>
					</tr>
				</table>
			</cfsavecontent> 
			#browse#
		</cfif>
		
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%">
							<input type="checkbox" class="checkbox" name="row" onclick="selectAll(this)">
						</th>
						<th width="20%">#stText.remote.ot.type#</th>
						<th width="39%">#stText.remote.ot.name#</th>
						<th width="20%">#stText.remote.ot.nextExecution#<!---<br /><span class="comment" style="color:##DFE9F6">(mm/dd/yyyy HH:mm:ss)</span>---></th>
						<th width="15%">#stText.remote.ot.tries#</th>
						<th width="3%">&nbsp;</th>
					</tr>
				</thead>
				<tbody>
					<cfif result.open+result.closed eq 0>
						<tr><td colspan="6" style="text-align:center">
							<b>#stText.remote.ot.noOt#</b>
						</td></tr>
					</cfif>
					<!--- FILTER take out temporary
					<tr>
						<td width="200"></td>
						<td class="tblHead">
						<select name="typeFilter" style="width:120px">
						<option value="" <cfif not len(session.filter.type)> selected</cfif>>- all -</option>
						<cfloop array="#types#" index="i">
							<option <cfif i EQ session.filter.type> selected</cfif>>#i#</option>
						</cfloop>
						</select>
						
						</td>
						<td width="250" class="tblHead"><input type="text" name="nameFilter" style="width:250px" value="#session.filter.name#" /></td>
						<th scope="row"><input type="text" name="nextFilter" style="width:90px" value="#session.filter.next#" /></th>
						<th scope="row"><input type="text" name="triesFilter" style="width:90px" value="#session.filter.tries#" /></th>
						<th scope="row"><input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.filter#"></th>
					</tr>
					--->
					<cfloop query="tasks">
						<cfset css="">
						<cfset next=inMinutes(tasks.nextExecution,true)>
						<cfset closed=tasks.closed NEQ "" and tasks.closed>
						<cfif closed><cfset next='-'></cfif>
						<!--- filter 
							doFilter(session.filter.type,tasks.type,false)
							and
							doFilter(session.filter.name,tasks.name,false)
							and
							doFilter(session.filter.next,next,true)
							and
							doFilter(session.filter.tries,tasks.tries,true)--->
						<cfif true>
							<cfif tasks.closed NEQ "">
								<cfset css=iif(not tasks.closed,de('Green'),de('Red'))>
							</cfif>
							<!--- and now display --->
							<tr>
								<td>
									<input type="checkbox" class="checkbox" name="row_#tasks.currentrow#" value="#tasks.currentrow#">
								</td>
								<td class="tblContent#css#"><input type="hidden" name="id_#tasks.currentrow#" value="#tasks.id#">
									#tasks.type#
								</td>
								<td class="tblContent#css#">#wrap(tasks.name,80)#</td>
								<!---
								<td class="tblContent#css#">
									<cfif isDate(tasks.lastExecution) and year(tasks.lastExecution) NEQ 1970>
										<!--- #dateFormat(tasks.lastExecution,'mm/dd/yyyy')# #timeFormat(tasks.lastExecution,'HH:mm:ss')#--->
										#toTime(tasks.lastExecution)#
									<cfelse>
										-
									</cfif>
								</td>
								--->
								<td class="tblContent#css#">
									<cfif closed> 
										<center>-</center>
									<cfelse>
										#lsDateFormat(tasks.nextExecution)# #timeFormat(tasks.nextExecution,'HH:mm:ss')#
									</cfif>
								</td>
								<td class="tblContent#css#">
									#tasks.tries#
								</td>
								<td>
									#renderEditButton("#request.self#?action=#url.action#&action2=edit&id=#tasks.id#")#
									
								</td>
							</tr>
						</cfif>
					</cfloop>
				</tbody>
				<cfif result.open+result.closed gt 0>
					<tfoot>
						 <tr>
							<td colspan="6">
								<input type="reset" class="bl button reset" name="cancel" value="#stText.Buttons.Cancel#">
								<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.Execute#">
								<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.Delete#">
								<input type="submit" class="br button" name="mainAction" value="#stText.Buttons.DeleteAll#">
							</td>	
						</tr>
					</tfoot>
				</cfif>
			</table>
		</cfformClassic>
		<cfif result.open+result.closed gt 0>
			#browse#
		</cfif>
	</cfif>
</cfoutput>
