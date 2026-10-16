<cftry>
	<cfscript>
	setting show=false;
	develop=false;
	collectionName="lucee-documentation";

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
		var code=replace(html,"<code class=""language-","<code class=""lucee-ml"" lang=""language-","all");
		//code=replace(code,"<code>","<code class=""lucee"">","all");
		//code=replace(code,"<blockquote>","<blockquote class=""lucee"">","all");
		//code=replace(code,"<h1>","<h1 class=""lucee"">","all");
		code=replace(code,"</h1>",(isEmpty(data.since?:"")?"":(" (Lucee #data.since#)"))&"</h1>","all");
		//code=replace(code,"<h2>","<h2 class=""lucee"">","all");
		//code=replace(code,"<h3>","<h3 class=""lucee"">","all");
		//code=replace(code,"<h4>","<h4 class=""lucee"">","all");
		//code=replace(code,"<h5>","<h5 class=""lucee"">","all");
		//code=replace(code,"<h6>","<h6 class=""lucee"">","all");
		//code=replace(code,"<p>","<p class=""lucee"">","all");
			
		code=replace(code,"~","`","all");
		return '<span class="lucee-debug">#code#</span>' ;	
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
			code=mid(code,1,(endIndex-1)+endNeedleLength)&startBlockquote&result&endBlockquote&mid(code,endIndex+endNeedleLength);
			last=endIndex+endNeedleLength+len(result)+len(startBlockquote)+len(endBlockquote);
		}
		return code;
	}
	
	function executeCode(code) {
		var id=createUniqueID();
		var ramdir="ram://templatesmonitor"&id;
		var mappingName="/monitoringexecute"&id;
		var currSettings=getApplicationSettings();
		try {
			if(!directoryExists(ramdir)) directoryCreate(ramdir);
			currSettings.mappings[mappingName]=ramdir;
			application action="update" mappings=currSettings.mappings;
			
			fileWrite(ramdir&"/index#id#.cfm", arguments.code);
			savecontent variable="local.result" {
				include mappingName&"/index#id#.cfm";
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

	function get(url,timeout, defaultValue) {
		http url=arguments.url timeout=arguments.timeout result="local.res";
		
		if(res.status_code>=200 && res.status_code<300) return res.filecontent;
		return arguments.defaultValue;
	}

	function recipeHash() {

	}


	function importRecipes(loadContent=false,giveHash=false) {
		var tmp=listToArray(server.lucee.version,".");
		var branch=tmp[1]&"."&tmp[2];
		
	////////// read local index ////////
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
		var localIndex=localDirectory&"index.json";
		var first=!fileExists(localIndex);

		// load local index
		if(!first) {
			var localIndexContent=trim(fileRead(localIndex));
			var localIndexData=deserializeJSON(localIndexContent);
			var localIndexHash=hash(localIndexContent);
		}
		else {
			var localIndexContent="";
			var localIndexData=[];
			var localIndexHash="";
		}
		

	////////// read remote index ////////
		var rootPath=(server.system.environment.LUCEE_DOC_RECIPES_PATH?:"https://raw.githubusercontent.com/lucee/lucee-docs/master");
		var remoteIndexPath=rootPath&"/docs/recipes/index.json";
		var remoteIndexContent=trim(get(remoteIndexPath,createTimeSpan(0,0,0,10),""));
		var offline=false;
		if(remoteIndexContent=="") {
			remoteIndexContent=localIndexContent;
			remoteIndexData=localIndexData;
			var remoteIndexHash="";
			offline=true;
		}
		else {
			remoteIndexData=deserializeJSON(remoteIndexContent);
			var remoteIndexHash=hash(remoteIndexContent);
		}

		var indexHash=localIndexHash;
		// in case the local data differs from remote or we do not have local data at all
		if(!offline && (first || localIndexHash!=remoteIndexHash)) {
			setting requesttimeout="120";
			loop array=remoteIndexData item="local.entry" label="outer" {
				entry.url=rootPath&entry.path;
				entry.local=localDirectory&listLast(entry.file,"\/");
				if(!first) {
					loop array=localIndexData item="local.e" {
						if(e.file==entry.file) {
							if( (e.hash?:"b")==(entry.hash?:"a")) {
								if(fileExists(entry.local)) {
									entry.content=readRecipe(localDirectory&listLast(entry.file,"\/"));
									continue outer;
								}
							}
							else {
								if(fileExists(entry.local)) {
									fileDelete(entry.local);
								}
							}
						}
					}
				}
			}
			try { 
				if(hasLocalDir) {
					indexHash=remoteIndexHash;
					fileWrite(localIndex, remoteIndexContent);
				}
			}
			catch(ex2) {
				log log="application" exception=ex2;
			}
			var indexData=remoteIndexData;
		}
		// we just get the local data
		else {
			loop array=localIndexData item="local.entry" {
				entry.url=rootPath&entry.path;
                entry.local=localDirectory&listLast(entry.file,"\/");
				if(fileExists(entry.local)) {
					// read existing content from local
					entry.content=readRecipe(entry.local);
				}
			}
			var indexData=localIndexData;
		}

        // SORT
        arraySort(indexData,function(l,r) {
            return compareNoCase(l.title, r.title);
        });
		if(loadContent) {
			loop array=indexData item="local.record" {
				getContent(record);
			}
		}
		return giveHash?indexHash:indexData;
	}

    function resetRecipes() {
		application.recipeArray[server.lucee.version]=nullValue();
		application.recipes[server.lucee.version]=nullValue();
    }
    function readRecipe(localFile, boolean fromContent=false) {
		var content=fromContent?localFile:fileRead(localFile);
        //var hash=hash(content,"md5");
		var endIndex=find("-->", content,4);
		if(endIndex==0) return content;
		
        //var rawMeta=trim(mid(content,startIndex+5,endIndex-(startIndex+5)));
		//var json=deserializeJSON(rawMeta);
		return trim(mid(content,endIndex+3));
    }

	function getContent(data) {
		var cannotReach="Sorry, this recipe is not avialble at the moment";
		if(isNull(data.content) || isEmpty(data.content) || data.content==cannotReach) {
			var content=get(data.url,createTimeSpan(0,0,0,5), "");
			if(!isEmpty(content)) {
				fileWrite(data.local,content);
				data.content=readRecipe(content,true);
			}
			else {
				data.content=cannotReach;
			}
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

	function recipesAsQuery(index, keywordsFlat=true) {
        loop array=index item="local.entry" {
            if(isNull(asQry)) {
				var columns=entry.keyArray();
				var asQry=QueryNew(columns);
			}
			var row=queryAddRow(asQry);
			loop array=columns item="local.col" {
				var val=entry[col];
				if(keywordsFlat && col=="keywords" && isArray(val)) val=val.toList();
				querySetCell(asQry, col, val);
			}
        }
        return asQry;
	}


	function searchSupported() {
		var id="EFDEB172-F52E-4D84-9CD1A1F561B3DFC8";
		if(!extensionexists(id)) return false;
		
		var info=extensionInfo(id);
		var major=listFirst(info.version,".");
		return major>=3;
	}

	function getFunctionDataAll() {
		var qry=queryNew(["title","body","url"]);
		loop array=structKeyArray(getFunctionList()) item="local.name" {
			var row=queryAddRow(qry);
			var data=getFunctionData(name);
			
	
	// put the string together
			var str=trim("
	## Function #name#
	
	Json function library descriptor for the function #name#
	
	documentation: https://docs.lucee.org/reference/functions/#name#.html
	
	```json
	#serializeJson(var:data,compact:false)#
	```
	");
			querySetCell(qry,"title",name,row);    
			querySetCell(qry,"body",str,row);              
			querySetCell(qry,"url","https://docs.lucee.org/reference/functions/#name#.html",row);            
			// echo(markdownToHTML(str));
			// echo("<hr>");
		}
		return qry;
	}
	
	function getTagDataAll() {
		var qry=queryNew(["title","body","url"]);
		loop struct=getTagList() index="local.prefix" item="local.tags" {
			loop array=StructKeyArray(tags) item="local.name" {
				var data=getTagData(prefix,name);
				var row=queryAddRow(qry);
				var str=trim("
	## Tag #prefix##name#
	
	Json tag library descriptor for the tag #prefix##name#
	
	documentation: https://docs.lucee.org/reference/tags/#name#.html
	
	```json
	#serializeJson(var:data,compact:false)#
	```
	");         querySetCell(qry,"title","#prefix##name#",row);    
				querySetCell(qry,"body",str,row);              
				querySetCell(qry,"url","https://docs.lucee.org/reference/tags/#name#.html",row);            
				//echo(markdownToHTML(str));
				
			}
		} 
		return qry;
		//
	}
	


	function createCollection() {
		if(!searchSupported()) return;
		
		// create if needed
		collection action= "list" name="local.collections";
		var hasColl=false;
		loop query=collections {
			if(collections.name==collectionName) {
				hasColl=true;
				break;
			}
		}
		if(!hasColl) {
			// collection directory
			try {
				var collDirectory=expandPath("{lucee-config-dir}/doc/search");
				if(!directoryExists(collDirectory)) {
					directoryCreate(collDirectory,true);
				}
			}
			catch(ex) {
				dump(ex);
				var collDirectory=expandPath("{temp-directory}");
			}
			// create collection TF-IDF
			collection 
				action= "Create" 
				collection=collectionName 
				path=collDirectory 
				mode="hybrid"
				embedding="TF-IDF"
				ratio="0.5";
			
		}	
	}

	function createIndex() {
		if(!searchSupported()) return;
		createCollection();
		
		// hash recipe
		var hashRecipe=server.system.properties["lucee.recipe.hash"]?:"";
		if(isEmpty(hashRecipe)) {
			hashRecipe=importRecipes(true,true);
			System::setProperty("lucee.recipe.hash",hashRecipe);
		}

		// hash tags
		var hashTags=server.system.properties["lucee.tag.hash"]?:"";
		if(isEmpty(hashTags)) {
			var qryTags=getTagDataAll();
			hashTags=hash(qryTags.toString(),"quick");
			System::setProperty("lucee.tag.hash",hashTags);
		}

		// hash functions
		var hashFunctions=server.system.properties["lucee.functions.hash"]?:"";
		if(isEmpty(hashFunctions)) {
			var qryFunctions=getFunctionDataAll();
			hashFunctions=hash(qryFunctions.toString(),"quick");
			System::setProperty("lucee.functions.hash",hashFunctions);
		}

		
		index action="list"
			name="local.indexes"
			collection=collectionName;
		
		// do we have a it?
		var updateRecipe=true;
		var updateTags=true;
		var updateFunctions=true;
		
		loop query=indexes {
			if(indexes.custom4=="hash:"&hashRecipe) {
				updateRecipe=false;
			}
			else if(indexes.custom4=="hash:"&hashTags) {
				updateTags=false;
			}
			else if(indexes.custom4=="hash:"&hashFunctions) {
				updateFunctions=false;
			}
		}
		// index collection
		if(updateRecipe) {
			var qryRecipes=recipesAsQuery(importRecipes(true));
			// index recipes
			index action="update" 
				type="custom"
				collection=collectionName 
				key="url"
				title="title"
				body="CONTENT,keywords"
				custom1="keywords"
				custom2="url"
				custom3="local"
				custom4="hash:"&hashRecipe
				query="qryRecipes";
		}
		if(updateTags) {
			if(isNull(local.qryTags)) local.qryTags=getTagDataAll();
			index action="update" 
				type="custom"
				collection=collectionName 
				key="url"
				title="title"
				body="body"
				query="qryTags"
				custom4="hash:"&hashTags;
		}
		if(updateFunctions) {
			if(isNull(local.qryFunctions)) local.qryFunctions=getFunctionDataAll();
			index action="update" 
				type="custom"
				collection=collectionName 
				key="url"
				title="title"
				body="body"
				query="qryFunctions"
				custom4="hash:"&hashFunctions;
		}
	}

	function augmentSearchCriteria( criteria) {
		if(!searchSupported()) return criteria;	
		createIndex();

		arguments.criteria=rereplace(criteria, "([+\-&|!(){}\[\]\^""~*?:\\\/])", "\\1", "ALL");
		
		search 
			contextpassages=3
			contextHighlightBegin="<match>" contextHighlightEnd="</match>"
			contextBytes=3000
			contextpassageLength=1000
			name="local.searchResults"
			collection=collectionName 
			criteria=criteria
			suggestions="always"
			maxrows=3;

		var augmentedQuery="User Query: #criteria#";
		var searchResultsAsArray=[];
		var total=0;
		var maxSize4Full=100;
		loop query=searchResults  {
			var cntxt=searchResults.context;
			var pass=cntxt.passages;
			var src=searchResults.custom2;
			src=replace(src,"https://raw.githubusercontent.com/lucee/lucee-docs/master/","https://github.com/lucee/lucee-docs/blob/master/");
			var arrSrc=[];
			loop query=pass {
				arrayAppend(arrSrc, [
					"start":pass.start,
					"end":pass.end,
					"score":pass.score,
					"data": pass.original
				]);
			}

			
			arrayAppend(searchResultsAsArray, [
				"title":searchResults.title,
				"summary":searchResults.summary,
				"keywords":searchResults.custom1,
				"source":src,
				"score":searchResults.score,
				"rank":searchResults.rank,
				"content":arrSrc
			]);
		}
		if(len(searchResultsAsArray)) {
			augmentedQuery&="
Documentation Context: #serializeJSON(searchResultsAsArray)#";
		}
		return augmentedQuery;
	}


	if(develop || isNull(application.recipeArray[server.lucee.version]) || len(application.recipeArray[server.lucee.version])==0) {
		application.recipeArray[server.lucee.version]=importRecipes();
	}
	recipeArray=application.recipeArray[server.lucee.version]

	
	if(develop || isNull(application.recipes[server.lucee.version]) || len(application.recipes[server.lucee.version])==0) {
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
		<style>
			span.lucee-debug  {
				font-size: 16px !important;
			}
			span.lucee-debug h1, span.lucee-debug h2, span.lucee-debug h3, span.lucee-debug h4, span.lucee-debug h5, span.lucee-debug h6 {
				color: ##4e7620 !important;
			}
			span.lucee-debug h1 {
				font-size: 32px !important;
				margin-top: 20px !important; 
			}
			span.lucee-debug h2 {font-size: 28px !important;}
			span.lucee-debug h3 {font-size: 22px !important;}
			span.lucee-debug h4 {font-size: 18px !important;}
			span.lucee-debug h5 {font-size: 16px !important;}
			span.lucee-debug h6 {font-size: 16px !important;}
			span.lucee-debug p {
				font-size: 18px !important;
				align:center;
				color: ##333 !important;
			}
			
			/* Style for inline code */
			span.lucee-debug code {
				background-color: ##000 !important;
				color: ##fff !important;
				padding: 2px 4px !important;
				margin: 2px 4px !important;
				font-family: 'Courier New', Courier, monospace !important;
	
				border: solid 0px ##000 !important; 
				border-radius: 3px !important;
				white-space: nowrap !important; /* Prevent line breaks within the code */
				font-weight: bold !important;
	
			}
			
			/* Style for multi line code */
			span.lucee-debug code.lucee-ml {
				display: block !important;
				background-color: ##333 !important;
				padding: 10px 25px 10px 25px !important;
				border: solid 1px ##eee !important; 
				border-radius: 1em !important;
				color: ##ccffff !important;
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

			span.lucee-debug blockquote {
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
			span.lucee-debug code.lucee-debug .keyword {color: ##dcdcaa; !important}
			span.lucee-debug code.lucee-ml .variable {color: ##4ec9b0; !important}
			span.lucee-debug code.lucee-ml .p {color: ##d4d4d4; !important}
			span.lucee-debug code.lucee-ml .literal {color: ##ce9178; !important}

			.lucee-debug .nf {color: ##569cd6; !important}
			.lucee-debug .nv {color: ##9cdcfe; !important}
			.lucee-debug .syntaxFunc {color: ##dcdcaa; !important}
			.lucee-debug .syntaxType {color: ##4ec9b0; !important}
			.lucee-debug .p {color: ##d4d4d4; !important}
			.lucee-debug .nt {color: ##569cd6; !important}
			.lucee-debug .na {color: ##9cdcfe; !important}
			.lucee-debug .s {color: ##ce9178; !important}
			.lucee-debug .err {color: ##d4d4d4; !important}
			.lucee-debug .syntaxAttr { color: ##dcdcaa; !important}
				
			
			 </style>
	<cfif isNull(type)>
		
			<cfscript>
				NL="
";
				
				if(!isEmpty(form.id?:"")) {
					if(!structKeyExists(session, "documentationAISession") || (session.documentationAISession.id?:"") != form.id) {
						ais=createAISession("id:"&form.id, 
						"You are a Lucee expert and documentation guide. "
						&"Users will ask questions about Lucee functions, tags, or configurations running Lucee version #server.lucee.version#. "
						&"Some queries will include relevant documentation context while others may not have matching documentation. "
						&"When documentation context is provided, it will be an array of documents, each containing: "&NL
						&"- A 'title' field containing the document title "&NL
						&"- A 'summary' field with a brief overview "&NL
						&"- A 'keywords' field listing relevant terms "&NL
						&"- A 'content' field containing the documentation text in markdown format in pieces as an array, every element contains the folowing pieces"&NL
						&"   - start - start position in the original text"&NL
						&"   - end - end position in the original text"&NL
						&"   - score - score  score from Lucene for that piece"&NL
						&"   - data - the content piece itself"&NL
						&"- A 'source' field with the documentation URL "&NL
						&"- A 'score' field indicating the relevance to the query "&NL
						&"- A 'rank' field indicating the position in search results "&NL&NL
						&"Documents are ordered by relevance, with the most relevant first. "
						&"When documentation context is provided, base your response primarily on that information and include the source URLs for reference. "
						&"When no context is provided, respond based on your general knowledge of Lucee. "
						&"Respond concisely in plain HTML, without using triple backticks. "
						&"Biggest heading tag you can use is h2. "
						&"Start every answer with a h2 tag containg a title for your answer."
						&"Regular text in a p tag."
						&"For multi-line code examples, use <code class=""lucee-ml"">. "
						&"For inline code, use <code>. Avoid <code> within heading tags, and ensure all code is properly escaped with &lt;. "
						&"Structure responses clearly and briefly for direct HTML integration. "
						&"When responding: "&NL
						&"1. Use provided documentation context when available "&NL
						&"2. Include source URLs when referencing specific documentation "&NL
						&"3. Maintain consistent HTML formatting throughout your response "&NL
						&"4. if the request is nt clear, say so and do not assume to much "&NL
						&"5. whenever possible make a code example "&NL
						&"6. code examples as much as possible in cfscript code "&NL
						&"7. For queries without context, provide the best possible answer based on general Lucee expertise"
						,4
						,0.3
						);
						// 	Respond concisely in plain markdown format (no starting ```markdown) without mentioning the origin of the data. 
						try {
							meta=LuceeAIGetMetaData(ais);
							label='Generated by #meta.label?:""# (#meta.model?:""#)';
						}
						catch(e) {
							label="";
						}	
						session.documentationAISession={"ais":ais,"label":label,"id":form.id};
					}
					else {
						ais=session.documentationAISession.ais;
						label=session.documentationAISession.label;
					}
					//echo("<!-- start pre -->");
					echo("<span class=""lucee-debug"">");
					md=LuceeInquiryAISession(ais,augmentSearchCriteria(form.search),function(msg) {
						echo(msg);
						cfflush(throwonerror=false);
						
					});
					echo("</span><h4>#label#</h4>");

					if(develop) {
						dump(md);
					}
					//echo("<!-- end pre -->");
					//md=LuceeInquiryAISession(ais,form.search);
					//md=executeCodeFragments(md);
					//code=enhanceHTML(markdownToHTML(md));
					
					
					//echo(code);
					

					
				}
				else {
					echo('<span class="lucee-debug"><p>
    To use AI to provide information about [#htmleditFormat(reReplace(form.search, '[^a-zA-Z0-9]', ' ', 'all'))#], 
    you need to create an AI endpoint and set it as <code>default="documentation"</code>. 
    For more details, see: <a href="https://github.com/lucee/lucee-docs/blob/master/docs/recipes/ai.md" target="_blank">AI Documentation Guide</a>.
					</p></span>
');
			
				}
			</cfscript>
		<cfabort>
	</cfif>
	<cfif "recipes" NEQ type>
		<span class="lucee-debug">
			<!--- title --->
			<h1>#ucFirst(type)# #data.name?:data.title#</h1>
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
		</span>
	</cfif>


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
	<span class="lucee-debug">
	<h3>Syntax</h3>
	
	<code class="lucee-ml"><span class="nf">#data.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#): </span><span class="syntaxType">#data.returntype#</span></code>
	
	<!--- Syntax member TODO css missing--->
	<cfif !isNull(data.member)>
		<cfset first=true>
		<cfset optCount=0>
		<h3>Member Syntax</h3>
		<code class="lucee-ml"><span class="nf">#data.member.type#.#data.member.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="i" item="item"><cfif item.status EQ "hidden" or data.member.position EQ i><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#): </span><span class="syntaxType"><cfif data.member.chaining>#data.member.type#<cfelse>#data.returntype#</cfif></span></code>
	</cfif>
	
	
	<!--- Argumente --->
	<h3>Arguments</h3>
	<cfif data.argumentType EQ "fixed" and not arraylen(data.arguments)>
		<p>This function has no arguments</p>
	<cfelse>
		<p>
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
	</span>
	
	<!----------------------------------------
	------------------- TAG -------------
	------------------------------------------>
	<cfelse>
		<span class="lucee-debug">
	<!--- Body --->
	<h3>Body</h3>
	<p>#body[ data.bodyType ]#</p>
	
	<!--- Syntax --->
	<cfset arrAttrNames= data.attributes.keyArray().sort( 'textnocase' )>
	<cfset tagName = data.namespace & data.namespaceseperator & data.name>
	<cfif data.hasNameAppendix><cfset tagName &= "CustomName"></cfif>
	
	<h3>Tag Syntax</h3>
	<code class="lucee-ml"><!---
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
		---></code>
	
	<!--- SCRIPT --->
		<cfif data.keyExists( "script" ) && data.script.type != "none">
			<cfset arrAttrNames = data.attributes.keyArray().sort( 'textnocase' )>
			<h3>Script Syntax</h3>
			<p>This tag is also supported within cfscript</p>
			<code class="lucee-ml"><!---
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
	---></code>
		</cfif>
	
	<!--- Attributes --->
	<h3>Attributes</h3>
	<cfif data.attributeType == "fixed" && !arrayLen( arrAttrNames )>
		<p>This tag has no attributes</p>
	<cfelse>
		<p>#attrtype[data.attributeType]#
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
</span>
	</cfif>
	
	
	<!--- Category --->
	<cfif structKeyExists(data, "keywords") AND !arrayIsEmpty(data.keywords)>
		<span class="lucee-debug">
		<h3>Category</h3>
		<p>#arraytolist(data.keywords,", ")#</p>
		</span>
	</cfif>
	
	</cfoutput>
	
	
		<cfcatch>
			<p style="color:red">An error occurred; see application log for more details</p>
			<cflog log="application" exception="#cfcatch#">
			<cfdump var="#cfcatch#" enabled="#develop?:false#">
		</cfcatch>
	</cftry>