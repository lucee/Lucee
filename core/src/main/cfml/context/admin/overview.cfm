<!---
Defaults --->

<cfif structKeyExists(form,"adminMode")>		
	<cfadmin
		action="updateAdminMode"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		mode="#form.adminMode#"
		merge="#!isNull(form.switch) && form.switch=="merge"#"
		keep="#!isNull(form.keep)#">
	<cflocation url="#request.self#?action=#url.action#" addtoken=false>
</cfif>


<cfset current.label = "Lucee " & server.lucee.version & " - " & current.label>
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
<!---
Redirect to entry --->
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
    <script src="../res/js/echarts-all.js.cfm" type="text/javascript"></script>
    <script type="text/javascript">
    	var chartTimer;
    	labels={'heap':"Heap",'nonheap':"Non-Heap",'cpuSystem':"Whole System",'cpuProcess':"Lucee Process"};
		function requestData(){
			jQuery.ajax({
				type: "POST",
				<cfoutput>url: "./#request.self#?action=chartAjax",</cfoutput>
				success: function(result){
					var arr =["heap","nonheap"];
					$.each(arr,function(index,chrt){
						window["series_"+chrt] = window[chrt+"Chart"].series[0].data; //*charts*.series[0].data
						window["series_"+chrt].push(result[chrt]); // push the value into series[0].data
						window[chrt+"Chart"].series[0].data = window["series_"+chrt];
						if(window[chrt+"Chart"].series[0].data.length > 60){
						window[chrt+"Chart"].series[0].data.shift(); //shift the array
						}
						window[chrt+"Chart"].xAxis[0].data.push(new Date().toLocaleTimeString()); // current time
						if(window[chrt+"Chart"].xAxis[0].data.length > 60){
						window[chrt+"Chart"].xAxis[0].data.shift(); //shift the Time value
						}
						window[chrt].setOption(window[chrt+"Chart"]); // passed the data into the chats
					});
					var arr2 =["cpuSystem"];
					$.each(arr2,function(index,chrt){
						cpuSystemSeries1 = cpuSystemChartOption.series[0].data; //*charts*.series[0].data
						cpuSystemSeries1.push(result["cpuSystem"]); // push the value into series[0].data
						cpuSystemSeries2 = cpuSystemChartOption.series[1].data; //*charts*.series[0].data
						cpuSystemSeries2.push(result["cpuProcess"]); // push the value into series[0].data
						cpuSystemChartOption.series[0].data = cpuSystemSeries1;
						cpuSystemChartOption.series[1].data = cpuSystemSeries2;
						if(cpuSystemChartOption.series[0].data.length > 60){
							cpuSystemChartOption.series[0].data.shift(); //shift the array
						}
						if(cpuSystemChartOption.series[1].data.length > 60){
							cpuSystemChartOption.series[1].data.shift(); //shift the array
						}
						cpuSystemChartOption.xAxis[0].data.push(new Date().toLocaleTimeString()); // current time
						if(cpuSystemChartOption.xAxis[0].data.length > 60){
						cpuSystemChartOption.xAxis[0].data.shift(); //shift the Time value
						}
						window[chrt].setOption(cpuSystemChartOption); // passed the data into the chats
					});
					if (chartTimer !== null)
						chartTimer = setTimeout(requestData, 5000);
				}
			})
		}
		var dDate=[new Date().toLocaleTimeString()]; // current time


		// intialize charts
		$.each(["heap","nonheap"], function(i, data){
			window[data] = echarts.init(document.getElementById(data),'macarons'); // intialize echarts
			window[data+"Chart"] = {
				backgroundColor: ["#EFEDE5"],
				tooltip : {'trigger':'axis',
					formatter : function (params) {
						return 'Series' + "<br>" + params[0].seriesName + ": " + params[0].value + "%" + '<br>' +params[0].name ;
					}
				},

				color: ["<cfoutput>#request.adminType EQ "server" ? '##3399CC': '##BF4F36'#</cfoutput>"],
				grid : {
					width: '82%',
					height: '65%',
					x:'30px',
					y:'25px'
				},
				xAxis : [{
					'type':'category',
					'boundaryGap' : false,
					'data':[0]
				}],
				yAxis : [{
					'type':'value',
					'min':'0',
					'max':'100',
					'splitNumber': 2
				}],
				series : [
					{
					'name': labels[data] +' Memory',
					'type':'line',
					smooth:true,
					itemStyle: {normal: {areaStyle: {type: 'default'}}},
					'data': [0]
					}
				]
			}; // data
			window[data].setOption(window[data+"Chart"]); // passed the data into the chats
		});

		var cpuSystem = echarts.init(document.getElementById('cpuSystem'),'macarons'); // intialize echarts
		var cpuSystemChartOption = {
			backgroundColor: ["#EFEDE5"],
			tooltip : {'trigger':'axis',
				formatter : function (params) {
					var series2 = "";
					if(params.length == 2) {
						series2 =  params[1].seriesName + ": "+ params[1].value + "%" + '<br>' +params[0].name;
					}
					return 'Series' + "<br>" + params[0].seriesName + ": " + params[0].value + "%" + '<br>'  + series2;
				}
			},
			legend: {
				data:['System CPU', 'Lucee CPU']
			},

			color: ["<cfoutput>#request.adminType EQ "server" ? '##3399CC': '##BF4F36'#</cfoutput>", "<cfoutput>#request.adminType EQ "server" ? '##BF4F36': '##3399CC'#</cfoutput>"],
			grid : {
				width: '82%',
				height: '65%',
				x:'30px',
				y:'25px'
			},
			xAxis : [{
				'type':'category',
				'boundaryGap' : false,
				'data':[0]
			}],
			yAxis : [{
				'type':'value',
				'min':'0',
				'max':'100',
				'splitNumber': 2
			}],
			series : [
				{
				'name': 'System CPU',
				'type':'line',
				smooth:true,
				itemStyle: {normal: {areaStyle: {type: 'default'}}},
				'data': [0]
				},
				{
				'name': 'Lucee CPU',
				'type':'line',
				smooth:true,
				itemStyle: {normal: {areaStyle: {type: 'default'}}},
				'data': [0]
				}

			]
		}; // data
		// console.log(cpuSystemChartOption);
		cpuSystem.setOption(cpuSystemChartOption); // passed the data into the chats
        requestData();
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
<cfadmin
    action="getExtensions"
    type="server"
    password="#session["password"&request.adminType]#"
    returnVariable="docsServer">
<cfadmin
    action="getExtensions"
    type="web"
    password="#session["password"&request.adminType]#"
    returnVariable="docsWeb">

	<cfif request.adminType EQ "server">
		<cfset names=StructKeyArray(info.servlets)>
		<cfif !ArrayContainsNoCase(names,"Rest",true)>
			<div class="warning nofocus">
				#stText.Overview.warning.warningMsg# 
			</div>
		</cfif>
		<cfif getJavaVersion() LT 8>
			<div class="warning nofocus">
				#stText.Overview.warning.JavaVersion# #server.java.version# #stText.Overview.warning.JavaVersionNotSupport#
				<cfif getJavaVersion() EQ 7>
					#stText.Overview.warning.Java7NotSupport# 
				</cfif>
			</div>
		</cfif>
	</cfif>

	<cfset systemInfo=GetSystemMetrics()>


	<!--- installed libs --->
	<cfif request.adminType EQ "web">	
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




	<cfset stText.Overview.modeMulti="You are in Multi Mode">
	<cfset stText.Overview.modeSingle="You are in Single Mode">
	<cfset stText.Overview.modeMultiDesc="You are running Lucee in Multi Mode, this means you have a single Server Administrator where you can set settings for all web contexts/webs and a Web Administrator for every single web context/web.">
	<cfset stText.Overview.modeSingleDesc="You are running Lucee in Single Mode, this means you only have a single Administrator, one place where you do all your configurations for all web contexts/webs.
"> 
	
	<cfset stText.Overview.modeMultiSwitch="Switch to Single Mode?">
	<cfset stText.Overview.modeSingleSwitch="Switch to Multi Mode?">
	<cfset stText.Overview.modeMultiSwitchDesc="You wanna activate Lucee in Single Mode, this means you only have a single Administrator, one place where you do all your configurations for all web contexts/webs.">
	<cfset stText.Overview.modeSingleSwitchDesc="You wanna activate Multi Mode, mean having a Server Administrator where you can set settings for all web contexts/webs and a Web Administrator for every single web context/web, you can simply switch to Multi Mode here.">


	<cfset stText.Overview.switchMerge="Merge and Switch">
	<cfset stText.Overview.switchMergeDesc="All settings from all web contexts/webs get stored into the server context">
	<cfset stText.Overview.switchLeave="Just Switch">
	<cfset stText.Overview.switchLeaveDesc="Switch to single mode and forget all settings done in all web contexts/webs">
	
	<cfset stText.Overview.switchKeep="keep all web context/web configuration in place, so i can go back to multi mode">


	<cfset stText.Buttons.switch="Switch">


<cfif request.adminType=="server">
	
		
	<cfformClassic onerror="customError" action="#request.self#?action=overview" method="post">
		<input type="hidden" name="adminMode" value="#request.singlemode?"multi":"single"#">
		<h2>#stText.Overview[request.singlemode?"modeSingle":"modeMulti"]#</h2>
		<div class="itemintro">#stText.Overview[request.singlemode?"modeSingleDesc":"modeMultiDesc"]#</div>
		<table class="maintbl">
		<tbody>
			<tr>
				<th scope="row">
					<h4>#stText.Overview[request.singlemode?"modeSingleSwitch":"modeMultiSwitch"]#</h4>
#stText.Overview[request.singlemode?"modeSingleSwitchDesc":"modeMultiSwitchDesc"]#
				</th>
				<cfif !request.singleMode>
	
				<td>
					<ul class="radiolist" id="sp_options">
						<li>
							<label>
								<input type="radio" class="radio" name="switch" value="merge" checked="checked">
								<b>#stText.Overview.switchMerge#</b>
							</label>
							<div class="comment">#stText.Overview.switchMergeDesc#</div>
						</li>
						<li>
							<label>
								<input type="radio" class="radio" name="switch" value="leave">
								<b>#stText.Overview.switchLeave#</b>
							</label>
							<div class="comment">#stText.Overview.switchLeaveDesc#</div>
						</li>
					</ul>
					<br><br>
					<input type="checkbox" class="checkbox" name="keep" value="keep" checked="checked">
					<div class="comment">#stText.Overview.switchKeep#</div>
				</td>
				</cfif>
			</tr>
		</tbody>
		<tfoot>
			<tr>
				<td colspan="2">
					<input type="submit" class="b button submit" name="mainAction1" value="#stText.Buttons.switch#">
				</td>
			</tr>
		</tfoot>
		</table>
</cfformClassic>

</cfif>






	<table>
		<tr>
			<div id="updateInfoDesc"><div style="text-align: center;"><img src="../res/img/spinner16.gif.cfm"></div></div>
			<cfhtmlbody>
				<script type="text/javascript">
					$( function() {
						$('##updateInfoDesc').load('?action=update&adminType=#request.admintype#');
					} );
				</script>
			</cfhtmlbody>
		</tr>

		<tr>
			<td valign="top" colspan="3">
				<h2>#stText.setting.info#</h2>
			<table width="100%"><tr><td>
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
								<div id="nonheap" style="min-width: 100px; height: 150px; margin: 0 auto;"></div>
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
									<div id="cpuSystem" style="min-width: 100px; height: 150px; margin: 0 auto;"></div>
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
				<!--- Requests/Threads ---->
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
				<!--- Datasource ---->
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
				<!--- Tasks ---->
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
				</td></tr></table>
			</td>
		</tr>


		<cfadmin
		action="getMinVersion"
		type="server"
		password="#session["password"&request.adminType]#"
		returnVariable="minVersion">
		<tr>
			<td valign="top" colspan="3">
				<br>
				<!--- Info --->
				<h2>#stText.Overview.Info#</h2>
				<table width="100%">
				<tr>
					<td width="50%">
						<table class="maintbl">
							<tbody>

								<tr>
									<th scope="row">#stText.Overview.Version#</th>
									<td>Lucee #server.lucee.version#</td>
								</tr>
								<tr>
									<th scope="row">#stText.Overview.VersionName#</th>
									<td><a href="#server.lucee.versionNameExplanation#" target="_blank">#server.lucee.versionName#</a></td>
								</tr>
								<tr>
									<th nowrap="nowrap" scope="row">#stText.Overview.ReleaseDate#</th>
									<td>#lsDateFormat(server.lucee['release-date'])#</td>
								</tr>
								<cfif !request.singleMode && request.adminType EQ "web">
								<tr>
									<th nowrap="nowrap" scope="row">#stText.Overview.label#</th>
									<td>#info.label#</td>
								</tr>
								</cfif>

								<cfif request.adminType EQ "web">
								<tr>
									<th nowrap="nowrap" scope="row">#stText.Overview.InstalledTLs#</th>
									<td>
										<cfloop index="idx" from="1" to="#arrayLen(tlds)#">
											- #tlds[idx]# <!--- ( #iif(tlds[idx].type EQ "cfml",de('lucee'),de('jsp'))# ) ---><br>
										</cfloop>
									</td>
								</tr>
								</cfif>
								<cfif request.adminType EQ "web">
								<tr>
									<th nowrap="nowrap" scope="row">#stText.Overview.InstalledFLs#</th>
									<td>
										<cfloop index="idx" from="1" to="#arrayLen(flds)#">
											- #flds[idx]#<br>
										</cfloop>
									</td>
								</tr>
								</cfif>

							</tbody>
						</table>
					</td>
					<td width="50%">
						<table class="maintbl">
							<tbody>
								<tr>
									<th scope="row">#stText.Overview.remote_addr#</th>
									<td>#cgi.remote_addr#</td>
								</tr>
								<tr>
									<th scope="row">Loader Version</th>
									<td>#minversion#</td>
								</tr>
								<tr>
									<th scope="row">#stText.overview.servletContainer#</th>
									<td>#server.servlet.name#</td>
								</tr>
								<tr>
									<th scope="row">Java</th>
									<td>
										<!--- <cfset serverNow=createObject('java','java.util.Date')> --->
										#server.java.version# (#server.java.vendor#)<cfif structKeyExists(server.java,"archModel")> #server.java.archModel#bit</cfif>
									</td>
								</tr>
								<tr>
									<th scope="row">#stText.Overview.server_name#</th>
									<td>#cgi.server_name#</td>
								</tr>
								<tr>
									<th scope="row">#stText.Overview.OS#</th>
									<td>#server.OS.Name# (#server.OS.Version#)<cfif structKeyExists(server.os,"archModel")> #server.os.archModel#bit</cfif></td>
								</tr>
								<tr>
									<th scope="row">Architecture</th>
									<td>
									<cfif StructKeyExists(server.os,"archModel") and StructKeyExists(server.java,"archModel")>
									<cfif server.os.archModel NEQ server.os.archModel>OS #server.os.archModel#bit/JRE #server.java.archModel#bit<cfelse>#server.os.archModel#bit</cfif>
									</cfif>
									</td>
								</tr>
								<cfif !request.singleMode && request.adminType EQ "web">
									<tr>
										<th scope="row">#stText.Overview.hash#</th>
										<td>#info.hash#</td>
									</tr>
								</cfif>

							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td width="50%">
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
									<th scope="row">#stText.setting.dotNotation#</th>
									<td <cfif compiler.DotNotationUpperCase>style="color:##cc0000"</cfif>>
										<cfif compiler.DotNotationUpperCase>#stText.setting.dotNotationUpperCase#<cfelse>#stText.setting.dotNotationOriginalCase#</cfif>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
					<td width="50%">
						<table class="maintbl">
							<tbody>
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
					<td colspan="2">
						<table class="maintbl">
							<tbody>
								<tr>
								</tr>
								<tr>
									<th scope="row">#stText.Overview.config#</th>
									<td>#info.config#</td>
								</tr>
								<cfif !isNull(info.root)>
								<tr>
									<th scope="row">#stText.Overview.webroot#</th>
									<td>#info.root?:''#</td>
								</tr>
								</cfif>
							</tbody>
						</table>
					</td>
				</tr>
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
								<a href="https://lucee.org/support.html" target="_blank">#stText.Overview.Professional#</a>
								<div class="comment">#stText.Overview.ProfessionalDesc#</div>
							</td>
						</tr>
						<!--- Doc --->
						<tr>
							<td>
								<a href="https://docs.lucee.org" target="_blank">#stText.Overview.onlineDocsLink#</a>
								<div class="comment">#stText.Overview.onlineDocsDesc#</div>
							</td>
						</tr>
						<!--- Reference --->
						<tr>
							<td>
								<cfif Listfind(valueList(docsServer.name),"Lucee Documentation") eq 0 && Listfind(valueList(docsWeb.name),"Lucee Documentation") eq 0>
									<a href="#cgi.script_name#?action=ext.applications" title="#stText.overview.installDocsLink#">
										#stText.overview.installDocsLink#</a>
								<cfelse>
									<a href="../doc/index.cfm" target="_blank">#stText.Overview.localRefLink#</a>
								</cfif>
								<div class="comment">#stText.Overview.localRefDesc#</div>
							</td>
						</tr>
						<!--- Mailing List --->
						<tr>
							<td>
								<a href="https://groups.google.com/group/lucee" target="_blank">#stText.Overview.Mailinglist#</a>
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
								<a href="http://blog.lucee.org" target="_blank">#stText.Overview.blog#</a>
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
			#stText.Overview.contexts.desc# 
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
							<td>#rst.path#</td>
							<td>#rst.config_file#</td>
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
		if(verArr[1]>2) {
			return verArr[1];
		} else if (verArr.len() GT 1) {
			return verArr[2];
		} else {
		    return val(server.java.version);
		}
	}
</cfscript>
