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
	
	function markdownToHTMLLine(md) {
		var html=trim(markdownToHTML(trim(md)));
		if(find("<p>",html)==1) {
			var index=findLast("</p>",html);
			if(index+3==len(html)){
				html=mid(html,1,index-1); // first remove the ending p
				html=mid(html,4); // then remove the beginning p
			}	
		}
		return html;
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
		var ramdir="ram://templates"&createUniqueID();
		var mappingName="/monitoringexecute";
		
		var currSettings=getApplicationSettings();
		try {
			if(!directoryExists(ramdir)) directoryCreate(ramdir);
			if(!structKeyExists(currSettings.mappings, mappingName)) {
				currSettings.mappings[mappingName]=ramdir;
				application action="update" mappings=currSettings.mappings;
			}
			fileWrite(ramdir&"/index.cfm", code);
			savecontent variable="local.result" {
				include mappingName&"/index.cfm";
			}
			return result;
		}
		//catch(e) {}
		finally { 
			//try {
				if(!directoryExists(ramdir)) directoryDelete(ramdir, true);
			//}catch(ee) {}
		}
		return "";
	}
	
	function importRecipes() {
		var tmp=listToArray(server.lucee.version,".");
		var branch=tmp[1]&"."&tmp[2];
		var rootPath="https://raw.githubusercontent.com/lucee/Lucee/#branch#";
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
		var entries=[];
			
		// do we have a change
		var localIndex=localDirectory&"index.json";
		// TODO update only the files with a changed hash
		var first=!fileExists(localIndex);
		
		if(first || hash(trim(fileRead(localIndex)))!=indexHash) {
			// load old index
			if(!first) {
				var oldIndex=deserializeJSON(trim(fileRead(localIndex)));
			}
			
			setting requesttimeout="120";
			var index=deserializeJSON(indexContent);
			loop array=index item="local.entry" label="outer" {
				if(!first) {
					loop array=oldIndex item="local.e" {
						
						if(e.file==entry.file && (e.hash?:"b")==(entry.hash?:"a")) {
							arrayAppend(entries, trim(fileRead(localDirectory&listLast(entry.file,"\/"))));
							continue outer;
						}
					}
				}
				
				var entryPath=rootPath&entry.path;
				var entryContent=trim(fileRead(entryPath));
				var name=listLast(entryPath,"\/");
				try {
					if(hasLocalDir) {
						fileWrite(localDirectory&name, entryContent);
					}
				}
				catch(ex1) {
					log log="application" exception=ex1;
				}
				arrayAppend(entries, entryContent);
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
			var files=directoryList(path:localDirectory,filter:"*.md");
			loop array=files item="local.file" {
				arrayAppend(entries, trim(fileRead(file)));
			}
		}
		return entries;
	}
	
	function readRecipes(cookbookDirectory) {
		var entries=importRecipes();
		var recipes=[:];
		loop array=entries item="local.content" {
			// extract metadata from header
			startIndex=find("<!--", content);
			if(startIndex!=1) continue;
			endIndex=find("-->", content,4);
			if(endIndex==0) continue;
			var rawMeta=trim(mid(content,startIndex+5,endIndex-(startIndex+5)));
			var json=deserializeJSON(rawMeta);
			json["content"]=trim(mid(content,endIndex+3))
			if(!isNull(json.categories)) {
				loop array=json.categories item="local.cat" {
					arrayAppend(json.keywords, cat);
				}
			}
			recipes[json.title]=json;
		}
		return recipes;
	}
	
	function getRecipeStruct(recipes) {
		var titles=[];
		loop struct=recipes index="local.k" item="local.v" {
			arrayAppend(titles,v.title);
		}
		arraySort(titles,"textnocase");
	
		var data=[];
		loop array=titles item="local.title" {
			var d=["id":recipes[title].id,"title":title,"keywords":lcase(arrayToList(recipes[title].keywords))];
			if(!isNull(recipes[title].since))d["since"]=recipes[title].since;
			arrayAppend(data,d);
		}
		return data;
	}
	
	cookbookDirectory="/Users/mic/Projects/Lucee/Lucee6/docs/recipes";
	if(develop || isNull(application.recipes[server.lucee.version])) {
		application.recipes[server.lucee.version]=readRecipes(cookbookDirectory);
	}
	recipes=application.recipes[server.lucee.version];

	if(develop || isNull(application.recipeStruct[server.lucee.version])) {
		application.recipeStruct[server.lucee.version]=getRecipeStruct(recipes);
	}
	recipeStruct=application.recipeStruct[server.lucee.version]




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
			'recipes':recipeStruct?:{},
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
		<div class="title">#ucFirst(type)# #data.name?:data.title#</div>
		<!--- deprecated? --->
		<cfif (data.status?:"") EQ "deprecated">
			<div class="warning nofocus">This #type# is deprecated</div>
		</cfif>
		
		<!--- description --->
		<span>
			<cfif not StructKeyExists(data, "description")>
				<em>No description found</em>
			<cfelse>
				#markdownToHTML(data.description)#
			</cfif>
		</span>
	</cfif>
	<style>
		/* Style for inline code */
		.recipy {
			 background-color: ##EEE;
			 color: ##333;
			 padding: 2px 4px;
			 border-radius: 3px;
			 font-family: 'Courier New', Courier, monospace;
	
			 border: solid 1px ##333; 
			 border-radius: 8px;
			 white-space: nowrap; /* Prevent line breaks within the code */
	
		 }
	
		 /* Style for block code */
		 pre ##recipy {
			 display: block;
			 background-color: ##333;
			 padding: 25px 25px 25px 25px;
			 border: solid 1px ##eee; 
			 border-radius: 1em;
			 color: ##3399cc;
			 margin: 1px;
			 white-space: pre; /* Preserve whitespace and formatting */
			 overflow-x: auto;
			 word-wrap: break-word;
			 max-width: 90%; /* Ensure it doesn't overflow the container */
			 font-weight: normal;
			 font-family: "Courier New", Courier, monospace, sans-serif;
			 font-size: 16px;
			 white-space: pre-wrap;
			 word-break: break-all;
			 word-wrap: break-word; 
			 tab-size: 2;
		 }
		 .resultml {
			 display: block;
			 background-color: ##EEE;
			 padding: 15px;
			 border: solid 1px ##333; 
			 border-radius: 1em;
			 color: ##4e7620;
			 margin: 1px;
			 white-space: pre; /* Preserve whitespace and formatting */
			 overflow-x: auto;
			 word-wrap: break-word;
			 max-width: 90%; /* Ensure it doesn't overflow the container */
			 font-weight: normal;
			 font-family: "Courier New", Courier, monospace, sans-serif;
			 font-size: 16px;
			 white-space: pre-wrap;
			 word-break: break-all;
			 word-wrap: break-word; 
			 tab-size: 2;
		 }
	
		 .lucee_execute_result {
			background-color: white;
			border: solid 1px ##333;
			border-radius: 1em;
			padding: 10px;
			margin-top: 30px; /* Increase margin-top to accommodate the overlapping text */
			max-width: 90%; /* Ensure it doesn't overflow the container */
			font-size: 16px;
			position: relative; /* Needed for the absolute positioning of the label */
		}
	
		.lucee_execute_result::before {
			content: "Generated Output from the example above";
			position: absolute;
			top: -10px; /* Adjust this value to position the text correctly */
			left: 30px; /* Indent the text 20 pixels from the left */
			background-color: white;
			padding: 0 5px; /* Add some padding to the label */
			font-size: 16px;
			color: ##333;
			font-weight: bold;
		}
	
		.language-lucee .nf {color: ##569cd6;}
		.language-lucee .nv {color: ##9cdcfe;}
		.language-lucee .syntaxFunc {color: ##dcdcaa;}
		.language-lucee .syntaxType {color: ##4ec9b0;}
		.language-lucee .p {color: ##d4d4d4;}
		.language-lucee .nt {color: ##569cd6;}
		.language-lucee .na {color: ##9cdcfe;}
		.language-lucee .s {color: ##ce9178;}
		.language-lucee .err {color: ##d4d4d4;}
		.language-lucee .syntaxAttr { color: ##dcdcaa;}
	
	
	 </style>
	<!----------------------------------------
	------------------- Recipes -------------
	------------------------------------------>
	<cfif type=="recipes">
	
	<cftry>
		<cfset md=data.content>
		<cfset md=executeCodeFragments(md)>
		<cfset code=markdownToHTML(md)>
		
		<cfset code=replace(code,"<code class=""language-","<code id=""recipy"" class=""language-","all")>
		<cfset code=replace(code,"<code>","<code class=""recipy"">","all")>
		<cfset code=replace(code,"<blockquote>","<blockquote class=""resultml"">","all")>
		<cfset code=replace(code,"<h1>","<div class=""title"">","all")>
		<cfset code=replace(code,"</h1>",(isEmpty(data.since?:"")?"":(" (Lucee #data.since#)"))&"</div>","all")>
		<cfset code=replace(code,"~","`","all")>
		
	
		
		
		
		#code#<br>www
		<cfcatch><cfdump var="#cfcatch#"></cfcatch>
		</cftry>
	<cfif develop>
	<h1>MD</h1>
		<pre>#replace(md,"<","&lt;","all")#</pre>
	<h1>HTML</h1>
		<pre>#replace(code,"<","&lt;","all")#</pre>
	</cfif>
	<!----------------------------------------
	------------------- FUNCTION -------------
	------------------------------------------>
	<cfelseif type=="function">
	
	<!--- Syntax TODO css missing--->
	<cfset first=true>
	<cfset optCount=0>
	<div class="section-title">Syntax</div>
	
	<pre class="pad"><code id="recipy" class="language-lucee"><span class="nf">#data.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#): </span><span class="syntaxType">#data.returntype#</span></code></pre>
	
	<!--- Syntax member TODO css missing--->
	<cfif !isNull(data.member)>
		<cfset first=true>
		<cfset optCount=0>
	<div class="section-title">Member Syntax</div>
	<pre class="pad"><code id="recipy" class="language-lucee"><span class="nf">#data.member.type#.#data.member.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="i" item="item"><cfif item.status EQ "hidden" or data.member.position EQ i><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#): </span><span class="syntaxType"><cfif data.member.chaining>#data.member.type#<cfelse>#data.returntype#</cfif></span></code></pre>
	
	
	</cfif>
	
	
	<!--- Argumente --->
	<div class="section-title">Arguments</div>
	<cfif data.argumentType EQ "fixed" and not arraylen(data.arguments)>
		<div class="text">This function has no arguments</div>
	<cfelse>
		<div class="text">
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
		</div>
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
	<div class="section-title">Body</div>
	<div class="pad">#body[ data.bodyType ]#</div>
	
	<!--- Syntax --->
	<cfset arrAttrNames= data.attributes.keyArray().sort( 'textnocase' )>
	<cfset tagName = data.namespace & data.namespaceseperator & data.name>
	<cfif data.hasNameAppendix><cfset tagName &= "CustomName"></cfif>
	
	
	<pre><code id="recipy" class="language-lucee"><!---
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
			<div class="text">This tag is also supported within cfscript</div>
			<!--- <cfabort showerror="Test"/> --->
			<pre><code id="recipy" class="language-lucee">
	<span class="nt">&lt;cfscript&gt;</span>
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
	<span class="nt">&lt;/cfscript></span>
	</code></pre>
		</cfif>
	
	<!--- Attributes --->
	<div class="section-title">Attributes</div>
	<cfif data.attributeType == "fixed" && !arrayLen( arrAttrNames )>
		<div class="pad">This tag has no attributes</div>
	<cfelse>
		<div class="pad">#attrtype[data.attributeType]#
			<cfif data.attributeType == "dynamic">
				<cfif data.attrMin GT 0 && data.attrMax GT 0>
					#replace( replace( attr.minMax, "{min}", data.attrMin ), "{max}", data.attrMax )#
				<cfelseif data.attrMin GT 0>
					#replace( attr.min, "{min}", data.attrMin )#
				<cfelseif data.attrMax GT 0>
					#replace( attr.max, "{max}", data.attrMax )#
				</cfif>
			</cfif>
		</div>
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
		<div class="section-title">Category</div>
		<div class="pad">#arraytolist(data.keywords,", ")#</div>
	</cfif>
	
	</cfoutput>
	
	
		<cfcatch>
			<cfset systemOutput(cfcatch,1,1)>
			<cfset echo(cfcatch)>
		</cfcatch>
	</cftry>