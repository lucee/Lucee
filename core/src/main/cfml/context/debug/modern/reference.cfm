<cftry>
	<cfscript>
	setting show=false;
	develop=false;
	
	minmax="It must have at least {min} arguments but a maximum of {max}.";
	min="It must have at least {min} arguments.";
	max="Only the number of arguments is restricted to {max}.";
	argtype.fixed="The arguments for this function are set. You can not use other arguments except the following ones.";
	argtype.dynamic="There is no restriction for this function regarding its arguments.";
	
	attrtype.noname="This tag only allows one attribute value (no name)";
	attrtype.mixed="This tag has a fixed definition of attributes (see below). In addition it allowes to use any additional attribute.";
	attrtype.fixed="The attributes for this tag are fixed. Except for the following attributes no other attributes are allowed.";
	attrtype.dynamic="There is no restriction for attributes for this tag.";
	
	attr.max="Only the number of attributes is restricted to {max}.";
	attr.minmax="This tag must have at least {min} attributes but the most {max}.";
	attr.min="This tag must have at least {min} attributes.";
	
	
	body.prohibited="This tag can&apos;t have a body.";
	body.free="This tag may have a body.";
	body.required="This tag must have a body.";
	
	if(!isNull(url.search)) form.search=url.search;
	if(!isNull(url.typ)) form.typ=url.typ;
	
	function enhanceHTML(html) {
		var code=replace(html,"<code class=""language-","<code id=""lucee"" class=""language-","all");
		code=replace(code,"<code>","<code class=""lucee"">","all");
		code=replace(code,"<blockquote>","<blockquote class=""lucee"">","all");
		code=replace(code,"<h1>","<h1 class=""lucee"">","all");
		code=replace(code,"</h1>",(isEmpty(data.since?:"")?"":(" (Lucee #data.since#)"))&"</h1>","all");
		code=replace(code,"<h2>","<h2 class=""lucee"">","all");
		code=replace(code,"<h3>","<h3 class=""lucee"">","all");
		code=replace(code,"<h4>","<h4 class=""lucee"">","all");
		code=replace(code,"<h5>","<h5 class=""lucee"">","all");
		code=replace(code,"<h6>","<h6 class=""lucee"">","all");
		code=replace(code,"<p>","<p class=""lucee"">","all");
			
		code=replace(code,"~","`","all");
		return code;	
	}

	function markdownToHTMLLine(md) {
		var html=trim(markdownToHTML(trim(md)));
		if(find("<p>",html)==1) {
			var index=findLast("</p>",html);
			if(index+3==len(html)){
				html=mid(html,1,index-1); // first remove the ending p
				html=mid(html,4); // then remove the beginning p
			}	
		}
		return enhanceHTML(html);
	}
	
	function executeCodeFragments(code) {
		var startIndex=0;
		var last=0;
		var needle='```run';
		var endNeedle='```';
		var startBlockquote='
<div class="lucee_execute_result">';
		var endBlockquote='</div>';
		
		var needleLength=len(needle);
		var endNeedleLength=len(endNeedle);
		while((startIndex=find(needle, code,last))) {// 
			last=startIndex+needleLength;
			var endIndex=find(endNeedle, code,last);
			if(endIndex==0) return "end not found";//code; TODO
			var result=executeCode(mid(code,startIndex+needleLength,endIndex-(startIndex+needleLength)));
			//dump(result);
			code=mid(code,1,(endIndex-1)+endNeedleLength)&startBlockquote&result&endBlockquote&mid(code,endIndex+endNeedleLength);
			last=endIndex+endNeedleLength+len(result)+len(startBlockquote)+len(endBlockquote);
		}
		return code;
	}
	
	function executeCode(code) {
		if(isNull(variables.counter)) variables.counter=1;
		else variables.counter++;
		var ramdir="ram://templatesmonitor"&variables.counter;
		var mappingName="/monitoringexecute"&variables.counter;
		
		var currSettings=getApplicationSettings();
		try {
			if(!directoryExists(ramdir)) directoryCreate(ramdir);
			currSettings.mappings[mappingName]=ramdir;
			application action="update" mappings=currSettings.mappings;
			
			fileWrite(ramdir&"/index#variables.counter#.cfm", code);
			savecontent variable="local.result" {
				include mappingName&"/index#variables.counter#.cfm";
			}
			return result;
		}
		catch(e) {
			return "error: "&(e.message?:"");
		}
		finally { 
			try {
				if(!directoryExists(ramdir)) directoryDelete(ramdir, true);
			}catch(ee) {}
		}
		return "";
	}

	function importRecipes() {
		var tmp=listToArray(server.lucee.version,".");
		var branch=tmp[1]&"."&tmp[2];
		var rootPath=(server.system.environment.LUCEE_DOC_RECIPES_PATH?:"https://raw.githubusercontent.com/lucee/lucee-docs/master");
		var indexPath=rootPath&"/docs/recipes/index.json";
		var indexContent=trim(fileRead(indexPath));
		var indexHash=hash(indexContent);
		
		// changed?
		var localDirectory=expandPath("{lucee-config-dir}/recipes/");
		var localIndexPath=localDirectory&"index.json";
		
		// create local directory if possible and needed
		var hasLocalDir=true;
		if(!fileExists(localIndexPath)) {
			try {
				if(!directoryExists(localDirectory)) {
					directorycreate(localDirectory,true);
				}
			}
			catch(e) {
				hasLocalDir=false;
			}
		}
		
			
		// do we have a change
		var localIndex=localDirectory&"index.json";
		
		var first=!fileExists(localIndex);
		// load old index
		if(!first) {
			var oldIndex=deserializeJSON(trim(fileRead(localIndex)));
		}

		if(first || hash(trim(fileRead(localIndex)))!=indexHash) {
			
			
			setting requesttimeout="120";
			var index=deserializeJSON(indexContent);
            loop array=index item="local.entry" label="outer" {
				entry.url=rootPath&entry.path;
				entry.local=localDirectory&listLast(entry.file,"\/");
				if(!first) {
					loop array=oldIndex item="local.e" {
						
						if(e.file==entry.file && (e.hash?:"b")==(entry.hash?:"a")) {
							// read existing content from local
							entry.content=readRecipe(localDirectory&listLast(entry.file,"\/"));
							continue outer;
						}
					}
				}
			}
			try { 
				if(hasLocalDir) {
					fileWrite(localIndex, indexContent);
				}
			}
			catch(ex2) {
				log log="application" exception=ex2;
			}
		}
		else {
			var index=oldIndex;
			loop array=index item="local.entry" {
				var f=localDirectory&listLast(entry.file,"\/");
                entry.url=rootPath&entry.path;
				entry.local=localDirectory&listLast(entry.file,"\/");
				if(fileExists(f)) {
					// read existing contnt from local
					entry.content=readRecipe(localDirectory&listLast(entry.file,"\/"));
					
				}
			}
		}

        // SORT
        arraySort(index,function(l,r) {
            return compareNoCase(l.title, r.title);
        });
        
		return index;
	}

    function readRecipe(localFile) {
        var content=fileRead(localFile);
        var endIndex=find("-->", content,4);
		if(endIndex==0) return content;
		
        //var rawMeta=trim(mid(content,startIndex+5,endIndex-(startIndex+5)));
		//var json=deserializeJSON(rawMeta);
		return trim(mid(content,endIndex+3));
    }

	function getContent(data) {
		if(isNull(data.content)) {
			var content=fileRead(data.url);
			fileWrite(data.local,content);
			data.content=readRecipe(data.local)	
		}
		return data.content;
	}

	


    function recipesAsStruct(index) {
        var data=[:];
        loop array=index item="local.entry" {
            data[entry.title]=entry;
        }
        return data;
	}


	if(develop || isNull(application.recipeArray[server.lucee.version])) {
		application.recipeArray[server.lucee.version]=importRecipes();
	}
	recipeArray=application.recipeArray[server.lucee.version]

	
	if(develop || isNull(application.recipes[server.lucee.version])) {
		application.recipes[server.lucee.version]=recipesAsStruct(recipeArray);
	}
	recipes=application.recipes[server.lucee.version];




	functions=getFunctionList();
	// flatten functions
	ffunctions=structKeyArray(functions).sort("textnocase");
	
	tags=getTagList();
	prefixTags={};
	nonPrefixTags={};
	// flatten tags
	ftags=[];
	loop struct=tags index="k" item="v" {
		loop struct=v index="kk" item="vv" {
			arrayAppend(ftags,k&kk);
			vvv={'p':k,'n':kk};
			prefixTags[k&kk]=vvv;
			nonPrefixTags[k&kk]=vvv;
		}
	}
	arraySort(ftags,"textnocase");
	
	// not requesting data for a specific tag,function,...
	if(isNull(form.search)) {
		echo((serializeJson({
			'recipes':recipeArray?:{},
			'function':ffunctions,
			'tag':ftags})));
		abort;
	}
	if(!isNull(form.typ) && form.typ=="recipes") {
		
		data=recipes[form.search]?:nullValue();
		if(!isNull(data))type="recipes";
		
	}
	else if(structKeyExists(functions,form.search)) {
		type="function";
		data=getFunctionData(form.search);
	}
	else if(structKeyExists(prefixTags,form.search)) {
		type="tag";
		dd=prefixTags[form.search];
		data=getTagData(dd.p,dd.n);
	}
	else if(structKeyExists(nonPrefixTags,form.search)) {
		type="tag";
		dd=nonPrefixTags[form.search];
		data=getTagData(dd.p,dd.n);
	}
	
	
	</cfscript>
	
	<cfoutput>
	<cfif isNull(type)>
		<div class="section-title">no matching tag,function or component found 
			for <b>#htmleditFormat(reReplace( form.search, '[^a-zA-Z0-9]', ' ', 'all' ))#</b></div>
		<cfabort>
	</cfif>
	<cfif "recipes" NEQ type>
		<!--- title --->
		<h1 class="lucee">#ucFirst(type)# #data.name?:data.title#</h1>
		<!--- deprecated? --->
		<cfif (data.status?:"") EQ "deprecated">
			<div class="warning nofocus">This #type# is deprecated</div>
		</cfif>
		
		<!--- description --->
		<span>
			<cfif not StructKeyExists(data, "description")>
				<em>No description found</em>
			<cfelse>
				#enhanceHTML(markdownToHTML(data.description))#
			</cfif>
		</span>
	</cfif>

<style>
	h1.lucee, h2.lucee, h3.lucee, h4.lucee, h5.lucee, h6.lucee {
		color: ##4e7620 !important;
	}
	h1.lucee {
		font-size: 32px !important;
		margin-top: 20px !important; 
	}
	h2.lucee {font-size: 28px !important;}
	h3.lucee {font-size: 22px !important;}
	p.lucee {
		xmax-width: 100% !important; /* Ensure it doesn't overflow the container */
		font-size: 16px !important;
		align:center;
		color: ##333 !important;
	}
		/* Style for inline code */
		code.lucee {
			 background-color: ##EEE !important;
			 color: ##333 !important;
			 padding: 2px 4px !important;
			 font-family: 'Courier New', Courier, monospace !important;
	
			 border: solid 1px ##333 !important; 
			 border-radius: 5px !important;
			 white-space: nowrap !important; /* Prevent line breaks within the code */
	
		 }
	
		 /* Style for block code */
		 pre code##lucee {
			 display: block !important;
			 background-color: ##333 !important;
			 padding: 25px 25px 25px 25px !important;
			 border: solid 1px ##eee !important; 
			 border-radius: 1em !important;
			 color: ##3399cc !important;
			 margin: 1px !important;
			 white-space: pre !important; /* Preserve whitespace and formatting */
			 overflow-x: auto !important;
			 word-wrap: break-word !important;
			 xmax-width: 90% !important; /* Ensure it doesn't overflow the container */
			 font-weight: normal !important;
			 font-family: "Courier New", Courier, monospace, sans-serif !important;
			 font-size: 16px !important;
			 white-space: pre-wrap !important;
			 word-break: break-all !important;
			 word-wrap: break-word !important; 
			 tab-size: 2 !important;
		 }
		 blockquote.lucee {
			 display: block !important;
			 background-color: ##EEE !important;
			 padding: 15px !important;
			 border: solid 1px ##333 !important; 
			 border-radius: 1em !important;
			 color: ##4e7620 !important;
			 margin: 1px !important;
			 white-space: pre !important; /* Preserve whitespace and formatting */
			 overflow-x: auto !important;
			 word-wrap: break-word !important;
			xmax-width: 90% !important; /* Ensure it doesn't overflow the container */
			 font-weight: normal !important;
			 font-family: "Courier New", Courier, monospace, sans-serif !important;
			 font-size: 16px !important;
			 white-space: pre-wrap !important;
			 word-break: break-all !important;
			 word-wrap: break-word !important; 
			 tab-size: 2 !important;
		 }
	
		 .lucee_execute_result {
			background-color: white !important;
			border: solid 1px ##333 !important;
			border-radius: 1em !important;
			padding: 10px !important;
			margin-top: 30px !important; /* Increase margin-top to accommodate the overlapping text */
			margin-bottom: 10px !important; /* Increase margin-top to accommodate the overlapping text */
			xmax-width: 90% !important; /* Ensure it doesn't overflow the container */
			font-size: 16px !important;
			position: relative !important; /* Needed for the absolute positioning of the label */
		}
	
		.lucee_execute_result::before {
			content: "Generated Output from the example above" !important;
			position: absolute !important;
			top: -10px !important; /* Adjust this value to position the text correctly */
			left: 30px !important; /* Indent the text 20 pixels from the left */
			background-color: white !important;
			padding: 0 5px !important; /* Add some padding to the label */
			font-size: 16px !important;
			color: ##333 !important;
			font-weight: bold !important;
		}
	
		.language-lucee .nf {color: ##569cd6; !important}
		.language-lucee .nv {color: ##9cdcfe; !important}
		.language-lucee .syntaxFunc {color: ##dcdcaa; !important}
		.language-lucee .syntaxType {color: ##4ec9b0; !important}
		.language-lucee .p {color: ##d4d4d4; !important}
		.language-lucee .nt {color: ##569cd6; !important}
		.language-lucee .na {color: ##9cdcfe; !important}
		.language-lucee .s {color: ##ce9178; !important}
		.language-lucee .err {color: ##d4d4d4; !important}
		.language-lucee .syntaxAttr { color: ##dcdcaa; !important}
	
	
	 </style>
	<!----------------------------------------
	------------------- Recipes -------------
	------------------------------------------>
	<cfif type=="recipes">
	<cftry>
		<cfset md=getContent(data)>
		<cfset md=executeCodeFragments(md)>
		<cfset code=enhanceHTML(markdownToHTML(md))>
		
		#code#<br>
		<cfcatch>
			<p style="color:red">Unable to load content; see application log for more details</p>
			<cflog log="application" exception="#cfcatch#">
		</cfcatch>
	</cftry>
	
	
	<cfif develop>
	<h1>MD</h1>
		<pre>#replace(md?:"","<","&lt;","all")#</pre>
	<h1>HTML</h1>
		<pre>#replace(code?:"","<","&lt;","all")#</pre>
	</cfif>
	<!----------------------------------------
	------------------- FUNCTION -------------
	------------------------------------------>
	<cfelseif type=="function">
	
	<!--- Syntax TODO css missing--->
	<cfset first=true>
	<cfset optCount=0>
	<h3 class="lucee">Syntax</h3>
	
	<pre><code id="lucee" class="language-lucee"><span class="nf">#data.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#): </span><span class="syntaxType">#data.returntype#</span></code></pre>
	
	<!--- Syntax member TODO css missing--->
	<cfif !isNull(data.member)>
		<cfset first=true>
		<cfset optCount=0>
		<h3 class="lucee">Member Syntax</h3>
	<pre><code id="lucee" class="language-lucee"><span class="nf">#data.member.type#.#data.member.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="i" item="item"><cfif item.status EQ "hidden" or data.member.position EQ i><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#): </span><span class="syntaxType"><cfif data.member.chaining>#data.member.type#<cfelse>#data.returntype#</cfif></span></code></pre>
	
	
	</cfif>
	
	
	<!--- Argumente --->
	<h3 class="lucee">Arguments</h3>
	<cfif data.argumentType EQ "fixed" and not arraylen(data.arguments)>
		<p class="lucee">This function has no arguments</p>
	<cfelse>
		<p class="lucee">
			#argtype[data.argumentType]#
			<cfif data.argumentType EQ "dynamic">
				<cfif data.argMin GT 0 and data.argMax GT 0>
				#replace(replace(minMax,"{min}",data.argMin),"{max}",data.argMax)#
				<cfelseif data.argMin GT 0>
				#replace(min,"{min}",data.argMin)#
				<cfelseif data.argMax GT 0>
				#replace(max,"{max}",data.argMax)#
				</cfif>
	
			</cfif>
		</p>
	</cfif>
	
	
	<cfif data.argumentType EQ "fixed" and arraylen(data.arguments)>
		<cfset hasdefaults=false>
		<cfloop array="#data.arguments#" index="key" item="val">
			<cfif !isNull(val.defaultValue)><cfset hasdefaults=true></cfif>
		</cfloop>
		<table class="details">
		<thead>
		<tr>
			<th class="txt-l">Name</th>
			<th class="txt-l">Type</th>
			<th class="txt-l">Required</th>
			<cfif hasdefaults><th width="7%">Default</th></cfif>
			<th class="txt-l">Description</th>
		</tr>
		</thead>
		<tbody>
		<cfloop array="#data.arguments#" index="attr">
			<cfif attr.status EQ "hidden"><cfcontinue></cfif>
			<tr>
				<td class="txt-l">#attr.name	#</td>
				<td class="txt-l">#attr.type#</td>
				<td class="txt-l">#YesNoFormat(attr.required)#</td>
				<cfif hasdefaults>
					<td>
						<cfif isNull(attr.defaultValue)>&nbsp;<cfelse>#attr.defaultValue#</cfif>
					</td>
				</cfif>
				<td>
					<cfif attr.status == "deprecated">
						<b class="error">This Argument is deprecated</b>
					<cfelse>
						#markdownToHTMLLine(attr.description)#
					</cfif>
					&nbsp;
				</td>
			</tr>
		</cfloop>
		</tbody>
		</table>
		
		</cfif>
	
	
	<!----------------------------------------
	------------------- TAG -------------
	------------------------------------------>
	<cfelse>
		
	<!--- Body --->
	<h3 class="lucee">Body</h3>
	<p class="lucee">#body[ data.bodyType ]#</p>
	
	<!--- Syntax --->
	<cfset arrAttrNames= data.attributes.keyArray().sort( 'textnocase' )>
	<cfset tagName = data.namespace & data.namespaceseperator & data.name>
	<cfif data.hasNameAppendix><cfset tagName &= "CustomName"></cfif>
	
	<h3 class="lucee">Tag Syntax</h3>
	<pre><code id="lucee" class="language-lucee"><!---
			---><span class="nt">&lt;#tagName#</span><!---
			---><cfif data.attributeType == "noname"><!---
				---> <span class="syntaxTag">##<!---
					---><cfloop array="#arrAttrNames#" index="key"><!---
						--->#data.attributes[key].type# <cfbreak><!---
					---></cfloop><!---
					--->expression##<!---
				---></span><!---
			---><cfelse><!---
				---><cfloop array="#arrAttrNames#" index="key"><!---
					---><cfset attr = data.attributes[ key ]><!---
					---><cfif attr.status EQ "hidden"><cfcontinue></cfif><!---
					--->
		<cfif !attr.required><span class="err">[</span></cfif><!---
					---><span class="na">#key#=</span><!---
					---><span class="s"><!---
						---><cfif !attr.required><i></cfif><cfif attr.keyExists("values")>#attr["values"].toList("|")#<cfelse>#attr.type#</cfif><cfif !attr.required></i></cfif><!---
					---></span><!---
					---><cfif !attr.required><span class="err">]</span></cfif><!---
				---></cfloop><!---
			---></cfif><!---
	
			---><cfif data.attributeType == "dynamic" || data.attributeType == "mixed"> <span class="syntaxAttr">...</span> </cfif><!---
			---><cfif data.bodyType == "prohibited"><!---
				---><span class="nt">&gt;</span><!---
			---><cfelseif data.bodyType == "free"><!---
				---><span class="nt">&gt;</span><!---
				---><span class="err">[</span><!---
				---><span class="nt">&lt;/#tagName#&gt;</span><!---
				---><span class="err">]</span><!---
			---><cfelseif data.bodyType == "required"><!---
				---><span class="nt">&gt;<!---
				--->
	&lt;/#tagName#&gt;</span><!---
			---></cfif><!---
		---></code></pre>
	
	<!--- SCRIPT --->
		<cfif data.keyExists( "script" ) && data.script.type != "none">
			<cfset arrAttrNames = data.attributes.keyArray().sort( 'textnocase' )>
			<h3 class="lucee">Script Syntax</h3>
			<p class="lucee">This tag is also supported within cfscript</p>
			<pre><code id="lucee" class="language-lucee"><!---
			---><span class="nt">&lt;cfscript&gt;</span>
		<span class="nt">#data.name#</span><!---
		---><cfif data.attributeType == "noname"><!---
			---> <span class="syntaxAttr">##<!---
				---><cfloop array="#arrAttrNames#" index="key">#data.attributes[ key ].type# <cfbreak></cfloop><!---
				--->expression##<!---
			---></span><!---
		---><cfelseif data.script.type == "single"><!---  AND listFindNoCase("abort,break", data.name) ---><!---
			---> <span class="syntaxAttr"><!---
				---><cfloop array="#arrAttrNames#" index="key"><!---
					---><cfset ss = data.attributes[ key ].scriptSupport><!---
					---><cfset attr = data.attributes[ key ]><!---
					---><cfif ss != "none"><!---
						---><cfif ss == "optional"><span class="err">[</span></cfif><!---
						---><cfif attr.keyExists("values")>#attr["values"].toList("|")#<cfelse>#attr.type#</cfif><!---
						---><cfif data.script.rtexpr> expression</cfif><!---
						---><cfif ss == "optional"><span class="err">]</span></cfif><!---
						---><cfbreak><!---
					---></cfif><!---
				---></cfloop><!---
			---></span><!---
		---><cfelse><!---
			---><cfloop array="#arrAttrNames#" index="key"><!---
				---><cfset attr=data.attributes[key]><!---
				---><cfif attr.status == "hidden"><cfcontinue></cfif><!---
			---><cfif !attr.required><span class="err">
		[</span></cfif><!---
				---><span class="na">#key#=</span><!---
				---><span class="s"><!---
					---><cfif !attr.required></cfif><!---
					---><cfif attr.keyExists("values")>#attr["values"].toList("|")#<cfelse>#attr.type#</cfif><!---
					---><cfif !attr.required><!---
						---><span class="err">]</span><!---
				---></span><!---
					---></cfif><!---
			---></cfloop><!---
		---></cfif><!---
	
		---><cfif data.attributeType == "dynamic" || data.attributeType == "mixed"><!---
			---><span class="syntaxAttr">...</span><!---
		---></cfif><!---
		---><cfif data.bodyType == "prohibited"><!---
			---><span class="syntaxAttr">;</span><!---
		---><cfelseif data.bodyType == "required" || data.bodyType == "free"><!---
			---><span class="syntaxAttr"> {
				[...]
		}</span><!---
		---></cfif>
<span class="nt">&lt;/cfscript></span><!---
	---></code></pre>
		</cfif>
	
	<!--- Attributes --->
	<h3 class="lucee">Attributes</h3>
	<cfif data.attributeType == "fixed" && !arrayLen( arrAttrNames )>
		<p class="lucee">This tag has no attributes</p>
	<cfelse>
		<p class="lucee">#attrtype[data.attributeType]#
			<cfif data.attributeType == "dynamic">
				<cfif data.attrMin GT 0 && data.attrMax GT 0>
					#replace( replace( attr.minMax, "{min}", data.attrMin ), "{max}", data.attrMax )#
				<cfelseif data.attrMin GT 0>
					#replace( attr.min, "{min}", data.attrMin )#
				<cfelseif data.attrMax GT 0>
					#replace( attr.max, "{max}", data.attrMax )#
				</cfif>
			</cfif>
		</p>
	</cfif>
	
	
	<cfset isdefault = Findnocase('defaultValue', serializeJSON(data.attributes))>
	<cfif ( data.attributeType == "fixed" || data.attributeType == "mixed" ) && arrayLen( arrAttrNames )>
		<table class="details">
		<thead>
		<tr>
			<th>Name</th>
			<th>Type</th>
			<th>Required</th>
			<cfif val(isdefault)><th width="7%">Default</th></cfif>
			<th>Description</th>
		</tr>
		</thead>
			<tbody>
				<cfloop array="#arrAttrNames#" index="key">
					<cfset attr=data.attributes[key]>
					<cfif attr.status EQ "hidden"><cfcontinue></cfif>
					<tr>
						<td>#key#</td>
						<td><cfif attr.type EQ "object">any<cfelse>#attr.type#</cfif></td>
						<td>#YesNoFormat(attr.required)#</td>
						<cfif val(isdefault)><td><cfif structKeyExists(attr, "defaultValue")>#attr.defaultValue#</cfif></td></cfif>
						<td><cfif attr.status EQ "deprecated"><b class="error">This Attribute is deprecated</b><cfelse>#markdownToHTMLLine(attr.description)#</cfif>&nbsp;</td>
					</tr>
				</cfloop>
			</tbody>
		</table>
	</cfif>

	</cfif>
	
	
	<!--- Category --->
	<cfif structKeyExists(data, "keywords") AND !arrayIsEmpty(data.keywords)>
		<h3 class="lucee">Category</h3>
		<p class="lucee">#arraytolist(data.keywords,", ")#</p>
	</cfif>
	
	</cfoutput>
	
	
		<cfcatch>
			<cfset systemOutput(cfcatch,1,1)>
			<cfset echo(cfcatch)>
		</cfcatch>
	</cftry>