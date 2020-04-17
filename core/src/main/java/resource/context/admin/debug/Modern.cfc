<cfcomponent extends="Debug" output="no">

	<cfscript>
	  fields=array(
group("Debugging Tab","Debugging tag includes execution time,Custom debugging output",2)
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
//,field("Scope Variables","scopesList","",true,"Enable Scope reporting","checkbox","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL")
,group("Metrics Tab","",2)
,field("Metrics","tab_Metrics","Enabled",true,"Select the Metrics tab to show on debugOutput","checkbox","Enabled")
//,field("Charts","metrics_charts","HeapChart,NonHeapChart,WholeSystem",false,"Select the chart to show on metrics Tab. It will show only if the metrics tabs is enabled","checkbox","HeapChart,NonHeapChart,WholeSystem")
,group("Reference Tab","",2)
,field("Reference","tab_Reference","Enabled",true,"Select the Reference tab to show on DebugOutput","checkbox","Enabled")
);

		string function getLabel(){
			return "Modern";
		}

		string function getDescription(){
			return "The new style debug template";
		}

		string function getid(){
			return "lucee-modern";
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

		function isSectionOpen( string name ) {
			try{
			if ( arguments.name == "ALL" && !structKeyExists( Cookie, variables.cookieName ) )
				return true;

			var cookieValue = structKeyExists( Cookie, variables.cookieName ) ? Cookie[ variables.cookieName ] : 0;

			return cookieValue && ( bitAnd( cookieValue, this.allSections[ arguments.name ] ) );
			}
			catch(e){
				return false;
			}
		}

		function isEnabled( custom, key ) {

			return structKeyExists( arguments.custom, arguments.key ) && ( arguments.custom[ arguments.key ] == "Enabled" || arguments.custom[ arguments.key ] == "true" );
		}


		variables.cookieName = "lucee_debug_modern";

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
<cfscript>
var hasRefTab=(custom.tab_Reference?:"")=="Enabled";
var hasMetTab=(custom.tab_Metrics?:"")=="Enabled";


variables.chartStr = {};
if(structKeyExists(arguments.custom, "metrics_Charts")) {
	loop list=arguments.custom.metrics_Charts index="i" {
		if(i EQ "HeapChart")
			variables.chartStr[i] = "heap";
		else if(i EQ "NonHeapChart")
			variables.chartStr[i] = "nonheap";
		else if(i EQ "WholeSystem")
			variables.chartStr[i] = "cpuSystem";
	}
}

	
</cfscript>
		<cfif !structKeyExists(arguments.custom,'minimal')><cfset arguments.custom.minimal="0"></cfif>
		<cfif !structKeyExists(arguments.custom,'highlight')><cfset arguments.custom.highlight="250000"></cfif>
		<cfif !structKeyExists(arguments.custom,'scopes')><cfset arguments.custom.scopes=false></cfif>
		<cfif !structKeyExists(arguments.custom,'general')><cfset arguments.custom.general="Enabled"></cfif>

		<cfset var time=getTickCount() />
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

		<cfset this.allSections = this.buildSectionStruct()>
		<cfset var isExecOrder  = this.isSectionOpen( "ExecOrder" )>

		<cfif isExecOrder>

			<cfset querySort(pages,"id","asc") />
		<cfelse>

			<cfset querySort(pages,"avg","desc") />
		</cfif>

		<cfset var implicitAccess=arguments.debugging.implicitAccess />
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
			.ldTab {
  overflow: hidden;
  border: 0px; 
	

}

.ldTab button {
	background-color: #333;
	color: white !important;
	padding : 5px 16px 5px 16px !important; 
	margin: 4px 0px 4px 0px !important; 
	border-bottom-width: 0px !important; 
	border-color:white;
	font-size: 15px !important; 
	font-weight: 500 !important;
  	outline: none;
  	display: none;
}
.ldTab button:hover {
  background-color: #666;
}
.ldTab button.active {
  background-color: #3399cc !important;
  color: #FFF !important;
  outline: none;
}
.ldTabContent {
	display: none;
  	width: auto !important;  
	margin: -1em 1em 0em 1em !important; 
	padding: 1em; 
	background-color: #FFF; 
	border: 1px solid #CCC; 
	border-radius: 5px; 
}



	.ldTabContent { margin: 2.5em 1em 0 1em; padding: 1em; background-color: #FFF; color: #222; border: 1px solid #CCC; border-radius: 5px; text-shadow: none; }
	.ldTabContent.collapsed	{ padding: 0; border-width: 0; }
	.ldTabContent legend 	{ padding: 0 1em; background-color: #FFF; color: #222; }
	.ldTabContent legend span { font-weight: normal; }

	.ldTabContent, .ldTabContent td	{ font-family: Helvetica, Arial, sans-serif; font-size: 9pt; line-height: 1.35; vertical-align: top;}
	.ldTabContent.large, .ldTabContent.large td	{ font-size: 10pt; }
	.ldTabContent.small, .ldTabContent.small td	{ font-size: 8.5pt; }

	.ldTabContent table		{ empty-cells: show; border-collapse: collapse; border-spacing: 0; }
	.ldTabContent table.details	{ margin-top: 0.5em; border: 1px solid #ddd; margin-left: 9pt; max-width: 100%; }
	.ldTabContent table.details th { font-size: 9pt; font-weight: normal; background-color: #f2f2f2; color: #3c3e40; }
	.ldTabContent table.details td, .ldTabContent table.details th { padding: 2px 4px; border: 1px solid #ddd; }

	.ldTabContent .title	{ margin-top: 1.25em; font-size: 2.5em; font-weight: normal; color:#3399cc; }
	
	.ldTabContent .section-title	{ margin-top: 1.25em; font-size: 1.75em; font-weight: normal; color:#555; }

	.ldTabContent .section-title:first-child	{ margin-top: 12px; }
	.ldTabContent .label		{ white-space: nowrap; vertical-align: top; text-align: right; padding-right: 1em; background-color: inherit; color: inherit; text-shadow: none; }
	.ldTabContent .collapsed	{ display: none; }
	.ldTabContent .bold 		{ font-weight: bold; }
	.ldTabContent .txt-c 	{ text-align: center; }
	.ldTabContent .txt-l 	{ text-align: left; }
	.ldTabContent .txt-r 	{ text-align: right; }
	.ldTabContent .faded 	{ color: #999; }
	.ldTabContent .ml14px 	{ margin-left: 14px; }
	.ldTabContent table.details td.txt-r { padding-right: 1em; }
	.ldTabContent .num-lsv 	{ font-weight: normal; }
	.ldTabContent tr.nowrap td { white-space: nowrap; }
	.sortby {text-decoration: underline; font-weight: bold;}
	.ldTabContent .sortby.selected, #-lucee-debugging .sortby:hover { background-color: #25A; color: #FFF !important; cursor: pointer; text-decoration: none;}
	.ldTabContent table.details tr > th > a { color: #25A !important; text-decoration: underline; }
	.ldTabContent tr.red td, #-lucee-debugging .red 	{ background-color: #FDD; }

	.ldTabContent .sortby.selected, #-lucee-debugging .sortby:hover { background-color: #25A; color: #FFF; }
	.ldTabContent .pad 	{ padding-left: 16px; }
	.ldTabContent a 	{  color: #25A;}
	.ldTabContent a.large 	{font-size: 12pt;}
	.ldTabContent td a 	{ color: #25A; }
	.ldTabContent .warning{ color: red; }
	.ldTabContent td a:hover	{ color: #58C; text-decoration: underline; }
	.ldTabContent pre 	{ background-color: #EEE; padding: 1em; border: solid 1px #333; border-radius: 1em; white-space: pre-wrap; word-break: break-all; word-wrap: break-word; tab-size: 2; }
	.ldTabContent input 	{ 
			font-size: 12pt;
  			outline: none;background-color: #FFF; 
  			padding: 0.5em; border: solid 1px #333; 
  			border-radius: 5px;
  	}

	.-lucee-icon-plus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIRhI+hG7bwoJINIktzjizeUwAAOw==) no-repeat left center; padding: 4px 0 4px 16px; }
	.-lucee-icon-minus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIQhI+hG8brXgPzTHllfKiDAgA7)     no-repeat left center; padding: 4px 0 4px 16px; }
		</style>

<script language="javascript">
ldActiveTab='-lucee-debugging';

function ldShow(id) {
	var el=document.getElementById(id);
	if(el!=null)el.style.display = "inline";
}

function ldSelectTab(evt, tabName) {
	ldActiveTab=tabName;
  // Declare all variables
  var i, ldTabContent, ldTabLinks;

  // Get all elements with class="tabcontent" and hide them
  ldTabContent = document.getElementsByClassName("ldTabContent");
  for (i = 0; i < ldTabContent.length; i++) {
    ldTabContent[i].style.display = "none";
  }

  ldTabLinks = document.getElementsByClassName("ldTabLinks");
  for (i = 0; i < ldTabLinks.length; i++) {
    ldTabLinks[i].className = ldTabLinks[i].className.replace(" active", "");
  }

  // Show the current tab, and add an "active" class to the button that opened the tab
  document.getElementById(tabName).style.display = "block";
  if(evt==null) {
  	document.getElementById("ldDebug").className += " active";
  }
  else {
  	evt.currentTarget.className += " active";
  }
}

window.addEventListener('keydown',function(e){
	if(e.keyIdentifier=='U+000A'||e.keyIdentifier=='Enter'||e.keyCode==13){
		if(e.target.nodeName=='INPUT' && e.target.type=='text' && e.target.name=="luceesearchvalue"){
			e.preventDefault();
			//luceeSearchSugestions(e.target.value,true);
			return false;
		}
	}
},true);



function load(type) {
	var el=document.getElementById("-lucee-"+type);
	el.innerHTML='<div class="section-title">... loading '+type+' data</div>'; // TODO better wait thing

	var xhttp = new XMLHttpRequest();
  	xhttp.onreadystatechange = function() {
    	if (this.readyState == 4 && this.status == 200) {
    		el.innerHTML= this.responseText;
    	}
  	};
  	xhttp.open("GET", "/lucee/debug/modern/"+type+".cfm", true);
  	xhttp.send();

}

function loadRef() {
	luceeRefData={};
	var xhttp = new XMLHttpRequest();
  	xhttp.onreadystatechange = function() {
    	if (this.readyState == 4 && this.status == 200) {
    		luceeRefData=JSON.parse(this.responseText.trim());
    	}
  	};
  	xhttp.open("GET", "/lucee/debug/modern/reference.cfm", true);
  	xhttp.send();
}
function luceeSearchSugestions(val,force) {

	var src=document.getElementById("-lucee-docs-search-input");
	var allFunctions=false;
	var allTags=false;

	if(val==null) {// TODO check if exists
		var val=src.value;
	}
	else if(val=='functions') {
		allFunctions=true;
		src.value="";
	}
	else if(val=='tags') {
		allTags=true;
		src.value="";
	}


	val=val.toLowerCase();

	// check for match with functions
	var funcs='';
	var count=0;
	var match;
	var func=luceeRefData.function;
	var i=0;
	var fcol=1;
	for (var k in func) {
		if(allFunctions || (!allTags && func[k].indexOf(val)!=-1)) {
			funcs+='<a onclick="luceeSearch(\''+func[k]+'\')" >'+func[k]+'</a>';
			if((i%50)==49) {
				funcs+='</td><td>';
				fcol++;
			}
			else funcs+='<br>';
			
			count++;
			match=func[k];
		}
		i++;
	}
	
	// check for match with functions
	var tags='';
	var tag=luceeRefData.tag;
	var i=0;
	var tcol=1;
	for (var k in tag) {
		if(allTags || (!allFunctions && tag[k].indexOf(val)!=-1) ){
			tags+='<a onclick="luceeSearch(\''+tag[k]+'\')" >'+tag[k]+'</a>';
			if((i%50)==49) {
				tags+='</td><td>';
				tcol++;
			}
			else tags+='<br>';
			
			count++;
			match=tag[k];
		}
		i++;
	}

	var html='<table class="details">';
	html+='<thead><tr>';
	if(funcs.length)html+='<th colspan="'+fcol+'" class="txt-l">Functions</th>';
	if(tags.length)html+='<th colspan="'+tcol+'"  class="txt-l">Tags</th>';
	html+='</tr></thead>';
	html+='<tbody><tr>';
	if(funcs.length)html+='<td>'+funcs+'</td>';
	if(tags.length)html+='<td>'+tags+'</td>';
	html+='</tr></tbody></table>';

	var el=document.getElementById("-lucee-search-result");
	if(count==0)el.innerHTML="";
	else if(count==1 && match==val) {
		src.innerHTML=match;
		luceeSearch(match);
	}
	else el.innerHTML=html; 
}

function luceeSearch(val) {
	var src=document.getElementById("-lucee-docs-search-input");
	
	if(val==null) {// TODO check if exists
		var val=src.value;
	}
	else {
		src.value=val;
	}
	

	var el=document.getElementById("-lucee-search-result");
	
	var xhttp = new XMLHttpRequest();
  	xhttp.onreadystatechange = function() {
    	if (this.readyState == 4 && this.status == 200) {
    		el.innerHTML= this.responseText;
    	}
  	};
  	xhttp.open("POST", "/lucee/debug/modern/reference.cfm", true);
  	xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  	xhttp.send("search="+val);
}


function ldLoadECharts() {
  var script = document.createElement('script');
  script.setAttribute('src', "/lucee/res/js/echarts-all.js.cfm");
  script.setAttribute('type', 'text/javascript');

  script.onload = ldConfigureCharts;
  script.onreadystatechange = ldConfigureCharts;
  document.getElementsByTagName("head")[0].appendChild(script);
  isLoadedECharts=true;

  
}


isLoadedECharts=false;
function ldMetrics() {
//ldActiveTab
	if(true || !isLoadedECharts) {
		ldLoadECharts();
		return;
	}
	//else ldRequestData();
}

function ldConfigureCharts() {
	labels={'heap':"Heap",'nonheap':"Non-Heap",'cpuSystem':"Whole System",'cpuProcess':"Lucee Process"};
	
	var bg="#FFF";
	var blue="#3399CC";
	var red="#BF4F36";

	var yAxis = [{
			'type':'value',
			'min':'0',
			'max':'100',
			'splitNumber': 2
		}];


	// MEMORY
	var chartNames=["heap","nonheap"];
	for(var i in chartNames) {
		var data=chartNames[i];
		var el=document.getElementById(data);
		window[data] = echarts.init(el,'macarons'); 
		window[data+"Chart"] = {
			backgroundColor: [bg],
			tooltip : {
				'trigger':'axis',
				formatter : function (params) {
					return 'Series' + "<br>" + params[0].seriesName + ": " + params[0].value + "%" + '<br>' +params[0].name ;
				}
			},
			color: [blue],
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
			yAxis : yAxis,
			series : [{
				'name': labels[data] +' Memory',
				'type':'line',
				smooth:true,
				itemStyle: {normal: {areaStyle: {type: 'default'}}},
				'data': [0]
			}]
		}; // data	
		window[data].setOption(window[data+"Chart"]); // passed 
	}


	// CPU
	var el=document.getElementById('cpuSystem');
	cpuSystem = echarts.init(el,'macarons'); // intialize echarts
	cpuOption = {
		backgroundColor: [bg],
		tooltip : {
			'trigger':'axis',
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
		color: [blue, red],
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
		yAxis : yAxis,

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
	// console.log(ldActiveTab);
	cpuSystem.setOption(cpuOption); // passed the data into the chats

	ldRequestData();
}


function ldRequestData() {
	if(ldActiveTab!="-lucee-metrics") {
		//setTimeout(ldRequestData, 1000);
		return;
	}
	var xhttp = new XMLHttpRequest();
  	xhttp.onreadystatechange = function() {
    	if (this.readyState == 4 && this.status == 200) {
    		
    		var result=JSON.parse(this.responseText.trim());

    		// Memory	
			var arr =["heap","nonheap"];
			for(var index in arr) {
				var chrt=arr[index];
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
			};

			// CPU
			var arr2 =["cpuSystem"];
			for(var index in arr2) {
				var chrt=arr2[index];
				cpuSeries1 = cpuOption.series[0].data; //*charts*.series[0].data
				cpuSeries1.push(result["cpuSystem"]); // push the value into series[0].data
				cpuSeries2 = cpuOption.series[1].data; //*charts*.series[0].data
				cpuSeries2.push(result["cpuProcess"]); // push the value into series[0].data
				cpuOption.series[0].data = cpuSeries1;
				cpuOption.series[1].data = cpuSeries2;
				if(cpuOption.series[0].data.length > 60){
					cpuOption.series[0].data.shift(); //shift the array
				}
				if(cpuOption.series[1].data.length > 60){
					cpuOption.series[1].data.shift(); //shift the array
				}
				cpuOption.xAxis[0].data.push(new Date().toLocaleTimeString()); // current time
				if(cpuOption.xAxis[0].data.length > 60){
				cpuOption.xAxis[0].data.shift(); //shift the Time value
				}
				cpuSystem.setOption(cpuOption); // passed the data into the chats
				
			};
			setTimeout(ldRequestData, 1000);
    	}
  	};
  	xhttp.open("POST", "	/lucee/debug/modern/metrics.cfm", true);
  	xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  	xhttp.send();
	
}
		








</script>


<div class="ldTab">
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<!---

Debug Button
  ---><button 
  			id="ldDebug" 
  			class="ldTabLinks" 
  			style="<cfif not hasMetTab and not hasRefTab>display:none;</cfif>border-radius: 6px 0px 0px 6px;" 
  			onclick="ldSelectTab(event, '-lucee-debugging')">Debugging</button><!---

Metrics Button
   ---><cfif (custom.tab_Metrics?:"")=="Enabled"><button 
   			id="ldMetrics" 
   			class="ldTabLinks" 
   			style="border-left-width: 0px;<cfif not hasRefTab>border-radius: 0px 6px 6px 0px;</cfif>"
   			onclick="ldMetrics();ldSelectTab(event, '-lucee-metrics')">Metrics</button></cfif><!---

Reference Button
    ---><cfif (custom.tab_Reference?:"")=="Enabled"><button id="ldRef" style="border-left-width: 0px;border-radius: 0px 6px 6px 0px; " class="ldTabLinks" onclick="loadRef();ldSelectTab(event, '-lucee-reference')">Reference</button></cfif>
</div>
<!----------------------------------------
--------------- METRICS ------------------
------------------------------------------
---><fieldset id="-lucee-metrics" class="ldTabContent">
	<div class="section-title">Memory</div>
	<span>Memory used by the JVM, heap and non heap.</span>

	<table class="details" width="100%">
	<tbody>
	<tr>
		<td width="50%"><b>Heap</b>
			<div id="heap" style="min-width: 100px; height: 150px; margin: 0 auto;"></div>
		</td>
		<td width="50%"><b>Non-Heap</b><br>
			<div id="nonheap" style="min-width: 100px; height: 150px; margin: 0 auto;"></div>
		</td>
	</tr>
	</tbody>
	</table>

	<div class="section-title">CPU</div>
	<span>Average CPU load of the last 20 seconds on the whole system and this Java Virtual Machine (Lucee Process).</span>
	<table class="details" width="50%">
	<tbody>
	<tr>
		<td >
			<div id="cpuSystem" style="min-width: 100px; height: 150px; margin: 0 auto;"></div>
		</td>
	</tr>
	</tbody>
	</table>
</fieldset>

<!----------------------------------------
--------------- REFERENCE ------------------
------------------------------------------
---><fieldset id="-lucee-reference" class="ldTabContent">
		<br><br>
		<div class="pad">
		<form autocomplete="off">
			<a class="large" onclick="luceeSearchSugestions('functions')">Functions | </a><a onclick="luceeSearchSugestions('tags')" class="large">Tags | </a><input onkeyup="luceeSearchSugestions();" 
				id="-lucee-docs-search-input" 
				name="luceesearchvalue"
					placeholder="Search Tag or Function" 
					type="text">
			<!---<input onclick="luceeSearch()" type="button" name="go" value="go">--->
				
		</form>
		</div>
		<div id="-lucee-search-result"></div>
		<br><Br>
				<cfoutput><span class="pan">
		The documentation here aims to provide a thorough reference for the Lucee Server. You will find reference material on Lucee <a href="#cgi.hostName#/lucee/doc/tags.cfm">tags</a>, <a href="#cgi.hostName#/lucee/doc/functions.cfm">functions</a>, <a href="#cgi.hostName#/lucee/doc/components.cfm">components</a> and <a href="#cgi.hostName#/lucee/doc/objects.cfm">objects</a>. <span>You can reach the online version of the Lucee Server documentation <a href="https://docs.lucee.org/">here</a>.</span>
		</span></cfoutput>
	</fieldset>






		<cfoutput>

			<cfset var sectionId = "ALL">
			<cfset var isOpen = this.isSectionOpen( sectionId )>

<!----------------------------------------
--------------- DEBUG ------------------
------------------------------------------
---><fieldset 
				id="-lucee-debugging" 
				class="ldTabContent" style="display: inline;">

				<div id="-lucee-debugging-ALL">

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

							<cfset renderSectionHeadTR( sectionId, "Template:", "#HTMLEditFormat(_cgi.SCRIPT_NAME)# (#HTMLEditFormat(expandPath(_cgi.SCRIPT_NAME))#)" )>

							<tr>
								<td class="pad label">User Agent:</td>
								<td class="pad">#_cgi.http_user_agent#</td>
							</tr>
							<tr>
								<td colspan="2" id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
					<cfif structKeyExists(debugging,"abort")>
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

					<table>
						<cfset renderSectionHeadTR( sectionId
							, "#unitFormat( arguments.custom.unit, tot-q-loa, prettify )# ms
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Application" )>

						<tr><td><table>
							<tr>
								<td class="pad txt-r">#unitFormat( arguments.custom.unit, loa,prettify )# ms</td>
								<td class="pad">Startup/Compilation</td>
							</tr>
							<tr>
								<td class="pad txt-r">#unitFormat( arguments.custom.unit, q,prettify )# ms</td>
								<td class="pad">Query</td>
							</tr>
							<tr>
								<td class="pad txt-r bold">#unitFormat( arguments.custom.unit, tot, prettify )# ms</td>
								<td class="pad bold">Total</td>
							</tr>
						</table></td></tr>
						<tr>
							<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
											<td id="-lucee-debugging-pages-#pages.currentRow#" oncontextmenu="__LUCEE.debug.selectText( this.id );">#pages.src#</td>
											<td class="txt-r faded" title="#pages.id#">#ordermap[pages.id]#</td>
										</tr>
									</cfloop>
									<cfif hasBad>
										<tr class="red"><td colspan="3">red = over #unitFormat( arguments.custom.unit, arguments.custom.highlight * 1000 ,prettify)# ms average execution time</td></tr>
									</cfif>
								</table>
							</td><!--- #-lucee-debugging-#sectionId# !--->
						</tr>
					</table>


					<cfset this.doMore( arguments.custom, arguments.debugging, arguments.context )>


					<!--- Exceptions --->
					<cfif structKeyExists( arguments.debugging, "exceptions" ) && arrayLen( arguments.debugging.exceptions )>

						<cfset sectionId = "Exceptions">
						<cfset isOpen = this.isSectionOpen( sectionId )>

						<div class="section-title">Caught Exceptions</div>
						<table>

							<cfset renderSectionHeadTR( sectionId, "#arrayLen(arguments.debugging.exceptions)# Exception#arrayLen( arguments.debugging.exceptions ) GT 1 ? 's' : ''# Caught" )>

							<tr>
								<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
								</td><!--- #-lucee-debugging-#sectionId# !--->
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
								<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
								</td><!--- #-lucee-debugging-#sectionId# !--->
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
								<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
								</td><!--- #-lucee-debugging-#sectionId# !--->
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
								<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
								</td><!--- #-lucee-debugging-#sectionId# !--->
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
								<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">
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
						<cfloop struct="#debugging.datasources#" index="dsn" item="item">
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
								<td id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'#">


									<table><tr><td>
										<b>General</b>
										<table class="details">
										<tr>
											<th>Name</th>
											<th>Open Connections</th>
											<th>Max Connections</th>
										</tr>
										<cfloop struct="#debugging.datasources#" index="local.dsName" item="local.dsData">
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
													<th>Line</th>
													<cfif hasCachetype><th>Cache Type</th></cfif>

												</tr>
												<tr>
													<th></th>
													<td>#queries.name#</td>
													<td class="txt-r">#queries.count#</td>
													<td class="txt-r">#unitFormat(arguments.custom.unit, queries.time,prettify)#</td>
													<td>#queries.datasource#</td>
													<td>#queries.src#</td>
													<td>#queries.line#</td>
													<cfif hasCachetype><td>#isEmpty(queries.cacheType)?"none":queries.cacheType#</td></cfif>
												</tr>
												<tr>
													<th class="label">SQL:</th>
													<td id="-lucee-debugging-query-sql-#queries.currentRow#" colspan="7" oncontextmenu="__LUCEE.debug.selectText( this.id );"><pre>#trim( queries.sql )#</pre></td>
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
														<th colspan="8"><b>Query usage within the request</b></th>
													</tr>

													<cfset local.arr = usageRead>
													<cfset local.arrLenU = arrayLen( arr )>
													<cfif arrLenU>
														<tr>
															<td colspan="8">
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
															<td colspan="8">
																Unused:
																<cfloop from="1" to="#arrLenN#" index="local.ii">
																	#arr[ ii ]# <cfif ii LT arrLenN>, </cfif>
																</cfloop>
															</td>
														</tr>
														<tr class="red">
															<td colspan="8"><b>#arrLenU ? numberFormat( arrLenU / ( arrLenU + arrLenN ) * 100, "999.9" ) : 100# %</b></td>
														</tr>
													</cfif>
												</cfif>

											</table>

										</cfloop>

									</tr></td></table>
								</td><!--- #-lucee-debugging-#sectionId# !--->
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

										<table id="-lucee-debugging-#sectionId#" class="#isOpen ? '' : 'collapsed'# ml14px"><tr><td>

											<cfif isOpen>
												<cftry><cfdump var="#v#" keys="1000" label="#sc GT 1000?"First 1000 Records":""#"><cfcatch>not available</cfcatch></cftry>
											<cfelse>
												the Scope will be displayed with the next request
											</cfif>
										</td></tr></table><!--- #-lucee-debugging-#sectionId# !--->
									</td></tr>
								<cfelse>

									<tr>
										<td class="faded" style="padding-left: 16px;"><b>#k# Scope</b> (Not Enabled for this Application)</td>
									</tr>
								</cfif>
							</cfloop>

						</table>
					</cfif>

				</div><!--- #-lucee-debugging-ALL !--->
			</fieldset><!--- #-lucee-debugging !--->
		</cfoutput>


		<script>
			<cfset this.includeInline( "/lucee/res/js/util.min.js" )>

			var __LUCEE = __LUCEE || {};

			__LUCEE.debug = {

				<cfoutput>
				  cookieName: 	"#variables.cookieName#"
				, bitmaskAll: 	Math.pow( 2, 31 ) - 1
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

					var btn = __LUCEE.util.getDomObject( "-lucee-debugging-btn-" + name );
					var obj = __LUCEE.util.getDomObject( "-lucee-debugging-" + name );
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
		</script>
<cfif hasMetTab or hasRefTab>
<script>
ldShow("ldDebug");
ldShow("ldMetrics");
ldShow("ldRef");
ldSelectTab(null,'-lucee-debugging');
</script>
</cfif>



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
			<td><a id="-lucee-debugging-btn-#arguments.sectionId#" class="-lucee-icon-#isOpen ? 'minus' : 'plus'#" onclick="__LUCEE.debug.toggleSection( '#arguments.sectionId#' );">
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

	    function getAllFunctions() {
			var result = getFunctionList().keyArray().sort( 'textnocase' ).filter( function( el ) { return left( arguments.el, 1 ) != '_'; } );
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

		function  getAllComponents() localmode=true {
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

	</cfscript>


</cfcomponent>