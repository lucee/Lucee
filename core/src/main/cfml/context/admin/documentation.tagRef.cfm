<cfparam name="url.tag" default="">
<cfset tagList=getTagList()>

<cfscript>
	NL="
";

stText.doc.attr.values="the following values are supported:
<values>";
stText.doc.attr.default="Default Value";

	function formatDesc(required attr ){
		var arr=listToArray(trim(attr.description),NL);
		var desc="";
		var in=false;
		loop array="#arr#" index="local.i" item="local.line" {
			if(i>1) desc&="<br>";
			if(left(trim(line),1)=='-') {
				line=(in?'':'<ul style="margin-top:4px;margin-left:0px;margin-bottom:0px;padding-left:16px;">')&replace(line,'-','<li>');
				in=true;
			}
			desc&=line;
		}
		if(in)desc&="</ul>";
		
		// has value defintion
		var doit=false;
		if(!isNull(attr.values)) {
			// only add this, if this is not already in the desc
			loop array="#attr.values#" item="local.v" {
				if(!findnocase(v,desc)) {
					doit=true;
				}
			}
			if(doit){
				if(trim(desc)>0)desc&="<br><br>";
				desc&=replace(stText.doc.attr.values,"<values>","<br>"&attrList(attr.values,', '));
			}
		}
		
		return desc;
	}
	
	function attrList(required array arr, delimiter='|') localMode="true"{
		arraySort(arr,"textnocase")
		return arrayToList(arr,delimiter);
		
	}
	
	function attrValue(required attr){
		if(isNull(attr.values) or !isArray(attr.values))
			return attr.type;
		return attrList(attr.values);
	}
	
	
</cfscript>

<cfoutput>
	<script type="text/javascript">
		function detail(field){
			var value=field.options[field.selectedIndex].value;
			var path="#request.self#?action=#url.action#&tag="+value;
			window.location=(path);
		}
	</script>

	<form action="#request.self#">
		<input type="hidden" name="action" value="#url.action#" />
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.doc.choosetag#</th>
					<td>
						<select name="tag" onchange="detail(this)" class="large">
							<option value=""> -------------- </option>
							<cfloop collection="#tagList#" item="ns">
								<cfset arr=StructKeyArray(tagList[ns])>
								<cfset ArraySort(arr,'textnocase')>
								<cfloop array="#arr#" index="key">
									<option value="#ns#,#key#" <cfif url.tag EQ ns&","&key>selected="selected"</cfif>>#ns##key#</option>
								</cfloop>
							</cfloop>
						</select>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input class="bs button submit" type="submit" name="mainAction" value="#stText.Buttons.OK#"> 
					</td>
				</tr>
			</tfoot>
		</table>
	</form>

	<cfif len(url.tag)>
		<cfset data=getTagData(listFirst(url.tag),listLast(url.tag))>
		<h2>Documentation for tag <em>&lt;#uCase(replace(url.tag, ',', ''))#&gt;</em></h2>
		<cfif data.status EQ "deprecated">
			<div class="warning nofocus">#stText.doc.depTag#</div>
		</cfif>
		<!--- Desc --->
		<div class="text">
			<cfif not StructKeyExists(data, "description") or data.description eq "">
				<em>No decription found</em>
			<cfelse>
				#data.description#
			</cfif>
		</div>
		<!--- Body --->
		<h3>#stText.doc.bodyTitle#</h3>
		<div class="text">#stText.doc.body[data.bodyType]#</div>
		
		<cfif not StructKeyExists(data, "attributes")>
			<cfset data.attributes = {} />
		</cfif>
		<cfif not StructKeyExists(data, "attributetype")>
			<cfset data.attributetype = "fixed" />
		</cfif>
		<h2>#stText.doc.example#</h2>
		<cfset tagName=data.namespace&data.namespaceseperator&data.name>
		<cfif data.hasNameAppendix><cfset tagName&="CustomName"></cfif>
		<cfset arrAttrNames=StructKeyArray(data.attributes)>
		<cfset ArraySort(arrAttrNames,'textnocase')>
<!--- color coded example tag --->
<pre><span class="syntaxTag">&lt;#tagName#</span><cfif data.attributeType EQ "noname"> <span class="syntaxAttr">##<cfloop array="#arrAttrNames#" index="key">#data.attributes[key].type# <cfbreak></cfloop>expression##</span> <cfelse><!--- 
---><cfloop array="#arrAttrNames#" index="key"><cfset attr=data.attributes[key]><cfif attr.status EQ "hidden"><cfcontinue></cfif>
	<cfif not attr.required><span class="syntaxAttr">[</span></cfif><!---
	---><span class="syntaxAttr">#key#</span>=<span class="syntaxText">"<cfif not attr.required><i></cfif>#attrValue(attr)#<cfif not attr.required></i></cfif>"</span><!---
	---><cfif not attr.required><span class="syntaxAttr">]</span></cfif></cfloop></cfif><!---

---><cfif data.attributeType EQ "dynamic" or data.attributeType EQ "mixed"> <span class="syntaxAttr">...</span> </cfif><cfif data.bodyType EQ "prohibited"><span class="syntaxTag">&gt;</span>
<cfelseif data.bodyType EQ "free"><span class="syntaxTag">&gt;

[&lt;/#tagName#&gt;]</span>
<cfelseif data.bodyType EQ "required"><span class="syntaxTag">&gt;

&lt;/#tagName#&gt;</span></cfif></pre>


		<cfif structKeyExists(data,"script") and data.script.type NEQ "none">
			<cfset arrAttrNames=StructKeyArray(data.attributes)>
			<cfset ArraySort(arrAttrNames,'textnocase')>
			<div class="text">#stText.doc.alsoScript#</div>
			<pre><span class="syntaxTag">&lt;cfscript></span>
	<span class="syntaxAttr">#data.name#</span><!---
No Name
 ---><cfif data.attributeType EQ "noname"> <span class="syntaxAttr">##<cfloop array="#arrAttrNames#" index="key">#data.attributes[key].type# <cfbreak></cfloop>expression##</span><!---
 
Single type 
 ---><cfelseif data.script.type EQ "single"><span class="syntaxAttr"><cfloop array="#arrAttrNames#" index="key"><cfset ss=data.attributes[key].scriptSupport><cfif ss NEQ "none"> <!--- 
 ---><cfif ss EQ "optional">[</cfif>#data.attributes[key].type#<cfif data.script.rtexpr> expression</cfif><cfif ss EQ "optional">]</cfif><cfbreak></cfif></cfloop></span><!--- 
 
 
multiple
---><cfelse><cfloop array="#arrAttrNames#" index="key"><cfset attr=data.attributes[key]><cfif attr.status EQ "hidden"><cfcontinue></cfif>
	<cfif not attr.required><span class="syntaxAttr">[</span></cfif><!---
	---><span class="syntaxAttr">#key#</span>=<span class="syntaxText">"<cfif not attr.required><i></cfif>#attrValue(attr)#<cfif not attr.required></i></cfif>"</span><!---
	---><cfif not attr.required><span class="syntaxAttr">]</span></cfif></cfloop></cfif><!---

---><cfif data.attributeType EQ "dynamic" or data.attributeType EQ "mixed"> <span class="syntaxAttr">...</span> </cfif><cfif data.bodyType EQ "prohibited"><span class="syntaxAttr">;</span><cfelseif data.bodyType EQ "required" or data.bodyType EQ "free"><span class="syntaxAttr"> {

}</span></cfif>
<span class="syntaxTag">&lt;/cfscript></span></pre>
		</cfif>

		<!--- Attributes --->
		<h2>#stText.doc.attrTitle#</h2>
		<cfif data.attributeType EQ "fixed" and not arrayLen(arrAttrNames)>
			<div class="text">#stText.doc.attr.zero#</div>
		<cfelse>
			<div class="text">#stText.doc.attr.type[data.attributeType]#
				<cfif data.attributeType EQ "dynamic">
					<cfif data.attrMin GT 0 and data.attrMax GT 0>
						#replace(replace(stText.doc.attr.minMax,"{min}",data.attrMin),"{max}",data.attrMax)#
					<cfelseif data.attrMin GT 0>
						#replace(stText.doc.attr.min,"{min}",data.attrMin)#
					<cfelseif data.attrMax GT 0>
						#replace(stText.doc.attr.max,"{max}",data.attrMax)#
					</cfif>
				</cfif>
			</div>
		</cfif>
		<cfif (data.attributeType EQ "fixed" or data.attributeType EQ "mixed") and arrayLen(arrAttrNames)>
			<cfset hasdefaults=false>
			<cfloop array="#arrAttrNames#" index="key">
				<cfset attr=data.attributes[key]>
				<cfif !isNull(attr.defaultValue)><cfset hasdefaults=true></cfif>
			</cfloop>
			<table class="maintbl">
				<thead>
					<tr>
						<th width="20%">#stText.doc.attr.name#</th>
						<th width="7%">#stText.doc.attr._type#</th>
						<th width="7%">#stText.doc.attr.required#</th>
						<cfif hasdefaults><th width="7%">#stText.doc.attr.default#</th></cfif>
						<th width="65%">#stText.doc.attr.description#</th>
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
							<cfif hasdefaults><td><cfif isNull(attr.defaultValue)>&nbsp;<cfelse>#attr.defaultValue#</cfif></td></cfif>
							<td><cfif attr.status EQ "deprecated"><b class="error">#stText.doc.depAttr#</b><cfelse>#formatDesc(attr)#</cfif></td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfif>
	</cfif>
</cfoutput>