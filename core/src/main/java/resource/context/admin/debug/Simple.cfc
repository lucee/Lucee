<cfcomponent extends="Debug" output="no">

	<cfscript>
		fields=array(
			  group("Execution Time","Execution times for templates, includes, modules, custom tags, and component method calls. Template execution times over this minimum highlight time appear in red.",3)
			, field("Minimal Execution Time","minimal","0",true,{_appendix:"microseconds",_bottom:"Execution times for templates, includes, modules, custom tags, and component method calls. Outputs only templates taking longer than the time (in microseconds) defined above."},"text40")
			, field("Highlight","highlight","250000",true,{_appendix:"microseconds",_bottom:"Highlight templates taking longer than the following (in microseconds) in red."},"text50")
			, field("Time format","timeFormat","standard",true,
			{_top:"Format used for time values.",
			standard:"Always display fratcal number part of a number (123.001)",
			natural:"Only display fratcal number part for small numbers (123)"
			},"radio","standard,natural")
			, group("Custom Debugging Output","Define what is outputted",3)
			, field("Scope Variables","scopes","Enabled",false,"Enable Scope reporting","checkbox","Enabled")
			, field("General Debug Information ","general","Enabled",false,
				"Select this option to show general information about this request. General items are Lucee Version, Template, Time Stamp, User Locale, User Agent, User IP, and Host Name. ",
				"checkbox","Enabled")
		);

		string function getLabel(){
			return "Simple";
		}

		string function getDescription(){
			return "Simple debug template that is functional without JavaScript";
		}

		string function getid(){
			return "lucee-simple";
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
			if (!QueryColumnExists(qry,columnName))
				return true;
			return isEmpty(arrayToList(queryColumnData(qry,columnName), ""));
		}

		function isSectionOpen(string name) {
			try{
				if (arguments.name == "ALL" && !structKeyExists(Cookie, variables.cookieName))
					return true;

				var cookieValue = structKeyExists(Cookie, variables.cookieName) ? Cookie[ variables.cookieName ] : 0;

				return cookieValue && (bitAnd(cookieValue, this.allSections[ arguments.name ]));
			}
			catch(e){
				return false;
			}
		}

		function isEnabled( custom, key ) {

			return structKeyExists( arguments.custom, arguments.key ) && ( arguments.custom[ arguments.key ] == "Enabled" || arguments.custom[ arguments.key ] == "true" );
		}

		variables.cookieName = "lucee_debug_simple";
		variables.bitmaskAll = 2 ^ 31 - 1;
		variables.scopeNames = [ "Application", "CGI", "Client", "Cookie", "Form", "Request", "Server", "Session", "URL" ];

		function buildSectionStruct() {

			var otherSections = [ "ALL", "Dump", "ExecTime", "ExecOrder", "Exceptions", "ImpAccess", "Info", "Query", "Timer", "Trace", "More" ];
			var i = 0;

			var result = {};

			for ( var k in otherSections )
				result[ k ] = 2 ^ i++;

			for ( var k in variables.scopeNames )
				result[ k ] = 2 ^ i++;

			return result;
		}
	</cfscript>

	<cffunction name="output" returntype="void">
		<cfargument name="custom" type="struct" required="yes" />
		<cfargument name="debugging" required="true" type="struct" />
		<cfargument name="context" type="string" default="web" />
		<cfsilent>
		<cfif !structKeyExists(arguments.custom,'minimal')><cfset arguments.custom.minimal="0"></cfif>
		<cfif !structKeyExists(arguments.custom,'highlight')><cfset arguments.custom.highlight="250000"></cfif>
		<cfif !structKeyExists(arguments.custom,'scopes')><cfset arguments.custom.scopes=false></cfif>
		<cfif !structKeyExists(arguments.custom,'general')><cfset arguments.custom.general="Enabled"></cfif>

		<cfscript>
			var time=getTickCount();
			var _cgi = arguments?.debugging?.scope?.cgi ?: cgi

			if(isNull(arguments.debugging.pages)) 
				local.pages=queryNew('id,count,min,max,avg,app,load,query,total,src');
			else local.pages=arguments.debugging.pages;

			var hasQueries=!isNull(arguments.debugging.queries);
			if(!hasQueries) 
				local.queries=queryNew('name,time,sql,src,line,count,datasource,usage,cacheTypes');
			else local.queries=arguments.debugging.queries;

			if(isNull(arguments.debugging.exceptions)) 
				local.exceptions=[];
			else local.exceptions=arguments.debugging.exceptions;

			if(isNull(arguments.debugging.timers)) 
				local.timers=queryNew('label,time,template');
			else local.timers=arguments.debugging.timers;

			if(isNull(arguments.debugging.traces)) 
				local.traces=queryNew('type,category,text,template,line,var,total,trace');
			else local.traces=arguments.debugging.traces;

			if(isNull(arguments.debugging.dumps)) 
				local.dumps=queryNew('output,template,line');
			else local.dumps=arguments.debugging.dumps;

			if(isNull(arguments.debugging.implicitAccess)) 
				local.implicitAccess=queryNew('template,line,scope,count,name');
			else local.implicitAccess=arguments.debugging.implicitAccess;

			if(isNull(arguments.debugging.dumps)) 
				local.dumps=queryNew('output,template,line');
			else local.dumps=arguments.debugging.dumps;

			local.times=arguments.debugging.times;
		</cfscript>




		
		<cfset this.allSections = this.buildSectionStruct()>
		<cfset var isExecOrder  = this.isSectionOpen( "ExecOrder" )>

		<cfif isExecOrder>

			<cfset querySort(pages,"id","asc") />
		<cfelse>

			<cfset querySort(pages,"avg","desc") />
		</cfif>

		<cfset querySort(implicitAccess,"template,line,count","asc,asc,desc") />
		<cfparam name="arguments.custom.unit" default="millisecond">
		<cfparam name="arguments.custom.color" default="black">
		<cfparam name="arguments.custom.bgcolor" default="white">
		<cfparam name="arguments.custom.font" default="Times New Roman">
		<cfparam name="arguments.custom.size" default="medium">
		<cfset var unit={
			millisecond:"ms"
			,microsecond:"µs"
			,nanosecond:"ns"
			} />

		<cfset var ordermap={}>
		<cfloop query="#arguments.debugging.history#">
			<cfif !structkeyExists(ordermap, arguments.debugging.history.id)><cfset ordermap[ arguments.debugging.history.id ]=structCount(ordermap)+1></cfif>
		</cfloop>
		<cfset var prettify=structKeyExists(arguments.custom,'timeformat') and arguments.custom.timeformat EQ "natural">
		</cfsilent>
		<cfif arguments.context EQ "web">
			</td></td></td></th></th></th></tr></tr></tr></table></table></table></a></abbrev></acronym></address></applet></au></b></banner></big></blink></blockquote></bq></caption></center></cite></code></comment></del></dfn></dir></div></div></dl></em></fig></fn></font></form></frame></frameset></h1></h2></h3></h4></h5></h6></head></i></ins></kbd></listing></map></marquee></menu></multicol></nobr></noframes></noscript></note></ol></p></param></person></plaintext></pre></q></s></samp></script></select></small></strike></strong></sub></sup></table></td></textarea></th></title></tr></tt></u></ul></var></wbr></xmp>
		</cfif>
		<style type="text/css">
			#-lucee-debug 			{ margin: 2.5em 1em 0 1em; padding: 1em; background-color: #FFF; color: #222; border: 1px solid #CCC; border-radius: 5px; text-shadow: none; }
			#-lucee-debug.collapsed	{ padding: 0; border-width: 0; }
			#-lucee-debug legend 	{ padding: 0 1em; background-color: #FFF; color: #222; }
			#-lucee-debug legend span { font-weight: normal; }

			#-lucee-debug, #-lucee-debug td	{ font-family: Helvetica, Arial, sans-serif; font-size: 9pt; line-height: 1.35; }
			#-lucee-debug.large, #-lucee-debug.large td	{ font-size: 10pt; }
			#-lucee-debug.small, #-lucee-debug.small td	{ font-size: 8.5pt; }

			#-lucee-debug table		{ empty-cells: show; border-collapse: collapse; border-spacing: 0; }
			#-lucee-debug table.details	{ margin-top: 0.5em; border: 1px solid #ddd; margin-left: 9pt; max-width: 100%; }
			#-lucee-debug table.details th { font-size: 9pt; font-weight: normal; background-color: #f2f2f2; color: #3c3e40; }
			#-lucee-debug table.details td, #-lucee-debug table.details th { padding: 2px 4px;  border: 1px solid #ddd; }

			#-lucee-debug .section-title	{ margin-top: 1.25em; font-size: 1.25em; font-weight: normal; color:#555; }
			#-lucee-debug .section-title:first-child	{ margin-top: auto; }
			#-lucee-debug .label		{ white-space: nowrap; vertical-align: top; text-align: right; padding-right: 1em; background-color: inherit; color: inherit; text-shadow: none; }

		<cfif structKeyExists(Cookie, variables.cookieName)>
			#-lucee-debug .collapsed	{ display: none; }
		</cfif>

			#-lucee-debug .bold 		{ font-weight: bold; }
			#-lucee-debug .txt-c 	{ text-align: center; }
			#-lucee-debug .txt-l 	{ text-align: left; }
			#-lucee-debug .txt-r 	{ text-align: right; }
			#-lucee-debug .faded 	{ color: #999; }
			#-lucee-debug .ml14px 	{ margin-left: 14px; }
			#-lucee-debug table.details td.txt-r { padding-right: 1em; }
			#-lucee-debug .num-lsv 	{ font-weight: normal; }
			#-lucee-debug tr.nowrap td { white-space: nowrap; }
			#-lucee-debug tr.red td, #-lucee-debug .red 	{ background-color: #FDD; }

			#-lucee-debug .sortby.selected, #-lucee-debug .sortby:hover { background-color: #25A; color: #FFF !important; cursor: pointer; text-decoration: none;}
			.sortby { text-decoration: underline; font-weight: bold; }
			#-lucee-debug table.details tr > th > a { color: #25A !important; text-decoration: underline; }
			#-lucee-debug .pad 	{ padding-left: 16px; }
			#-lucee-debug a 	{ cursor: pointer; }
			#-lucee-debug td a 	{ color: #25A; }
			#-lucee-debug .warning{ color: red; }
			#-lucee-debug td a:hover	{ color: #58C; text-decoration: underline; }
			#-lucee-debug pre 	{ background-color: #EEE; padding: 1em; border: solid 1px #333; border-radius: 1em; white-space: pre-wrap; word-break: break-all; word-wrap: break-word; tab-size: 2; }

			.-lucee-icon-plus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIRhI+hG7bwoJINIktzjizeUwAAOw==) no-repeat left center; padding: 4px 0 4px 16px; }
			.-lucee-icon-minus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIQhI+hG8brXgPzTHllfKiDAgA7)     no-repeat left center; padding: 4px 0 4px 16px; }
		</style>

		<cfoutput>

			<cfset var sectionId = "ALL">
			<cfset var isOpen = this.isSectionOpen( sectionId )>

			<!-- Lucee Debug Output !-->
			<fieldset id="-lucee-debug" class="#arguments.custom.size# #isOpen ? '' : 'collapsed'#">

				<legend><a id="-lucee-debug-btn-#sectionId#" class="-lucee-icon-#isOpen ? 'minus' : 'plus'#" onclick="__LUCEE.debug.toggleSection( '#sectionId#' ) ? __LUCEE.util.removeClass('-lucee-debug', 'collapsed') : __LUCEE.util.addClass('-lucee-debug', 'collapsed');">
				 Lucee Debug Output</a> <span>(#this.getLabel()#)</span></legend>

				<div id="-lucee-debug-ALL" class="#isOpen ? '' : 'collapsed'#">

					<!--- General --->
					<cfif isEnabled( arguments.custom, 'general' )>

						<div class="section-title">Debugging Information</div>
					    <cfif getJavaVersion() LT 8 >
							<div class="warning">
								You are running Lucee with Java #server.java.version# Lucee does not formally support this version of Java. Consider updating to the latest Java version for security and performance reasons.
							</div>
					    </cfif>

						<cfset sectionId = "Info">
						<cfset isOpen = this.isSectionOpen( sectionId )>
						<table>

							<cfset renderSectionHeadTR( sectionId, "Template:", "#encodeForHtml(_cgi.REQUEST_URL)#<br>#encodeForHtml(expandPath(_cgi.SCRIPT_NAME))#" )>

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
												#ucFirst(server.coldfusion.productlevel)# #server.lucee.version# (CFML Version #server.ColdFusion.ProductVersion#)
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

					<!--- Abort --->
					<cfif structKeyExists(arguments.debugging,"abort")>
						<div class="section-title">Abort</div>
						<table>
							<tr>
								<td class="pad txt-r">#debugging.abort.template#:#debugging.abort.line#</td>
							</tr>
						</table>
					</cfif>

					<!--- Execution Time --->
					<cfset sectionId = "ExecTime">
					<cfset isOpen = this.isSectionOpen( sectionId )>

					<div class="section-title">Execution Time</div>
					<cfset local.loa=0>
					<cfset local.tot=0>
					<cfset local.q=0>

					<cfloop query="pages">
						<cfset tot=tot+pages.total>
						<cfset q=q+pages.query>
						<cfif pages.avg LT arguments.custom.minimal*1000>
							<cfcontinue>
						</cfif>
						<cfset local.bad=pages.avg GTE arguments.custom.highlight*1000>
						<cfset loa=loa+pages.load />
					</cfloop>
					<cfif !hasQueries>
						<cfset q=arguments.debugging.times.query>
					</cfif>
					<cfif pages.recordcount EQ 0>
						<cfset tot=arguments.debugging.times.total>
						<cfset q=arguments.debugging.times.query>
						<cfset loa=0>
					</cfif>

					<table>
						<cfset renderSectionHeadTR( sectionId
							, "#unitFormat( arguments.custom.unit, tot, prettify )# ms
								&nbsp;&nbsp;&nbsp; Total" )>

						<tr><td <cfif !pages.recordcount>  id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#"</cfif>><table>
							<tr>
								<td class="pad txt-r">#unitFormat( arguments.custom.unit, loa,prettify )# ms</td>
								<td class="pad">Startup/Compilation</td>
							</tr>
							<tr>
								<td class="pad txt-r bold">#unitFormat( arguments.custom.unit, tot-q-loa, prettify )# ms</td>
								<td class="pad bold">Application</td>
							</tr>
							<tr>
								<td class="pad txt-r">#unitFormat( arguments.custom.unit, q,prettify )# ms</td>
								<td class="pad">Query</td>
							</tr>
						</table></td></tr>
						<cfif pages.recordcount>
						<tr>
							<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
								<table class="details">
									<tr>
										<th>Total Time (ms)</th>
										<th>Count</th>
										<th><cfif isExecOrder><a onclick="__LUCEE.debug.clearFlag( 'ExecOrder' ); __LUCEE.util.addClass( this, 'selected' );" class="sortby" title="Order by Avg Time (starting with the next request)">Avg Time</a><cfelse>Avg Time</cfif> (ms)</th>
										<th>Template</th>
										<th><cfif isExecOrder>Order<cfelse><a onclick="__LUCEE.debug.setFlag( 'ExecOrder' ); __LUCEE.util.addClass( this, 'selected' );" class="sortby" title="Order by ID (starting with the next request)">Order</a></cfif></th>
									</tr>
									<cfset loa=0>
									<cfset tot=0>
									<cfset q=0>
									<cfset var hasBad = false>
									<cfloop query="pages">
										<cfset tot=tot+pages.total>
										<cfset q=q+pages.query>
										<cfif pages.avg LT arguments.custom.minimal * 1000>
											<cfcontinue>
										</cfif>
										<cfset bad=pages.avg GTE arguments.custom.highlight * 1000>
										<cfif bad>
											<cfset hasBad = true>
										</cfif>
										<cfset loa=loa+pages.load>
										<tr class="nowrap #bad ? 'red' : ''#">
											<td class="txt-r" title="#pages.total - pages.load#">#unitFormat(arguments.custom.unit, pages.total-pages.load,prettify)#</td>
											<td class="txt-r">#pages.count#</td>
											<td class="txt-r" title="#pages.avg#"><cfif pages.count GT 1>#unitFormat(arguments.custom.unit, pages.avg,prettify)#<cfelse>-</cfif></td>
											<td id="-lucee-debug-pages-#pages.currentRow#" oncontextmenu="__LUCEE.debug.selectText( this.id );">#pages.src#</td>
											<td class="txt-r faded" title="#pages.id#">#ordermap[pages.id]#</td>
										</tr>
									</cfloop>
									<cfif hasBad>
										<tr class="red"><td colspan="3">red = over #unitFormat( arguments.custom.unit, arguments.custom.highlight * 1000 ,prettify)# ms average execution time</td></tr>
									</cfif>
								</table>
							</td><!--- #-lucee-debug-#sectionId# !--->
						</tr></cfif>
					</table>


					<cfset this.doMore( arguments.custom, arguments.debugging, arguments.context )>


					<!--- Exceptions --->
					<cfif structKeyExists( arguments.debugging, "exceptions" ) && arrayLen( exceptions )>

						<cfset sectionId = "Exceptions">
						<cfset isOpen = this.isSectionOpen( sectionId )>

						<div class="section-title">Exceptions</div>
						<table>

							<cfset renderSectionHeadTR( sectionId, "#arrayLen(exceptions)# Exception#arrayLen( exceptions ) GT 1 ? 's' : ''# Caught" )>

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
										<cfloop array="#exceptions#" index="local.exp">
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
											<th>Time (ms)</th>
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
											<th>Total Time (ms)</th>
											<th>Trace Slot Time (ms)</th>
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


					<!--- Queries --->
					<cfif queries.recordcount>

						<cfset sectionId = "Query">
						<cfset isOpen = this.isSectionOpen( sectionId )>
						<cfset local.total  =0>
						<cfset local.records=0>
						<cfset local.openConns=0>
						<cfloop struct="#arguments.debugging.datasources#" index="dsn" item="item">
							<cfset local.openConns=item.openConnections>
						</cfloop>

						<cfloop query="queries">
							<cfset total   += queries.time>
							<cfset records += queries.count>
						</cfloop>
						<div class="section-title">Datasource Information</div>
						<table>
							<cfset renderSectionHeadTR( sectionId, "#queries.recordcount# Quer#queries.recordcount GT 1 ? 'ies' : 'y'# Executed (Total Records: #records#; Total Time: #unitFormat( arguments.custom.unit, total ,prettify)# ms; Total Open Connections: #openConns#)" )>

							<tr>
								<td id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'#">


									<table><tr><td>
										<b>General</b>
										<table class="details">
										<tr>
											<th>Name</th>
											<th>Open Connections</th>
											<th>Max Connections</th>
										</tr>
										<cfloop struct="#arguments.debugging.datasources#" index="local.dsName" item="local.dsData">
										<tr>
											<td class="txt-r">#dsData.name#</td>
											<td class="txt-r">#dsData.openConnections#</td>
											<td class="txt-r">#dsData.connectionLimit==-1?'INF':dsData.connectionLimit#</td>
										</tr>
										</cfloop>
										</table>
									<cfset hasCachetype=ListFindNoCase(queries.columnlist,"cachetype") gt 0>
									<br><b>SQL Queries</b>
										<cfloop query="queries">

											<table class="details">
												<tr>
													<th></th>
													<th>Name</th>
													<th>Records</th>
													<th>Time (ms)</th>
													<th>Datasource</th>
													<th>Source</th>
													<cfif hasCachetype><th>Cache Type</th></cfif>

												</tr>
												<tr>
													<th></th>
													<td>#queries.name#</td>
													<td class="txt-r">#queries.count#</td>
													<td class="txt-r">#unitFormat(arguments.custom.unit, queries.time,prettify)#</td>
													<td>#queries.datasource#</td>
													<td>#queries.src#</td>
													<cfif hasCachetype><td>#isEmpty(queries.cacheType)?"none":queries.cacheType#</td></cfif>
												</tr>
												<tr>
													<th class="label">SQL:</th>
													<td id="-lucee-debug-query-sql-#queries.currentRow#" colspan="6" oncontextmenu="__LUCEE.debug.selectText( this.id );"><pre>#trim( queries.sql )#</pre></td>
												</tr>

												<cfif listFindNoCase(queries.columnlist, 'usage') && isStruct(queries.usage)>

													<cfset local.usage=queries.usage>
													<cfset local.usageNotRead = []>
													<cfset local.usageRead  = []>

													<cfloop collection="#usage#" index="local.item" item="local.value">
														<cfif !value>
															<cfset arrayAppend( usageNotRead, item )>
														<cfelse>
															<cfset arrayAppend( usageRead, item )>
														</cfif>
													</cfloop>

													<tr>
														<th colspan="7"><b>Query usage within the request</b></th>
													</tr>

													<cfset local.arr = usageRead>
													<cfset local.arrLenU = arrayLen( arr )>
													<cfif arrLenU>
														<tr>
															<td colspan="7">
																Used:<cfloop from="1" to="#arrLenU#" index="local.ii">
																	#arr[ ii ]# <cfif ii LT arrLenU>, </cfif>
																</cfloop>
															</td>
														</tr>
													</cfif>
													<cfset local.arr = usageNotRead>
													<cfset local.arrLenN = arrayLen( arr )>
													<cfif arrLenN>
														<tr class="red">
															<td colspan="7">
																Unused:
																<cfloop from="1" to="#arrLenN#" index="local.ii">
																	#arr[ ii ]# <cfif ii LT arrLenN>, </cfif>
																</cfloop>
															</td>
														</tr>
														<tr class="red">
															<td colspan="7"><b>#arrLenU ? numberFormat( arrLenU / ( arrLenU + arrLenN ) * 100, "999.9" ) : 100# %</b></td>
														</tr>
													</cfif>
												</cfif>

											</table>

										</cfloop>

									</tr></td></table>
								</td><!--- #-lucee-debug-#sectionId# !--->
							</tr>
						</table>
					</cfif>

					<!--- Scopes --->
					<cfif isEnabled( arguments.custom, "scopes" )>

						<cfset local.scopes = variables.scopeNames>

						<cfset local.appSettings = getApplicationSettings()>
						<cfset local.isScopeEnabled = true>

						<div class="section-title">Scope Information</div>
						<table cellpadding="0" cellspacing="0">

							<cfloop array="#local.scopes#" index="local.k">

								<tr><td style="font-size: 4px;">&nbsp;</td></tr>

								<cfset sectionId = k>
								<cfswitch expression="#k#">

									<cfcase value="Client">

										<cfset isScopeEnabled = local.appSettings.clientManagement>
									</cfcase>
									<cfcase value="Session">

										<cfset isScopeEnabled = local.appSettings.sessionManagement>
									</cfcase>
									<cfdefaultcase>

										<cfset isScopeEnabled = true>
									</cfdefaultcase>
								</cfswitch>

								<cfif isScopeEnabled>

									<cfset isOpen = this.isSectionOpen( sectionId )>
									<cfset local.v = evaluate( k )>
									<cfset local.sc = structCount( v )>

									<cftry>

										<cfset local.estSize = byteFormat( sc == 0 ? 0 : sizeOf( v ) )>

										<cfcatch>

											<cfset local.estSize = "not available">
										</cfcatch>
									</cftry>

									<cfset renderSectionHeadTR( sectionId, "<b>#k# Scope</b> #sc ? '(~#estSize#)' : '(Empty)' #" )>

									<tr><td colspan="3">

										<table id="-lucee-debug-#sectionId#" class="#isOpen ? '' : 'collapsed'# ml14px"><tr><td>

											<cfif isOpen>
												<cftry><cfdump var="#v#" keys="1000" label="#sc GT 1000?"First 1000 Records":""#"><cfcatch>not available</cfcatch></cftry>
											<cfelse>
												the Scope will be displayed with the next request
											</cfif>
										</td></tr></table><!--- #-lucee-debug-#sectionId# !--->
									</td></tr>
								<cfelse>

									<tr>
										<td class="faded" style="padding-left: 16px;"><b>#k# Scope</b> (Not Enabled for this Application)</td>
									</tr>
								</cfif>
							</cfloop>

						</table>
					</cfif>

				</div><!--- #-lucee-debug-ALL !--->
			</fieldset><!--- #-lucee-debug !--->
		</cfoutput>


		<script>
			<cfset this.includeInline( "/lucee/res/js/util.min.js" )>

			var __LUCEE = __LUCEE || {};

			__LUCEE.debug = {

				<cfoutput>
				  cookieName: 	"#variables.cookieName#"
				, bitmaskAll: 	#variables.bitmaskAll#
				, allSections: 	#serializeJSON( this.allSections )#
				</cfoutput>

				, setFlag: 		function( name ) {

					var value = __LUCEE.util.getCookie( __LUCEE.debug.cookieName, __LUCEE.debug.allSections.ALL ) | __LUCEE.debug.allSections[ name ];
					__LUCEE.util.setCookie( __LUCEE.debug.cookieName, value );
					return value;
				}

				, clearFlag: 	function( name ) {

					var value = __LUCEE.util.getCookie( __LUCEE.debug.cookieName, 0 ) & ( __LUCEE.debug.bitmaskAll - __LUCEE.debug.allSections[ name ] );
					__LUCEE.util.setCookie( __LUCEE.debug.cookieName, value );
					return value;
				}

				, toggleSection: 	function( name ) {

					var btn = __LUCEE.util.getDomObject( "-lucee-debug-btn-" + name );
					var obj = __LUCEE.util.getDomObject( "-lucee-debug-" + name );
					var isOpen = ( __LUCEE.util.getCookie( __LUCEE.debug.cookieName, 0 ) & __LUCEE.debug.allSections[ name ] ) > 0;

					if ( isOpen ) {

						__LUCEE.util.removeClass( btn, '-lucee-icon-minus' );
						__LUCEE.util.addClass( btn, '-lucee-icon-plus' );
						__LUCEE.util.addClass( obj, 'collapsed' );
						__LUCEE.debug.clearFlag( name );
					} else {

						__LUCEE.util.removeClass( btn, '-lucee-icon-plus' );
						__LUCEE.util.addClass( btn, '-lucee-icon-minus' );
						__LUCEE.util.removeClass( obj, 'collapsed' );
						__LUCEE.debug.setFlag( name );
					}

					return !isOpen;					// returns true if section is open after the operation
				}

				, selectText:	__LUCEE.util.selectText
			};

			<cfif !structKeyExists(Cookie, variables.cookieName) || (Cookie[variables.cookieName] == variables.bitmaskAll)>
				var luceeStyle = document.createElement("style");
				luceeStyle.type = 'text/css';
				luceeStyle.innerHTML = "#-lucee-debug .collapsed { display: none; }";
				document.getElementById("-lucee-debug").prepend(luceeStyle);
			</cfif>
		</script>
	</cffunction><!--- output() !--->


	<cffunction name="doMore" returntype="void">
		<cfargument name="custom"    type="struct" required="#true#">
		<cfargument name="debugging" type="struct" required="#true#">
		<cfargument name="context"   type="string" default="web">

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

	<cfscript>

		function unitFormat( string unit, numeric time, boolean prettify=false ) {
			if ( !arguments.prettify ) {
				return NumberFormat( arguments.time / 1000000, ",0.000" );
			}

			// display 0 digits right to the point when more or equal to 100ms
			if ( arguments.time >= 100000000 )
				return int( arguments.time / 1000000 );

			// display 1 digit right to the point when more or equal to 10ms
			if ( arguments.time >=  10000000 )
				return ( int( arguments.time / 100000 ) / 10 );

			// display 2 digits right to the point when more or equal to 1ms
			if ( arguments.time >=   1000000 )
				return ( int( arguments.time / 10000 ) / 100 );

			// display 3 digits right to the point
			return ( int( arguments.time / 1000 ) / 1000 );
		}


		function byteFormat( numeric size ) {

			var values = [ [ 1099511627776, 'TB' ], [ 1073741824, 'GB' ], [ 1048576, 'MB' ], [ 1024, 'KB' ] ];

			for ( var i in values ) {

				if ( arguments.size >= i[ 1 ] )
					return numberFormat( arguments.size / i[ 1 ], '9.99' ) & i[ 2 ];
			}

			return arguments.size & 'B';
		}

		/** reads the file contents and writes it to the output stream */
		function includeInline(filename) cachedWithin=createTimeSpan(0,1,0,0) {

			echo(fileRead(expandPath(arguments.filename)));
		}

		function getJavaVersion() {
	        var verArr=listToArray(server.java.version,'.');
	        if(verArr[1]>2) return verArr[1];
	        return verArr[2];
	    }

	</cfscript>


</cfcomponent>