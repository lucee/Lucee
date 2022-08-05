<cfsetting showDebugOutput=false>
<cfinclude template="/lucee/admin/resources/text.cfm">
<cfset stText.doc.attr.default="Default Value">

<cfparam name="URL.namespace" default="cf">


<cfset arrNamespaces = Application.objects.utils.getTagNamespaces()>
<cfset arrAllItems   = Application.objects.utils.getAllTags()>
<cfset stText.doc.attr.type = {}>
<cfset stText.doc.attr.type.dynamic =  "There is no restriction for attributes for this tag.">
<cfset stText.doc.attr.type.fixed =  "The attributes for this tag are fixed. Except for the following attributes no other attributes are allowed.">
<cfset stText.doc.attr.type.mixed =  "This tag has a fixed definition of attributes (see below). In addition it allowes to use any additional attribute">
<cfset stText.doc.attr.type.noname =  "This tag only allows one attribute value (no name).">
<cfif len( url.item )>
	<cfset itemPos = arrAllItems.findNoCase( url.item )>
	<cfif !itemPos>
		<cfset url.item = "">
	</cfif>

	<cfloop array="#arrNamespaces#" item="ns">

		<cfif left( url.item, len( ns ) ) == ns>

			<cfset url.item = mid( url.item, len( ns ) + 1 )>
			<cfset url.namespace = ns>
		</cfif>
	</cfloop>
	<cfset prevLinkItem = itemPos GT 1 ? arrAllItems[itemPos-1] : "">
	<cfset nextLinkItem = itemPos NEQ arrAllItems.len() ? arrAllItems[itemPos+1] : "">
<cfelse>
	<cfset prevLinkItem = "">
	<cfset nextLinkItem = "">
</cfif>

<cfsavecontent variable="Request.htmlBody">
	<style type="text/css">
		.tt-suggestion.tt-selectable p{
			margin: 0px !important;
		}
		.tt-suggestion.tt-selectable{
			cursor: pointer;
		}
		.tt-suggestion.tt-selectable:hover{
			background-color: #01798A;
			color: #FFFFFF;
		}
	</style>
	<script src="assets/js/jquery-1.9.min.js.cfm" type="text/javascript"></script>
	<script src="assets/js/typeahead.min.js.cfm"></script>

	<script type="text/javascript">

		<cfoutput>
			var typeaheadData = #serializeJson( arrAllItems )#;
		</cfoutput>

		var substringMatcher = function(strs) {
			return function findMatches(q, cb) {
				var matches, substringRegex;

				// an array that will be populated with substring matches
				matches = [];

				// regex used to determine if a string contains the substring `q`
				substrRegex = new RegExp(q, 'i');

				// iterate through the pool of strings and for any string that
				// contains the substring `q`, add it to the `matches` array
				$.each(strs, function(i, str) {
					if (substrRegex.test(str)) {
						matches.push(str);
					}
				});

				cb(matches);
			};
		};

		$( function() {
			$( '#lucee-docs-search-input' ).typeahead(
				{
					hint: true,
					highlight: true,
					minLength: 1
				},
				{ source: substringMatcher(typeaheadData) }
			).on('typeahead:selected', typeaheadSelected);

			function typeaheadSelected($e, datum){
				window.location.href = "tags.cfm?item=" + datum.toString();
			}
		});

		$(".tile.tile-collapse.tile-collapse-full").on("click", function(event){
			$(".tile.tile-collapse.tile-collapse-full").not($(this)).removeClass("active");
			$(".tile.tile-collapse.tile-collapse-full").not($(this)).find(".tile-toggle").each(function(idx,elem){
				$($(elem).data("target")).removeClass("in");
			});
		});
	</script>
</cfsavecontent>

<cfmodule template="doc_layout.cfm" title="Lucee Tag Reference" prevLinkItem="#prevLinkItem#" nextLinkItem="#nextLinkItem#">

<cfoutput>
	<cfif len( url.item )>
		<cfset data = getTagData( url.namespace, url.item )>
		<cfset tagName = data.namespace & data.namespaceseperator & data.name>

		<cfparam name="data.attributes" default="#{}#">
		<cfparam name="data.attributetype" default="fixed">

		<cfif !structKeyExists(url, "isAjaxRequest")>
			<div class="tile-wrap">
				<div class="tile">
					<ul class="margin-no-top margin-right margin-no-bottom margin-left">
						<li><a href="index.cfm">Home</a></li>
						<li><a href="tags.cfm">Lucee tags</a></li>
						<li class="active">&lt;#lCase( tagName )#&gt;</li>
					</ul>
				</div>
			</div>
		<cfelse>
			<h2 style="text-align: center;">Lucee Tags</h2>
		</cfif>
		<h2>Tag <em>&lt;#uCase( tagName )#&gt;</em></h2>

		<cfif data.status == "deprecated">
			<div class="warning nofocus">#stText.doc.depTag#</div>
		</cfif>

		<!--- Desc --->
		<div class="text">
			<cfif !data.keyExists( "description" ) || !len( data.description )>
				<em>No description found</em>
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
			--->
<span class="err">[</span><!---
			---><span class="nt">&lt;/#tagName#&gt;</span><!---
			---><span class="err">]</span><!---
		---><cfelseif data.bodyType == "required"><!---
			---><span class="nt">&gt;<!---
			--->
&lt;/#tagName#&gt;</span><!---
		---></cfif><!---
	---></pre>

	<cfif data.keyExists( "script" ) && data.script.type != "none">
		<cfset arrAttrNames = data.attributes.keyArray().sort( 'textnocase' )>
		<div class="text">#stText.doc.alsoScript#</div>
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
			---><cfif attr.status == "hidden"><cfcontinue></cfif>
		<cfif !attr.required><span class="err">[</span></cfif><!---
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
		<cfset isdefault = Findnocase('defaultValue', serializeJSON(data))>
		<cfif ( data.attributeType == "fixed" || data.attributeType == "mixed" ) && arrayLen( arrAttrNames )>
			<table class="table maintbl">
				<thead>
					<tr>
						<th width="21%">#stText.doc.attr.name#</th>
						<th width="7%">#stText.doc.attr._type#</th>
						<th width="7%">#stText.doc.attr.required#</th>
						<cfif val(isdefault)><th width="7%">#stText.doc.attr.default#</th></cfif>
						<th width="65">#stText.doc.attr.description#</th>
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
							<td><cfif attr.status EQ "deprecated"><b class="error">#stText.doc.depAttr#</b><cfelse>#Application.objects.utils.formatAttrDesc( attr.description )#</cfif>&nbsp;</td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfif>
	<cfelse><!--- len( url.item) !--->

		<div class="tile-wrap">
			<div class="tile">
				<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
					<li><a href="index.cfm">Home</a></li>
					<li class="active">Lucee tags</li>
				</ul>
			</div>
		</div>

		<p>Tags are at the core of Lucee Server's templating language. You can check out every tag that has been created using the A-Z index below.</p>

		<cfset qryAllItems = queryNew("tags")>
		<cfloop array="#arrAllItems#" index="ai">
			<cfset QueryAddRow(qryAllItems, ["#lCase(ai)#"])>
		</cfloop>

		<cfset list = "_,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z">

		<div class="tile-wrap tile-wrap-animation">
			<cfloop index="i"  list="#list#">
				<cfif i EQ "_">
					<cfset queryList = queryNew("tags")>
					<cfset QueryAddRow(queryList, ["cf_"])>
				<cfelse>
					<cfquery name="queryList" dbtype="query" params=#["cf#i#%"]#>
						SELECT tags FROM qryAllItems  WHERE tags LIKE ?;
					</cfquery>
				</cfif>
				
				<div class="tile tile-collapse tile-collapse-full">
					<div class="tile-toggle" data-target="##function-#lCase(i)#" data-toggle="tile">
						<div class="tile-inner">
							<div class="text-overflow"><strong>#uCase(i)#</strong></div>
						</div>
					</div>
					<div class="tile-active-show collapse" id="function-#lCase(i)#">
						<cfloop list="#valueList(queryList.tags)#" index="currTag">
							<span class="tile">
								<div class="tile-inner">
									<div class="text-overflow"><a href="tags.cfm?item=#currTag#">&lt;#currTag#&gt;</a></div>
								</div>
							</span>
						</cfloop>
					</div>
				</div>
			</cfloop>
		</div>

	</cfif><!--- len( url.item) !--->

</cfoutput>


</cfmodule><!--- doc_layout !--->
