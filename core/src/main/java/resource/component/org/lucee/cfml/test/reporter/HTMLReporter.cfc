<cfcomponent>
<cfscript>
	function init(){ 
		return this; 
	}

	/**
	* Get the name of the reporter
	*/
	function getName(){
		return "HTML";
	}

	/**
	* Do the reporting thing here using the incoming test results
	* The report should return back in whatever format they desire and should set any
	* Specifc browser types if needed.
	* @results.hint The instance of the TestBox TestResult object to build a report on
	* @testbox.hint The TestBox core object
	* @options.hint A structure of options this reporter needs to build the report with
	*/
	any function runReport( 
		required testbox.system.testing.TestResult results,
		required testbox.system.testing.TestBox testbox,
		struct options={}
	){
		// content type
		getPageContext().getResponse().setContentType( "text/html" );
		
		// bundle stats
		bundleStats = arguments.results.getBundleStats();
		
		// prepare base links
		baseURL = "?";
		if( structKeyExists( url, "method") ){ baseURL&= "method=#URLEncodedFormat( url.method )#"; }
		if( structKeyExists( url, "output") ){ baseURL&= "output=#URLEncodedFormat( url.output )#"; }

		// prepare incoming params
		if( !structKeyExists( url, "testMethod") ){ url.testMethod = ""; }
		if( !structKeyExists( url, "testSpecs") ){ url.testSpecs = ""; }
		if( !structKeyExists( url, "testSuites") ){ url.testSuites = ""; }
		if( !structKeyExists( url, "testBundles") ){ url.testBundles = ""; }

		// prepare the report
		savecontent variable="local.report"{
			html(results,testbox,options,baseURL);
		}
		return local.report;
	}
</cfscript>	
	
	<!--- TODO this is just a temporary solution, make better! --->
	<cffunction name="html" access="private" localmode="modern">
		<cfargument name="results" type="testbox.system.testing.TestResult">
		<cfargument name="testbox" type="testbox.system.testing.TestBox">
		<cfargument name="options" type="struct">
		<cfargument name="baseURL" type="string">

<cfoutput>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="generator" content="TestBox v#testbox.getVersion()#">
	<title>Pass: #results.getTotalPass()# Fail: #results.getTotalFail()# Errors: #results.getTotalError()#</title>
	<script src="/lucee/admin/resources/js/jquery-1.7.2.min.js.cfm" type="text/javascript"></script><!--- TODO use version indepent file --->
</cfoutput>
	<style>
body{
	font-family:  Monaco, "Lucida Console", monospace;
	font-size: 10.5px;
	line-height: 14px;
}
h1,h2,h3,h4{ margin-top: 3px;}
h1{ font-size: 14px;}
h2{ font-size: 13px;}
h3{ font-size: 12px;}
h4{ font-size: 11px; font-style: italic;}
ul{ margin-left: -10px;}
li{ margin-left: -10px; list-style: none;}
a{ text-decoration: none;}
a:hover{ text-decoration: underline;}
/** status **/
.passed { color: green; }
.failed { color: orange; }
.error { color: red; }
.skipped{ color: blue;}
div.skipped{ display: none;}
/** utility **/
.centered { text-align: center !important; }
.inline{ display: inline !important; }
.margin10{ margin: 10px; }
.padding10{ padding: 10px; }
.margin0{ margin: 0px; }
.padding0{ padding: 0px; }
.float-right{ float: right;}
.float-left{ float: left;}
/** boxes **/
.box{ border:1px solid gray; margin: 10px 0px; padding: 10px; background-color: #f5f5f5}
.buttonBar{ float:right; margin: 10px 5px 0px 10px }
.debugdata{ display:none; }
/** stats **/
#globalStats{ background-color: #dceef4 }
.specStatus, .reset{ cursor:pointer;}
.suite{ margin-left: -1px;}
	</style>
<cfoutput>
	<script>
	$(document).ready(function() {
		// spec toggler
		$("span.specStatus").click( function(){
			toggleSpecs( $( this ).attr( "data-status" ), $( this ).attr( "data-bundleid" ) );
		});
		// spec toggler
		$("span.reset").click( function(){
			resetSpecs();
		});
	});
	function resetSpecs(){
		$("div.spec").each( function(){
			$(this).show();
		});
	}
	function toggleSpecs( type, bundleid ){
		$("div.spec").each( function(){
			var $this = $( this );
		
			// if bundleid passed and not the same bundle, skip
			if( bundleid != undefined && $this.attr( "data-bundleid" ) != bundleid ){
				return;
			}

			// toggle the opposite type
			if( !$this.hasClass( type ) ){
				$this.fadeOut();
			}
			else{
				// show the type you sent
				$this.fadeIn();
			}

		} );
	}
	function toggleDebug( specid ){
		$("div.debugdata").each( function(){
			var $this = $( this );
		
			// if bundleid passed and not the same bundle
			if( specid != undefined && $this.attr( "data-specid" ) != specid ){
				return;
			}
			// toggle.
			$this.fadeToggle();
		});
	}
	</script>
</head>

<body>

<!-- Header --->
<p>TestBox #testbox.getVersion()#</p>

<!-- Global Stats --->
<div class="box" id="globalStats">

<div class="buttonBar">
	<a href="#baseURL#"><button title="Run all the tests">Run All</button></a>
	<button onclick="toggleDebug()" title="Toggle the test debug information">Debug</button>
</div>

<h2>Global Stats (#results.getTotalDuration()# ms)</h2>
[ Bundles/Suites/Specs: #results.getTotalBundles()#/#results.getTotalSuites()#/#results.getTotalSpecs()# ]
[ <span class="specStatus passed" data-status="passed">Pass: #results.getTotalPass()#</span> ]
[ <span class="specStatus failed" data-status="failed">Failures: #results.getTotalFail()#</span> ]
[ <span class="specStatus error" data-status="error">Errors: #results.getTotalError()#</span> ]
[ <span class="specStatus skipped" data-status="skipped">Skipped: #results.getTotalSkipped()#</span> ]
[ <span class="reset" title="Clear status filters">Reset</span> ]
<br>
<cfif arrayLen( results.getLabels() )>
[ Labels Applied: #arrayToList( results.getLabels() )# ]
</cfif>

</div>

<!--- Bundle Info --->
<cfloop array="#bundleStats#" index="thisBundle">
	<!--- Skip if not in the includes list --->
	<cfif len( url.testBundles ) and !listFindNoCase( url.testBundles, thisBundle.path )>
		<cfcontinue>
	</cfif>
	<cfset showOnlyName=thisBundle.totalFail+thisBundle.totalError ==0>
		
	<!--- Bundle div --->
	<div class="box" id="bundleStats_#thisBundle.path#">
		<cfset link="#baseURL#&testBundles=#URLEncodedFormat( thisBundle.path )#">
		<cfif showOnlyName>
			<a class="specStatus passed" href="#link#" title="Run only this bundle">#thisBundle.path#</a> (#thisBundle.totalDuration# ms)
		<cfelse>
			
			<!--- bundle stats --->
			<h2><a href="#link#" title="Run only this bundle">#thisBundle.path#</a> (#thisBundle.totalDuration# ms)</h2>
			[ Suites/Specs: #thisBundle.totalSuites#/#thisBundle.totalSpecs# ]
			[ <span class="specStatus passed" 	data-status="passed" data-bundleid="#thisBundle.id#">Pass: #thisBundle.totalPass#</span> ]
			[ <span class="specStatus failed" 	data-status="failed" data-bundleid="#thisBundle.id#">Failures: #thisBundle.totalFail#</span> ]
			[ <span class="specStatus error" 	data-status="error" data-bundleid="#thisBundle.id#">Errors: #thisBundle.totalError#</span> ]
			[ <span class="specStatus skipped" 	data-status="skipped" data-bundleid="#thisBundle.id#">Skipped: #thisBundle.totalSkipped#</span> ]
			[ <span class="reset" title="Clear status filters">Reset</span> ]
			
			<!-- Globa Error --->
			<cfif !isSimpleValue( thisBundle.globalException )>
				<h2>Global Bundle Exception<h2>
				<cfdump var="#thisBundle.globalException#" />
			</cfif>

			<!-- Iterate over bundle suites -->
			<cfloop array="#thisBundle.suiteStats#" index="suiteStats">
				<div class="suite #lcase( suiteStats.status)#" data-bundleid="#thisBundle.id#">
				<ul>
					#genSuiteReport( suiteStats, thisBundle )#
				</ul>
				</div>
			</cfloop>

			<!--- Debug Panel --->
			<cfif arrayLen( thisBundle.debugBuffer )>
				<hr>
				<h2>Debug Stream <button onclick="toggleDebug( '#thisBundle.id#' )" title="Toggle the test debug stream">+</button></h2>
				<div class="debugdata" data-specid="#thisBundle.id#">
					<p>The following data was collected in order as your tests ran via the <em>debug()</em> method:</p>
					<cfdump var="#thisBundle.debugBuffer#" />
				</div>
			</cfif>
		</cfif>
	</div>
</cfloop>

<!--- <cfdump var="#results#"> --->
</body>
</html>
</cfoutput>


	</cffunction>

	<!--- Recursive Output --->
<cffunction name="genSuiteReport" output="false" access="private">
	<cfargument name="suiteStats">
	<cfargument name="bundleStats">
	
	<cfsavecontent variable="local.report">
		<cfoutput>
		<!--- Suite Results --->
		<li>
			<a title="Total: #arguments.suiteStats.totalSpecs# Passed:#arguments.suiteStats.totalPass# Failed:#arguments.suiteStats.totalFail# Errors:#arguments.suiteStats.totalError# Skipped:#arguments.suiteStats.totalSkipped#" 
			   href="#baseURL#&testSuites=#URLEncodedFormat( arguments.suiteStats.name )#&testBundles=#URLEncodedFormat( arguments.bundleStats.path )#" 
			   class="#lcase( arguments.suiteStats.status )#"><strong>+#arguments.suiteStats.name#</strong></a> 
			(#arguments.suiteStats.totalDuration# ms)
		</li>
			<cfloop array="#arguments.suiteStats.specStats#" index="local.thisSpec">

				<!--- Spec Results --->
				<ul>
				<div class="spec #lcase( local.thisSpec.status )#" data-bundleid="#arguments.bundleStats.id#" data-specid="#local.thisSpec.id#">
					<li>
						<a href="#baseURL#&testSpecs=#URLEncodedFormat( local.thisSpec.name )#&testBundles=#URLEncodedFormat( arguments.bundleStats.path )#" class="#lcase( local.thisSpec.status )#">#local.thisSpec.name# (#local.thisSpec.totalDuration# ms)</a>
						
						<cfif local.thisSpec.status eq "failed">
							- <strong>#htmlEditFormat( local.thisSpec.failMessage )#</strong>
							  <button onclick="toggleDebug( '#local.thisSpec.id#' )" title="Show more information">+</button><br>
							<div class="box debugdata" data-specid="#local.thisSpec.id#">
								<cfdump var="#local.thisSpec.failorigin#" label="Failure Origin">
							</div>
						</cfif>
						
						<cfif local.thisSpec.status eq "error">
							- <strong>#htmlEditFormat( local.thisSpec.error.message )#</strong>
							  <button onclick="toggleDebug( '#local.thisSpec.id#' )" title="Show more information">+</button><br>
							<div class="box debugdata" data-specid="#local.thisSpec.id#">
								<cftry>
									<cfset local.catch=local.thisSpec.error>
									<cfset addClosingHTMLTags=false>
									<cfinclude template="#getPageContext().getConfig().getErrorTemplate(500)#">
									<cfcatch>
										<cfdump var="#local.thisSpec.error#" label="Exception Structure">
									</cfcatch>
								</cftry>
							</div>
						</cfif>
					</li>
				</div>
				</ul>
			</cfloop>

			<!--- Do we have nested suites --->
			<cfif arrayLen( arguments.suiteStats.suiteStats )>
				<cfloop array="#arguments.suiteStats.suiteStats#" index="local.nestedSuite">
					<div class="suite #lcase( arguments.suiteStats.status )#" data-bundleid="#arguments.bundleStats.id#">
						<ul>
						#genSuiteReport( local.nestedSuite, arguments.bundleStats )#
					</ul>
					</div>
				</cfloop>
			</cfif>	

		</cfoutput>
	</cfsavecontent>

	<cfreturn local.report>
</cffunction>

</cfcomponent>