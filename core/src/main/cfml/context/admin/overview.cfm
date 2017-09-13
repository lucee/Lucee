<!---
Defaults --->
<cfset error.message="">
<cfset error.detail="">
<cfparam name="form.mainAction" default="none">

<!--- load asynchron all extension providers  --->
<cfparam name="application[request.admintype].preloadedExtensionProviders" default="false" type="boolean">
<cfif !application[request.admintype].preloadedExtensionProviders>
	<cfinclude template="ext.functions.cfm">
	<cfset application[request.admintype].preloadedExtensionProviders=true>
</cfif>

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE Label --->
		<cfcase value="#stText.Buttons.Update#">
			<cfset data.label=toArrayFromForm("label")>
			<cfset data.hash=toArrayFromForm("hash")>

			<cfloop index="idx" from="1" to="#arrayLen(data.label)#">
				<cfif len(trim(data.label[idx]))>
                	<cfadmin
                    action="updateLabel"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"

                    label="#data.label[idx]#"
                    hash="#data.hash[idx]#">
                 </cfif>
            </cfloop>
		</cfcase>
	<!--- UPDATE API Key --->
		<cfcase value="#stText.Buttons.OK#">
			<cfadmin
                    action="updateApiKey"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"
                    key="#trim(form.apiKey)#">
		</cfcase>
	</cfswitch>
	<cfcatch>

		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>
<cfadmin 
	action="surveillance" 
	type="#request.adminType#" 
	password="#session["password"&request.adminType]#" 
	returnVariable="surveillance">

<!---
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction NEQ "none">
	<cflocation url="#request.self#" addtoken="no">
</cfif>

<!---
Error Output --->
<cfset printError(error)>

<cfset stText.setting.info="System Metrics">

<cfset stText.setting.memory="Memory">
<cfset stText.setting.memoryDesc="Memory used by the JVM, heap and non heap.">
<cfset stText.setting.request="Request/Threads">
<cfset stText.setting.requestDesc="Request and threads (started by &lt;cfthread>) currently running on the system.">

<cfset stText.setting.req="Requests">
<cfset stText.setting.reqDesc="Web requests running at the moment.">
<cfset stText.setting.queued="Queued Requests">
<cfset stText.setting.queuedDesc="Request in queued waiting to execute.">
<cfset stText.setting.thread="Threads">
<cfset stText.setting.threadDesc="Threads (started by &lt;cfthread>) currently running.">

<cfset stText.setting.scopes="Scopes in Memory">
<cfset stText.setting.scopesDesc="Scopes actually hold in Memory (a Scope not necessary is kept in Memory for it's hole life time).">
<cfset stText.setting.scopeSession="Session">
<cfset stText.setting.scopeApplication="Application">
<cfset stText.setting.scopeClient="Client">

<cfset stText.setting.cpu="CPU">
<cfset stText.setting.cpuDesc="Average CPU load of the last 20 seconds on the whole system and this Java Virtual Machine (Lucee Process).">
<cfset stText.setting.cpuProcess="Lucee Process">
<cfset stText.setting.cpuSystem="Whole System">



<cfset stText.setting.datasource="Datasource Connections">
<cfset stText.setting.datasourceDesc="Datasource Connection open at the Moment.">
<cfset stText.setting.task="Task Spooler">
<cfset stText.setting.taskDesc="Active and closed tasks in Task Spooler. This includes for exampe tasks to send mails.">
<cfset stText.setting.taskOpen="Open">
<cfset stText.setting.taskClose="Close">


				<cfset stText.Overview.resources="External Resources">
				<cfset stText.Overview.onlineDocsDesc="Lucee online documentation.">
				<cfset stText.Overview.localRefDesc="Local reference for tags, functions and components.">


<cfset pool['HEAP']="Heap">
<cfset pool['NON_HEAP']="Non-Heap">

<cfset pool['HEAP_desc']="Memory used for all objects that are allocated.">
<cfset pool['NON_HEAP_desc']="Memory used to store all cfc/cfm templates, java classes, interned Strings and meta-data.">

<cfset pool["Par Eden Space"]="The pool from which memory is initially allocated for most objects.">
<cfset pool["Par Survivor Space"]="The pool containing objects that have survived the garbage collection of the Eden space.">
<cfset pool["CMS Old Gen"]="The pool containing objects that have existed for some time in the survivor space.">
<cfset pool["CMS Perm Gen"]="The pool containing all the reflective data of the virtual machine itself, such as class and method objects.">
<cfset pool["Code Cache"]="The HotSpot Java VM also includes a code cache, containing memory that is used for compilation and storage of native code.">


<cfset pool["Eden Space"]=pool["Par Eden Space"]>
<cfset pool["PS Eden Space"]=pool["Par Eden Space"]>

<cfset pool["Survivor Space"]=pool["Par Survivor Space"]>
<cfset pool["PS Survivor Space"]=pool["Par Survivor Space"]>

<cfset pool["Perm Gen"]=pool["CMS Perm Gen"]>

<cfset pool["Tenured Gen"]=pool["CMS Old Gen"]>
<cfset pool["PS Old Gen"]=pool["CMS Old Gen"]>

<cffunction name="printBar" returntype="string">
	<cfargument name="used" type="numeric" required="yes">
	<cfargument name="comment" type="string" default="" required="false">
    <cfsavecontent variable="local.ret"><cfoutput>
			
			<div class="percentagebar tooltipMe"><!---
				---><div style="width:#used#%"><span>#used#%</span></div><!---
			---></div>
		<cfif len(comment)><div class="comment">#comment#</div></cfif>
	</cfoutput>
	</cfsavecontent>
	<cfreturn ret />
</cffunction>



<cffunction name="printMemory" returntype="string">
	<cfargument name="usage" type="query" required="yes">
	<cfargument name="showTitle" type="boolean" default="true" required="false">
    <cfset var height=12>
    <cfset var width=100>
	<cfset var used=evaluate(ValueList(arguments.usage.used,'+'))>
	<cfset var max=evaluate(ValueList(arguments.usage.max,'+'))>
	<cfset var init=evaluate(ValueList(arguments.usage.init,'+'))>

	<cfset var qry=QueryNew(arguments.usage.columnlist)>
	<cfset QueryAddRow(qry)>
    <cfset QuerySetCell(qry,"type",arguments.usage.type)>
    <cfset QuerySetCell(qry,"name",variables.pool[arguments.usage.type])>
    <cfset QuerySetCell(qry,"init",init,qry.recordcount)>
    <cfset QuerySetCell(qry,"max",max,qry.recordcount)>
    <cfset QuerySetCell(qry,"used",used,qry.recordcount)>
    <cfset arguments.usage=qry>
	<cfsavecontent variable="local.ret"><cfoutput>
			<cfif arguments.showTitle><b>#pool[usage.type]#</b></cfif>
		<cfloop query="usage">
   			<cfset local._used=int(width/arguments.usage.max*arguments.usage.used)>
    		<cfset local._free=width-_used>
			<cfset local.pused=int(100/arguments.usage.max*arguments.usage.used)>
   			<cfset local.pfree=100-pused>
    		#printBar(pused,pool[usage.type& "_desc"]?:'')#
		</cfloop>
	</cfoutput></cfsavecontent>
	<cfreturn ret />
</cffunction>




<cfset total=query(
	name:["Total"],
	type:[""],
	used:[server.java.totalMemory-server.java.freeMemory],
	max:[server.java.totalMemory],
	init:[0]
)>
<cfoutput>
	<div class="pageintro">
		#stText.Overview.introdesc[request.adminType]#
	</div>

	<cfadmin
		action="getInfo"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="info">

	<cfadmin
		action="getAPIKey"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="apiKey">

<cfadmin
	action="getCompilerSettings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="compiler">


<cfadmin
	action="getScope"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="scope">

<cfadmin
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	action="getPerformanceSettings"
	returnVariable="performance">

<cfadmin
	action="getContexts"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="contexts">

	<cfif request.adminType EQ "server">
		<cfset names=StructKeyArray(info.servlets)>
		<cfif !ArrayContainsNoCase(names,"Rest")>
			<div class="warning nofocus">
				The REST Servlet is not configured in your enviroment!
			</div>
		</cfif>
		<cfif listGetAt(server.java.version,2,'.') EQ 7>
			<div class="warning nofocus">
				Java 7 has been End-of-Life'd since April 2015. You should upgrade to Java 8 for performance and security reasons.
			</div>
		</cfif>
	</cfif>
	<cfset systemInfo=GetSystemMetrics()>

	<table>
		<tr>
			<div id="updateInfoDesc"><div style="text-align: center;"><img src="../res/img/spinner16.gif.cfm"></div></div>
				<cfhtmlbody>
					<script type="text/javascript">
						$( function() {

							$('##updateInfoDesc').load('update.cfm?#session.urltoken#&adminType=#request.admintype#');
						} );
					</script>
				</cfhtmlbody>
		</tr>
		<tr>
			<td valign="top" width="50%">

				<h2>#stText.setting.info#</h2>

				
				<!--- Memory --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th colspan="2" scope="row">
									#stText.setting.memory#<br>
									<span class="comment">#stText.setting.memoryDesc#</span>
								</th>
							</tr>
							<tr>
								<td width="50%"><b>#pool['heap']#</b><br>
									#printMemory(getmemoryUsage("heap"),false)#
								</td>
								<td width="50%"><b>#pool['non_heap']#</b><br>
									#printMemory(getmemoryUsage("non_heap"),false)#
								</td>
							</tr>

					</tbody>
				</table>
				
				<!--- CPU --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th colspan="3" scope="row">
									#stText.setting.cpu#<br>
									<span class="comment">#stText.setting.cpuDesc#</span>
								</th>
							</tr>
							<tr>
								<cfset nbr=int(systemInfo.cpuSystem*100)>
								<td width="50%"><b>#stText.setting.cpuSystem#</b><br>
								#printBar(nbr)#</td>
								<cfset nbr=int(systemInfo.cpuProcess*100)>
								<td width="50%"><b>#stText.setting.cpuProcess#</b><br>
								#printBar(nbr)#
								</td>
							</tr>

					</tbody>
				</table>
				
				<!--- Scopes --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th colspan="3" scope="row">
									#stText.setting.scopes#<br>
									<span class="comment">#stText.setting.scopesDesc#</span>
								</th>
							</tr>
							<tr>
								<td align="center" width="33%"><b>#stText.setting.scopeApplication#</b></td>
								<td align="center"><b>#stText.setting.scopeSession#</b></td>
								<td align="center" width="33%"><b>#stText.setting.scopeClient#</b></td>
							</tr>
							<tr>
								<td align="center">#systemInfo.applicationContextCount#</td>
								<td align="center">#systemInfo.sessionCount#</td>
								<td align="center">#systemInfo.clientCount#</td>
							</tr>

					</tbody>
				</table>
				
				<!--- Request --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th colspan="3" scope="row">
									#stText.setting.request#<br>
									<span class="comment">#stText.setting.requestDesc#</span>
								</th>
							</tr>
							<tr>
								<td align="center" width="33%"><b>#stText.setting.req#</b></td>
								<td align="center"><b>#stText.setting.queued#</b></td>
								<td align="center" width="33%"systemInfo.sessionCount><b>#stText.setting.thread#</b></td>
							</tr>
							<tr>
								<cfset nbr=systemInfo.activeRequests>
								<td align="center" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
								<cfset nbr=systemInfo.activeThreads>
								<td align="center" <cfif nbr GTE 20> style="color:##cc0000"</cfif>>#nbr#</td>
								<cfset nbr=systemInfo.queueRequests>
								<td align="center" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
							</tr>

					</tbody>
				</table>
				
				<!--- Datasource --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th scope="row">
									#stText.setting.datasource#<br>
									<span class="comment">#stText.setting.datasourceDesc#</span>
								</th>
							</tr>
							<tr>
								<cfset nbr=systemInfo.activeDatasourceConnections>
								<td align="center" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
							</tr>


					</tbody>
				</table>
				
				<!--- Tasks --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th colspan="3" scope="row">
									#stText.setting.task#<br>
									<span class="comment">#stText.setting.taskDesc#</span>
								</th>
							</tr>
							<tr>
								<td align="center"><b>#stText.setting.taskOpen#</b></td>
								<td align="center"><b>#stText.setting.taskClose#</b></td>
							</tr>
							<tr>
								<cfset nbr=systemInfo.tasksOpen>
								<td align="center" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
								<cfset nbr=systemInfo.tasksClosed>
								<td align="center" <cfif nbr GTE 20> style="color:##cc0000"</cfif>>#nbr#</td>
							</tr>

					</tbody>
				</table>
				
			</td>
			<td width="2%"></td>
			<td valign="top" width="50%">
				
				
					
				<!--- Info --->
				<h2>#stText.Overview.Info#</h2>
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Overview.Version#</th>
							<td>Lucee #server.lucee.version#</td>
						</tr>
						<cfif StructKeyExists(server.lucee,'versionName')>
							<tr>
								<th scope="row">#stText.Overview.VersionName#</th>
								<td><a href="#server.lucee.versionNameExplanation#" target="_blank">#server.lucee.versionName#</a></td>
							</tr>
						</cfif>
						<tr>
							<th scope="row">#stText.Overview.ReleaseDate#</th>
							<td>#lsDateFormat(server.lucee['release-date'])#</td>
						</tr>
						<tr>
							<th scope="row">#stText.Overview.CFCompatibility#</th>
							<td>#replace(server.ColdFusion.ProductVersion,',','.','all')#</td>
						</tr>
					</tbody>
				</table>

				<br />
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Overview.config#</th>
							<td>#info.config#</td>
						</tr>
						<cfif request.adminType EQ "web">
							<tr>
								<th scope="row">#stText.Overview.webroot#</th>
								<td>#info.root#</td>
							</tr>

							<cfadmin
								action="getTLDs"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								returnVariable="tlds">
							<cfadmin
								action="getFLDs"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								returnVariable="flds">

							<cfif isQuery(tlds)>
								<cfset tlds=listToArray(valueList(tlds.displayname))>
							</cfif>
							<cfif isQuery(flds)>
								<cfset flds=listToArray(valueList(flds.displayname))>
							</cfif>
						</cfif>

						<tr>
							<th scope="row">#stText.Overview.OS#</th>
							<td>#server.OS.Name# (#server.OS.Version#)<cfif structKeyExists(server.os,"archModel")> #server.os.archModel#bit</cfif></td>
						</tr>
						<tr>
							<th scope="row">#stText.Overview.remote_addr#</th>
							<td>#cgi.remote_addr#</td>
						</tr>
						<tr>
							<th scope="row">#stText.Overview.server_name#</th>
							<td>#cgi.server_name#</td>
						</tr>
						<tr>
							<th scope="row">#stText.overview.servletContainer#</th>
							<td>#server.servlet.name#</td>
						</tr>

						<cfif request.adminType EQ "web">
							<tr>
								<th scope="row">#stText.Overview.InstalledTLs#</th>
								<td>
									<cfloop index="idx" from="1" to="#arrayLen(tlds)#">
										- #tlds[idx]# <!--- ( #iif(tlds[idx].type EQ "cfml",de('lucee'),de('jsp'))# ) ---><br>
									</cfloop>
								</td>
							</tr>
							<tr>
								<th scope="row">#stText.Overview.InstalledFLs#</th>
								<td>
									<cfloop index="idx" from="1" to="#arrayLen(flds)#">
										- #flds[idx]#<br>
									</cfloop>
								</td>
							</tr>
							<!---
							<tr>
								<th scope="row">#stText.Overview.DateTime#</th>
								<td>
									#lsdateFormat(now())#
									#lstimeFormat(now())#
								</td>
							</tr>
							<tr>
								<th scope="row">#stText.Overview.ServerTime#</th>
								<td>

									#lsdateFormat(date:now(),timezone:"jvm")#
									#lstimeFormat(time:now(),timezone:"jvm")#
								</td>
							</tr>
							--->
						</cfif>
						<tr>
							<th scope="row">Java</th>
							<td>
								<!--- <cfset serverNow=createObject('java','java.util.Date')> --->
								#server.java.version# (#server.java.vendor#)<cfif structKeyExists(server.java,"archModel")> #server.java.archModel#bit</cfif>
							</td>
						</tr>
						<cfif StructKeyExists(server.os,"archModel") and StructKeyExists(server.java,"archModel")>
							<tr>
								<th scope="row">Architecture</th>
								<td>
									<cfif server.os.archModel NEQ server.os.archModel>OS #server.os.archModel#bit/JRE #server.java.archModel#bit<cfelse>#server.os.archModel#bit</cfif>
								</td>
							</tr>
						</cfif>
							<cfif request.adminType EQ "web">
							<tr>
								<th scope="row">#stText.Overview.label#</th>
								<td>#info.label#</td>
							</tr>
							<tr>
								<th scope="row">#stText.Overview.hash#</th>
								<td>#info.hash#</td>
							</tr>
						</cfif>
					</tbody>
				</table>

				

				<!---<h2>#stText.overview.langPerf#</h2>--->
				<table class="maintbl">
					<tbody>
							<tr>
								<th scope="row">#stText.setting.inspectTemplate#</th>
								<td <cfif performance.inspectTemplate EQ "always">style="color:##cc0000"</cfif>>
									<cfif performance.inspectTemplate EQ "never">
										#stText.setting.inspectTemplateNever#
									<cfelseif performance.inspectTemplate EQ "once">
										#stText.setting.inspectTemplateOnce#
									<cfelseif performance.inspectTemplate EQ "always">
										#stText.setting.inspectTemplateAlways#
									</cfif>
								</td>
							</tr>
							<tr>
								<th scope="row">#stText.compiler.nullSupport#</th>
								<td <cfif !compiler.nullSupport>style="color:##cc0000"</cfif>>
									<cfif compiler.nullSupport>
										#stText.compiler.nullSupportFull#
									<cfelse>
										#stText.compiler.nullSupportPartial#
									</cfif>
							</td>
							</tr>
							<tr>
								<th scope="row">#stText.setting.dotNotation#</th>
								<td <cfif compiler.DotNotationUpperCase>style="color:##cc0000"</cfif>>
									<cfif compiler.DotNotationUpperCase>#stText.setting.dotNotationUpperCase#<cfelse>#stText.setting.dotNotationOriginalCase#</cfif>
								</td>
							</tr>
							<!---<tr>
								<th scope="row">#stText.setting.suppressWSBeforeArg#</th>
								<td <cfif !compiler.suppressWSBeforeArg>style="color:##cc0000"</cfif>>#yesNoFormat(compiler.suppressWSBeforeArg)#</td>
							</tr> --->

							<tr>
								<th scope="row">#stText.Scopes.LocalMode#</th>
								<td <cfif scope.localMode EQ "classic">style="color:##cc0000"</cfif>>
									<cfif scope.localMode EQ "modern">#stText.Scopes.LocalModeModern#<cfelse>#stText.Scopes.LocalModeClassic#</cfif>
								</td>
							</tr>

					</tbody>
				</table>
				
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<br>
				<!--- Resources 
				<h2>#stText.Overview.resources#</h2>--->
				<table class="maintbl">
					<tbody>
						<!--- Prof Support --->
						<tr>
							<td>
								<a href="http://lucee.org/support.html" target="_blank">#stText.Overview.Professional#</a>
								<div class="comment">#stText.Overview.ProfessionalDesc#</div>
							</td>
						</tr>
						<!--- Doc --->
						<tr>
							<td>
								<a href="http://docs.lucee.org" target="_blank">#stText.Overview.onlineDocsLink#</a>
								<div class="comment">#stText.Overview.onlineDocsDesc#</div>
							</td>
						</tr>
						<!--- Reference --->
						<tr>
							<td>
								<a href="../doc/index.cfm" target="_blank">#stText.Overview.localRefLink#</a>
								<div class="comment">#stText.Overview.localRefDesc#</div>
							</td>
						</tr>
						<!--- Mailing List --->
						<tr>
							<td>
								<a href="http://groups.google.com/group/lucee" target="_blank">#stText.Overview.Mailinglist#</a>
								<div class="comment">#stText.Overview.MailinglistDesc#</div>
							</td>
						</tr>
						<!--- Jira --->
						<tr>
							<td>
								<a href="http://issues.lucee.org/" target="_blank">#stText.Overview.issueTracker#</a>
								<div class="comment">#stText.Overview.issueTrackerDesc#</div>
							</td>
						</tr>
						<!--- Blog --->
						<tr>
							<td>
								<a href="http://blog.lucee.org/" target="_blank">#stText.Overview.blog#</a>
								<div class="comment">#stText.Overview.blogDesc#</div>
							</td>
						</tr>
						<!--- Twitter --->
						<tr>
							<td>
								<a href="https://twitter.com/##!/lucee_server" target="_blank">#stText.Overview.twitter#</a>
								<div class="comment">#stText.Overview.twitterDesc#</div>
							</td>
						</tr>
					</tbody>
				</table>
			</td>
		</tr>
	</table>

	<cfif request.admintype EQ "server">

		<h2>#stText.Overview.contexts.title#</h2>
		<div class="itemintro">
			You can label your web contexts here, so they are more clearly distinguishable for use with extensions etc.
		</div>
		<cfformClassic onerror="customError" action="#request.self#" method="post">
			<table class="maintbl">
				<thead>
					<tr>
						<th width="15%">#stText.Overview.contexts.label#</th>
						<th width="25%">#stText.Overview.contexts.url#</th>
						<th width="30%">#stText.Overview.contexts.webroot#</th>
						<th width="30%">#stText.Overview.contexts.config_file#</th>
					</tr>
				</thead>
				<tbody>
					<cfset rst=contexts>
					<cfloop query="contexts">
						<tr>
							<td>
								<input type="hidden" name="hash_#rst.currentrow#" value="#rst.hash#"/>
								<input type="text" style="width:99%" name="label_#rst.currentrow#" value="#rst.label#"/>
							</td>
							<td><cfif len(rst.url)><a target="_blank" href="#rst.url#/lucee/admin/web.cfm">#rst.url#</a></cfif></td>
							<td><input type="text" class="xlarge" name="path_#rst.currentrow#" value="#rst.path#" readonly="readonly"/></td>
							<td><input type="text" class="xlarge" style="width:99%" name="cf_#rst.currentrow#" value="#rst.config_file#" readonly="readonly"/></td>
						</tr>

						<cfset filesThreshold = 100000>
						<cfif ( contexts.clientElements GT filesThreshold ) || ( contexts.sessionElements GT filesThreshold )>

							<tr>
								<td colspan="4" style="background-color:##FCC;" align="center">
									Warning:
									<cfif ( contexts.clientElements GT filesThreshold )>
										<b>#numberFormat( contexts.clientElements, "," )#</b> Client files
									</cfif>
									<cfif ( contexts.sessionElements GT filesThreshold )>
										<b>#numberFormat( contexts.sessionElements, "," )#</b> Session files
									</cfif>
								</td>
							</tr>
						</cfif>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4">
							<input class="button submit" type="submit" name="mainAction" value="#stText.Buttons.Update#">
							<input class="button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>
</cfoutput>
