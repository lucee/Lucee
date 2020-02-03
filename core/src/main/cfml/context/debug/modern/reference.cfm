<cftry>
<cfscript>


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
	echo(lcase(serializeJson({
		'function':ffunctions,
		'tag':ftags})));
	abort;
}

if(structKeyExists(functions,form.search)) {
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
	<div class="section-title">no matching tag,function or component found for #form.search#</div>
	<cfabort>
</cfif>

<!--- title --->
<div class="title">#ucFirst(type)# #data.name#</div>
<!--- deprecated? --->
<cfif data.status EQ "deprecated">
	<div class="warning nofocus">This #type# is deprecated</div>
</cfif>

<!--- description --->
<span>
	<cfif not StructKeyExists(data, "description")>
		<em>No description found</em>
	<cfelse>
		#replace( replace( data.description, '	', '&nbsp;&nbsp;&nbsp;', 'all' ), chr(10), '<br>', 'all' )#
	</cfif>
</span>


<!--- Category --->
<cfif structKeyExists(data, "keywords") AND !arrayIsEmpty(data.keywords)>
	<div class="section-title">Category</div>
	<div class="pad">#arraytolist(data.keywords)#</div>
</cfif>

<!----------------------------------------
------------------- FUNCTION -------------
------------------------------------------>
<cfif type=="function">

<!--- Syntax TODO css missing--->
<cfset first=true>
<cfset optCount=0>
<div class="section-title">Syntax</div>
<pre class="pad"><span class="nf">#data.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType">#data.returntype#</span></pre>

<!--- Syntax member TODO css missing--->
<cfif !isNull(data.member)>
	<cfset first=true>
	<cfset optCount=0>
<div class="section-title">Member Syntax</div>
<pre class="pad"><span class="nf">#data.member.type#.#data.member.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="i" item="item"><cfif item.status EQ "hidden" or data.member.position EQ i><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType"><cfif data.member.chaining>#data.member.type#<cfelse>#data.returntype#</cfif></span></pre>


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
					#attr.description#
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


<pre><!---
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
				--->&nbsp;<cfif !attr.required><span class="err">[</span></cfif><!---
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
	---></pre>

<!--- SCRIPT --->
	<cfif data.keyExists( "script" ) && data.script.type != "none">
		<cfset arrAttrNames = data.attributes.keyArray().sort( 'textnocase' )>
		<div class="text">This tag is also supported within cfscript</div>
		<!--- <cfabort showerror="Test"/> --->
<pre>
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
		---><cfif !attr.required><span class="err">[</span></cfif><!---
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
</pre>
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
					<td><cfif attr.status EQ "deprecated"><b class="error">#stText.doc.depAttr#</b><cfelse>#attr.description#</cfif>&nbsp;</td>
				</tr>
			</cfloop>
		</tbody>
	</table>
</cfif>






</cfif>


</cfoutput>


	<cfcatch>
		<cfset systemOutput(cfcatch,1,1)>
		<cfset echo(cfcatch)>
	</cfcatch>
</cftry>