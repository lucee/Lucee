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
<cfhtmlbody>
	<script src="../res/js/highChart.js.cfm" type="text/javascript"></script>
	<script>
		function requestData(){
			$.ajax({
				type: "POST",
				url: "./server.cfm?action=chartAjax",
				success: function(data){
					var series_heap = heapchart.series[0];
					var series_nonheap = nonheapchart.series[0];
					var series_cpuSystem = cpuSystemChart.series[0];
					var series_cpuProcess = cpuProcessChart.series[0];
					var shift_heap = series_heap.data.length > 100; // shift the chart if more than 100 entries available
					var shift_nonheap = series_nonheap.data.length > 100; // shift the chart if more than 100 entries available
					var shift_cpuSystem = series_cpuSystem.data.length > 100; // shift the chart if more than 100 entries available
					var shift_cpuProcess = series_cpuProcess.data.length > 100; // shift the chart if more than 100 entries available
					var x = (new Date()).getTime(); // current time
					var x = (new Date()).getTime(); // current time
					var y_heap = data.heap;
					var y_nonheap = data.non_heap;
					var y_cpuSys = data.cpuSystem;
					var y_cpuProcess = data.cpuProcess;
					heapchart.series[0].addPoint([x, y_heap], true, shift_heap);
					nonheapchart.series[0].addPoint([x, y_nonheap], true, shift_nonheap);
					cpuSystemChart.series[0].addPoint([x, y_cpuSys], true, shift_cpuSystem);
					cpuProcessChart.series[0].addPoint([x, y_cpuProcess], true, shift_cpuProcess);
					setTimeout(requestData, 1000);
				}
			})
		}
		$(document).ready(function() {
			Highcharts.setOptions({
			    global: {
			        useUTC: false
			    }
			});

			function initiateNewChart(cName, cType, sName){
				return Highcharts.chart(cName, {
					chart: {
						type: cType,
						animation: Highcharts.svg,
						marginRight: 10,
						marginBottom: 20,
						backgroundColor: "#EFEDE5"
					},
					plotOptions: {
						series: {
							marker: {
								enabled: false
							}
						}
					},
					colors: ["<cfoutput>#request.adminType EQ "server" ? '##3399CC': '##BF4F36'#</cfoutput>"],
					title: {
						text: ""
					},
					xAxis: {
						type: 'datetime',
						tickPixelInterval: 150,
						labels: {
							y: 15
						}
					},
					yAxis: {
						min: 0,
   						max: 100,
						title: {
							text: ""
						},
						labels: {
							formatter: function() {
								return this.value + "%";
							}
						},
						plotLines: [{
							value: 0,
							width: 15
						}]
					},
					tooltip: {
						formatter: function () {
							return '<b>' + this.series.name + '</b><br/>' +
							Highcharts.dateFormat('%H:%M:%S', this.x) + '<br/>' +
							Highcharts.numberFormat(this.y, 0)+"%";
						}
					},
					legend: {
						enabled: false
					},
					exporting: {
						enabled: false
					},
					credits: {
						enabled: false
					},
					series: [{
						name: sName,
						data: []
					}]
				});
			}

			// charts
			heapchart = initiateNewChart("heap", "areaspline", "HeapMemorySeries");
			nonheapchart = initiateNewChart("nonheap", "areaspline",  "Non-HeapSeries");
			cpuSystemChart = initiateNewChart("cpuSystem", "areaspline", "WholeSystemSeries");
			cpuProcessChart = initiateNewChart("cpuProcess", "areaspline", "luceeProcessSeries");

			// initiating the ajax data get process
			requestData();
		});
	</script>
</cfhtmlbody>

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
		<cfif getJavaVersion() LT 8>
			<div class="warning nofocus">
				You are running Lucee with Java #server.java.version# Lucee does not formally support this version of Java. Consider updating to the latest Java version for security and performance reasons.
				<cfif getJavaVersion() EQ 7>
					Java 7 has been End-of-Life'd since April 2015.
				</cfif>
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
			<td valign="top" colspan="3">

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
			<td valign="top" colspan="3">
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
							<td width="50%"><b>#pool['heap']#</b>
								<div id="heap" style="min-width: 100px; height: 150px; margin: 0 auto;"></div>
							</td>
							<td width="50%"><b>#pool['non_heap']#</b><br>
								<div id="nonheap" style="min-width: 100px; height: 150px; margin: 0 auto"></div>
							</td>
						</tr>

					</tbody>
				</table>
				
				<!--- CPU --->
				<table class="maintbl">
					<tbody>
							<tr>
								<th colspan="2" scope="row">
									#stText.setting.cpu#<br>
									<span class="comment">#stText.setting.cpuDesc#</span>
								</th>
							</tr>
							<tr>
								<td width="50%"><b>#stText.setting.cpuSystem#</b>
									<div id="cpuSystem" style="min-width: 100px; height: 150px; margin: 0 auto"></div>
								</td>
								<td width="50%"><b>#stText.setting.cpuProcess#</b><br>
									<div id="cpuProcess" style="min-width: 100px; height: 150px; margin: 0 auto"></div>
								</td>
							</tr>

					</tbody>
				</table>
				<br>
				<!--- Scopes --->
				<table class="maintbl">
					<tbody>
						<tr>
							<th rowspan="3" scope="row" style="width:50%">
								#stText.setting.scopes#<br>
								<span class="comment">#stText.setting.scopesDesc#</span>
							</th>
							<td style="width:35%"><b>#stText.setting.scopeApplication#</b></td>
							<td align="right" style="width:15%">#systemInfo.applicationContextCount#</td>
						</tr>
						<tr>
							<td><b>#stText.setting.scopeSession#</b></td>
							<td align="right">#systemInfo.sessionCount#</td>
						</tr>
						<tr>
							<td><b>#stText.setting.scopeClient#</b></td>
							<td align="right">#systemInfo.clientCount#</td>
						</tr>

					</tbody>
				</table>
				<table class="maintbl">
					<tbody>

						<tr>
							<th rowspan="3" scope="row" style="width:50%">
								#stText.setting.request#<br>
								<span class="comment">#stText.setting.requestDesc#</span>
							</th>
							<td style="width:35%"><b>#stText.setting.req#</b></td>
							<cfset nbr=systemInfo.activeRequests>
							<td align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
						</tr>
						<tr>
							<td><b>#stText.setting.queued#</b></td>
							<cfset nbr=systemInfo.activeThreads>
							<td align="right" <cfif nbr GTE 20> style="color:##cc0000"</cfif>>#nbr#</td>
						</tr>
						<tr>
							<td><b>#stText.setting.thread#</b></td>
							<cfset nbr=systemInfo.queueRequests>
							<td align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
						</tr>

					</tbody>
				</table>
				<table class="maintbl">
					<tbody>

						<tr>
							<th scope="row" style="width:50%">
								#stText.setting.datasource#<br>
								<span class="comment">#stText.setting.datasourceDesc#</span>
							</th>
							<cfset nbr=systemInfo.activeDatasourceConnections>
							<td style="width:35%">&nbsp;</td>
							<td align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
						</tr>

					</tbody>
				</table>
				<table class="maintbl">
					<tbody>

						<tr>
							<th rowspan="2" scope="row" style="width:50%">
								#stText.setting.task#<br>
								<span class="comment">#stText.setting.taskDesc#</span>
							</th>
							<td style="width:35%"><b>#stText.setting.taskOpen#</b></td>
							<cfset nbr=systemInfo.tasksOpen>
							<td align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
						</tr>
						<tr>
							<td><b>#stText.setting.taskClose#</b></td>
							<cfset nbr=systemInfo.tasksClosed>
							<td align="right" <cfif nbr GTE 20> style="color:##cc0000"</cfif>>#nbr#</td>
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
<cfscript>
	function getJavaVersion() {
		var verArr=listToArray(server.java.version,'.');
		if(verArr[1]>2) return verArr[1];
		return verArr[2];
	}
</cfscript>