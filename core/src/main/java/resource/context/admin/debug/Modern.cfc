<cfcomponent extends="Debug" output="no">
	<cfscript>
	  fields=array(
group("Debugging Tab","Debugging tag includes execution time,Custom debugging output",2)
,field("Debugging","Tab_Debug","Enabled",true,"Select the Debugging tab to show on DebugOutput","checkbox","Enabled")
,field("Minimal Execution Time","minimal","0",true,{_appendix:"microseconds",_bottom:"Execution times for templates, includes, modules, custom tags, and component method calls. Outputs only templates taking longer than the time (in microseconds) defined above."},"text40")
,field("Highlight","highlight","250000",true,{_appendix:"microseconds",_bottom:"Highlight templates taking longer than the following (in microseconds) in red."},"text50")
,field("Expression","expression","Enabled",false,"Enter expression to evaluate","checkbox","Enabled")
,field("General Debug Information ","general","Enabled",false,
"Select this option to show general information about this request. General items are Lucee Version, Template, Time Stamp, User Locale, User Agent, User IP, and Host Name. ",
"checkbox","Enabled")
,field("Display callstack","callStack","Enabled",false,"Display callstack for templates","checkbox","Enabled")
,field("Display percentages","displayPercentages","Enabled",false,"Display percentages for each entry","checkbox","Enabled")
,field("Highlight colored","colorHighlight","Enabled",true,"Color the output based on the overall percentages","checkbox","Enabled")
,field("Warn Session size","sessionSize","100",true,{_appendix:"KB",_bottom:"Warn in debugging, if the current session is above the following (in KB) size."},"text50")
,group("Metrics Tab","",2)
,field("Metrics","tab_Metrics","Enabled",true,"Select the Metrics tab to show on debugOutput","checkbox","Enabled")
,field("Charts","metrics_charts","HeapChart,NonHeapChart,WholeSystem,LuceeProcess",false,"Select the chart to show on metrics Tab. It will show only if the metrics tabs is enabled","checkbox","HeapChart,NonHeapChart,WholeSystem,LuceeProcess")
,group("Reference Tab","",2)
,field("Reference","tab_Reference","Enabled",true,"Select the Reference tab to show on DebugOutput","checkbox","Enabled")
);
		string function getLabel(){
			return "Modern";
		}

		string function getDescription(){
			return "The new style debug template, with extended functionality";
		}

		string function getid(){
			return "lucee-modern-extended";
		}

		void function onBeforeUpdate(struct custom){
			throwWhenNotNumeric(custom,"minimal");
			throwWhenNotNumeric(custom,"highlight");
		}

		private void function throwWhenEmpty(struct custom, string name){
			if(!structKeyExists(custom,name) or len(trim(custom[name])) EQ 0)
			throw "value for ["&name&"] is not defined";
		}

		private void function throwWhenNotNumeric(struct custom, string name){
			throwWhenEmpty(arguments.custom, arguments.name);
			if(!isNumeric(trim(arguments.custom[arguments.name])))
			throw "value for [" & arguments.name & "] must be numeric";
		}


		private function isColumnEmpty(query qry,string columnName){
			if(!QueryColumnExists(qry,columnName)) return true;
			return !len(arrayToList(queryColumnData(qry,columnName),""));
		}

		function isSectionOpen( string name, string section = "debugging" ) {
			try{
			if ( arguments.name == "ALL" && !structKeyExists( Cookie, variables["cookieName_" & section] ) )
				return true;

			var cookieValue = structKeyExists( Cookie, variables["cookieName_" & section] ) ? Cookie[ variables["cookieName_" & section] ] : 0;

			return cookieValue && ( bitAnd( cookieValue, this.allSections[ arguments.name ] ) );
			}
			catch(e){
				return false;
			}
		}

		function isEnabled( custom, key ) {
			return structKeyExists( arguments.custom, arguments.key ) && ( arguments.custom[ arguments.key ] == "Enabled" || arguments.custom[ arguments.key ] == "true" );
		}

		variables.cookieName_debugging   = "lucee_debug_modern";
		variables.cookieSortOrder       = "lucee_debug_modern_sort";
		variables.cookieFilterTemplates = "lucee_debug_modern_filter";
		variables.cookieExpressions     = "LUCEE_DEBUG_MODERN_EXPRESSIONS";
		variables.cookieName_metrics = "lucee_metrics_modern";
		variables.cookieName_docs = "lucee_docs_modern";

		function buildSectionStruct() {
			var otherSections = [ "ALL", "Dump", "ExecTime", "ExecOrder", "Exceptions", "ImpAccess", "Info", "Query", "Timer", "Trace", "More", "Expression", "memChart", "cpuChart", "docs_Info", "tags", "functions", "components", "scopesInMemory", "request_Threads", "datasource_connection", "task_Spooler" ];
			var i = 0;
			var result = {};
			for ( var k in otherSections )
				result[ k ] = 2 ^ i++;
			return result;
		}
	</cfscript>

	<cffunction name="output" returntype="void">
		<cfargument name="custom" required="true" type="struct" />
		<cfargument name="debugging" required="true" type="struct" />
		<cfargument name="context" type="string" default="web" />
		<cfparam name="arguments.custom.size" default="medium">
		<cfset this.allSections  = this.buildSectionStruct()>
		<cfset variables.tabsPresent = "">
		<cfloop list="#structKeyList(arguments.CUSTOM)#" index="i">
			<cfif i EQ "Tab_Debug">
				<cfset variables.tabsPresent = listAppend(variables.tabsPresent, "debug")>
			<cfelseif i EQ "tab_Metrics">
				<cfset variables.tabsPresent = listAppend(variables.tabsPresent, "metrics")>
			<cfelseif i EQ "tab_Reference">
				<cfset variables.tabsPresent =  listAppend(variables.tabsPresent, "reference")>
			</cfif>
		</cfloop>
		<cfset variables.chartStr = {}>
		<cfloop list="#arguments.custom.metrics_Charts#" index="i">
			<cfif i EQ "HeapChart">
				<cfset variables.chartStr[i] = "heap">
			<cfelseif i EQ "NonHeapChart">
				<cfset variables.chartStr[i] = "nonheap">
			<cfelseif i EQ "WholeSystem">
				<cfset variables.chartStr[i] = "cpuSystem">
			<cfelseif i EQ "LuceeProcess">
				<cfset variables.chartStr[i] = "cpuProcess">
			</cfif>
		</cfloop>

		<cfset variables.tbsStr = {}>
		<cfloop list="#variables.tabsPresent#" index="i">
			<cfif i EQ "Debug">
				<cfset variables.tbsStr[i] = "debugging">
			<cfelseif i EQ "metrics">
				<cfset variables.tbsStr[i] = "metrics">
			<cfelseif i EQ "reference">
				<cfset variables.tbsStr[i] = "docs">
			</cfif>
		</cfloop>
		<script>
			<cfset this.includeFileInline( "/lucee/res/js/jquery-1.12.4.min.js" )>
		</script>
		<style>
			<cfset this.includeFileInline( "/lucee/res/css/modernDebug.css" )>
		</style>

		<cfset local.sFileName_Debug = createUUID() & "_debug.cfm">
		<cfset local.sFileName_Metrics = createUUID() & "_metrics.cfm">
		<cfset local.sFileName_Docs = createUUID() & "_docs.cfm">
		<cfif arguments.context EQ "web">
			</td></td></td></th></th></th></tr></tr></tr></table></table></table></a></abbrev></acronym></address></applet></au></b></banner></big></blink></blockquote></bq></caption></center></cite></code></comment></del></dfn></dir></div></div></dl></em></fig></fn></font></form></frame></frameset></h1></h2></h3></h4></h5></h6></head></i></ins></kbd></listing></map></marquee></menu></multicol></nobr></noframes></noscript></note></ol></p></param></person></plaintext></pre></q></s></samp></script></select></small></strike></strong></sub></sup></table></td></textarea></th></title></tr></tt></u></ul></var></wbr></xmp>
		</cfif>

		<cfoutput>
			<cfset var sectionId = "ALL">
			<cfset var isDebugAllOpen = this.isSectionOpen( sectionId )>
			<cfset isMetricAllOpen = this.isSectionOpen( sectionId, "metrics" )>
			<cfset isDocsAllOpen = this.isSectionOpen( sectionId, "docs" )>
			<cfif isDebugAllOpen && isMetricAllOpen && isDocsAllOpen>
				<cfset isDebugAllOpen = false>
				<cfset isMetricAllOpen = false>
				<cfset isDocsAllOpen = false>
			</cfif>

			<cfset var sectionId = "ALL">
			<cfset var isOpen = this.isSectionOpen( sectionId, "debugging" ) OR this.isSectionOpen( sectionId, "metrics" ) OR this.isSectionOpen( sectionId, "docs" )>
			<!-- Lucee Debug Output !-->
			
			<fieldset id="-lucee-debug" class="#arguments.custom.size# #isOpen ? '' : 'collapsed'#">
				<cfset isdebugOpen = this.isSectionOpen( sectionId )>
				<cfset ismetricsOpen = this.isSectionOpen( sectionId, "metrics" )>
				<cfset isdocsOpen = this.isSectionOpen( sectionId, "docs" )>
				<cfif isdebugOpen && ismetricsOpen && isdocsOpen>
					<cfset isdebugOpen = false>
					<cfset ismetricsOpen = false>
					<cfset isdocsOpen = false>
				</cfif>
				<!--- Lucee Debug Output --->
				<legend style="line-height: 20px !important; width:auto !important;">
					<cfif enableTab("debug")>
						<button id="-lucee-debug-btn-#sectionId#" class="#isdebugOpen ? 'btnActive' : 'buttonStyle' # btnOvr" onclick="clickAjax('debug'); __LUCEE.debug.toggleSection( '#sectionId#' );">
							<!--- Lucee Debug Output --->
							Debugging
						</button>
						<cfif enableTab("Metrics")>
							<span class="dashColor"> - </span>
						</cfif>
					</cfif>
					<!--- <span>(#this.getLabel()#)</span> --->
					<cfif enableTab("Metrics")>
						<button id="-lucee-metrics-btn-#sectionId#" class="#ismetricsOpen ? 'btnActive' : 'buttonStyle' # btnOvr" onclick="clickAjax('metrics');  __LUCEE.debug.toggleSection( '#sectionId#', 'metrics' );">
							Metrics
						</button> 
						<cfif enableTab("Reference")>
							<span class="dashColor"> - </span>
						</cfif>
					</cfif>

					<cfif enableTab("Reference")>
						<button id="-lucee-docs-btn-#sectionId#" class="#isdocsOpen ? 'btnActive' : 'buttonStyle' # btnOvr" onclick="clickAjax('docs'); __LUCEE.debug.toggleSection( '#sectionId#', 'docs' ); ">
							Reference
						</button>
					</cfif>
				</legend>
				<cfif enableTab("debug")>
					<div id="-lucee-debug-ALL" class="#isdebugOpen ? '' : 'collapsed'#">Loading debugging data...</div>
				</cfif>
				
				<cfif enableTab("metrics")>
					<div class="wholeContainer">
					<div id="-lucee-metrics-ALL" class="#isMetricAllOpen ? '' : 'collapsed'#">
						<div class="section-title" style="padding-bottom:5px; padding-top:23px;">System Metrics</div>
						<div class="titleStyle">Memory Chart & CPU Chart</div>
						<div class="metricsCharts">Memory Used By java & Average CPU load of the last 20 seconds on the whole system and this Java Virtual Machine (Lucee Process).</div>
						<div class="Chart_container">
							<cfset chartStruct = variables.chartStr> 
							<cfset loadCharts("#chartStruct#")>
						</div>

						<div id="-lucee-metrics-data" class="#ismetricsOpen ? '' : 'collapsed'# ">Loading Metrics data...</div>

						<!--- <cfset metricsAllInfo = replace(sContent.tab2, chr(9), "", "ALL")>
						<cfset cachePut(sFileName_Metrics, metricsAllInfo, 0.001)> --->
					</div><!--- #-lucee-metrics-ALL !--->
					</div>
				</cfif>
				<cfif enableTab("Reference")>
					<div id="-lucee-docs-ALL" class="#isDocsAllOpen ? '' : 'collapsed'#">Loading Doucuments data...</div>
					<cfset str = {}>
					<cfset str.functions  = getAllFunctions()>
					<cfset str.tags = getAllTags()>
					<cfset str.components = getAllComponents()>
					<cfset allArryItem = []>
					<cfloop collection="#str#" index="lst">
						<cfloop array=#str[lst]# index="i">
							<cfset arrayAppend(allArryItem, i)>
						</cfloop>
					</cfloop>

					<!--- <cfset docsScontent = replace(sContent.tab3, chr(9), "", "ALL")>
					<cfset cachePut(sFileName_Docs, docsScontent , 0.001)> --->
				</cfif>
			</fieldset><!--- #-lucee-debug !--->

			<cfif enableTab("Reference") AND ( !structKeyExists(request, "fromAdmin") )>
				<div id="ex1" class="modal">
					<!--- <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button> --->
					<div class="modal-body">
					</div>
				</div>
			</cfif>

			<script>
				<cfset this.includeFileInline( "/lucee/res/js/util.min.js" )>
				<cfset this.includeFileInline( "/lucee/res/js/echarts-all.js" )>
				<cfset this.includeFileInline( "/lucee/res/js/typeahead.min.js" )>
				<cfset this.includeFileInline( "/lucee/res/js/base.min.js" )>
				<cfif !structKeyExists(url, "isAjaxRequest")>
				<cfset this.includeFileInline( "/lucee/res/js/jquery.modal.min.js" )>
				</cfif>
				var __LUCEE = __LUCEE || {};
				var oLastObj = false;
				sectionArray = [];

				__LUCEE.debug = {

					cookieName_debugging: 	"#variables.cookieName_debugging#"
					, cookieName_metrics: 	"#variables.cookieName_metrics#"
					, cookieName_docs: 	"#variables.cookieName_docs#"
					, bitmaskAll: 	Math.pow( 2, 31 ) - 1
					, allSections: 	#serializeJSON( this.allSections )#

					, setFlag: 		function( name, section) {
						if(typeof(section) == 'undefined'){
							var section = "debugging";
						}

						var value = __LUCEE.util.getCookie( __LUCEE.debug["cookieName_"+section], __LUCEE.debug.allSections.ALL ) | __LUCEE.debug.allSections[ name ];
						__LUCEE.util.setCookie( __LUCEE.debug["cookieName_"+section], value );
						return value;
					}

					, clearFlag: 	function( name, section) {

						if(typeof(section) == 'undefined'){
							var section = "debugging";
						}

						var value = __LUCEE.util.getCookie( __LUCEE.debug["cookieName_"+section], 0 ) & ( __LUCEE.debug.bitmaskAll - __LUCEE.debug.allSections[ name ] );
						__LUCEE.util.setCookie( __LUCEE.debug["cookieName_"+section], value );
						return value;
					}

					, toggleSection: 	function( name, section ) {
						if(typeof(section) == 'undefined'){
							var section = "debugging";
						}

						if(section == "debugging"){
							var sectionClass = "debug";
						}else if(section == "metrics"){
							var sectionClass = "metrics";
						} else if(section == "docs"){
							var sectionClass = "docs";
						}

						// All main sections
						var otherMainSections = [];
						$.each(#serializeJSON( variables.tbsStr )#, function(i, data) {
							otherMainSections.push(data)
						});
						// Find and remove item from an array
						var i = otherMainSections.indexOf(section);
						if(i != -1) {
							otherMainSections.splice(i, 1);
						}

						var btn = __LUCEE.util.getDomObject( "-lucee-" + sectionClass + "-btn-" + name );
						var obj = __LUCEE.util.getDomObject( "-lucee-" + sectionClass + "-" + name );
						var isOpen = ( __LUCEE.util.getCookie( __LUCEE.debug["cookieName_"+section], 0 ) & __LUCEE.debug.allSections[ name ] ) > 0;
						if ( isOpen ) {
							__LUCEE.util.removeClass( btn, '-lucee-icon-minus' );
							__LUCEE.util.addClass( btn, '-lucee-icon-plus' );
							__LUCEE.util.addClass( obj, 'collapsed' );
							__LUCEE.debug.clearFlag( name, section );
							__LUCEE.util.removeClass( btn, 'btnActive' );
							__LUCEE.util.addClass( btn, 'buttonStyle' );
						} else {
							__LUCEE.util.removeClass( btn, '-lucee-icon-plus' );
							__LUCEE.util.addClass( btn, 'btnActive' );
							__LUCEE.util.addClass( btn, '-lucee-icon-minus' );
							__LUCEE.util.removeClass( obj, 'collapsed' );
							__LUCEE.debug.setFlag( name, section );
						}

						// collapse other main sections -- start
						for( x in otherMainSections ){
							if(otherMainSections[x] == "debugging"){
								var sectionClass = "debug";
							}else if(otherMainSections[x] == "metrics"){
								var sectionClass = "metrics";
							} else if(otherMainSections[x] == "docs"){
								var sectionClass = "docs";
							}
							var btn = __LUCEE.util.getDomObject( "-lucee-" + sectionClass + "-btn-" + name );
							var obj = __LUCEE.util.getDomObject( "-lucee-" + sectionClass + "-" + name );
							__LUCEE.util.removeClass( btn, '-lucee-icon-minus' );
							__LUCEE.util.addClass( btn, 'buttonStyle' );
							__LUCEE.util.removeClass( btn, 'btnActive' );
							__LUCEE.util.addClass( btn, '-lucee-icon-plus' );
							__LUCEE.util.addClass( obj, 'collapsed' );
							__LUCEE.debug.clearFlag( name, otherMainSections[x] );
						}
						return !isOpen;
					}

					, selectText: 	function( id ) {

				        if ( document.selection ) {

				            var range = document.body.createTextRange();
				            range.moveToElementText( document.getElementById( id ) );
				            range.select();
				        } else if ( window.getSelection ) {

				            var range = document.createRange();
				            range.selectNode( document.getElementById( id ) );
				            window.getSelection().addRange( range );
				        }
				    }
				    , setSortOrder: function(oObj, sOrder ) {
				    	if (oLastObj) {
							__LUCEE.util.removeClass( oLastObj, 'selected' );
				    	}
						__LUCEE.util.setCookie(<cfoutput>'#variables.cookieSortOrder#'</cfoutput>, sOrder);
						__LUCEE.util.addClass( oObj, 'selected' );
						alert("Sort order next display: " + sOrder);
						oLastObj = oObj;
				    }
				    , setTemplateFilter: function(sPath, bRecursive ) {
				    	if (sPath == '') {
							__LUCEE.util.setCookie(<cfoutput>'#variables.cookieFilterTemplates#'</cfoutput>, '');
							alert('Filter reset!')
				    	} else {
					    	var sCookie = __LUCEE.util.getCookie(<cfoutput>'#variables.cookieFilterTemplates#'</cfoutput>);
					    	sCookie = sCookie + '|' + sPath + '!' + bRecursive;
							__LUCEE.util.setCookie(<cfoutput>'#variables.cookieFilterTemplates#'</cfoutput>, sCookie);
							alert('Filter set to ' + sPath + ' recursive: ' + (bRecursive ? 'Yes' : 'No'));
						}
				    }
				    , addExpression: function() {
				    	sExpression = document.getElementById('expr_newValue').value;
				    	var sCookie = __LUCEE.util.getCookie(<cfoutput>'#variables.cookieExpressions#'</cfoutput>);
				    	if (!sCookie) {
				    		sCookie = '';
				    	}
				    	sCookie = sCookie + '|' + sExpression;
						__LUCEE.util.setCookie(<cfoutput>'#variables.cookieExpressions#'</cfoutput>, sCookie);
						alert('Expression ' + sExpression + ' added. Refresh to see results: ' + sCookie);
				    }
				    , clearExpressions: function() {
						__LUCEE.util.setCookie(<cfoutput>'#variables.cookieExpressions#'</cfoutput>, '');
						alert('Expressions cleared!');
				    }
				};

				function clickAjax(section){
					var lcl = #serializeJson(local)#;
					var i = sectionArray.indexOf(section);
					if(i == -1){
						sectionArray.push(section);
						$.each(lcl, function(i, data) {
							if(i=="SFILENAME_"+section.toUpperCase()){
								var fileName =  lcl[i];
								var oAjax = new XMLHttpRequest();
								oAjax.onreadystatechange = function() {
									if(this.readyState == 4 && this.status == 200) {
										//var result = $.parseHTML(this.responseText);
										if(section == 'metrics'){
											$("##-lucee-"+section+"-data").removeClass('collapsed');
											$("##-lucee-"+section+"-data").html(this.responseText);
										} else{
											$("##-lucee-"+section+"-ALL").html(this.responseText);
											if(section == 'docs'){
												bindTypeaheadJS();
											}
										}
									}
								};
								var ajaxURL = "/lucee/appLogs/readDebug.cfm?TAB="+section;
								<cfif structKeyExists(request, "fromAdmin") AND request.fromAdmin EQ true>
									ajaxURL += "&fromAdmin=true";
								</cfif>
								oAjax.open("GET", ajaxURL, true);
								oAjax.send();
							}
						});
					}
				}
			</script>
			<cfif enableTab("metrics")>
			<script>
				function requestData(){
					jQuery.ajax({
						type: "POST",
						url: "/lucee-server/admin/debug/chartProcess.cfc?method=sysMetric",
						success: function(result){
							var arr =["heap","nonheap", "cpuSystem", "cpuProcess"];
							$.each(arr,function(index,chrt){
								window["series_"+chrt] = window[chrt+"Chart"].series[0].data; //*charts*.series[0].data
								window["series_"+chrt].push(result[chrt]); // push the value into series[0].data
								window[chrt+"Chart"].series[0].data = window["series_"+chrt];
								if(window[chrt+"Chart"].series[0].data.length > 100){
								window[chrt+"Chart"].series[0].data.shift(); //shift the array
								}
								window[chrt+"Chart"].xAxis[0].data.push(new Date().toLocaleTimeString()); // current time
								if(window[chrt+"Chart"].xAxis[0].data.length > 5){
								window[chrt+"Chart"].xAxis[0].data.shift(); //shift the Time value
								}
								window[chrt].setOption(window[chrt+"Chart"]); // passed the data into the chats
							});
							setTimeout(requestData, 1000);
						}
					})
				}
				var dDate=[new Date().toLocaleTimeString()]; // current time


				// intialize charts
				$.each(["heap","nonheap", "cpuSystem", "cpuProcess"], function(i, data){
					window[data] = echarts.init(document.getElementById(data),'macarons'); // intialize echarts
					window[data+"Chart"] = {
						backgroundColor: ["##EFEDE5"],
						tooltip : {'trigger':'axis'},
						color: ['##0000FF'],
						grid : {
							width: '75%',
							height: '65%',
							x:'30px',
							y:'20px'
						},
						xAxis : [{
							'type':'category',
							'boundaryGap' : false,
							'data': [0]
						}],
						yAxis : [{
							'type':'value',
							'min':'0',
							'max':'100',
							'splitNumber': 2
						}],
						series : [
							{
							'name': data +' Memory',
							'type':'line',
							smooth:true,
							itemStyle: {normal: {areaStyle: {type: 'default'}}},
							'data': [0]
							}
						]
					}; // data
					window[data].setOption(window[data+"Chart"]); // passed the data into the chats
				});
				requestData();
			</script>
			</cfif>
			<cfif enableTab("Reference")>
				<script>

					function bindTypeaheadJS(){
						var allArr = #serializeJson(allArryItem)#;
						var types = #serializeJson(str)#;

						var substringMatcher = function(strs) {
							return function findMatches(q, cb) {
								var matches, substringRegex;
								// an array that will be populated with substring matches
								matches = [];
								// regex used to determine if a string contains the substring `q`
								substrRegex = new RegExp(q, 'i');
								// iterate through the pool of strings and for any string that
								// contains the substring `q`, add it to the `matches` array
								$.each(strs, function(i, str) {
									if (substrRegex.test(str)) {
									matches.push(str);
									}
								});

								cb(matches);
							};
						};

						$( function() {
							$( '##lucee-docs-search-input' ).typeahead(
								{
									hint: true,
									highlight: true,
									minLength: 1
								},
								{
								  name: 'keyWords',
								  source: substringMatcher(allArr),
								   templates: {
									    empty:  '<div class="moreResults"><span onclick="moreInfo()">No Results Found</span></div>'
							  	}
							}
						).on('typeahead:selected', typeaheadSelected);
							function typeaheadSelected($e, datum){
								$.each(types, function(i, data) {
									$.each(data, function(x, y){
										if(datum.toString() == y){
											callDesc(i, datum.toString());
										}
									});
								});
							}
						});


						$(document).ready(function() {
							$('##lucee-docs-search-input').focus();
						    $('##-lucee-docs-btn-ALL').on('click', function() {
								$('.tt-hint').hide();
						    });
							$('.tt-menu').on('click', function(){
								$('.icon-close').hide();
							});
						});

						$('##-lucee-docs-btn-ALL').on('click', function() {
							$('##lucee-docs-search-input').focus();
					    });
					}

					function callDesc(type, item){
						var docURL = "/lucee/doc/" + type + ".cfm?isAjaxRequest=true&fromAdmin=#structKeyExists(request, 'fromAdmin')#&item=" + item;
						$.ajax({
							type: "get",
							url: docURL,
							success: function(data){
								$( ".modal-body" ).html("" + data.toString() + "");
								$('<div class="blocker"></div>').appendTo(document.body);
								$("##ex1").show();
								$("##ex1").modal('show');
								$('.close-modal').on('click', function() {
									$('##lucee-docs-search-input').val('');
									$("div.blocker").remove();
									$(".modal-body").html("");
									$("##ex1").hide();
									$("btnOvr").addClass("button.buttonStyle");
									$("btnOvr").addClass("button.btnActive");
								});
							}
						});
					}
				</script>
			</cfif>
			<cfif isdebugOpen && enableTab("debug") || ismetricsOpen && enableTab("metrics")  ||  isdocsOpen && enableTab("Reference")>
				<script>
					var isdebugOpen = #isdebugOpen# && #enableTab("debug")#;
					var ismetricsOpen = #ismetricsOpen# && #enableTab("metrics")#;
					var isdocsOpen = #isdocsOpen# && #enableTab("Reference")#;
					if(isdebugOpen ){
						section = "debug";
					}else if(ismetricsOpen){
						section = "metrics";
					}
					else{
						section = "docs";
					}
					clickAjax(section);
				</script>
			</cfif>
		</cfoutput>
	</cffunction><!--- output() !--->


	<cffunction name="doMore" returntype="void">
		<cfargument name="custom"    type="struct" required="#true#">
		<cfargument name="debugging" type="struct" required="#true#">
		<cfargument name="context"   type="string" default="web">
	</cffunction>

	<cffunction name="readDebug" returntype="void"  >
		<cfargument name="custom" required="true" type="struct" />
		<cfargument name="debugging" required="true" type="struct" />
		<cfargument name="context" type="string" default="web" />
		<cfsilent>
			<cfset local.stStats = filterByTemplates(arguments.debugging)>
			<cfset arguments.debugging.minimal          = arguments.custom.minimal ?: 0>
			<cfset arguments.debugging.highlight        = arguments.custom.highlight ?: 250000>
			<cfset arguments.debugging.expression       = arguments.custom.expression ?: false>
			<cfset arguments.debugging.timers           = arguments.debugging.timers ?: queryNew('label,time,template')>
			<cfset arguments.debugging.traces           = arguments.debugging.traces ?: queryNew('type,category,text,template,line,var,total,trace')>
			<cfset arguments.debugging.dumps            = arguments.debugging.dumps ?: queryNew('output,template,line')>
			<cfset arguments.custom.general             = arguments.custom.keyExists('general')>
			<cfset arguments.custom.callStack           = arguments.custom.keyExists('callStack')>
			<cfset arguments.custom.displayStats        = arguments.custom.keyExists('displayStats')>
			<cfset arguments.custom.colorHighlight      = arguments.custom.keyExists('colorHighlight')>
			<cfset arguments.custom.displayPercentages  = arguments.custom.keyExists('displayPercentages')>
			<cfset arguments.custom.sessionSize         = (arguments.custom.sessionSize ?: 100) * 1024>
			<cfset arguments.custom.no_of_charts         = (arguments.custom.no_of_charts ?: 2)>
			<!--- <cfset arguments.custom.sort_charts 		= (arguments.custom.sort_charts ?: '1,2,3,4')> --->


			<cfset var _cgi=structKeyExists(arguments.debugging,'cgi')?arguments.debugging.cgi:cgi />
			<cfset var pages=arguments.debugging.pages />
			<cfset var queries=arguments.debugging.queries />
			<cfif not isDefined('arguments.debugging.timers')>
				<cfset arguments.debugging.timers=queryNew('label,time,template') />
			</cfif>
			<cfif not isDefined('arguments.debugging.traces')>
				<cfset arguments.debugging.traces=queryNew('type,category,text,template,line,var,total,trace') />
			</cfif>
			<cfif not isDefined('arguments.debugging.dumps')>
				<cfset arguments.debugging.traces=queryNew('output,template,line') />
			</cfif>
			<cfset var timers=arguments.debugging.timers />
			<cfset var traces=arguments.debugging.traces />
			<cfset var dumps=arguments.debugging.dumps />

			<!---  calculate totals  --->
			<cfset local.loa      = 0>
			<cfset local.tot      = 0>
			<cfset local.totLucee = 0>
			<cfset local.totCnt   = 0>
			<cfset local.q        = 0>
			<cfset local.totAvg   = 0>
			<cfset queryAddColumn(pages, "method")>
			<cfset queryAddColumn(pages, "path")>
			<cfloop query="pages">
				<cfset querySetCell(pages, "total", pages.app + pages.load + pages.query, pages.currentRow)>
				<cfset querySetCell(pages, "avg", (pages.app + pages.load + pages.query) / pages.count, pages.currentRow)>
				<cfset aPage = listToArray(pages.src, '$')>
				<cfset querySetCell(pages, "path", pages.src, pages.currentRow)>
				<cfset querySetCell(pages, "src", contractPath(aPage[1]), pages.currentRow)>
				<cfset querySetCell(pages, "method", !isEmpty(aPage[2] ?: '') ? aPage[2] : '', pages.currentRow)>
				<cfset tot      += pages.total />
				<cfset totCnt   += pages.count />
				<cfset totLucee += pages.app>
				<cfset q        += pages.query />
				<cfset loa      += pages.load />
				<cfset totAvg   += pages.avg />
				<cfif pages.avg LT arguments.custom.minimal*1000>
					<cfcontinue>
				</cfif>
				<cfset local.bad=pages.avg GTE arguments.custom.highlight*1000 />
			</cfloop>
			<cfset local.iTotalTime  = tot>
			<cfset local.iTotalCount = totCnt>
			<cfset local.iTotalQuery = max(q, 0.01)><!--- To prevent division by zero, if the sum of queries is 0  --->
			<cfset local.iTotalLucee = totLucee>
			<cfset local.iTotalAvg   = totAvg>

			<cfset this.allSections  = this.buildSectionStruct()>
			<cfset var isExecOrder   = this.isSectionOpen( "ExecOrder" )>
			<cfset local.sCookieSort = cookie[variables.cookieSortOrder] ?: ' '>
			<cfif structKeyExists(pages, sCookieSort)>
				<cfif listFind("id,src", sCookieSort)>
					<cfset querySort(pages, sCookieSort,"asc")>
				<cfelse>
					<cfset querySort(pages, sCookieSort,"desc")>
				</cfif>
			</cfif>

			<cfset var implicitAccess=arguments.debugging.implicitAccess />
			<cfset querySort(implicitAccess,"template,line,count","asc,asc,desc") />
			<cfparam name="arguments.custom.unit" default=" ms">
			<cfparam name="arguments.custom.color" default="black">
			<cfparam name="arguments.custom.bgcolor" default="white">
			<cfparam name="arguments.custom.font" default="Times New Roman">
			<cfparam name="arguments.custom.size" default="medium">
			<cfset var unit={
				millisecond:"ms"
				,microsecond:"s"
				,nanosecond:"ns"
				} />
			<cfset var ordermap={}>
			<cfloop query="#arguments.debugging.history#">
				<cfif !structkeyExists(ordermap, arguments.debugging.history.id)><cfset ordermap[ arguments.debugging.history.id ]=structCount(ordermap)+1></cfif>
			</cfloop>
			<cfset var prettify=structKeyExists(arguments.custom,'timeformat') and arguments.custom.timeformat EQ "natural">
		</cfsilent>
		<!--- General --->

		<div id="debugContainer">
			<cfoutput>
			<cfif isEnabled( arguments.custom, 'general' )>
				<div class="section-title" style="padding-top:23px;">Debugging Information</div>
				<cfoutput>
					<h3 style="color:red" class="section-title">&nbsp;Your session is larger than #byteFormat(arguments.custom.sessionSize)#. Be aware</h2>
				</cfoutput>
				<cfif isDefined("session") AND sizeOf(session) gt arguments.custom.sessionSize>
				</cfif>
				<cfset sectionId = "Info">
				<cfset isOpen = this.isSectionOpen( sectionId )>
				<table>
					<cfset renderSectionHeadTR( sectionId, "Template:", "#HTMLEditFormat(_cgi.SCRIPT_NAME)# (#HTMLEditFormat(expandPath(_cgi.SCRIPT_NAME))#)" )>
					<tr>
						<td class="pad label">User Agent:</td>
						<td class="pad">#_cgi.http_user_agent#</td>
					</tr>
					<tr>
						<td colspan="2" id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table class="ml14px">
								<tr>
									<td class="label" colspan="2">
										#server.coldfusion.productname#
										<cfif StructKeyExists(server.lucee,'versionName')>(<a href="#server.lucee.versionNameExplanation#" target="_blank">#server.lucee.versionName#</a>)
										</cfif>
										#ucFirst(server.coldfusion.productlevel)# #uCase(server.lucee.state)# #server.lucee.version# (CFML Version #server.ColdFusion.ProductVersion#)
									</td>
								</tr>
								<tr>
									<td class="label">Time Stamp</td>
									<td class="cfdebug">#LSDateFormat(now())# #LSTimeFormat(now())#</td>
								</tr>
								<tr>
									<td class="label">Time Zone</td>
									<td class="cfdebug">#getTimeZone()#</td>
								</tr>
								<tr>
									<td class="label">Locale</td>
									<td class="cfdebug">#ucFirst(GetLocale())#</td>
								</tr>
								<tr>
									<td class="label">Remote IP</td>
									<td class="cfdebug">#_cgi.remote_addr#</td>
								</tr>
								<tr>
									<td class="label">Host Name</td>
									<td class="cfdebug">#_cgi.server_name#</td>
								</tr>
								<tr>
									<td class="label">Debugging Template</td>
									<td class="cfdebug">#getCurrentTemplatePath()#</td>
								</tr>
								<cfif StructKeyExists(server.os,"archModel") and StructKeyExists(server.java,"archModel")>
									<tr>
										<td class="label">Architecture</td>
										<td class="cfdebug">
											<cfif server.os.archModel NEQ server.os.archModel>
												OS #server.os.archModel#bit/JRE #server.java.archModel#bit
											<cfelse>
												#server.os.archModel#bit
											</cfif>
										</td>
									</tr>
								</cfif>
							</table>
						</td>
					</tr>
				</table>
			</cfif>

			<!--- Filter information on top --->
			<cfif stStats.filterActive>
				<div class="section-title" style="color:red"><b>Debugging Filter active!</b></div>
				<table>
					<tr><td><table class="details" cellpadding="2" cellspacing="0">
						<tr>
							<th>Section</th>
							<th>Filtered</th>
							<th>of</th>
							<th>Filtered time</th>
							<th>of Total</th>
						</tr>

						<cfloop list="pages,queries,implicitAccess,timers,traces,exceptions,dumps" index="local.lst">
							<tr>
								<td>#UCFirst(lCase(lst))#:</td>
								<td class="txt-r">#stStats[lst].filtered#</td>
								<td class="txt-r">#stStats[lst].initial#</td>
								<td class="txt-r">
									<cfif stStats[lst].filtered and stStats[lst].keyExists("sumFilteredTime")>
										#unitFormat(arguments.custom.unit, stStats[lst].sumFilteredTime ?: 0)#
									<cfelse>
										&nbsp;
									</cfif>
								</td>
								<td class="txt-r">
									<cfif stStats[lst].initial and stStats[lst].keyExists("sumTime")>
										#unitFormat(arguments.custom.unit, stStats[lst].sumTime ?: 0)#
									<cfelse>
										&nbsp;
									</cfif>
								</td>
							</tr>
						</cfloop>
						<tr><th colspan="5"><b>Filtered templates:</b></th></tr>
						<tr>
							<th colspan="4" align="left">Template</th>
							<th>Recursive</th>
						</tr>
						<cfloop collection="#stStats.stTemplates#" index="local.sTemplate" item="local.stRecurse">
							<cfif sTemplate neq 'undefined'>
								<tr>
									<td colspan="4">
										#sTemplate#
									</td>
									<td align="center"><b>#stRecurse.recurse ? 'Yes' : 'No'#</b></td>
								</tr>
							</cfif>
						</cfloop>
						<tr><td colspan="5" align="center"><a onClick="__LUCEE.debug.setTemplateFilter('')">Reset Filter</a></td></tr>
					</table></td></tr>
				</table>

			</cfif>

			<!--- Execution Time --->
			<cfset sectionId = "ExecTime">
			<cfset isOpen = this.isSectionOpen( sectionId )>

			<div class="section-title">Execution Time</div>
			<cfset local.loa=0>
			<cfset local.tot=0>
			<cfset local.q=0>
			<cfset local.bDisplayLongExec = false>

			<cfloop query="pages">
				<cfset tot=tot+pages.total>
				<cfset q=q+pages.query>
				<cfif pages.avg LT arguments.custom.minimal*1000>
					<cfcontinue>
				</cfif>
				<cfset local.bad=pages.avg GTE arguments.custom.highlight*1000>
				<cfset loa=loa+pages.load />
			</cfloop>

			<table>
				<cfset renderSectionHeadTR( sectionId
					, "#unitFormat( arguments.custom.unit, tot-q-loa, prettify )#
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Application" )>
				<tr><td><table>
					<tr>
						<td class="pad txt-r">#unitFormat( arguments.custom.unit, loa,prettify )#</td>
						<td class="pad">Startup/Compilation</td>
					</tr>
					<tr>
						<td class="pad txt-r">#unitFormat( arguments.custom.unit, q,prettify )#</td>
						<td class="pad">Query</td>
					</tr>
					<tr>
						<td class="pad txt-r bold">#unitFormat( arguments.custom.unit, tot, prettify )#</td>
						<td class="pad bold">Total</td>
					</tr>
				</table></td></tr>
				<tr>
					<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
						<table class="details" cellpadding="2" cellspacing="0">
							<tr>
								<th>
									<cfif (sCookieSort ?: '-') neq 'id'>
										<a onclick="__LUCEE.debug.setSortOrder(this, 'id')" class="sortby" title="Order by ID (starting with the next request)">ID</a>
									<cfelse>
										ID
									</cfif>
								</th>
								<cfset local.stColumns = [
									{title:'Total time', field:'total'},
									{title:'Query',      field:'query'},
									{title:'CFML time',  field:'app'},
									{title:'Count',      field:'count'},
									{title:'Avg time',   field:'avg'}
								]>
								<cfloop collection="#stColumns#" index="local.lstHead" item="local.stColumn">
									<th align="center">
										<cfif (sCookieSort ?: '-') neq stColumn.field>
											<a onclick="__LUCEE.debug.setSortOrder(this, '#stColumn.field#')" class="sortby" title="Order by #stColumn.title# (starting with the next request)">#stColumn.title#</a>
										<cfelse>
											#stColumn.title#
										</cfif>
									</th>
									<cfif arguments.custom.displayPercentages><th align="center">%</th></cfif>
								</cfloop>
								<th>
									<cfif (sCookieSort ?: '-') neq 'src'>
										<a onclick="__LUCEE.debug.setSortOrder(this, 'src')" class="sortby" title="Order by Template (starting with the next request)">Template</a>
									<cfelse>
										Template
									</cfif>
								</th>
								<th>Method 
									<cfif arguments.custom.callStack> / Stack
										<cfset local.stPages = {}>
										<cfloop query="pages">
											<cfset local.sMethod = !isEmpty(pages.method) ? ':#pages.method#()' : ''>
											<cfset stPages[pages.id].src = '<span title="#pages.src##sMethod#">' & listLast(pages.src, '/\') & "<b>#sMethod#</b></span>">
										</cfloop>
										<cfloop from="#arguments.debugging.history.recordCount#" to="1" step="-1" index="local.iRec">
											<cfset stPages[arguments.debugging.history.id[iRec]].iHistPos = iRec>
										</cfloop>
									</cfif>
								</th>
								<th>Filter</th>
							</tr>
							<cfset loa=0 />
							<cfset tot=0 />
							<cfset q=0 />
							<cfloop query="pages">
								<cfset tot += pages.total - (pages.count * pages.avg) />
								<cfset q += pages.query />
								<cfif pages.avg LT arguments.custom.minimal*1000>
									<cfcontinue>
								</cfif>
								<cfset bad = pages.avg GTE arguments.custom.highlight*1000 />
								<cfset loa += pages.load />
								<cfset local.iPctTotal = pages.total / (iTotalTime eq 0 ? 1 : iTotalTime)>
								<cfset local.iPctCount = pages.count / (iTotalCount eq 0 ? 1 : iTotalCount)>
								<cfset local.iPctQuery = pages.query / (iTotalQuery eq 0 ? 1 : iTotalQuery)>
								<cfset local.iPctLucee = pages.app / (iTotalLucee eq 0 ? 1 : iTotalLucee)>
								<cfset local.iPctAvg   = pages.avg / (iTotalAvg eq 0 ? 1 : iTotalAvg)>
								<cfset local.sColor    = RGBtoHex(255 * iPctTotal, 160 * (1 - iPctTotal), 0)>
								<cfset sStyle = ''>
								<cfif arguments.custom.colorHighlight>
									<cfset sStyle = sColor>
								</cfif>
								<cfif bad><cfset sStyle = "red"></cfif>
								<tr class="#bad ? 'red': ''#">
									<td class="txt-r faded #sCookieSort eq 'id' ? 'sorted' : ''#" title="#pages.id#">#ordermap[pages.id]#</td>
									<td align="right" class="tblContent #sCookieSort eq 'total' ? 'sorted' : ''#">
										<font color="#sStyle#">#unitFormat(arguments.custom.unit, pages.app + pages.query)#</font>
										<cfif !bDisplayLongExec AND bad>
											<cfset bDisplayLongExec = true>
										</cfif>
									</td>
									<cfif arguments.custom.displayPercentages>
										<td align="right" class="tblContent" style="#sStyle#">
											<font color="#sStyle#">#numberFormat(iPctTotal*100, '999.9')#</font>
										</td>
									</cfif>
									<td align="right" class="tblContent #sCookieSort eq 'query' ? 'sorted' : ''#" style="#sStyle#">
										<font color="#sStyle#">#unitFormat(arguments.custom.unit, pages.query)#</font>
									</td>
									<cfif arguments.custom.displayPercentages>
										<td align="right" class="tblContent" style="#sStyle#">
											<font color="#sStyle#">#numberFormat(iPctQuery*100, '999.9')#</font>
										</td>
									</cfif>
									<td align="right" class="tblContent #sCookieSort eq 'app' ? 'sorted' : ''#" style="#sStyle#">
										<font color="#sStyle#">#unitFormat(arguments.custom.unit, pages.app)#</font>
									</td>
									<cfif arguments.custom.displayPercentages>
										<td align="right" class="tblContent" style="#sStyle#">
											<font color="#sStyle#">#numberFormat(iPctLucee*100, '999.9')#</font>
										</td>
									</cfif>
									<td align="center" class="tblContent #sCookieSort eq 'count' ? 'sorted' : ''#" style="#sStyle#">
										<font color="#sStyle#">#pages.count#</font>
									</td>
									<cfif arguments.custom.displayPercentages>
										<td align="right" class="tblContent" style="#sStyle#">
											<font color="#sStyle#">#numberFormat(iPctCount*100, '999.9')#</font>
										</td>
									</cfif>
									<td align="right" class="tblContent #sCookieSort eq 'avg' ? 'sorted' : ''#" style="#sStyle#">
										<font color="#sStyle#">#unitFormat(arguments.custom.unit, pages.avg)#</font>
									</td>
									<cfif arguments.custom.displayPercentages>
										<td align="right" class="tblContent" style="#sStyle#">
											<font color="#sStyle#">#numberFormat(iPctAvg*100, '999.9')#</font>
										</td>
									</cfif>
									<td align="left" class="tblContent #sCookieSort eq 'src' ? 'sorted' : ''#" style="#sStyle#" title="#pages.path#">
										<font color="#sStyle#">#listFirst(pages.src, "$")#</font>
									</td>
									<td align="left" class="tblContent" style="#sStyle#">
										<font color="#sStyle#">
											<cfif arguments.custom.callStack>
												<div style="width:80px;float:left"><b>Stacktrace:</b></div>
												<div style="float:left">#dspCallStackTrace(pages.id, stPages, arguments.debugging.history)#</div>
											</cfif>
											<cfif listLen(pages.src, "$") gt 1><br>
												<div style="width:80px;float:left"><b>Method:</b></div>
												<div style="float:left">#listLast(pages.src, "$")#</div>
											</cfif>
										</font>
									</td>
									<cfset local.sPage = replace(listFirst(pages.path, '$'), '\', '/', 'ALL')>
									<td>
										<a onClick="__LUCEE.debug.setTemplateFilter('#sPage#', 0)" title="Debug this file only.">
											<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB98KEwgeMjUl220AAAEISURBVDjLxZM9SgNxEMV/M/nASEDFLJJOPIO92wmWsbdZNugRRLCwySXcxtia1jIL6g08RbLgVlFh9z8Wi8JKSGATyGuHefPemxnYBHauXji4fAOgXoWgkWXkTgDQVdUsJfCCGC+Ie5UJUEB58sLxNZj8LwtAJ4gBzqxmE3W2r0JqQptcugCmMgQw7GF367OffrW+cUIS+UWIUui4k5xXp3qM2btDDlU5LU+Ti4/Z9lHDaS9TN60WolKyUQcwB8Ct1Zioc88qpCLWJpfHkgVjuNeahb8W/jJYuIUwLnqNm+T+ZABinXBMKYOFKNSdJ5E/mldeSjCNfIBR9TtY9RLnodnMQGU9n/kD+19X1oivU2EAAAAASUVORK5CYII="/>
										</a>
										&nbsp;
										<a onClick="__LUCEE.debug.setTemplateFilter('#sPage#', 1)" title="Debug this file and all included ones.">
											<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB98KEwglA1hJNG8AAAGuSURBVDjL3ZI9aFNhFIaf80WDQ7c0QjepuKc4FCnFm9LJUekQbTE2iSCKIJihmFAtpqAp+EuX9NafNjYiKU4OLt5iFQfBDp0KtkJ1aTpER02+41BuG+mtq+A7Hc7hvN9zXj741xK/aE95J8TwQi0Dm67zanR2UScXGgBccvYzeqZHggyMX2hIN7C6oiHdAJhcaDCXjZFLdPLQ+wVAOjWsMzNPtdVg37aT1Yg1ptNYGwHIdDdIFJcAON/doFCCKXdaptzpYAIj1I3qVyPUAcbT/TJy5B3f7nTdyg10Hd4rg50ThDYrclCFNr/3symYcLj//uvVz9GMp9GMp4XKh+ATaEqHMbTTlI7WjB/cu3t0YuUYc9kYy+vfKVTWADiXPKvHnfgOQZCa1nIlOyIXew2J4hKFyhoXerZmjx4/kWQyKRJNeScxVHdtW05dPvS2ms/n5W+PmJrrzFtLrrWpqtdqrjPvb068/KR+BrerHzX4I6XflEXktGLLm6W+QYCxG9f1QDhEcbWXZ1djLK/XGX/+hVrJkaGhQY3H+/7EiQx7P4Iwb5bfbxOMzS4q/5d+A67apf+Ijp6WAAAAAElFTkSuQmCC"/>
										</a>
									</td>
								</tr>
							</cfloop>
						</table>
						<cfif bDisplayLongExec>
							<font color="red">
								red = over #unitFormat(arguments.custom.unit,arguments.custom.highlight*1000)# average execution time
							</font>
							<br>
						</cfif>
					</td><!--- #-lucee-debug-#sectionId# !--->
				</tr>
			</table>


			<cfset this.doMore( arguments.custom, arguments.debugging, arguments.context )>

			<!--- Queries --->
			<cfif queries.recordcount>
				<cfset sectionId = "Query">
				<cfset isOpen = this.isSectionOpen( sectionId )>
				<cfset local.total  =0>
				<cfset local.records=0>
				<cfloop query="queries">
					<cfset total   += queries.time>
					<cfset records += queries.count>
				</cfloop>

				<div class="section-title">SQL Queries</div>
				<table>
					<cfset renderSectionHeadTR( sectionId, "#queries.recordcount# Quer#queries.recordcount GT 1 ? 'ies' : 'y'# Executed (Total Records: #records#; Total Time: #unitFormat( arguments.custom.unit, total ,prettify)#)" )>

					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table><tr><td>
								<cfset hasCachetype=ListFindNoCase(queries.columnlist,"cachetype") gt 0>
								<cfset local.bUsage = listFindNoCase(queries.columnlist, 'usage') && isStruct(queries.usage)>
								<table class="details">
									<tr>
										<th></th>
										<th>Name</th>
										<th>Records</th>
										<th>Time</th>
										<th>%</th>
										<cfif bUsage>
											<th>Usage</th>
										</cfif>
										<th>Datasource</th>
										<th>Source</th>
										<cfif hasCachetype><th>Cache Type</th></cfif>
									</tr>
									<cfloop query="queries">
										<cfif bUsage>
											<cfset local.usage=queries.usage>
											<cfset local.usageNotRead = []>
											<cfset local.usageRead  = []>
											<cfif not isStruct(queries.usage)>
												<cfset stUsage = {}>
											<cfelse>
												<cfset stUsage = queries.usage>
											</cfif>
											<cfloop collection="#stUsage#" index="local.item" item="local.value">
												<cfif !value>
													<cfset arrayAppend( usageNotRead, item )>
												<cfelse>
													<cfset arrayAppend( usageRead, item )>
												</cfif>
											</cfloop>
											<cfset local.iRSp = (arrayLen(usageRead) ? 1 : 0) + (arrayLen(usageNotRead) ? 1 : 0)>
											<cfset local.arrLenU = arrayLen( usageRead )>
											<cfset local.arrLenN = arrayLen( usageNotRead )>
											<cfset local.iPct    = arrLenU+arrLenN eq 0 ? 0 : arrLenU/(arrLenU+arrLenN) * 100>
										</cfif>

										<tr>
											<cfset isOpen = this.isSectionOpen( queries.currentRow )>
											<th>
												<a id="-lucee-debug-btn-qry-#queries.currentRow#" 
													class="-lucee-icon-#isOpen ? 'minus' : 'plus'#" 
													onclick="__LUCEE.debug.toggleSection( 'qry-#queries.currentRow#' );">&nbsp;</a>
											</th>
											<td>#queries.name#</td>
											<td class="txt-r">#queries.count#</td>
											<td class="txt-r">#unitFormat(arguments.custom.unit, queries.time,prettify)#</td>
											<td class="txt-r">
											<cfif total neq 0>
												#unitFormat(arguments.custom.unit, queries.time / total * 100,prettify)#
											<cfelse>
												#unitFormat(arguments.custom.unit, 0,prettify)#
											</cfif>
											</td>
											<cfif bUsage>
												<td class="txt-r" style="color:#getPctColor(iPct)#">
													<cfif iPct gte 0>
														#numberFormat(iPct, "999.9")# %
													<cfelseif iPct lt -1>
														Empty
													<cfelse>
														DDL SQL
													</cfif>
												</td>
											</cfif>
											<td>#queries.datasource#</td>
											<td title="#queries.src#">#contractPath(queries.src)#</td>
											<cfif hasCachetype><td>#isEmpty(queries.cacheType)?"none":queries.cacheType#</td></cfif>
										</tr>
										<tr id="-lucee-debug-qry-#queries.currentRow#" class="#isOpen ? '' : 'collapsed'#">
											<th>&nbsp;</th><td colspan="8">
											<table>
												<cfif arrLenU ?: 0>
													<tr>
														<th>Used:</th>
														<td colspan="8">#arrayToList(usageRead, ', ')#</td>
													</tr>
												</cfif>
												<cfif arrLenN ?: 0>
													<tr>
														<th>Not Used:</th>
														<td colspan="8" class="red">#arrayToList(usageNotRead, ', ')#</td>
													</tr>
												</cfif>
												<tr>
													<th class="label">SQL:</th>
													<td id="-lucee-debug-query-sql-#queries.currentRow#" colspan="8" onclick="__LUCEE.debug.selectText( this.id );">
														<cfset local.sSQL = replace(queries.sql, chr(9), "&nbsp;&nbsp;", "ALL")>
														<cfset sSQL = replace(sSQL, chr(10), "<br>", "ALL")>
														<div class="__sql">#trim( sSQL )#</div>
													</td>
												</tr>
											</table>
										</td></tr>
									</cfloop>
								</table>

							</tr></td></table>
						</td><!--- #-lucee-debug-#sectionId# !--->
					</tr>
				</table>
			</cfif>

			<!--- Exceptions --->
			<cfif structKeyExists( arguments.debugging, "exceptions" ) && arrayLen( arguments.debugging.exceptions )>

				<cfset sectionId = "Exceptions">
				<cfset isOpen = this.isSectionOpen( sectionId )>

				<div class="section-title">Caught Exceptions</div>
				<table>

					<cfset renderSectionHeadTR( sectionId, "#arrayLen(arguments.debugging.exceptions)# Exception#arrayLen( arguments.debugging.exceptions ) GT 1 ? 's' : ''# Caught" )>

					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table class="details">

								<tr>
									<th>Type</th>
									<th>Message</th>
									<th>Detail</th>
									<th>Template</th>
									<th>Line</th>
								</tr>
								<cfloop array="#arguments.debugging.exceptions#" index="local.exp">
									<tr>
										<td>#exp.type#</td>
										<td>#exp.message#</td>
										<td>#exp.detail#</td>
										<td>#exp.TagContext[1].template#</td>
										<td class="txt-r">#exp.TagContext[1].line#</td>
									</tr>
								</cfloop>

							</table>
						</td><!--- #-lucee-debug-#sectionId# !--->
					</tr>
				</table>
			</cfif>

			<!--- Implicit variable Access --->
			<cfif implicitAccess.recordcount>

				<cfset sectionId = "ImpAccess">
				<cfset isOpen = this.isSectionOpen( sectionId )>

				<div class="section-title">Implicit Variable Access</div>

				<table>
					<cfset renderSectionHeadTR( sectionId, "#implicitAccess.recordcount# Implicit Variable Access#( implicitAccess.recordcount GT 1 ) ? 'es' : ''#" )>

					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table class="details">

								<tr>
									<th>Template</th>
									<th>Line</th>
									<th>Scope</th>
									<th>Var</th>
									<th>Count</th>
								</tr>
								<cfset total=0 />
								<cfloop query="implicitAccess">
									<tr>
										<td>#implicitAccess.template#</td>
										<td class="txt-r">#implicitAccess.line#</td>
										<td>#implicitAccess.scope#</td>
										<td>#implicitAccess.name#</td>
										<td class="txt-r">#implicitAccess.count#</td>
									</tr>
								</cfloop>

							</table>
						</td><!--- #-lucee-debug-#sectionId# !--->
					</tr>
				</table>
			</cfif>

			<!--- Timers --->
			<cfif timers.recordcount>

				<cfset sectionId = "Timer">
				<cfset isOpen = this.isSectionOpen( sectionId )>

				<div class="section-title">CFTimer Times</div>

				<table>

					<cfset renderSectionHeadTR( sectionId, "#timers.recordcount# Timer#( timers.recordcount GT 1 ) ? 's' : ''# Set" )>

					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table class="details">

								<tr>
									<th align="center">Label</th>
									<th>Time</th>
									<th>Template</th>
								</tr>
								<cfloop query="timers">
									<tr>
										<td class="txt-r">#timers.label#</td>
										<td class="txt-r">#unitFormat( arguments.custom.unit, timers.time * 1000000,prettify )#</td>
										<td class="txt-r">#timers.template#</td>
									</tr>
								</cfloop>

							</table>
						</td><!--- #-lucee-debug-#sectionId# !--->
					</tr>
				</table>
			</cfif>

			<!--- Traces --->
			<cfif traces.recordcount>

				<cfset sectionId = "Trace">
				<cfset isOpen = this.isSectionOpen( sectionId )>

				<div class="section-title">Trace Points</div>

				<cfset hasAction=!isColumnEmpty(traces,'action') />
				<cfset hasCategory=!isColumnEmpty(traces,'category') />

				<table>

					<cfset renderSectionHeadTR( sectionId, "#traces.recordcount# Trace Point#( traces.recordcount GT 1 ) ? 's' : ''#" )>

					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table class="details">
								<tr>
									<th>Type</th>
									<cfif hasCategory>
										<th>Category</th>
									</cfif>
									<th>Text</th>
									<th>Template</th>
									<th>Line</th>
									<cfif hasAction>
										<th>Action</th>
									</cfif>
									<th>Var</th>
									<th>Total Time</th>
									<th>Trace Slot Time</th>
								</tr>
								<cfset total=0 />
								<cfloop query="traces">
									<cfset total=total+traces.time />
									<tr>
										<td>#traces.type#</td>
										<cfif hasCategory>
											<td>#traces.category#&nbsp;</td>
										</cfif>
										<td>#traces.text#&nbsp;</td>
										<td>#traces.template#</td>
										<td class="txt-r">#traces.line#</td>
										<cfif hasAction>
											<td>#traces.action#</td>
										</cfif>
										<td>
											<cfif len(traces.varName)>
												#traces.varName#
												<cfif structKeyExists(traces,'varValue')>
													= #traces.varValue#
												</cfif>
											<cfelse>
												&nbsp;
												<br />
											</cfif>
										</td>
										<td class="txt-r">#unitFormat(arguments.custom.unit, total,prettify)#</td>
										<td class="txt-r">#unitFormat(arguments.custom.unit, traces.time,prettify)#</td>
									</tr>
								</cfloop>

							</table>
						</td><!--- #-lucee-debug-#sectionId# !--->
					</tr>
				</table>
			</cfif>
			<!--- Dumps --->
			<cfif dumps.recordcount>

				<cfset sectionId = "Dump">
				<cfset isOpen = this.isSectionOpen( sectionId )>

				<div class="section-title">Dumps</div>

				<table>

					<cfset renderSectionHeadTR( sectionId, "#dumps.recordcount# Dump#( dumps.recordcount GT 1 ) ? 's' : ''#" )>

					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							<table class="details">
								<tr>
									<th>Output</th>
									<th>Template</th>
									<th>Line</th>
								</tr>
								<cfset total=0 />
								<cfloop query="dumps">
									<tr>
										<td>#dumps.output#</td>
										<td>#dumps.template#</td>
										<td class="txt-r">#dumps.line#</td>
									</tr>
								</cfloop>
							</table>
						</td>
					</tr>
				</table>
			</cfif>

			<!--- Expression --->
			<cfif isEnabled( arguments.custom, "expression" )>
				<cfset sectionId = "Expression">
				<cfset isOpen = this.isSectionOpen( sectionId )>
				<div class="section-title">Expressions</div>
				<cfset cookie[cookieExpressions] = cookie[cookieExpressions] ?: ''>
				<cfset local.aExpressions = listToArray(cookie[cookieExpressions], "|")>
				<table>
					<cfset renderSectionHeadTR( sectionId, "#aExpressions.len()# Expression#( aExpressions.len() neq 1 ) ? 's' : ''#" )>
					<tr>
						<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
							New expression: <input type="text" id="expr_newValue">&nbsp;
							<a onClick="__LUCEE.debug.addExpression()">Add</a><br>
							<table class="details">
								<tr>
									<th>Expression</th>
									<th>Value</th>
								</tr>
								<cfset total=0 />
								<cfloop array="#aExpressions#" index="local.iEx">
									<tr>
										<td>#iEx#</td>
										<td>
											<cftry>
												#dump(evaluate(iEx))#
												<cfcatch>
													<span style="color:red">Error retreiving value</span>
												</cfcatch>
											</cftry>
										</td>
									</tr>
								</cfloop>
							</table>
							<a onClick="__LUCEE.debug.clearExpressions()">Clear all</a>
						</td>
					</tr>
				</table>
			</cfif>
			</cfoutput>
		</div>
	</cffunction>

	<cffunction name="readMetrics" returntype="void"  >
		<cfoutput>
			<cfset systemInfo=GetSystemMetrics()>
			<cfset sectionId = "scopesInMemory">
			<cfset isOpen = this.isSectionOpen( sectionId, "metrics" )>
			<table class="chartDetails">
				<cfset renderSectionHeadTR2( "#sectionId#", "Scopes in Memory", "", "metrics" )>
				<tr>
					<td id="-lucee-metrics-#sectionId#" class="#isOpen ? '' : 'collapsed'#" >
						<table class="details" style="text-align: left;">
							<tbody>
								<tr>
									<th rowspan="3" scope="row" style="width: 39%;">
										<span class="chartTitle"><b>Scopes in Memory</b></span><br>
										<span class="comment_debugInfo">Scopes actually hold in Memory (a Scope not necessary is kept in Memory for it's hole life time).</span>
									</th>
									<td style="width:30%"><b>Application</b></td>
									<td style="width:10%" align="right">#systemInfo.applicationContextCount#</td>
								</tr>
								<tr>
									<td style="width:30%"><b>Session</b></td>
									<td style="width:10%" align="right">#systemInfo.sessionCount#</td>
								</tr>
								<tr>
									<td style="width:30%"><b>Client</b></td>
									<td style="width:10%" align="right">#systemInfo.clientCount#</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
			</table>
			<cfset sectionId = "request_Threads">
			<cfset isOpen = this.isSectionOpen( sectionId, "metrics" )>
			<table class="chartDetails">
				<cfset renderSectionHeadTR2( "#sectionId#", "Request/Threads", "", "metrics" )>
				<tr>
					<td id="-lucee-metrics-#sectionId#" class="#isOpen ? '' : 'collapsed'#" >
						<table class="details" style="text-align: left;">
							<tbody>
								<tr>
									<th rowspan="3" scope="row" style="width: 39%;">
										<span class="chartTitle"><b>Request/Threads</b></span><br>
										<span class="comment_debugInfo">Request and threads (started by &lt;cfthread&gt;) currently running on the system.</span>
									</th>
									<td style="width:30%"><b>Requests</b></td>
									<cfset nbr=systemInfo.activeRequests>
									<td style="width:10%" align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
								</tr>
								<tr>
									<td style="width:30%"><b>Queued Requests</b></td>
									<cfset nbr=systemInfo.activeThreads>
									 <td style="width:10%" align="right" <cfif nbr GTE 20> style="color:##cc0000"</cfif>>#nbr#</td>
								</tr>
								<tr>
									<td style="width:30%"><b>Threads</b></td>
									<cfset nbr=systemInfo.queueRequests>
									<td style="width:10%" align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
			</table>
			<cfset sectionId = "datasource_connection">
			<cfset isOpen = this.isSectionOpen( sectionId, "metrics" )>
			<table class="chartDetails">
				<cfset renderSectionHeadTR2( "#sectionId#", "Datasource Connections", "", "metrics" )>
				<tr>
					<td id="-lucee-metrics-#sectionId#" class="#isOpen ? '' : 'collapsed'#" >
						<table class="details" style="text-align: left;">
							<tbody>
								<tr>
									<th rowspan="2" scope="row" style="width: 39%;">
										<span class="chartTitle"><b>Datasource Connections</b></span><br>
										<span class="comment_debugInfo">Datasource Connection open at the Moment.</span>
									</th>
									<td style="width:30%">&nbsp;</td>
									<td style="width:10%">&nbsp;</td> 
								</tr>
								<tr>
									<td style="width:30%">&nbsp;</td>
									<cfset nbr=systemInfo.activeDatasourceConnections>
									<td style="width:10%" align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td> 
								</tr>
							</tbody>
						</table>
					</tr>
				</tr>
			</table>
			<cfset sectionId = "task_Spooler">
			<cfset isOpen = this.isSectionOpen( sectionId, "metrics" )>
			<table class="chartDetails">
				<cfset renderSectionHeadTR2( "#sectionId#", "Task Spooler", "", "metrics" )>
				<tr>
					<td id="-lucee-metrics-#sectionId#" class="#isOpen ? '' : 'collapsed'#" >
						<table class="details" style="text-align: left;">
							<tbody>
								<tr>
									<th rowspan="2" scope="row" style="width: 39%;">
										<span class="chartTitle"><b>Task Spooler</b></span><br>
										<span class="comment_debugInfo">Active and closed tasks in Task Spooler. This includes for exampe tasks to send mails.</span>
									</th>
									<td style="width:30%"><b>Open</b></td>
									<cfset nbr=systemInfo.tasksOpen>
									<td style="width:10%" align="right" <cfif nbr GTE 50> style="color:##cc0000"</cfif>>#nbr#</td>
								</tr>
								<tr>
									<td style="width:30%"><b>Close</b></td>
									<cfset nbr=systemInfo.tasksClosed>
									<td style="width:10%" align="right" <cfif nbr GTE 20> style="color:##cc0000"</cfif>>#nbr#</td> 
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
			</table>
		</cfoutput>
	</cffunction>

	<cffunction name="readDocs" returntype="void"  >
		<cfset str = {}>
		<cfset str.functions  = getAllFunctions()>
		<cfset str.tags = getAllTags()>
		<cfset str.components = getAllComponents()>
		<cfset allArryItem = []>
		<cfloop collection="#str#" index="lst">
			<cfloop array=#str[lst]# index="i">
				<cfset arrayAppend(allArryItem, i)>
			</cfloop>
		</cfloop>
		<cfif structKeyExists(url, "fromAdmin") AND url.fromAdmin EQ true>
			<cfset maxCols = 3>
			<cfset searchClass = "SearchField1">
		<cfelse>
			<cfset maxCols = 6>
			<cfset searchClass = "SearchField2">
		</cfif>
		<!--- <div class="section-title">Debugging Information</div> --->
		<cfoutput>
			<cfset sectionId = "docs_Info">
			<cfset isOpen = this.isSectionOpen( sectionId, "docs" )>
			<input class="InputSearch #searchClass# menu-search-focus" id="lucee-docs-search-input" placeholder="Search" type="search">
			<div class="section-title" style="padding-bottom:4px;">Reference</div>
			<table>
				<tr>
				<td>
					<div class="leftPart">
						<table>
							<tr>
								<td>
									The documentation here aims to provide a thorough reference for the Lucee Server. You will find reference material on Lucee <a href="#cgi.hostName#/lucee/doc/tags.cfm">tags</a>, <a href="#cgi.hostName#/lucee/doc/functions.cfm">functions</a>, <a href="#cgi.hostName#/lucee/doc/components.cfm">components</a> and <a href="#cgi.hostName#/lucee/doc/objects.cfm">objects</a>. <span>You can reach the online version of the Lucee Server documentation <a href="http://docs.lucee.org/">here</a>.#structKeyExists(url, "fromAdmin")#</span>
								</td>
							</tr>
						</table>
					</div>
				</td>
				</tr>
			</table>

			<cfset docsDesc = {}>
			<cfset docsDesc.tags = "Tags are at the core of Lucee Server's templating language. You can check out every tag that has been listed below.">
			<cfset docsDesc.functions = "Functions are at the core of Lucee Server's templating language. You can check out every Functions that has been listed below.">
			<cfset docsDesc.components = 'The packages listed here are based on the component mappings defined in the Lucee Administartor under "Archives & Resources/Component". Add your own packages here by register your components with a component mapping. What makes it easier to access your components. Test'>

			<table>
				<tr>
					<td >
						<table>
							<tr>
								<td class="paddingLeft">
									<cfloop collection="#Str#" index="key">
										<cfset sectionId = key>
										<cfset isOpen = this.isSectionOpen( sectionId, "docs" )>
										<table>
											<cfset renderSectionHeadTR2( "#Lcase(sectionId)#", "#key#", "", "docs" )>
											<tr>
												<td id="-lucee-docs-#Lcase(sectionId)#" class="#isOpen ? '' : 'collapsed'#" >
													<table>
														<tr>
															<td class="Desc">#docsDesc[key]#</td>
														</tr>
													</table>
													<table border="2" class="refTbl">
														<cfset count = 0>
														<cfloop array=#str[key]# index="i">
															<cfif count eq 0><tr></cfif>
																<td>
																	<a class="search_input" onclick="return callDesc('#key#', '#i#')">#i#</a>
																	<cfset count = count +1>
																</td>
															<cfif count EQ maxCols><cfset count = 0></tr></cfif>
														</cfloop>
													</table>
												</td>
											</tr>
										</table>
									</cfloop>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</cfoutput>
	</cffunction>

	<cffunction name="renderSectionHeadTR" output="#true#">

		<cfargument name="sectionId">
		<cfargument name="label1">
		<cfargument name="label2" default="">

		<cfset var isOpen = this.isSectionOpen( arguments.sectionId )>

		<tr>
			<td><a id="-lucee-debug-btn-#arguments.sectionId#" class="-lucee-icon-#isOpen ? 'minus' : 'plus'#" onclick="__LUCEE.debug.toggleSection( '#arguments.sectionId#' );">
				#arguments.label1#</a></td>
			<td class="pad"><a onclick="__LUCEE.debug.toggleSection( '#arguments.sectionId#' );">#arguments.label2#</a></td>
		</tr>
	</cffunction>

	<cffunction name="renderSectionHeadTR2" output="#true#">

		<cfargument name="sectionId">
		<cfargument name="label1">
		<cfargument name="label2" default="">
		<cfargument name="section" default="">

		<cfset var isOpen = this.isSectionOpen( "#arguments.sectionId#", "#section#" )>

		<tr>
			<td><a id="-lucee-#section#-btn-#arguments.sectionId#" class="-lucee-icon-#isOpen ? 'minus' : 'plus'#" onclick="__LUCEE.debug.toggleSection( '#arguments.sectionId#', '#section#' );">
				#arguments.label1#</a></td>
			<td class="pad"><a onclick="__LUCEE.debug.toggleSection( '#arguments.sectionId#', '#section#' );">#arguments.label2#</a></td>
		</tr>
	</cffunction> 

	<cffunction name="loadCharts" output="true">
		<cfargument name="chartStruct">
		<cfset chartsLabel = structNew("linked")>
		<cfset chartsLabel.HeapChart = "Heap Memory">
		<cfset chartsLabel.NonHeapChart = "Non Heap Memory">
		<cfset chartsLabel.WholeSystem = "CPU whole System">
		<cfset chartsLabel.LuceeProcess = "CPU Process only">

		<cfif structKeyExists(request, "fromAdmin") AND request.fromAdmin EQ true>
			<cfset chartClass = "twoCharts">
		<cfelse>
			<cfset chartClass = "fourCharts">
		</cfif>

		<cfloop item="i" collection="#chartsLabel#">
			<div class="chart_margin #chartClass#">
				<div style="text-align:center;  width: 280px; height: 180px; padding:7px; border-radius: 25px; border: 2px solid ##898989; -moz-box-sizing: unset !important">
					<div style="font-size: 14px;font-weight: bold;">#chartsLabel[i]#</div>
					<div id="#StructFind(arguments.chartStruct,"#i#")#" style="width: 250px; height: 130px; margin: 0 auto;"></div>
				</div>
			</div>
		</cfloop>
	</cffunction>

	<cfscript>

		function enableTab(tab){
			var tabIsPresent = ListFindNocase(variables.tabsPresent, arguments.tab);
			// find the tab which enabled in admin
			if(tabIsPresent > 0)
				return true;
			return false;
		}

		function unitFormat( string unit, numeric time, boolean prettify=false ) {
			if ( !arguments.prettify ) {
				return NumberFormat( arguments.time / 1000000, ",0.00" ) & arguments.unit;
			}
			// display 0 digits right to the point when more or equal to 100ms
			if ( arguments.time >= 100000000 )
				return int( arguments.time / 1000000 ) & arguments.unit;

			// display 1 digit right to the point when more or equal to 10ms
			if ( arguments.time >=  10000000 )
				return ( int( arguments.time / 100000 ) / 10 ) & arguments.unit;

			// display 2 digits right to the point when more or equal to 1ms
			if ( arguments.time >=   1000000 )
				return ( int( arguments.time / 10000 ) / 100 ) & arguments.unit;

			// display 3 digits right to the point
			return ( int( arguments.time / 1000 ) / 1000 ) & arguments.unit;
		}


		function byteFormat( numeric size ) {

			var values = [ [ 1099511627776, 'TB' ], [ 1073741824, 'GB' ], [ 1048576, 'MB' ], [ 1024, 'KB' ] ];

			for ( var i in values ) {

				if ( arguments.size >= i[ 1 ] )
					return numberFormat( arguments.size / i[ 1 ], '9.99' ) & i[ 2 ];
			}

			return arguments.size & 'B';
		}


		function includeFileInline( filename ) cachedWithin=createTimeSpan(0, 1, 0, 0) {

			echo( fileRead( expandPath( arguments.filename ) ) );
		}

		function getAllFunctions() {
			var result = getFunctionList().keyArray().sort( 'textnocase' ).filter( function( el ) { return left( el, 1 ) != '_'; } );

			return result;
		}

		function getAllTags() {

			var result = [];

			var itemList = getTagList();

			for ( local.ns in itemList.keyArray() ) {

				for ( local.key in itemList[ ns ].keyArray() ) {
					result.append( ns & key );
				}
			}
			result.sort( 'textnocase' );

			return result;
		}

		function  getAllComponents() {

			// getting available component packages
			tmpStr.componentDetails={};
			tmpStr.componentDetails.pack=["org.lucee.cfml"];


			arraySort(tmpStr.componentDetails.pack, "textnocase");
			tmpStr.componentDetails.cfcs=[];
			for(index=tmpStr.componentDetails.pack.len();index>0;index--) {
				currPack=tmpStr.componentDetails.pack[index];
				try{
					var tmpComponents=ComponentListPackage(currPack);
				}
				catch(e) {
					arrayDeleteAt(tmpStr.componentDetails.pack,index);
					continue;
				}

				for(i=1;i<=tmpComponents.len();i++){
					tmpComponents[i]=currPack&"."&tmpComponents[i];
				}
				if(tmpComponents.len()==0) {
					arrayDeleteAt(tmpStr.componentDetails.pack,index);
					continue;
				}
				else arrayAppend(tmpStr.componentDetails.cfcs, tmpComponents, true);
			}
			arraySort(tmpStr.componentDetails.cfcs, "textnocase");

			return tmpStr.componentDetails.cfcs;
		}

	    private string function getPctColor(required numeric iPct) {
    		local.iPct = 1 - arguments.iPct / 100;
    		return RGBtoHex(255 * iPct, 160 * (1 - iPct), 0);
		}

		private string function RGBtoHex(r,g,b){
			Var hexColor="";
			Var hexPart = '';
			Var i=0;

			/* Loop through the Arguments array, containing the RGB triplets */
			for (i=1; i lte 3; i=i+1){
				/* Derive hex color part */
				hexPart = formatBaseN(Arguments[i],16);
				/* Pad with "0" if needed */
				if (len(hexPart) eq 1){ hexPart = '0' & hexPart; } 

				/* Add hex color part to hexadecimal color string */
				hexColor = hexColor & hexPart;
			}
			return '##' & hexColor;
		}

		private string function dspCallStackTrace(required numeric id, required struct stPages, required query stHistory) {
			local.sRet = arguments.stPages[arguments.id].src;
			local.iLvl = arguments.stHistory.level[arguments.stPages[arguments.id].iHistPos];
			loop from="#arguments.stPages[arguments.id].iHistPos#" to="1" step="-1" index="local.iRec" {
				if (arguments.stHistory.level[iRec] lt iLvl) {
					if (!isEmpty(arguments.stPages[arguments.stHistory.id[iRec]].src ?: "")) {
						sRet &= " &laquo; " & (arguments.stPages[arguments.stHistory.id[iRec]].src ?: " - ");
						iLvl = arguments.stHistory.level[iRec];
					}
				};
			};
			return sRet;
		};

		private struct function filterByTemplates(required struct stDebugging) {
			local.stTemplates = {};
			local.stRet       = {filterActive:false};
			local.qPages      = arguments.stDebugging.pages;
			local.qHistory    = arguments.stDebugging.history;
			local.aTemplates  = listToArray(urlDecode(cookie[cookieFilterTemplates] ?: ''), '|');
			local.stTemplates = {};
			loop list="pages,queries,implicitAccess,timers,traces,exceptions,dumps" index="local.lst" {
				if (isQuery(arguments.stDebugging[lst])) {
					stRet[lst] = {
						initial:arguments.stDebugging[lst].recordCount
					};
					if (structKeyExists(arguments.stDebugging[lst], 'total')) {
						stRet[lst].sumTime = arraySum(valueArray(arguments.stDebugging[lst].total));
					}
					if (structKeyExists(arguments.stDebugging[lst], 'time')) {
						stRet[lst].sumTime = arraySum(valueArray(arguments.stDebugging[lst].time));
					}
				} else {
					stRet[lst] = {
						initial:arrayLen(arguments.stDebugging[lst])
					};
				}
			}
			loop array="#aTemplates#" index="local.i" item="local.sPath" {
				local.sReplPath = replace(listFirst(sPath, '!'), '/', server.separator.file, 'ALL');
				stTemplates[sReplPath] = {recurse: listLast(sPath, '!') eq 1};
			}
			if (isEmpty(stTemplates)) {
				return stRet;
			}
			stRet.stTemplates = duplicate(stTemplates);
			local.stDebugTemplates = duplicate(stTemplates);
			loop query="qPages" {
				local.stTemplates[qPages.id] = listFirst(qPages.src, "$");
				if (!isEmpty(stTemplates)) {
					if (structKeyExists(stTemplates, qPages.src)) {
						stDebugTemplates[qPages.src].id = qPages.id;
					}
				} else {
					stDebugTemplates[qPages.src] = {id:qPages.id, template:qPages.src, recurse:false};
				}
			}

			// jetzt aus der history die includeten templates rauslesen
			loop collection="#stDebugTemplates#" index="local.sTemplate" item="local.stSettings" {
				if (!stSettings.recurse) {
					continue;
				}
				local.iLevel = 0;
				loop query="qHistory" {
					if (qHistory.id eq (stSettings.id ?: 0)) {
						iLevel = qHistory.level;
						continue;
					};
					if (iLevel gt 0) {
						if (iLevel lt qHistory.level) {
							stDebugTemplates[stTemplates[qHistory.id]] = {id:qHistory.id, recurse:false};
						} else {
							iLevel = 0;
						}
					} else {
						continue;
					}
				}
			}
			// get a list of templates and a list of id's
			local.lstIDs = "";
			local.lstTemplates = "";

			structEach(stDebugTemplates, function(string sKey, any oValue) {
				if (!isEmpty(oValue.id ?: "")) {
					lstIDs       = listAppend(lstIDs, oValue.id);
					lstTemplates = listAppend(lstTemplates, "'#arguments.sKey#'");
				}
			});

			// now we got them, so let's filter the stuff
			// pages
			query dbtype="query" name="arguments.stDebugging.pages" {
				echo("
					SELECT * from arguments.stDebugging.pages where id in (#lstIDs#)
					");
			}
			loop from="#arguments.stDebugging.queries.recordCount#" to="1" step="-1" index="local.iRec" {
				if (!structKeyExists(stDebugTemplates, arguments.stDebugging.queries.src[iRec])) {
					queryDeleteRow(arguments.stDebugging.queries, iRec);
				}
			}

			loop from="#arguments.stDebugging.implicitAccess.recordCount#" to="1" step="-1" index="local.iRec" {
				if (!structKeyExists(stDebugTemplates, arguments.stDebugging.implicitAccess.template[iRec])) {
					queryDeleteRow(arguments.stDebugging.implicitAccess, iRec);
				}
			}

			loop from="#arguments.stDebugging.timers.recordCount#" to="1" step="-1" index="local.iRec" {
				if (!structKeyExists(stDebugTemplates, arguments.stDebugging.timers.template[iRec])) {
					queryDeleteRow(arguments.stDebugging.timers, iRec);
				}
			}

			loop from="#arguments.stDebugging.traces.recordCount#" to="1" step="-1" index="local.iRec" {
				if (!structKeyExists(stDebugTemplates, arguments.stDebugging.traces.template[iRec])) {
					queryDeleteRow(arguments.stDebugging.traces, iRec);
				}
			}

			loop from="#arrayLen(arguments.stDebugging.exceptions)#" to="1" step="-1" index="local.iRec" {
				local.sErrorTemplate = arguments.stDebugging.exceptions[iRec].tagContext[1].template ?: "";
				if (!structKeyExists(stDebugTemplates, sErrorTemplate)) {
					arrayDeleteAt(arguments.stDebugging.exceptions, iRec);
				}
			}

			loop from="#arguments.stDebugging.dumps.recordCount#" to="1" step="-1" index="local.iRec" {
				if (!structKeyExists(stDebugTemplates, arguments.stDebugging.dumps.template[iRec])) {
					arrayDeleteAt(arguments.stDebugging.dumps, iRec);
				}
			}

			loop list="pages,queries,implicitAccess,timers,traces,exceptions,dumps" index="local.lst" {
				if (isQuery(arguments.stDebugging[lst])) {
					stRet[lst].filtered = arguments.stDebugging[lst].recordCount;
					if (structKeyExists(arguments.stDebugging[lst], 'total')) {
						stRet[lst].sumFilteredTime = arraySum(valueArray(arguments.stDebugging[lst].total));
					}
					if (structKeyExists(arguments.stDebugging[lst], 'time')) {
						stRet[lst].sumFilteredTime = arraySum(valueArray(arguments.stDebugging[lst].time));
					}
				} else {
					stRet[lst].filtered = arrayLen(arguments.stDebugging[lst]);
				}
				if (stRet[lst].filtered neq stRet[lst].initial) {
					stRet.filterActive = true;
				}
			}
			return stRet;
		}
	</cfscript>
</cfcomponent>

