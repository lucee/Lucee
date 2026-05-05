<cfscript>
NL="
";
function ComponentListPackageAsStruct(string package, cfcNames=structnew("linked")){
	try{
		arguments._cfcNames=ComponentListPackage(arguments.package);
		loop array="#arguments._cfcNames#" index="local.i" item="local.el" {
			arguments.cfcNames[local.el]=arguments.package&"."& local.el;
		}
	}
	catch(e){}
	return arguments.cfcNames;

}

/**
* cast a String to a File Object
* @param strFile string to cast
* @return File Object
*/
function printError(error,boolean longversion=false) {
	if(IsSimpleValue(arguments.error))arguments.error=struct(message:arguments.error);
	if(not StructKeyExists(arguments.error,'detail'))arguments.error.detail="";
	else if(arguments.error.message EQ arguments.error.detail)arguments.error.detail="";

	// log to log file
	if(arguments.error.message NEQ "" && arguments.error.detail NEQ "" || !isNull(arguments.error.cfcatch) ){
		if(isNull(error.cfcatch))
			log type="error" log="application" text=error.message&";"&error.detail;
		else
			log type="error" log="application" exception=error.cfcatch;
	}

	

	if(StructKeyExists(arguments.error,'message') and arguments.error.message NEQ "") {
		header statuscode="500";
		param name="url.rawError" default="false";
		if ( url.rawError ){
			echo( arguments.error.message & chr(10) );
			if ( len(arguments.error.detail) )
				echo( arguments.error.detail & chr(10) );
			if (!isNull(arguments.error.exception.StackTrace) && !isEmpty(arguments.error.exception.StackTrace)) {
				echo(arguments.error.exception.StackTrace);
			}
			abort;
		} 
		writeOutput('<div class="error">');
		writeOutput(br(arguments.error.message));
		

		if (!isNull(arguments.error.exception.StackTrace) && !isEmpty(arguments.error.exception.StackTrace)) {
			echo('<p>
			<button id="error-details">Show Details</button>
			<div id="stack-trace" style="display: none;">#arguments.error.exception.StackTrace#</div>');

			htmlbody {
				echo(
				'<script>
					$("##error-details").click(function(){
						if($(this).text() == "Show Details"){
							$(this).text("Hide Details");
						} else{
							$(this).text("Show Details");
						}
						$("##stack-trace").toggle();
					});
				</script>'
				);
			}
		}
		writeOutput('<br>');
		writeOutput(br(arguments.error.detail));

		if(!isNull(url.debug) || longversion) {
			if(StructKeyExists(error,"TagContext")){
				loop array="#error.TagContext#" index="local.i" item="local.tc" {
					writeOutput('<br><span class="comment">');

					if(i==1) writeOutput('error occurred in ');
					else writeOutput('called by ');
					writeOutput(error.TagContext[i].template&':'&error.TagContext[i].line&"</span>");
				}
			}
			//if(!isNull(error.cfcatch)) dump(error.cfcatch);

		}
		//ErrorCode,addional,TagContext,StackTrace,type,Detail,Message,ExtendedInfo
		writeOutput('</div>');
	}
}

function noAccess(string text){
	writeOutput('<div class="CheckError">#text#</div></cfoutput><br /><br />');
}

function br(str) {
	str=replace(str,'
','<br>','all');

	return str;
}


/**
* cast a String to a File Object
* @param strFile string to cast
* @return File Object
*/
function toInt(number obj) {
	return obj;
}

function two(number) {
	if(arguments.number LT 10) return "0"&arguments.number;
	return arguments.number;
}

/**
* read all elements from form scope with pattern <arguments.fieldname>_<int>
* @param fieldName wished field name to extract
* @return Array with all values
*/
function toArrayFromForm(fieldName) {
	var len=len(fieldName)+1;
	var rtn=array();
	for(var key in form) {
		if(findNoCase(fieldName&"_",key) EQ 1) {
			var index=right(key,len(key)-len);
			if(isNumeric(index))rtn[index]=form[key];
		}
	}
	return rtn;
}

/**
* returns a specified line from a query as struct
*/
function queryRow2Struct(query,row) {
	var sct=struct();
	var columns=listToArray(arguments.query.columnlist);
	for(var el in columns) {
		sct[el]=arguments.query[el][arguments.row];
	}
	return sct;
}

function nullIfNoDate(fieldName) {
	var d=trim(form[fieldName&"_day"]);
	var m=trim(form[fieldName&"_month"]);
	var y=trim(form[fieldName&"_year"]);
	if(isNumeric(d) and isNumeric(m) and isNumeric(y)) {
		return CreateDate(y,m,d);
	}
}

function nullIfNoTime(fieldName) {
	var h=trim(form[fieldName&"_hour"]);
	var m=trim(form[fieldName&"_minute"]);
	var s=0;
	if(structKeyExists(form,fieldName&"_second") and isNumeric(trim(form[fieldName&"_second"]))) s=trim(form[fieldName&"_second"]);
	if(isNumeric(h) and isNumeric(m)) {
		return CreateTime(h,m,s);
	}
}

function toStructInterval(raw) {
	var interval.raw=arguments.raw;
	interval.second=arguments.raw;
	interval.minute=0;
	interval.hour=0;

	if(interval.second GTE 60*60) {
		interval.hour=int(interval.second/(60*60));
		var _hour=interval.hour*60*60;
		interval.second=interval.second-_hour;
	}

	if(interval.second GTE 60) {
		interval.minute=int(interval.second/60);
		var _minute=interval.minute*60;
		interval.second=interval.second-_minute;
	}
	return interval;
}

function cut(_str,max) {
	if(not isDefined('arguments._str') or len(arguments._str) EQ 0) return "&nbsp;";
	if(len(arguments._str) GT arguments.max) return left(arguments._str, arguments.max)&"...";
	return arguments._str;
}

function getForm(formKey, default) {
	if(not structKeyExists(form,formKey)) return default;
	return form[formKey];
}

function go(action,action2='',others=struct()) {

	var qsArr=listToArray(cgi.query_string,'&');
	var rtn=request.self&"?action="&action;
	if(len(action2)) rtn=rtn&"&action2="&action2;
	var item="";
	var oKeys=structKeyArray(others);

	for(var i=1; i LTE arrayLen(qsArr); i=i+1) {
		item=listToArray(qsArr[i],'=');
		if(not structKeyExists(others,item[1]) and item[1] NEQ "action" and item[1] NEQ "action2") {
			rtn=rtn&'&'&item[1]&"="&(item[2]?:"");
		}
	}

	for(i=1; i LTE arrayLen(oKeys); i=i+1) {
		rtn=rtn&'&'&oKeys[i]&"="&others[oKeys[i]];
	}


	return rtn;
}
function byteFormat(numeric raw){
	if(raw EQ 0) return "0b";

	var b=raw;
    var rtn="";
   	var kb=int(b/1024);
    var mb=0;
    var gb=0;
    var tb=0;

    if(kb GT 0) {
    	b-=kb*1024;
        mb=int(kb/1024);
        if(mb GT 0){
        	kb-=mb*1024;
			gb=int(mb/1024);
            if(gb GT 0) {
                mb-=gb*1024;
				tb=int(gb/1024);
                if(tb GT 0) {
                    gb-=tb*1024;
                }
            }
        }
    }

    if(tb) rtn&=tb&"tb ";
    if(gb) rtn&=gb&"gb ";
    if(mb) rtn&=mb&"mb ";
    if(kb) rtn&=kb&"kb ";
    if(b) rtn&=b&"b ";
    return trim(rtn);
}


function byteFormatShort(numeric raw, string preference='' ){
	if(raw EQ 0) return "0b";

	var b=raw;
    var rtn="";
   	var kb=int(b/1024);
    var mb=0;
    var gb=0;
    var tb=0;

    if(kb GT 0) {
    	b-=kb*1024;
        mb=int(kb/1024);
        if(mb GT 0){
        	kb-=mb*1024;
			gb=int(mb/1024);
            if(gb GT 0) {
                mb-=gb*1024;
				tb=int(gb/1024);
                if(tb GT 0) {
                    gb-=tb*1024;
                }
            }
        }
    }

	if(preference EQ "tb") return _byteFormatShort(tb,gb,"tb");
	if(preference EQ "gb") return _byteFormatShort(gb,mb,"gb");
	if(preference EQ "mb") return _byteFormatShort(mb,kb,"mb");
	if(preference EQ "kb") return _byteFormatShort(kb,b,"kb");

    if(tb) return _byteFormatShort(tb,gb,"tb");
	if(gb) return _byteFormatShort(gb,mb,"gb");
	if(mb) return _byteFormatShort(mb,kb,"mb");
	if(kb) return _byteFormatShort(kb,b,"kb");

    return b&"b ";
}

function _byteFormatShort(numeric left,numeric right,string suffix){
	var rtn=left&".";
	right=int(right/1024*1000)&"";
	while(stringlen(right) lt 3) right="0"&right;

	right=left(right,2);

	return rtn&right&suffix;
}


function renderSettings(name, value, text="",boolean isExpand=false) {
	if(isNull(application.systemPropOrEnvVarInfo)) {
		application.systemPropOrEnvVarInfo=GetSystemPropOrEnvVarInfo();
	}
	if(isNull(formatForConsole)) {
		var stText=caller.stText;
		var formatForConsole=caller.formatForConsole;
	}
	var data=application.systemPropOrEnvVarInfo[name];
	var desc  = len(arguments.text) ? arguments.text:  stText.settings.sysopenvvar;
	
	
	
	// SIMPLE
	if(data.type == "simple") {
		if(!arguments.isExpand) {
			echo('<div class="coding-tip-trigger">% #stText.settings.syspropenvvar# %</div>');
		}

		echo('<div class="coding-tip #arguments.isExpand ? 'expanded' : ''#">');
		echo('<div class="disp-flex"><cfif len(desc)><span class="comment">#desc#</span></cfif> <span class="copy flex-pull-right">copy</span></div>');
		var res=formatForConsole(arguments.value);
		echo('
System Property:
<code>-D#data.systemProperties[1]#=#res.sp#</code>
Enviroment Variable:
<code>#data.environmentVariables[1]#=#res.ev#</code>
		');
		echo('</div>');
	}
	
	// LIST
	else {
		var qry=arguments.value.value;

		if(!arguments.isExpand) {
			echo('<div class="coding-tip-trigger">% System Properties %</div>');
		}
		echo('<div class="coding-tip #arguments.isExpand ? 'expanded' : ''#">');
		echo('<div class="disp-flex"><cfif len(desc)><span class="comment">#desc#</span></cfif> <span class="copy flex-pull-right">copy</span></div>');
		//dump(qry.columnlist);
		
		echo('System Properties:<code>');
		var cols=arguments.value.columns;
		loop query=qry {
			if(qry.currentrow>1) echo(NL);
			loop array=cols item="local.col" {
				if(!queryColumnExists(qry,col)) continue;
				printVar("sp","-D#data.systemProperties[1]#.#qry.currentrow#.#col#",qry[col]);
			}
		}
		echo('</code>');
		echo('</div>');

		if(!arguments.isExpand) {
			echo('<div class="coding-tip-trigger">% Enviroment Variables %</div>');
		}
		echo('<div class="coding-tip #arguments.isExpand ? 'expanded' : ''#">');
		echo('<div class="disp-flex"><cfif len(desc)><span class="comment">#desc#</span></cfif> <span class="copy flex-pull-right">copy</span></div>');
		echo('Enviroment Variables:<code>');
		var cols=arguments.value.columns;
		loop query=qry {
			if(qry.currentrow>1) echo(NL);
			loop array=cols item="local.col" {
				if(!queryColumnExists(qry,col)) continue;
				printVar("ev","#data.environmentVariables[1]#_#qry.currentrow#_#ucase(col)#",qry[col]);
			}
		}
		echo('</code>');
		echo('</div>');
		
	}
}

function printVar(type,prefix,val) {
	if(isNull(val) || isEmpty(val)) {
		// do noting
	}
	else if(isStruct(val) || isArray(val)) {
		loop collection=val index="local.k" item="local.v" {
			if(type=="sp") printVar(type,"#prefix#.#k#",v);
			else printVar(type,"#prefix#_#ucase(k)#",v);
		}
	}
	else {
		echo("#prefix#=#formatForConsole(val)[type]##NL#");
	}
	
}

function formatForConsole(value) {
	var res={};
	if(isBoolean(arguments.value) or isNumeric(arguments.value)) {
		res.ev=res.sp=arguments.value;
	}
	else if(isStruct(arguments.value)) {
		res.ev=res.sp= serializeJson(arguments.value);
	}
	else {
		res.sp="'#trim(arguments.value)#'";
		res.ev="""#trim(arguments.value)#""";
	}
	return res;
}

function addPrimary(qry) {
	var col=[];
	loop query=qry {
		arrayAppend(col,qry.physicalFirst?"physical":"archive");
	}
	
	queryAddColumn(qry,"primary",col);

	return qry;
}

function removeCoreBundle(qry) {
	qry=duplicate(qry);
	var cols=queryColumnArray(qry);
	loop query=qry {
		loop array=cols item="local.col" {
			if(len(col)>10 && right(col,10)=="bundleName" && qry[col] == "lucee.core") {
				var prefix=left(col,len(col)-10);
				querySetCell(qry,col,"",qry.currentrow);
				if(queryColumnExists(qry,prefix&"BundleVersion"))querySetCell(qry,prefix&"BundleVersion","",qry.currentrow);

			}
		}
	}
	return qry;
}


function renderSysPropEnvVar(name, value, text="",defaultValue=true,boolean isExpand=false) {
	var uname=replace(ucase(arguments.name),".","_","all");
	var stText= application.stText[session.lucee_admin_lang];
	var desc  = len(arguments.text) ? arguments.text : stText.settings.sysopenvvar;

	if(isNull(arguments.value)) {
		arguments.value=server.system.environment[uname]?:(server.system.properties[name]?:arguments.defaultValue);
	}
	if(isBoolean(arguments.value) or isNumeric(arguments.value)) {
		local.ev=local.sp=arguments.value;
	}
	else {
		local.sp="'#trim(arguments.value)#'";
		local.ev="""#trim(arguments.value)#""";
	}
	if(!arguments.isExpand) {
		echo('<div class="coding-tip-trigger">% #stText.settings.syspropenvvar# %</div>');
	}
	```
<cfoutput>
<div class="coding-tip #arguments.isExpand ? 'expanded' : ''#">
<div class="disp-flex"><cfif !(isBoolean(desc) && !desc)>#desc#:</cfif> <span class="copy flex-pull-right">copy</span></div>
<code>// System Property
-D#trim(arguments.name)#=#sp# 
// Enviroment Variable
#uname#=#ev#</code>
	</div>
</cfoutput>
	```
}

</cfscript>




<cffunction name="createUIDFolder" output="no"
    	hint="create a new step cfc">
    	<cfargument name="uid" type="string">

        <cfset var info="">
        <cfset var data.directory="">
        <cfadmin
            action="getExtensionInfo"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            returnVariable="info">
        <cfset data.directory=info.directory>

        <!--- create directory --->
		<cfset var dest=data.directory>
        <cfif not DirectoryExists(dest)>
            <cfdirectory directory="#dest#" action="create" mode="777">
        </cfif>

        <!--- uid --->
        <cfset dest=dest&"/"&arguments.uid>
        <cfif not DirectoryExists(dest)>
            <cfdirectory directory="#dest#" action="create" mode="777">
        </cfif>

        <cfreturn dest>
    </cffunction>


<cffunction name="renderCodingTip" output="true">
	<cfargument name="codeSample"   default="">
	<cfargument name="text"         default="">
	<cfargument name="isExpand"     default="#false#" type="boolean">

	<cfset var stText= application.stText[session.lucee_admin_lang]>
	<cfset var desc  = len(arguments.text) ? arguments.text : stText.settings.appcfcdesc>

	<cfif !arguments.isExpand>
		<div class="coding-tip-trigger">&lt; #stText.settings.appcfccode# &gt;</div>
	</cfif>
	<div class="coding-tip #arguments.isExpand ? 'expanded' : ''#">
		<div class="disp-flex"><cfif !(isBoolean(desc) && !desc)>#desc#:</cfif> <span class="copy flex-pull-right">copy</span></div>
		<code>#trim(arguments.codeSample)#</code>
	</div>
</cffunction>





<cffunction name="renderEditButton" output="true">
	<cfargument name="href"   default="">
	<a class="editIcon" href="#arguments.href#"></a>
</cffunction>

<cffunction name="renderMailButton" output="true">
	<cfargument name="href"   default="">

	<a class="mail-icon" href="#arguments.href#" title="Send a test mail" width="25"></a>
</cffunction>

<cfscript>

	/**
	 * replaces double quote character with two consecutive quote characters
	 */
	public string function escapeDoubleQuotes(required string input) {
		return replace(arguments.input, '"', '""', "all");
	}

	/**
	 * replaces single quote character with two consecutive quote characters
	 */
	public string function escapeSingleQuotes(required string input) {
		return replace(arguments.input, "'", "''", "all");
	}

	function safeText (str){
		if (isNull(request.hasESAPI))
			request.hasESAPI = ExtensionExists( "37C61C0A-5D7E-4256-8572639BE0CF5838" );
		if (request.hasESAPI)
			return encodeForHtml(arguments.str);
		else
			return htmlEditFormat(arguments.str);
	}

	function lockedReadOnly () {
		var info="this record cannot be modified here, because because it is set as readonly.
				To modify the record, you will need to change the readonly flag inside the configuration file and then restart the server.";

		return '<span class="locked-indicator" data-tooltip="#info#">&##128274;</span>';
	}

	function lockedSysOpEnvVar (){
		 var info="this record cannot be modified here, because it is set as an environment variables or system properties on this server.
				To modify the value, you will need to remove the environment variables or the system properties and then restart the server.";
		return '<span class="locked-indicator" data-tooltip="#info#">&##128274;</span>';
	}




</cfscript>