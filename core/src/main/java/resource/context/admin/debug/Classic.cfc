<cfcomponent extends="Debug" output="no"><cfscript>

    fields=array(
		
		group("Execution Time","Execution times for templates, includes, modules, custom tags, and component method calls. Template execution times over this minimum highlight time appear in red.",3)
		//,field("Extensions","extension","cfm,cfc,cfml",false,"Output the templates with the following extensions","checkbox","cfm,cfc,cfml")
		
		
		//,field("Unit","unit","millisecond",true,"the unit used to display the execution time.","select","millisecond,microsecond,nanosecond")
		
		,field("Minimal Execution Time","minimal","0",true,
				{_appendix:"microseconds",_bottom:"Execution times for templates, includes, modules, custom tags, and component method calls. Outputs only templates taking longer than the time (in microseconds) defined above."},"text40")
		
		,field("Highlight","highlight","250000",true,
				{_appendix:"microseconds",_bottom:"Highlight templates taking longer than the following (in microseconds) in red."},"text50")
		
		
		
		,group("Custom Debugging Output","Define what is outputted",3)

		


		,field("General Debug Information ","general",true,false,
				"Select this option to show general information about this request. General items are Lucee Version, Template, Time Stamp, User Locale, User Agent, User IP, and Host Name. ","checkbox")
		
		,field("Scope Variables","scopes","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL",true,"Enable Scope reporting","checkbox","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL")
		
		
		,group("Output Format","Define details to the fomrat of the debug output",3)
		,field("Background Color","bgcolor","white",true,"Color in the back, ","text80")
		,field("Font Color","color","black",true,"Color used for the Font, ","text80")
		,field("Font Family","font","Times New Roman, Times, serif",true,"What kind of Font is used, ","text200")
		,field("Font Size","size","medium",true,"What kind of Font is usedThe size of the font in Pixel, ","select","small,medium,large")
	);
    
    



string function getLabel(){
	return "Classic";
}
string function getDescription(){
	return "The old style debug template";
}
string function getid(){
	return "lucee-classic"; 
}

string function readDebug(struct custom, struct debugging, string context){
	output(argumentcollection=arguments);
}

void function onBeforeUpdate(struct custom){
	throwWhenEmpty(custom,"color");
	throwWhenEmpty(custom,"bgcolor");
	throwWhenNotNumeric(custom,"minimal");
	throwWhenNotNumeric(custom,"highlight");
}

private void function throwWhenEmpty(struct custom, string name){
	if(!structKeyExists(custom,name) or len(trim(custom[name])) EQ 0)
		throw "value for ["&name&"] is not defined";
}
private void function throwWhenNotNumeric(struct custom, string name){
	throwWhenEmpty(custom,name);
	if(!isNumeric(trim(custom[name])))
		throw "value for ["&name&"] must be numeric";
}

private function isColumnEmpty(query query, string columnName){
	if(!QueryColumnExists(query,columnName)) return true;
	return !len(ArrayToList(QueryColumnData(query,columnName),'')); 
}

</cfscript>   
    
    
    <cffunction name="output" returntype="void">
    	<cfargument name="custom" type="struct" required="yes">
		<cfargument name="debugging" required="true" type="struct">
		<cfargument name="context" type="string" default="web"><cfsilent>
<cfscript>
	var time=getTickCount();
	var _cgi = arguments?.debugging?.scope?.cgi ?: cgi;

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
<cfset querySort(pages,"avg","desc")>
<cfset querySort(implicitAccess,"template,line,count","asc,asc,desc")>

<cfparam name="arguments.custom.unit" default="millisecond">
<cfparam name="arguments.custom.color" default="black">
<cfparam name="arguments.custom.bgcolor" default="white">
<cfparam name="arguments.custom.font" default="Times New Roman">
<cfparam name="arguments.custom.size" default="medium">

<cfset var unit={
millisecond:"ms"
,microsecond:"μs"
,nanosecond:"ns"

}>



</cfsilent><cfif arguments.context EQ "web"></td></td></td></th></th></th></tr></tr></tr></table></table></table></a></abbrev></acronym></address></applet></au></b></banner></big></blink></blockquote></bq></caption></center></cite></code></comment></del></dfn></dir></div></div></dl></em></fig></fn></font></form></frame></frameset></h1></h2></h3></h4></h5></h6></head></i></ins></kbd></listing></map></marquee></menu></multicol></nobr></noframes></noscript></note></ol></p></param></person></plaintext></pre></q></s></samp></script></select></small></strike></strong></sub></sup></table></td></textarea></th></title></tr></tt></u></ul></var></wbr></xmp></cfif>
<style type="text/css">
<cfoutput>
.cfdebug {color:#arguments.custom.color#;background-color:#arguments.custom.bgcolor#;font-family:"#arguments.custom.font#";
	font-size:<cfif arguments.custom.size EQ "small">smaller<cfelseif arguments.custom.size EQ "medium">small<cfelse>medium</cfif>;}
.cfdebuglge {color:#arguments.custom.color#;background-color:#arguments.custom.bgcolor#;font-family:#arguments.custom.font#;
	font-size:<cfif arguments.custom.size EQ "small">small<cfelseif arguments.custom.size EQ "medium">medium<cfelse>large</cfif>;}

.template_overage {	color: red; background-color: #arguments.custom.bgcolor#; font-family:#arguments.custom.font#; font-weight: bold;
	font-size:<cfif arguments.custom.size EQ "small">smaller<cfelseif arguments.custom.size EQ "medium">small<cfelse>medium</cfif>; }
</style>
 

<table class="cfdebug" bgcolor="#arguments.custom.bgcolor#" style="border-color:#arguments.custom.color#">
<tr>
    <cfif isEnabled(arguments.custom,"general")>
	<td>
 <!--- General --->
		<p class="cfdebug"><hr/>
		<b class="cfdebuglge"><a name="cfdebug_top">Debugging Information</a></b>
		<table class="cfdebug">
		<tr>
			<td class="cfdebug" colspan="2" nowrap>
			#server.coldfusion.productname#
			<cfif StructKeyExists(server.lucee,'versionName')>(<a href="#server.lucee.versionNameExplanation#" target="_blank">#server.lucee.versionName#</a>)</cfif>
			#ucFirst(server.coldfusion.productlevel)# 
			#server.lucee.version#
			(CFML Version #server.ColdFusion.ProductVersion#)
			</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> Template </td>
			<td class="cfdebug">#encodeForHtml(_cgi.REQUEST_URL)# <br> #encodeForHtml(expandPath(_cgi.SCRIPT_NAME))#</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> Time Stamp </td>
			<td class="cfdebug">#LSDateFormat(now())# #LSTimeFormat(now())#</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> Time Zone </td>
			<td class="cfdebug">#getTimeZone()#</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> Locale </td>
			<td class="cfdebug">#ucFirst(GetLocale())#</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> User Agent </td>
			<td class="cfdebug">#_cgi.http_user_agent#</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> Remote IP </td>
			<td class="cfdebug">#_cgi.remote_addr#</td>
		</tr>
		<tr>
			<td class="cfdebug" nowrap> Host Name </td>
			<td class="cfdebug">#_cgi.server_name#</td>
		</tr>
		<cfif StructKeyExists(server.os,"archModel") and StructKeyExists(server.java,"archModel")><tr>
			<td class="cfdebug" nowrap> Architecture</td>
			<td class="cfdebug"><cfif server.os.archModel NEQ server.os.archModel>OS #server.os.archModel#bit/JRE #server.java.archModel#bit<cfelse>#server.os.archModel#bit</cfif></td>
		</tr></cfif>
		</table>
		</p>
<!--- Execution Time --->
	<p class="cfdebug"><hr/><b class="cfdebuglge"><a name="cfdebug_execution">Execution Time</a></b></p>
	<a name="cfdebug_templates">
		<table border="1" cellpadding="2" cellspacing="0" class="cfdebug">
		<cfif pages.recordcount>
		<tr>
			<td class="cfdebug" align="center"><b>Total Time</b></td>
			<td class="cfdebug" align="center"><b>Avg Time</b></td>
			<td class="cfdebug" align="center"><b>Count</b></td>
			<td class="cfdebug"><b>Template</b></td>
		</tr></cfif>
<cfset var loa=0>
<cfset var tot=0>
<cfset var q=0>
<cfparam name="arguments.custom.minimal" default="0">
<cfparam name="arguments.custom.highlight" default="250000">
<cfloop query="pages">
		<cfset tot=tot+pages.total><cfset q=q+pages.query>
		<cfif pages.avg LT arguments.custom.minimal*1000><cfcontinue></cfif>
		<cfset var bad=pages.avg GTE arguments.custom.highlight*1000><cfset loa=loa+pages.load>
		<tr>
			<td align="right" class="cfdebug" nowrap><cfif bad><font color="red"><span class="template_overage"></cfif>#formatUnit(arguments.custom.unit, pages.total-pages.load)#<cfif bad></span></font></cfif></td>
			<td align="right" class="cfdebug" nowrap><cfif bad><font color="red"><span class="template_overage"></cfif>#formatUnit(arguments.custom.unit, pages.avg)#<cfif bad></span></font></cfif></td>
			<td align="center" class="cfdebug" nowrap>#pages.count#</td>
			<td align="left" class="cfdebug" nowrap><cfif bad><font color="red"><span class="template_overage"></cfif>#pages.src#<cfif bad></span></font></cfif></td>
		</tr>
</cfloop>                
<cfscript>
if(!pages.recordcount || !hasQueries) {
	tot=arguments.debugging.times.total;
	q=arguments.debugging.times.query;
	loa=0;
		
}
</cfscript>     
<cfif pages.recordcount>
	
<tr>
	<td align="right" class="cfdebug" nowrap><i>#formatUnit(arguments.custom.unit, loa)#</i></td><cfif pages.recordcount><td colspan=2>&nbsp;</td></cfif>
	<td align="left" class="cfdebug"><i>STARTUP, PARSING, COMPILING, LOADING, &amp; SHUTDOWN</i></td>
</tr>
</cfif>
<tr>
	<td align="right" class="cfdebug" nowrap><i>#formatUnit(arguments.custom.unit, tot-q-loa)#</i></td><cfif pages.recordcount><td colspan=2>&nbsp;</td></cfif>
	<td align="left" class="cfdebug"><i>APPLICATION EXECUTION TIME</i></td>
</tr>
<cfif listfirst(formatUnit(arguments.custom.unit, q)," ") gt 0>
	<tr>
		<td align="right" class="cfdebug" nowrap><i>#formatUnit(arguments.custom.unit, q)#</i></td><cfif pages.recordcount><td colspan=2>&nbsp;</td></cfif>
		<td align="left" class="cfdebug"><i>QUERY EXECUTION TIME</i></td>
	</tr>
</cfif>	
<tr>
	<td align="right" class="cfdebug" nowrap><i><b>#formatUnit(arguments.custom.unit, tot)#</i></b></td><cfif pages.recordcount><td colspan=2>&nbsp;</td></cfif>
	<td align="left" class="cfdebug"><i><b>TOTAL EXECUTION TIME</b></i></td>
</tr>
</table>
<font color="red"><span class="template_overage">red = over #formatUnit(arguments.custom.unit,arguments.custom.highlight*1000)# average execution time</span></font>
</a>



<!--- Exceptions --->
<cfif structKeyExists(arguments.debugging,"exceptions")  and arrayLen(arguments.debugging.exceptions)>
	<cfset var exceptions=arguments.debugging.exceptions>
    
	<p class="cfdebug"><hr/><b class="cfdebuglge">Exceptions</b></p>
		<table border="1" cellpadding="2" cellspacing="0" class="cfdebug">
		<tr>
			<td class="cfdebug"><b>Type</b></td>
			<td class="cfdebug"><b>Message</b></td>
			<td class="cfdebug"><b>Detail</b></td>
			<td class="cfdebug"><b>Template</b></td>
		</tr>
<cfset var exp="">
<cfloop array="#exceptions#" index="exp">
		<tr>
			<td class="cfdebug" nowrap>#exp.type#</td>
			<td class="cfdebug" nowrap>#exp.message#</td>
			<td class="cfdebug" nowrap>#exp.detail#</td>
			<td class="cfdebug" nowrap><cftry>#exp.TagContext[1].template#:<cftry>#exp.TagContext[1].line#<cfcatch></cfcatch></cftry><cfcatch></cfcatch></cftry></td>
		</tr>
</cfloop>                
 </table>
</cfif>


<!--- Timers --->
<cfif timers.recordcount>
	<p class="cfdebug"><hr/><b class="cfdebuglge">CFTimer Times</b></p>
		<table border="1" cellpadding="2" cellspacing="0" class="cfdebug">
		<tr>
			<td class="cfdebug" align="center"><b>Label</b></td>
			<td class="cfdebug"><b>Time</b></td>
			<td class="cfdebug"><b>Template</b></td>
		</tr>
<cfloop query="timers">
		<tr>
			<td align="right" class="cfdebug" nowrap>#timers.label#</td>
			<td align="right" class="cfdebug" nowrap>#formatUnit(custom.unit, timers.time * 1000000)#</td>
			<td align="right" class="cfdebug" nowrap>#timers.template#</td>
		</tr>
</cfloop>                
 </table>
</cfif>

<!--- Access Scope --->
<cfif implicitAccess.recordcount>
	<p class="cfdebug"><hr/><b class="cfdebuglge">Implicit variable Access</b></p>
		<table border="1" cellpadding="2" cellspacing="0" class="cfdebug">
		<tr>
			<td class="cfdebug"><b>Scope</b></td>
			<td class="cfdebug"><b>Template</b></td>
			<td class="cfdebug"><b>Line</b></td>
			<td class="cfdebug"><b>Var</b></td>
			<td class="cfdebug"><b>Count</b></td>
		</tr>
<cfset var total=0>
<cfloop query="implicitAccess">
		<tr>
			<td align="left" class="cfdebug" nowrap>#implicitAccess.scope#</td>
			<td align="left" class="cfdebug" nowrap>#implicitAccess.template#</td>
			<td align="left" class="cfdebug" nowrap>#implicitAccess.line#</td>
			<td align="left" class="cfdebug" nowrap>#implicitAccess.name#</td>
			<td align="left" class="cfdebug" nowrap>#implicitAccess.count#</td>
		</tr>
</cfloop>                
 </table>
</cfif> 

<!--- Traces --->
<cfif traces.recordcount>
	<cfset var hasAction=!isColumnEmpty(traces,'action')>
	<cfset var hasCategory=!isColumnEmpty(traces,'category')>
	<p class="cfdebug"><hr/><b class="cfdebuglge">Trace Points</b></p>
		<table border="1" cellpadding="2" cellspacing="0" class="cfdebug">
		<tr>
			<td class="cfdebug"><b>Type</b></td>
			<cfif hasCategory><td class="cfdebug"><b>Category</b></td></cfif>
			<td class="cfdebug"><b>Text</b></td>
			<td class="cfdebug"><b>Template</b></td>
			<td class="cfdebug"><b>Line</b></td>
			<cfif hasAction><td class="cfdebug"><b>Action</b></td></cfif>
			<td class="cfdebug"><b>Var</b></td>
			<td class="cfdebug"><b>Total Time</b></td>
			<td class="cfdebug"><b>Trace Slot Time</b></td>
		</tr>
<cfset total=0>
<cfloop query="traces">
<cfset total=total+traces.time>
		<tr>
			<td align="left" class="cfdebug" nowrap>#traces.type#</td>
			<cfif hasCategory><td align="left" class="cfdebug" nowrap>#traces.category#&nbsp;</td></cfif>
			<td align="let" class="cfdebug" nowrap>#traces.text#&nbsp;</td>
			<td align="left" class="cfdebug" nowrap>#traces.template#</td>
			<td align="right" class="cfdebug" nowrap>#traces.line#</td>
			<cfif hasAction><td align="left" class="cfdebug" nowrap>#traces.action#</td></cfif>
			<td align="left" class="cfdebug" nowrap><cfif len(traces.varName)>#traces.varName#<cfif structKeyExists(traces,'varValue')> = #traces.varValue#</cfif><cfelse>&nbsp;<br />
			</cfif></td>
			<td align="right" class="cfdebug" nowrap>#formatUnit(custom.unit, total)#</td>
			<td align="right" class="cfdebug" nowrap>#formatUnit(custom.unit, traces.time)#</td>
		</tr>
</cfloop>                
 </table>
</cfif> 


<!--- Queries --->
<cfif queries.recordcount>
<p class="cfdebug"><hr/><b class="cfdebuglge"><a name="cfdebug_sql">SQL Queries</a></b></p>
<cfloop query="queries">	
<code><b>#queries.name#</b> (Datasource=#queries.datasource#, Time=#formatUnit(arguments.custom.unit, queries.time)#, Records=#queries.count#) in <cfif len(queries.src)>#queries.src#:#queries.line#</cfif></code><br />
<cfif ListFindNoCase(queries.columnlist,'usage') and IsStruct(queries.usage)><cfset var usage=queries.usage><cfset var lstNeverRead="">
<cfloop collection="#usage#" index="local.item" item="local._val"><cfif not _val><cfset lstNeverRead=ListAppend(lstNeverRead,item,', ')></cfif></cfloop>
<cfif len(lstNeverRead)><font color="red">the following colum(s) are never read within the request:#lstNeverRead#</font><br /></cfif>
</cfif>
<pre>#queries.sql#</pre></cfloop>
</cfif>


<!--- Scopes --->
<cfset var scopes="Application,CGI,Client,Cookie,Form,Request,Server,Session,URL">
<cfif not structKeyExists(arguments.custom,"scopes")><cfset arguments.custom.scopes=""></cfif>
<cfif len(arguments.custom.scopes)>
<p class="cfdebug"><hr/><b class="cfdebuglge"><a name="cfdebug_scopevars">Scope Variables</a></b></p>
<cfset var name= "">
<cfloop list="#scopes#" index="name"><cfif not ListFindNoCase(arguments.custom.scopes,name)><cfcontinue></cfif>
<cfset var doPrint=true>
<cftry>
	<cfset var scp=evaluate(name)>
    <cfcatch><cfset doPrint=false></cfcatch>
</cftry>

<cfif doPrint and structCount(scp)>
<pre class="cfdebug"><b>#name# Variables:</b><cftry><cfloop index="key" list="#ListSort(StructKeyList(scp),"textnocase")#">
#(key)#=<cftry><cfif IsSimpleValue(scp[key])>#HTMLEditFormat(scp[key])#<!--- 
---><cfelseif isArray(scp[key])>Array (#arrayLen(scp[key])#)<!--- 
---><cfelseif isValid('component',scp[key])>Component (#GetMetaData(scp[key]).name#)<!--- 
---><cfelseif isStruct(scp[key])>Struct (#StructCount(scp[key])#)<!--- 
---><cfelseif IsQuery(scp[key])>Query (#scp[key].recordcount#)<!--- 
---><cfelse>Complex type</cfif><cfcatch></cfcatch></cftry></cfloop><cfcatch>error (#cfcatch.message#) occurred while displaying Scope #name#</cfcatch></cftry>
</pre>
</cfif>
</cfloop>
</cfif>
<font size="-1" class="cfdebug"><i>Debug Rendering Time: #formatUnit(arguments.custom.unit, getTickCount()-time)#</i></font><br />
	</td>
	</cfif>
</tr>
</table>
</cfoutput>
    
    </cffunction>
   
<cffunction name="formatUnit" output="no" returntype="string">
	<cfargument name="unit" type="string" required="yes">
	<cfargument name="time" type="numeric" required="yes">
    
    <cfif arguments.time GTE 100000000><!--- 1000ms --->
    	<cfreturn int(arguments.time/1000000)&" ms">
    <cfelseif arguments.time GTE 10000000><!--- 100ms --->
    	<cfreturn (int(arguments.time/100000)/10)&" ms">
    <cfelseif arguments.time GTE 1000000><!--- 10ms --->
    	<cfreturn (int(arguments.time/10000)/100)&" ms">
    <cfelse><!--- 0ms --->
    	<cfreturn (int(arguments.time/1000)/1000)&" ms">
    </cfif>
    
    
    <cfreturn (arguments.time/1000000)&" ms">
</cffunction>   
<!---<cffunction name="formatUnit2" output="no" returntype="string">
	<cfargument name="unit" type="string" required="yes">
	<cfargument name="time" type="numeric" required="yes">
    <cfif unit EQ "millisecond">
    	<cfreturn int(time/1000000)&" ms">
    <cfelseif unit EQ "microsecond">
    	<cfreturn int(time/1000)&" &micro;s">
    <cfelse>
    	<cfreturn int(time)&" ns">
    </cfif>
</cffunction>--->
</cfcomponent>