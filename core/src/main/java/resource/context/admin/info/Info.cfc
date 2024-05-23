<cfcomponent output="no">
	<cffunction name="info" returntype="void">
		<cfargument name="show" type="struct" required="yes" />
		<cfargument name="debugTemplate" type="component" required="no" />
		<cfargument name="debugArgs" type="struct" required="no" /><cfscript>

	var enabledKeys=[];
	if(arguments.show.debug?:false) arrayAppend(enabledKeys, "debug");
	if(arguments.show.doc?:false) arrayAppend(enabledKeys, "ref");
	if(arguments.show.metric?:false) arrayAppend(enabledKeys, "metric");
	//if(arguments.show.test?:false) arrayAppend(enabledKeys, "test");

	var labels=[
		"debug":"Debugging"
		,"metric":"Metrics"
		,"ref":"Reference"
		,"test":"Test"
	];

	var loads=[
		"metric":"ldMetric"
		,"ref":"loadRef"
	];
		
</cfscript>
		
</td></td></td></th></th></th></tr></tr></tr></table></table></table></a></abbrev></acronym></address></applet></au></b></banner></big></blink></blockquote></bq></caption></center></cite></code></comment></del></dfn></dir></div></div></dl></em></fig></fn></font></form></frame></frameset></h1></h2></h3></h4></h5></h6></head></i></ins></kbd></listing></map></marquee></menu></multicol></nobr></noframes></noscript></note></ol></p></param></person></plaintext></pre></q></s></samp></script></select></small></strike></strong></sub></sup></table></td></textarea></th></title></tr></tt></u></ul></var></wbr></xmp>
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
  background-color: #5f8731 !important;
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
	#-lucee-debug-ExecTime table.details th::after, #-lucee-debug-ImpAccess table.details th::after { content: '\00A0\21E9';}
	#-lucee-debug-ExecTime table.details th, #-lucee-debug-ImpAccess table.details th { cursor:pointer; } 

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
	.ldTabContent .sortby.selected, #-lucee-debug .sortby:hover { background-color: #25A; color: #FFF !important; cursor: pointer; text-decoration: none;}
	.ldTabContent table.details tr > th > a { color: #25A !important; text-decoration: underline; }
	.ldTabContent tr.red td, #-lucee-debug .red 	{ background-color: #FDD; }

	.ldTabContent .sortby.selected, #-lucee-debug .sortby:hover { background-color: #25A; color: #FFF; }
	.ldTabContent .pad 	{ padding-left: 16px; }
	.ldTabContent a 	{  color: #25A;}
	.ldTabContent a.large 	{font-size: 12pt; cursor: pointer;}
	.ldTabContent td a 	{ color: #25A; }
	.ldTabContent .warning{ color: red; }
	.ldTabContent .happy { color: green; }
	.ldTabContent td a:hover	{ color: #58C; text-decoration: underline; cursor: pointer;}
	.ldTabContent prex 	{ background-color: #EEE;margin-top:2px; padding: 1em; border: solid 1px #333; border-radius: 1em; white-space: pre-wrap; word-break: break-all; word-wrap: break-word; tab-size: 2; }
	.ldTabContent .prey 	{  
		font-weight: normal;
		font-family: "Courier New", Courier, monospace, sans-serif;
		white-space: pre-wrap; word-break: break-all; word-wrap: break-word; tab-size: 2; }
		.ldTabContent .innercircle 	{ background-color: #EEE;margin-top:2px; padding: 1em; border: solid 1px #333; border-radius: 1em; }
		.ldTabContent .inner 	{ margin-left:5px;margin-right:5px;margin-top:5px;margin-bottom:15px; }
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
ldActiveTab='-lucee-debug';

var ldTableSorter = function (ev){
	__LUCEE.debug.sortTable(ev.target, 'text');
};
function ldAttachTableSorters(){
	var sortTables = document.querySelectorAll('.details THEAD TH');
	for (var st = 0; st < sortTables.length; st++) {		
		sortTables[st].addEventListener('click', ldTableSorter);	
	}
}

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
	var cn="ldDebug";
	if(tabName=='-lucee-ref') cn="ldRef";
	else if(tabName=='-lucee-metric') cn="ldMetric";
	
  	document.getElementById(cn).className += " active";
  }
  else {
  	evt.currentTarget.className += " active";
  }
}

window.addEventListener('keydown',function(e){
	if(e.keyIdentifier=='U+000A'||e.keyIdentifier=='Enter'||e.keyCode==13){
		if(e.target.nodeName=='INPUT' && e.target.type=='text' && e.target.name=="luceesearchvalue"){
			e.preventDefault();
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
function ldMetric() {
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
	if(ldActiveTab!="-lucee-metric") {
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

Buttons
  ---><cfscript>
var first=true;
loop array=enabledKeys index="local.i" item="local.k" {
	first=i==1;
	last=i==len(enabledKeys);
	
	loader=structKeyExists(loads,k)?(loads[k]&"();"):"";
	
	```<cfoutput><button 
	id="ld#ucFirst(k)#" 
	class="ldTabLinks" 
	style="margin-right: 1px !important;border-width: 0px;border-radius: #first?6:0#px #last?6:0#px #last?6:0#px #first?6:0#px;" 
	onclick="#loader#ldSelectTab(event, '-lucee-#k#')">#labels[k]#</button></cfoutput>```
}

  </cfscript>
</div>

<!----------------------------------------
--------------- Test ------------------
------------------------------------------
---><fieldset id="-lucee-test" class="ldTabContent">
	<div class="section-title">Test</div>
	<span><!-- description --></span>
</fieldset>


<!----------------------------------------
--------------- METRIC ------------------
------------------------------------------
---><fieldset id="-lucee-metric" class="ldTabContent">
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
---><fieldset id="-lucee-ref" class="ldTabContent">
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

			

<!----------------------------------------
--------------- DEBUG ------------------
------------------------------------------
---><cfif (arguments.show.debug?:false)><fieldset 
				id="-lucee-debug" 
				class="ldTabContent" style="display: inline;">
				<cfif not isNull(arguments.debugTemplate)>
					<cfset arguments.debugTemplate.output(custom:arguments.debugArgs.custom,debugging:arguments.debugArgs.debugging,context:"inline")>
				</cfif>

</fieldset></cfif>
		</cfoutput>
		

<cfoutput><script>
	<cfloop array="#enabledKeys#" item="local.k">
		ldShow("ld#ucFirst(k)#");
	</cfloop>
ldSelectTab(null,'-lucee-#enabledKeys[1]#');
<cfif structKeyExists(loads,enabledKeys[1])>#loads[enabledKeys[1]]#();</cfif>


</script></cfoutput>




	</cffunction><!--- output() !--->






	<cfscript>

		function unitFormat( string unit, numeric time, boolean prettify=false ) {
			/*if ( !arguments.prettify ) {
				return NumberFormat( arguments.time / 1000000, ",0.000" );
			}*/

			// display 0 digits right to the point when more or equal to 100ms
			//if ( arguments.time >= 100000000 )
			//	return int( arguments.time / 1000000 );

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