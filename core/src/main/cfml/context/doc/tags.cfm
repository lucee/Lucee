<cfinclude template="/lucee/admin/resources/text.cfm">

<cfparam name="URL.namespace" default="cf">


<cfset arrNamespaces = Application.objects.utils.getTagNamespaces()>
<cfset arrAllItems   = Application.objects.utils.getAllTags()>

<cfif len( url.item )>

	<cfif !arrAllItems.findNoCase( url.item )>

		<cfset url.item = "">
	</cfif>

	<cfloop array="#arrNamespaces#" item="ns">

		<cfif left( url.item, len( ns ) ) == ns>

			<cfset url.item = mid( url.item, len( ns ) + 1 )>
			<cfset url.namespace = ns>
		</cfif>
	</cfloop>
</cfif>


<cfsavecontent variable="Request.htmlBody">

	<script type="text/javascript">

		<cfoutput>

			var typeaheadData = #serializeJson( arrAllItems )#;
		</cfoutput>

		$( function() {

			$( '#search-item' ).typeahead( {

				source: typeaheadData
			});
		});
	</script>
</cfsavecontent>


<cfmodule template="doc_layout.cfm" title="Lucee Tag Reference">


<cfoutput>

	<form id="form-item-selector" action="#CGI.SCRIPT_NAME#">
		<div class="centered x-large">

			#stText.doc.choosetag#:
			<input type="text" name="item" id="search-item" autocomplete="off">

			<input type="submit" value="#stText.Buttons.OK#">
		</div>
		<cfif len( url.item )>

			<div class="centered" style="padding: 0.5em;"><a href="#CGI.SCRIPT_NAME#">see all tags</a></div>
		</cfif>
	</form>


	<cfif len( url.item )>

		<cfset data = getTagData( url.namespace, url.item )>
		<cfset tagName = data.namespace & data.namespaceseperator & data.name>

		<cfparam name="data.attributes" default="#{}#">
		<cfparam name="data.attributetype" default="fixed">

		<h2>Tag <em>&lt;#uCase( tagName )#&gt;</em></h2>

		<cfif data.status == "deprecated">
			<div class="warning nofocus">#stText.doc.depTag#</div>
		</cfif>

		<!--- Desc --->
		<div class="text">
			<cfif !data.keyExists( "description" ) || !len( data.description )>
				<em>No decription found</em>
			<cfelse>
				#data.description#
			</cfif>
		</div>

		<!--- Body --->
		<h2>#stText.doc.bodyTitle#</h2>
		<div class="text">#stText.doc.body[ data.bodyType ]#</div>

		<h2>#stText.doc.example#</h2>

		<cfset arrAttrNames= data.attributes.keyArray().sort( 'textnocase' )>

		<cfif data.hasNameAppendix><cfset tagName &= "CustomName"></cfif>

		<!--- TODO: color coded example tag --->
		<pre>	<span class="syntaxTag">&lt;#tagName#</span><cfif data.attributeType == "noname"> <span class="syntaxAttr">##<cfloop array="#arrAttrNames#" index="key">#data.attributes[key].type# <cfbreak></cfloop>expression##</span> <cfelse><!---
	---><cfloop array="#arrAttrNames#" index="key"><cfset attr = data.attributes[ key ]><cfif attr.status EQ "hidden"><cfcontinue></cfif>
		<cfif !attr.required><span class="syntaxAttr">[</span></cfif><!---
		---><span class="syntaxAttr">#key#</span>=<span class="syntaxText">"<cfif !attr.required><i></cfif>#attr.type#<cfif !attr.required></i></cfif>"</span><!---
		---><cfif !attr.required><span class="syntaxAttr">]</span></cfif></cfloop></cfif><!---

	---><cfif data.attributeType == "dynamic" || data.attributeType == "mixed"> <span class="syntaxAttr">...</span> </cfif><cfif data.bodyType == "prohibited"><span class="syntaxTag">&gt;</span>
	<cfelseif data.bodyType == "free"><span class="syntaxTag">&gt;

	[&lt;/#tagName#&gt;]</span>
	<cfelseif data.bodyType == "required"><span class="syntaxTag">&gt;

	&lt;/#tagName#&gt;</span></cfif></pre>

		<cfif data.keyExists( "script" ) && data.script.type != "none">

			<cfset arrAttrNames = data.attributes.keyArray().sort( 'textnocase' )>
			<div class="text">#stText.doc.alsoScript#</div>
			<pre><span class="syntaxTag">	&lt;cfscript></span>
		<span class="syntaxAttr">#data.name#</span><!---
	No Name ---><cfif data.attributeType == "noname"> <span class="syntaxAttr">##<cfloop array="#arrAttrNames#" index="key">#data.attributes[ key ].type# <cfbreak></cfloop>expression##</span><!---
	Single type ---><cfelseif data.script.type == "single"><span class="syntaxAttr"><cfloop array="#arrAttrNames#" index="key"><cfset ss = data.attributes[ key ].scriptSupport><cfif ss != "none"> <!---
	 ---><cfif ss == "optional">[</cfif>#data.attributes[ key ].type#<cfif data.script.rtexpr> expression</cfif><cfif ss == "optional">]</cfif><cfbreak></cfif></cfloop></span><!---
	multiple ---><cfelse><cfloop array="#arrAttrNames#" index="key"><cfset attr=data.attributes[key]><cfif attr.status == "hidden"><cfcontinue></cfif>
		<cfif !attr.required><span class="syntaxAttr">[</span></cfif><!---
		---><span class="syntaxAttr">#key#</span>=<span class="syntaxText">"<cfif !attr.required><i></cfif>#attr.type#<cfif !attr.required></i></cfif>"</span><!---
		---><cfif !attr.required><span class="syntaxAttr">]</span></cfif></cfloop></cfif><!---

	---><cfif data.attributeType == "dynamic" || data.attributeType == "mixed"> <span class="syntaxAttr">...</span> </cfif><cfif data.bodyType == "prohibited"><span class="syntaxAttr">;</span><cfelseif data.bodyType == "required" || data.bodyType == "free"><span class="syntaxAttr"> {

	}</span></cfif>
	<span class="syntaxTag">&lt;/cfscript></span></pre>
		</cfif>

		<!--- Attributes --->
		<h2>#stText.doc.attrTitle#</h2>
		<cfif data.attributeType == "fixed" && !arrayLen( arrAttrNames )>
			<div class="text">#stText.doc.attr.zero#</div>
		<cfelse>
			<div class="text">#stText.doc.attr.type[data.attributeType]#
				<cfif data.attributeType == "dynamic">
					<cfif data.attrMin GT 0 && data.attrMax GT 0>
						#replace( replace( stText.doc.attr.minMax, "{min}", data.attrMin ), "{max}", data.attrMax )#
					<cfelseif data.attrMin GT 0>
						#replace( stText.doc.attr.min, "{min}", data.attrMin )#
					<cfelseif data.attrMax GT 0>
						#replace( stText.doc.attr.max, "{max}", data.attrMax )#
					</cfif>
				</cfif>
			</div>
		</cfif>
		<cfif ( data.attributeType == "fixed" || data.attributeType == "mixed" ) && arrayLen( arrAttrNames )>
			<table class="maintbl">
				<thead>
					<tr>
						<th width="21%">#stText.doc.attr.name#</th>
						<th width="7%">#stText.doc.attr._type#</th>
						<th width="7%">#stText.doc.attr.required#</th>
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
							<td><cfif attr.status EQ "deprecated"><b class="error">#stText.doc.depAttr#</b><cfelse>#Application.objects.utils.formatAttrDesc( attr.description )#</cfif>&nbsp;</td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfif>
	<cfelse><!--- len( url.item) !--->

		<!--- render index !--->
		<br>

		<cfset lastPrefix = "">
		<cfloop array="#arrAllItems#" item="ai" index="ii">

			<cfif left( ai, 3 ) != lastPrefix>

				<div style="height: 0.65em;"></div>
				<cfset lastPrefix = left( ai, 3 )>
			</cfif>

			<a href="#CGI.SCRIPT_NAME#?item=#ai#" class="index-item">#ai#</a>
		</cfloop>

	</cfif><!--- len( url.item) !--->

</cfoutput>


</cfmodule><!--- doc_layout !--->