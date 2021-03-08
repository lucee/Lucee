<cfscript>
component {

	variables.NEWLINE="
";
	variables.TAB = chr(9);
	variables.default={
		 browser : "html"
		,console : "text"
	};
	variables.supportedFormats=["simple", "text", "html", "classic", "javascript", "js"];

	// Meta data
	this.metadata.hint="Outputs the elements, variables and values of most kinds of CFML objects. Useful for debugging. You can display the contents of simple and complex variables, objects, components, user-defined functions, and other elements.";
	this.metadata.attributetype="fixed";
	this.metadata.attributes={
		"var": {required:false, type:"any",hint="Variable to display. Enclose a variable name in pound signs."},
		"eval": {required:false, type:"string",hint="name of the variable to display, also used as label, when no label defined."},
		"expand": {required:false, type:"boolean",default:true,hint="expands views"},
		"label": {required:false, type:"string",default:"",hint="header for the dump output."},
		"top": {required:false, type:"number",default:9999,hint="The number of rows to display."},
		"showUDFs": {required:false, type:"boolean",default:true,hint="show UDFs in cfdump output."},
		"show": {required:false, type:"string",default:"all",hint="show column or keys."},
		"output": {required:false, type:"string",default:"browser",hint="Where to send the results:
- console: the result is written to the console (System.out).
- debug: the result is send to the debug output.
- browser (default): the result is written the the browser response stream."},
		"metainfo": {required:false, type:"boolean",default:true,hint="Includes information about the query in the cfdump results."},
		"keys": {required:false, type:"number",default:9999,hint="For a structure, number of keys to display."},
		"hide": {required:false, type:"string",default:"all",hint="hide column or keys."},
		"format": {required:false, type:"string",default:"",hint="specify the output format of the dump, the following formats are supported:
- simple: - a simple html output (no javascript or css)
- text (default when output equal console): plain text output (no html)
- html (default when output  equal ""browser""): regular output with html/css/javascript
- classic: classic view with html/css/javascript
- javascript or js: regular output with javascript"
},
		"abort": {required:false, type:"boolean",default:false,hint="stops further processing of request."},
		"contextlevel": {required:false, type:"number",default:2,hidden:true},
		"async": {required:false, type="boolean", default=false, hint="if true and output is not to browser, Lucee builds the output in a new thread that runs in parallel to the thread that called the dump.  please note that if the calling thread modifies the data before the dump takes place, it is possible that the dump will show the modified data."},
		"enabled": { required: false, type: "boolean", default: true, hint: "dumps are enabled by default, pass false to short circuit a dump execution and effectively disable it" }
	};


	/* ==================================================================================================
	   INIT invoked after tag is constructed															=
	================================================================================================== */
	function init(required boolean hasEndTag, component parent) {
		return this;
	}

	/* ==================================================================================================
	   onStartTag																					   =
	================================================================================================== */
	boolean function onStartTag(required struct attributes, required struct caller) {
		// inital settings

		var attrib = arguments.attributes;

		if (!attrib.enabled)
			return false;

		//eval
		if(not structKeyExists(attrib,'var') and structKeyExists(attrib,'eval')) {
			if(not len(attrib.label))
				attrib['label'] = attrib.eval;
			// eval not supported when caller has "Handle unquoted tag attribute values as strings." disabled
			attrib['var'] = evaluate(attrib.eval, arguments.caller);

			/*try {}
			catch(local.e){
				if(isSimpleValue(attrib.eval)) rethrow;
				throw "attribute ""eval"" cannot be evaluated because it is not a string, the attribute ""eval"" is not supported for the Lucee dialect.";
			}*/
		}

		// context
		var context = GetCurrentContext();
		var contextLevel = structKeyExists(attrib,'contextLevel') ? attrib.contextLevel : 2;
		contextLevel = min(contextLevel,arrayLen(context));
		if (contextLevel > 0) {
			context = context[contextLevel].template & ":" &
					context[contextLevel].line;
		}
		else {
			context = '[unknown file]:[unknown line]';
		}

		// format
		attrib.format = trim(attrib.format);

		if(attrib.format == "js"){
			attrib.format = "javascript";
		}

		if(len(attrib.format) == 0) {
			if(attrib.output == "console")	  attrib.format = variables.default.console;
			else if(attrib.output == "browser") attrib.format = variables.default.browser;
			else								attrib.format = variables.default.console;
		}
		else if(!arrayFindNoCase(variables.supportedFormats, attrib.format)) {
			throw message="format [#attrib.format#] is not supported, supported formats are [#arrayToList(variables.supportedFormats)#]";
		}

		// create dump struct out of the object
		try {
			var meta = dumpStruct(structKeyExists(attrib,'var') ? attrib.var : nullValue(), attrib.top, attrib.show, attrib.hide, attrib.keys, attrib.metaInfo, attrib.showUDFs, attrib.label);
		}
		catch(e) {
			var meta = dumpStruct(structKeyExists(attrib,'var') ? attrib.var : nullValue(), attrib.top, attrib.show, attrib.hide, attrib.keys, attrib.metaInfo, attrib.showUDFs);
		}

		if (attrib.async && (attrib.output NEQ "browser")) {

			thread name="dump-#createUUID()#" attrib="#attrib#" meta="#meta#" context="#context#" caller="#arguments.caller#" {

				doOutput(attrib, meta, context, caller);
			}
		} else {

			doOutput(attrib, meta, context, arguments.caller);
		}

		if (attrib.abort)
			abort;

		return true;
	}


	function doOutput(attrib, meta, context, caller ) {

		var dumpID = createId();

		var hasReference = structKeyExists(arguments.meta,'hasReference') && arguments.meta.hasReference;
		var result = this[ arguments.attrib.format ](arguments.meta, arguments.context, arguments.attrib.expand, arguments.attrib.output, hasReference, 0, dumpID);

		// sleep(5000);	// simulate long process to test async=true

		if (arguments.attrib.output == "browser") {

			echo(variables.NEWLINE & '<!-- ==start== dump #now()# format: #arguments.attrib.format# -->' & variables.NEWLINE);
			echo('<div id="#dumpID#" class="-lucee-dump">#result#</div>' & variables.NEWLINE);
			echo('<!-- ==stop== dump -->' & variables.NEWLINE);
		}
		else if (arguments.attrib.output == "console") {
			systemOutput(result,true);
		}
		else if (arguments.attrib.output == "debug") {
				admin action="addDump" dump="#result#";
		} 
		else {
			if(arguments.attrib.format == 'text')
				file action="append" addnewline="yes" file="#arguments.attrib.output#" output="#result#";
			else
				file action="append" addnewline="yes" file="#arguments.attrib.output#" output="<div id=""#dumpID#"" class=""-lucee-dump"">#result#</div>";
		}
	}


	/* ==================================================================================================
	   javascript																							 =
	================================================================================================== */
	any function javascript(required struct meta,
						  required string context,
						  required string expand,
						  required string output,
						  required string hasReference ,
						  required string level ,
						  required string dumpID,
						  struct cssColors={}) {
		var NEWLINE=variables.NEWLINE;
		var id = createId();
		var rtn = "";
		var columnCount = structKeyExists(arguments.meta,'data') ? listLen(arguments.meta.data.columnlist) : 0;
		var title = !arguments.level ? arguments.context : '';
		var width = structKeyExists(arguments.meta,'width') ? arguments.meta.width : '';
		var height = structKeyExists(arguments.meta,'height') ? arguments.meta.height : '';
		var indent = repeatString(variables.TAB, arguments.level);

			// Header
			var variables.colorKeys={};
			var head="";
			if(arguments.level == 0){
				var colors=arguments.meta.colors[arguments.meta.colorId];

				// javascript
				head=('<script language="JavaScript" type="text/javascript">' & variables.NEWLINE);
				head&=("function dumpOC(name){");
				head&=("var tds=document.all?document.getElementsByTagName('tr'):document.getElementsByName(name);");
				head&=("var s=null;");
				head&=("name=name;");
				head&=("for(var i=0;i<tds.length;i++) {");
				head&=("if(document.all && tds[i].name!=name)continue;");
				head&=("s=tds[i].style;");
				head&=("if(s.display=='none') s.display='';");
				head&=("else s.display='none';");
				head&=("}");
				head&=("}"& variables.NEWLINE);

				head&='if(typeof(parseDumpJSON)!="function"){' & variables.NEWLINE;
				head&='function parseDumpJSON(dump, level, containerId){' & variables.NEWLINE;
					// level start
					head&='if(!level){' & variables.NEWLINE;
						head&='var dumpList=Object.keys(dump);' & variables.NEWLINE;
						head&='for(var i=0;i<dumpList.length;i++){' & variables.NEWLINE;
							head&='var body = document.getElementById(dumpList[i]);' & variables.NEWLINE;
							head&='var tbdy = document.createElement("tbody");' & variables.NEWLINE;
							head&='var currDumpObj=dump[dumpList[i]];' & variables.NEWLINE;
							head&='var tbl = document.createElement("table");' & variables.NEWLINE;
							head&='if(currDumpObj.WIDTH!="")' & variables.NEWLINE;
								head&='tbl.style.width=currDumpObj.WIDTH;' & variables.NEWLINE;
							head&='if(currDumpObj.HEIGHT!="")' & variables.NEWLINE;
								head&='tbl.style.height=currDumpObj.HEIGHT;' & variables.NEWLINE;
							head&='if(currDumpObj.TITLE!="")' & variables.NEWLINE;
								head&='tbl.title=currDumpObj.TITLE;' & variables.NEWLINE;
							// titleTR Start
							head&='if(typeof(currDumpObj.META.TITLE)!="undefined"){' & variables.NEWLINE;
								head&='var titleTR=document.createElement("tr");' & variables.NEWLINE;
								head&='var titleTD=document.createElement("td");' & variables.NEWLINE;
								// meta start
								head&='if(typeof(currDumpObj.META)!="undefined"){' & variables.NEWLINE;
									// onclick start
									head&='if(typeof(currDumpObj.META.ONCLICK) != "undefined"){' & variables.NEWLINE;
										head&='titleTD.setAttribute("onclick", currDumpObj.META.ONCLICK);' & variables.NEWLINE;
									head&='}' & variables.NEWLINE;
									// onclick end
									head&='titleTD.colSpan=currDumpObj.META.COLSPAN;' & variables.NEWLINE;
									head&='titleTD.className=currDumpObj.META.TDCLASS;' & variables.NEWLINE;
									head&='titleTD.innerHTML="<span>" + currDumpObj.META.TITLE + currDumpObj.META.ID + "</span>";' & variables.NEWLINE;
									head&='titleTD.style.cursor="pointer";' & variables.NEWLINE;
								head&='}' & variables.NEWLINE;
								// meta end
								head&='titleTR.appendChild(titleTD);' & variables.NEWLINE;
								head&='tbdy.appendChild(titleTR);' & variables.NEWLINE;
							head&='}' & variables.NEWLINE;
							// titleTR end
							head&='if(typeof(currDumpObj.META)!="undefined"){' & variables.NEWLINE;
								head&='var tbdyTRs=[];' & variables.NEWLINE;
								head&='for(var j=0;j<currDumpObj.META.DATA.length;j++){' & variables.NEWLINE;
									head&='tbdyTRs[j]=document.createElement("tr");' & variables.NEWLINE;
									head&='if(typeof(currDumpObj.META.DATA[j].NODEID) != "undefined"){' & variables.NEWLINE;
										head&='tbdyTRs[j].setAttribute("name", currDumpObj.META.DATA[j].NODEID);' & variables.NEWLINE;
									head&='}' & variables.NEWLINE;
									head&='if(currDumpObj.META.DATA[j].HIDDEN && typeof(currDumpObj.META.DATA[j].TITLE) != "undefined")' & variables.NEWLINE;
										head&='tbdyTRs[j].style.display="none";' & variables.NEWLINE;
									head&='var tbdyTDs=[];' & variables.NEWLINE;
									head&='for(var k=0;k<currDumpObj.META.DATA[j].NODEDATA.length;k++){' & variables.NEWLINE;
										head&='tbdyTDs[k]=document.createElement("td");' & variables.NEWLINE;
										head&='tbdyTDs[k].className=currDumpObj.META.DATA[j].NODEDATA[k].CLASS;' & variables.NEWLINE;
										head&='if(typeof(currDumpObj.META.DATA[j].NODEDATA[k].CONTENT)=="object"){' & variables.NEWLINE;
											head&='tbdyTDs[k].appendChild(parseDumpJSON(currDumpObj.META.DATA[j].NODEDATA[k].CONTENT, level+1,currDumpObj.META.DATA[j].NODEID));' & variables.NEWLINE;
										head&='}else' & variables.NEWLINE;
											head&='tbdyTDs[k].innerHTML=currDumpObj.META.DATA[j].NODEDATA[k].CONTENT;' & variables.NEWLINE;
										head&='tbdyTRs[j].appendChild(tbdyTDs[k]);' & variables.NEWLINE;
									head&='}' & variables.NEWLINE;
									head&='tbdy.appendChild(tbdyTRs[j]);' & variables.NEWLINE;
								head&='}' & variables.NEWLINE;
							head&='}' & variables.NEWLINE;
						head&='}' & variables.NEWLINE;
						head&='tbl.appendChild(tbdy);' & variables.NEWLINE;
						head&='body.appendChild(tbl);' & variables.NEWLINE;
					// level else
					head&='}else{' & variables.NEWLINE;
						head&='var tbl = document.createElement("table");' & variables.NEWLINE;
						head&='var tbdy = document.createElement("tbody");' & variables.NEWLINE;
						head&='var currDumpObj=dump;' & variables.NEWLINE;
						head&='if(currDumpObj.WIDTH!="")' & variables.NEWLINE;
							head&='tbl.style.width=currDumpObj.WIDTH;' & variables.NEWLINE;
						head&='if(currDumpObj.HEIGHT!="")' & variables.NEWLINE;
							head&='tbl.style.height=currDumpObj.HEIGHT;' & variables.NEWLINE;
						head&='if(currDumpObj.TITLE!="")' & variables.NEWLINE;
							head&='tbl.title=currDumpObj.TITLE;' & variables.NEWLINE;
						head&='if(typeof(currDumpObj.META.TITLE)!="undefined"){' & variables.NEWLINE;
							head&='var titleTR=document.createElement("tr");' & variables.NEWLINE;
							head&='var titleTD=document.createElement("td");' & variables.NEWLINE;
							// meta start
							head&='if(typeof(currDumpObj.META)!="undefined"){' & variables.NEWLINE;
								// onclick start
								head&='if(typeof(currDumpObj.META.ONCLICK) != "undefined"){' & variables.NEWLINE;
									head&='titleTD.setAttribute("onclick", currDumpObj.META.ONCLICK);' & variables.NEWLINE;
								head&='}' & variables.NEWLINE;
								// onclick end
								head&='titleTD.colSpan=currDumpObj.META.COLSPAN;' & variables.NEWLINE;
								head&='titleTD.className=currDumpObj.META.TDCLASS;' & variables.NEWLINE;
								head&='titleTD.innerHTML="<span>" + currDumpObj.META.TITLE + currDumpObj.META.ID + "</span>";' & variables.NEWLINE;
								head&='titleTD.style.cursor="pointer";' & variables.NEWLINE;
							head&='}' & variables.NEWLINE;
							// meta end
							head&='titleTR.appendChild(titleTD);' & variables.NEWLINE;
							head&='tbdy.appendChild(titleTR);' & variables.NEWLINE;
						head&='}' & variables.NEWLINE;
						head&='if(typeof(currDumpObj.META)!="undefined"){' & variables.NEWLINE;
							head&='var tbdyTRs=[];' & variables.NEWLINE;
							head&='for(var j=0;j<currDumpObj.META.DATA.length;j++){' & variables.NEWLINE;
								head&='tbdyTRs[j]=document.createElement("tr");' & variables.NEWLINE;
								head&='if(typeof(currDumpObj.META.DATA[j].NODEID) != "undefined"){' & variables.NEWLINE;
									head&='tbdyTRs[j].setAttribute("name", currDumpObj.META.DATA[j].NODEID);' & variables.NEWLINE;
								head&='}' & variables.NEWLINE;
								head&='if(currDumpObj.META.DATA[j].HIDDEN && typeof(currDumpObj.META.DATA[j].TITLE) != "undefined")' & variables.NEWLINE;
									head&='tbdyTRs[j].style.display="none";' & variables.NEWLINE;
								head&='var tbdyTDs=[];' & variables.NEWLINE;
								head&='for(var k=0;k<currDumpObj.META.DATA[j].NODEDATA.length;k++){' & variables.NEWLINE;
									head&='tbdyTDs[k]=document.createElement("td");' & variables.NEWLINE;
									head&='tbdyTDs[k].className=currDumpObj.META.DATA[j].NODEDATA[k].CLASS;' & variables.NEWLINE;
									head&='if(typeof(currDumpObj.META.DATA[j].NODEDATA[k].CONTENT)=="object")' & variables.NEWLINE;
										head&='tbdyTDs[k].appendChild(parseDumpJSON(currDumpObj.META.DATA[j].NODEDATA[k].CONTENT, level+1,currDumpObj.META.DATA[j].NODEID));' & variables.NEWLINE;
									head&='else' & variables.NEWLINE;
										head&='tbdyTDs[k].innerHTML=currDumpObj.META.DATA[j].NODEDATA[k].CONTENT;' & variables.NEWLINE;
									head&='tbdyTRs[j].appendChild(tbdyTDs[k]);' & variables.NEWLINE;
								head&='}' & variables.NEWLINE;
								head&='tbdy.appendChild(tbdyTRs[j]);' & variables.NEWLINE;
							head&='}' & variables.NEWLINE;
						head&='}' & variables.NEWLINE;
						head&='tbl.appendChild(tbdy);' & variables.NEWLINE;
						head&='return tbl;' & variables.NEWLINE;
					head&='}' & variables.NEWLINE;
					// level end
				head&='}' & variables.NEWLINE;
				head&='}' & variables.NEWLINE;
				head&=("</script>" & variables.NEWLINE);

				// styles
				var prefix="div###arguments.dumpID#";
				head&=('<style type="text/css">' & variables.NEWLINE);
				head&=('#prefix# table {font-family:Verdana, Geneva, Arial, Helvetica, sans-serif; font-size:11px; empty-cells:show; color:#colors.fontColor#; border-collapse:collapse;}' & variables.NEWLINE);
				head&=('#prefix# td {border:1px solid #colors.borderColor#; vertical-align:top; padding:2px; empty-cells:show;}' & variables.NEWLINE);
				head&=('#prefix# td span {font-weight:bold;}' & variables.NEWLINE);
				var count=0;
				loop struct="#arguments.meta.colors#" index="local.k" item="local.v" {
					variables.colorKeys[k]=count++;
					var bc=darkenColor(darkenColor(v.highLightColor));
					var fc=(bc);
					head&="#prefix# td.luceeN#variables.colorKeys[k]# {color:#fc#;border-color:#bc#;background-color:#v.normalColor#;}"& variables.NEWLINE;
					head&="#prefix# td.luceeH#variables.colorKeys[k]# {color:#fc#;border-color:#bc#;background-color:#v.highLightColor#;}"& variables.NEWLINE;
				}


				/*loop collection="#arguments.cssColors#" item="local.key" {
					head&="td.#key# {background-color:#arguments.cssColors[key]#;}"& variables.NEWLINE;
				}*/
				head&=('</style>' & variables.NEWLINE);

			}

		if(!arguments.level)
			rtn&= '<script language="JavaScript" type="text/javascript">' & "var dumpData={};" & variables.NEWLINE;
		var tempStruct={};
		tempStruct.ID=arguments.dumpID;
		tempStruct.WIDTH=width;
		tempStruct.HEIGHT=height;
		tempStruct.TITLE=title;
		tempStruct.META={};

		if(structKeyExists(arguments.meta, 'title')){
			tempStruct.META.ID=arguments.hasReference && structKeyExists(arguments.meta,'id') ? ' [#arguments.meta.id#]' : '';
			tempStruct.META.COMMENT=structKeyExists(arguments.meta,'comment') ? "<br />" & (left(arguments.meta.comment,4)=="<img"?arguments.meta.comment:replace(HTMLEditFormat(arguments.meta.comment),chr(10),' <br>','all')) : '';
			tempStruct.META.TITLE=arguments.meta.title;
			tempStruct.META.TDCLASS="luceeH#variables.colorKeys[arguments.meta.colorId]#";
			tempStruct.META.ONCLICK="dumpOC('#id#')";
			tempStruct.META.COLSPAN=columnCount;
		}else{
			tempStruct.ID="";
		}

		tempStruct.META.DATA=[];
		if(columnCount){
			loop query="arguments.meta.data"{
				var c = 1;
				tempStruct.META.DATA[arguments.meta.data.currentRow].NODEID=len(id) ? id : '';
				tempStruct.META.DATA[arguments.meta.data.currentRow].HIDDEN=!arguments.expand && len(id) ? true : false;
				tempStruct.META.DATA[arguments.meta.data.currentRow].NODEDATA=[];
				for(var col=1; col <= columnCount-1; col++){
					var node = arguments.meta.data["data" & col];
					tempStruct.META.DATA[arguments.meta.data.currentRow].NODEDATA[col].CLASS="#doHighlight(arguments.meta,c)?'luceeH':'luceeN'##variables.colorKeys[arguments.meta.colorId]#";
					if(isStruct(node)){
						tempStruct.META.DATA[arguments.meta.data.currentRow].TITLE="";
						var value=this.javascript(node, "", arguments.expand, arguments.output, arguments.hasReference, arguments.level+1,arguments.dumpID,arguments.cssColors);
						tempStruct.META.DATA[arguments.meta.data.currentRow].NODEDATA[col].CONTENT=value;
					}else{
						tempStruct.META.DATA[arguments.meta.data.currentRow].NODEDATA[col].CONTENT='#node#';
					}
					c*=2;
				}
			}
		}

		if(!arguments.level){
			rtn&="dumpData['#arguments.dumpID#']=#serializeJSON(tempStruct)#;";
			rtn&="</script>";
			head&='<script language="JavaScript" type="text/javascript">' & variables.NEWLINE;
			head&="parseDumpJSON(dumpData,0);" & variables.NEWLINE;
			head&="</script>";
			return rtn&head;
		}else{
			return tempStruct;
		}
	}


	/* ==================================================================================================
	   html																							 =
	================================================================================================== */
	string function html(required struct meta,
						  required string context,
						  required string expand,
						  required string output,
						  required string hasReference ,
						  required string level ,
						  required string dumpID,
						  struct cssColors={}) {

		var NL = variables.NEWLINE;
		var id = createId();
		var rtn = "";
		var columnCount = structKeyExists(arguments.meta,'data') ? listLen(arguments.meta.data.columnlist) : 0;
		var title = !arguments.level ? ' title="#arguments.context#"' : '';
		var width = structKeyExists(arguments.meta,'width') ? ' width="' & arguments.meta.width & '"' : '';
		var height = structKeyExists(arguments.meta,'height') ? ' height="' & arguments.meta.height & '"' : '';
		var indent = repeatString(variables.TAB, arguments.level);

		// Header
		var variables.colorKeys={};
		var head="";

		if (arguments.level == 0){

			var colors = arguments.meta.colors[arguments.meta.colorId];

			head&=('<style>' & variables.NEWLINE);
			head&=('.-lucee-dump .disp-none { display: none; }' & variables.NEWLINE);
			head&=('</style>' & variables.NEWLINE);
			head&=('<script>' & variables.NEWLINE);
			head&=('window.__Lucee = { initialized : false,
				addEventListeners : function(selector, event, handler, useCapture){
					useCapture = useCapture || false;
					Array.prototype.forEach.call(
						 document.querySelectorAll(selector)
						,function(el, ix) {
						  el.addEventListener(event, handler, useCapture);
						}
					);
				}
				,getNextSiblings   : function(el){
					var  orig = el
						,result = [];
					while (el && el.nodeType === Node.ELEMENT_NODE) {
						if (el !== orig)
							result.push(el);
						el = el.nextElementSibling || el.nextSibling;
					}
					return result;
				}
				,onDocumentReady		   : function(){
					var L = window.__Lucee;
					if (L.initialized)
						return;
					L.addEventListeners(".collapse-trigger", "click", function(evt){
						var tr = evt.target.closest("tr");
						var siblings = L.getNextSiblings(tr);
						siblings.forEach(function(el, ix){
							el.classList.toggle("disp-none");
						});
					});
					L.initialized = true;
				}
			}
			' & variables.NEWLINE);

			head&=('document.addEventListener("DOMContentLoaded", __Lucee.onDocumentReady);' & variables.NEWLINE);
			head&=('</script>' & variables.NEWLINE);

			// styles
			var prefix="div###arguments.dumpID#";
			head&=('<style type="text/css">' & NL);
			head&=('#prefix# table {font-family:Verdana, Geneva, Arial, Helvetica, sans-serif; font-size:11px; empty-cells:show; color:#colors.fontColor#; border-collapse:collapse;}' & NL);
			head&=('#prefix# td {border:1px solid #colors.borderColor#; vertical-align:top; padding:2px; empty-cells:show;}' & NL);
			head&=('#prefix# td span {font-weight:bold;}' & NL);
			var count=0;
			loop struct="#arguments.meta.colors#" index="local.k" item="local.v" {
				variables.colorKeys[k]=count++;
				var bc=darkenColor(darkenColor(v.highLightColor));
				var fc=(bc);
				head&="#prefix# td.luceeN#variables.colorKeys[k]# {color:#fc#;border-color:#bc#;background-color:#v.normalColor#;}"& NL;
				head&="#prefix# td.luceeH#variables.colorKeys[k]# {color:#fc#;border-color:#bc#;background-color:#v.highLightColor#;}"& NL;
			}


			/*loop collection="#arguments.cssColors#" item="local.key" {
				head&="td.#key# {background-color:#arguments.cssColors[key]#;}"& NL;
			}*/
			head&=('</style>' & NL);

		}

		var rows = [];
		arrayAppend(rows, '<table#width##height##title#>');

		if (structKeyExists(arguments.meta, 'title')){

			var metaID = arguments.hasReference && structKeyExists(arguments.meta, "id") ? " [#arguments.meta.id#]" : "";
			var comment = structKeyExists(arguments.meta, "comment") ?
				"<br>" & (left(arguments.meta.comment, 4) == "<img" ?
					arguments.meta.comment : replace(HTMLEditFormat(arguments.meta.comment), chr(10), " <br>", "all"))
				: '';

			arrayAppend(rows, '<tr>');
			arrayAppend(rows, '<td class="collapse-trigger luceeH#variables.colorKeys[arguments.meta.colorId]#" colspan="#columnCount#" style="cursor:pointer;">');

			arrayAppend(rows, '<span>#arguments.meta.title##metaID#</span>');
			arrayAppend(rows, comment & '</td>');
			arrayAppend(rows, '</tr>');
		}
		else {
			id = "";
		}

		// data
		if (columnCount) {

			loop query=arguments.meta.data {
				var c = 1;
				// var nodeID = len(id) ? ' name="#id#"' : '';
				var hidden = !arguments.expand && len(id) ? ' class="disp-none" ' : '';

				// arrayAppend(rows, '<tr#nodeID##hidden#>');
				arrayAppend(rows, '<tr#hidden#>');

				for (var col=1; col <= columnCount-1; col++) {
					var node = arguments.meta.data["data" & col];

					if (isStruct(node)) {

						var value = this.html(node, "", arguments.expand, arguments.output, arguments.hasReference, arguments.level + 1,arguments.dumpID, arguments.cssColors);

						arrayAppend(rows, '<td class="#doHighlight(arguments.meta, c) ? 'luceeH' : 'luceeN'##variables.colorKeys[arguments.meta.colorId]#">');
						arrayAppend(rows, value);
						arrayAppend(rows, '</td>');
					}
					else {

						arrayAppend(rows, '<td class="#doHighlight(arguments.meta,c)?'luceeH':'luceeN'##variables.colorKeys[arguments.meta.colorId]#">' & HTMLEditFormat(node) & '</td>');
					}

					c *= 2;
				}

				arrayAppend(rows, '</tr>');
			}
		}

		arrayAppend(rows, '</table>');

		return head & arrayToList(rows, "");
	}


	/* ==================================================================================================
	   classic																						  =
	================================================================================================== */
	string function classic(required struct meta,
							 string context = "",
							 string expand = "",
							 string output = "",
							 string hasReference = false,
							 string level = 0,
							 string dumpID = "") {

		var id =  createId();
		var rtn = "";
		var columnCount = structKeyExists(arguments.meta,'data') ? listLen(arguments.meta.data.columnlist) : 0;
		var title = !arguments.level ? ' title="#arguments.context#"' : '';
		var width = structKeyExists(arguments.meta,'width') ? ' width="' & arguments.meta.width & '"' : '';
		var height = structKeyExists(arguments.meta,'height') ? ' height="' & arguments.meta.height & '"' : '';
		var indent = repeatString(variables.TAB, arguments.level);

		// define colors
		if(arguments.level == 0){
				variables.colors=arguments.meta.colors[arguments.meta.colorId];
		}
		var borderColor = darkenColor(colors.highLightColor);
		//_dump(borderColor&"-"&colors.highLightColor);
		var normalColor = "white";


			if(arguments.level == 0){
				// javascript
				rtn&=('<script language="JavaScript" type="text/javascript">' & variables.NEWLINE);
				rtn&=("function dumpOC(name){");
				rtn&=("var tds=document.all?document.getElementsByTagName('tr'):document.getElementsByName(name);");
				rtn&=("var s=null;");
				rtn&=("name=name;");
				rtn&=("for(var i=0;i<tds.length;i++) {");
				rtn&=("if(document.all && tds[i].name!=name)continue;");
				rtn&=("s=tds[i].style;" & variables.NEWLINE);
				rtn&=("if(s.display=='none') s.display='';");
				rtn&=("else s.display='none';");
				rtn&=("}");
				rtn&=("}" & variables.NEWLINE);
				rtn&=("</script>" & variables.NEWLINE);

				// styles
				var prefix="div###arguments.dumpID#";
				rtn&=('<style type="text/css">' & variables.NEWLINE);
				rtn&=('#prefix# table {font-family:Verdana, Geneva, Arial, Helvetica, sans-serif; font-size:11px; empty-cells:show; color:#colors.fontColor#; border: 2px solid black; border-collapse:collapse;}' & variables.NEWLINE);
				rtn&=('#prefix# td {border:2px solid black; vertical-align:top; padding:2px; empty-cells:show;}' & variables.NEWLINE);
				rtn&=('#prefix# td span {font-weight:bold;}' & variables.NEWLINE);

				var count=0;
				loop struct="#arguments.meta.colors#" index="local.k" item="local.v" {
					var h1Color = darkenColor(v.highLightColor);
					var h2Color = (v.normalColor);
					var borderColor = darkenColor(darkenColor(v.highLightColor));
					variables.colorKeys[k]=count++;
					rtn&="#prefix# td.luceeN#variables.colorKeys[k]# {background-color:white;border-color:#borderColor#; color:black;cursor:pointer;}"& variables.NEWLINE;
					rtn&="#prefix# td.luceeH1#variables.colorKeys[k]# {background-color:#h1Color#;border-color:#borderColor#; color:white;cursor:pointer;}"& variables.NEWLINE;
					rtn&="#prefix# td.luceeH2#variables.colorKeys[k]# {background-color:#h2Color#;border-color:#borderColor#; color:black;cursor:pointer;}"& variables.NEWLINE;
				}


				rtn&=('</style>' & variables.NEWLINE);
			}

			rtn&=('<table cellspacing="0"#width##height##title#>');

			// title
			if(structKeyExists(arguments.meta, 'title')){
				var metaID = arguments.hasReference && structKeyExists(arguments.meta,'id') ? ' [#arguments.meta.id#]' : '';
				var comment = structKeyExists(arguments.meta,'comment') ? "<br />" & replace(HTMLEditFormat(arguments.meta.comment),chr(10),' <br>','all') : '';

				rtn&=('<tr>');
				rtn&=('<td onclick="dumpOC(''#id#'');" colspan="#columnCount#" class="luceeH1#variables.colorKeys[arguments.meta.colorId]#">');
				rtn&=('<span>#arguments.meta.title##metaID#</span>');
				rtn&=(comment & '</td>');
				rtn&=('</tr>');
			}
			else {
				id = "";
			}

			// data
			if(columnCount) {
				loop query="arguments.meta.data" {
					var c = 1;
					var nodeID = len(id) ? ' name="#id#"' : '';
					var hidden = !arguments.expand && len(id) ? ' style="display:none"' : '';

					rtn&=('<tr#nodeID##hidden#>');

					for(var col=1; col <= columnCount-1; col++) {
						var node = arguments.meta.data["data" & col];

						if(isStruct(node)) {
							var value = this.classic(node, "", arguments.expand, arguments.output, arguments.hasReference, arguments.level+1);

							rtn&=('<td class="#doHighlight(arguments.meta,c)?'luceeH2':'luceeN'##variables.colorKeys[arguments.meta.colorId]#">');
							rtn&=(value);
							rtn&=('</td>');
						}
						else {
							rtn&=('<td class="#doHighlight(arguments.meta,c)?'luceeH2':'luceeN'##variables.colorKeys[arguments.meta.colorId]#">' & HTMLEditFormat(node) & '</td>');
						}
						c *= 2;
					}
					rtn&=('</tr>');
				}
			}
			rtn&=('</table>');

		return rtn;
	}

	/* ==================================================================================================
	   simple																						   =
	================================================================================================== */
	string function simple(required struct meta,
							string context = "",
							string expand = "",
							string output = "",
							string hasReference = false,
							string level = 0) {

		var rtn = "";
		var col = 0;
		var columnCount = structKeyExists(arguments.meta,'data') ? listLen(arguments.meta.data.columnlist) : 0;
		var width = structKeyExists(arguments.meta,'width') ? ' width="' & arguments.meta.width & '"' : '';
		var height = structKeyExists(arguments.meta,'height') ? ' height="' & arguments.meta.height & '"' : '';
		var indent = repeatString(variables.TAB, arguments.level);

		// define colors
		if(arguments.level == 0){
				variables.colors=arguments.meta.colors[arguments.meta.colorId];
		}
			rtn&=('<table cellpadding="1" cellspacing="0" border="1" title="#arguments.context#"#width##height#>');

			// title
			if(structKeyExists(arguments.meta, 'title')){
				var metaID = arguments.hasReference && structKeyExists(arguments.meta,'id') ? ' [#arguments.meta.id#]' : '';
				var comment = structKeyExists(arguments.meta,'comment') ? "<br />" & replace(HTMLEditFormat(arguments.meta.comment),chr(10),' <br>','all') : '';

				rtn&=('<tr>');
				rtn&=('<td colspan="#columnCount#" bgcolor="#variables.colors.highLightColor#">');
				rtn&=('<b>#arguments.meta.title##metaID#</b>');
				rtn&=(comment & '</td>');
				rtn&=('</tr>');
			}

			// data
			var c = 1;
			if(columnCount) {
				loop query="arguments.meta.data" {
					c = 1;

					rtn&=('<tr>');

					for(col=1; col <= columnCount-1; col++) {
						var node = arguments.meta.data["data" & col];

						if(isStruct(node)) {
							var value = this.simple(node, "", arguments.expand, arguments.output, arguments.hasReference, arguments.level+1);

							rtn&=('<td bgcolor="#doHighlight(arguments.meta,c)?variables.colors.highLightColor:variables.colors.normalColor#">');
							rtn&=(value);
							rtn&=('</td>');
						}
						else {
							rtn&=('<td bgcolor="#doHighlight(arguments.meta,c)?variables.colors.highLightColor:variables.colors.normalColor#">' & HTMLEditFormat(node) & '</td>');
						}
						c *= 2;
					}
					rtn&=('</tr>');
				}
			}
			rtn&=('</table>');

		return rtn;
	}

	/* ==================================================================================================
	   text																							 =
	================================================================================================== */
	string function text(required struct meta,
						  string context = "",
						  string expand = "",
						  string output = "",
						  string hasReference = false,
						  string level = 0,
						  string parentIndent = "") {

		var rtn = "";
		var dataCount = structKeyExists(arguments.meta,'data') ? listLen(arguments.meta.data.columnlist) - 1 : 0;
		var indent = repeatString("	", arguments.level);
		var type = structKeyExists(arguments.meta,'type') ? arguments.meta.type : '';

		// title
		if(structKeyExists(arguments.meta, 'title')) {
			rtn = trim(arguments.meta.title);
			rtn &= arguments.hasReference && structKeyExists(arguments.meta,'id') ? ' [#arguments.meta.id#]' : '';
			rtn &= structKeyExists(arguments.meta,'comment') ? ' [' & trim(arguments.meta.comment) & ']' : '';
			rtn &= variables.NEWLINE;
		}

		// data
		if(dataCount > 0) {
			var qRecords = arguments.meta.data;

			loop query="qRecords" {
				var needNewLine = true;

				for(var x=1; x <= dataCount; x++) {
					var node = qRecords["data" & x];

					if(type == "udf") {
						if(needNewLine) {
							rtn &= variables.NEWLINE & arguments.parentIndent;
							rtn &= len(trim(node)) == 0 ? "[blank] " : node & " ";
							needNewLine = false;
						}
						else {
							rtn &= len(trim(node)) == 0 ? "[blank] " : node & " ";
						}
					}
					else if(isStruct(node)) {
						rtn &= this.text(node, "", arguments.expand, arguments.output, arguments.hasReference, arguments.level+1, indent) & variables.NEWLINE;
					}
					else if(len(trim(node)) > 0) {
						var test = asc(right(rtn, 1));

						if(test == 10 || test == 13) {
							rtn &= node & " ";
						}
						else {
							rtn &= node & " ";
						}

					}

				}
			}
		}
		if(arguments.output NEQ "console" && arguments.level EQ 0) {
			return rtn;
		}

		return rTrim(rtn);
	}

	/* ==================================================================================================
	   helper functions																				 =
	================================================================================================== */
	boolean function doHighlight(required struct meta, required numeric c) {


		// all highlight
		if(arguments.meta.data.highlight == -1) return true;
		// none highlight
		else if(arguments.meta.data.highlight == 0) return false;

		return bitand(arguments.meta.data.highlight, arguments.c)>0;
	}


	string function createId(){
		return  "x" & createUniqueId();
	}


	function doCSSColors(struct data,string color){
		var key=replace(arguments.color,"##","r");
		if(isNumeric(left(key,1)))key="r"&key;


		if(!structKeyExists(arguments.data,key))
			arguments.data[key]=arguments.color;
		return key;
	}


	/** darkens a hex color */
	function darkenColor(color, delta=3) {

		if ((len(arguments.color) != 7 && len(arguments.color) != 4) || left(arguments.color, 1) != '##')
			return arguments.color;

		if(len(arguments.color) ==4) {
			arguments.color="##"&arguments.color[2]&arguments.color[2]&arguments.color[3]&arguments.color[3]&arguments.color[4]&arguments.color[4];
		}
		var result = "##";

		for (var i=2; i<=7; i++) {

			var ch = inputBaseN(mid(arguments.color, i, 1), 16);
			ch = max(0, ch - arguments.delta);
			result &= formatBaseN(ch, 16);
		}

		return result;
	}


	/** brightens a hex color */
	function brightenColor(color, delta=3) {

		if (len(arguments.color) != 7 || left(arguments.color, 1) != '##')
			return arguments.color;

		var result = "##";

		for (var i=2; i<=7; i++) {

			var ch = inputBaseN(mid(arguments.color, i, 1), 16);
			ch = min(15, ch + arguments.delta);
			result &= formatBaseN(ch, 16);
		}

		return result;
	}

}
</cfscript>
