<cfsetting showDebugOutput=false>
<cfinclude template="/lucee/admin/resources/text.cfm">
<cfset stText.doc.attr.default="Default Value">

<cfset itemList = Application.objects.utils.getMemberFunctions()>
<cfset arrAllItems = itemList.keyArray().sort( 'textnocase' )>

<cfif len( url.item )>
	<cfset itemPos = arrAllItems.findNoCase( url.item )>
	<cfif !itemPos>
		<cfset url.item = "">
	</cfif>
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



<cfmodule template="doc_layout.cfm" title="Lucee object methods reference" prevLinkItem="#prevLinkItem#" nextLinkItem="#nextLinkItem#">

<cfoutput>
	<cfif len( url.item )>

		<cfset data = getFunctionData( Application.objects.utils.getBIFName( url.item ) )>

		<div class="tile-wrap">
			<div class="tile">
				<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
					<li><a href="index.cfm">Home</a></li>
					<li><a href="objects.cfm">Lucee objects</a></li>
					<li class="active">#ucFirst(data.member.type)#.#data.member.name#</li>
				</ul>
			</div>
		</div>
		<h2>Object Method <em>#ucFirst(data.member.type)#.#data.member.name#</em></h2>
		<cfif data.status EQ "deprecated">
			<div class="warning nofocus">#stText.doc.depFunction#</div>
		</cfif>
		<!--- Desc --->
		<div class="text">
			<cfif not StructKeyExists(data, "description") or data.description eq "">
				<em>No description found</em>
			<cfelse>
				#replace(replace(data.description,'	','&nbsp;&nbsp;&nbsp;','all'), server.separator.line,'<br />','all')#
			</cfif>
		</div>

		<cfset first=true>
		<cfset optCount=0>
		<pre><span class="syntaxFunc">#ucFirst(data.member.type)#.#data.member.name#(</span><cfloop array="#data.arguments#" index="index" item="item"><cfif index EQ data.member.position or item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="syntaxFunc">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="syntaxFunc">[</span></cfif><span class="syntaxType">#item.type#</span> <span class="syntaxText">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType"><cfif data.member.chaining>#data.member.type#<cfelse>#data.returntype#</cfif></span></pre>

		<!--- Category --->
		<cfif structKeyExists(data, "keywords") AND !arrayIsEmpty(data.keywords)>
			<h2>#stText.doc.category#</h2>
			<div class="text">#arraytolist(data.keywords)#</div>
		</cfif>

		<!--- Arguments --->
		<h2>#stText.doc.argTitle#</h2>
		<div class="itemintro">
			<cfif data.argumentType EQ "fixed" and arraylen(data.arguments) LTE 1>
				#stText.doc.arg.zero#
			<cfelse>
				#stText.doc.arg.type[data.argumentType]#
			</cfif>
		</div>
		<cfif data.argumentType EQ "fixed" and arraylen(data.arguments) GT 1>
			<cfset hasdefaults=false>
			<cfloop array="#data.arguments#" index="key" item="val">
				<cfif !isNull(val.defaultValue)><cfset hasdefaults=true></cfif>
			</cfloop>
			<table class="maintbl">
				<thead>
					<tr>
						<th width="20%">#stText.doc.arg.name#</th>
						<th width="7%">#stText.doc.arg._type#</th>
						<th width="7%">#stText.doc.arg.required#</th>
						<cfif hasdefaults><th width="7%">#stText.doc.attr.default#</th></cfif>
						<th width="66%">#stText.doc.arg.description#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop array="#data.arguments#" index="index" item="attr">
						<cfif index EQ data.member.position or attr.status EQ "hidden"><cfcontinue></cfif>
						<tr>
							<td>#attr.name	#</td>
							<td>#attr.type#&nbsp;</td>
							<td>#YesNoFormat(attr.required)#</td>
							<cfif hasdefaults><td><cfif isNull(attr.defaultValue)>&nbsp;<cfelse>#attr.defaultValue#</cfif></td></cfif>
							<td>
								<cfif attr.status == "deprecated">
									<b class="error">#stText.doc.depArg#</b>
								<cfelse>
									#Application.objects.utils.formatAttrDesc( attr.description )#
								</cfif>
							</td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfif>

	<cfelse><!--- len( url.item) !--->

		<!--- render index !--->

		<cfset qryAllItems = queryNew("Group,objects")>
		<cfloop array="#arrAllItems#" index="ai">
			<cfset QueryAddRow(qryAllItems, ["#lCase(listFirst(ai, "."))#","#lCase(listLast(ai, "."))#"])>
		</cfloop>

		<div class="tile-wrap">
			<div class="tile">
				<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
					<li><a href="index.cfm">Home</a></li>
					<li class="active">Lucee objects</li>
				</ul>
			</div>
		</div>

		<cfloop query="qryAllItems" group="Group">
			<div class="tile tile-collapse tile-collapse-full">
				<div class="tile-toggle" data-target="##function-#lCase(qryAllItems.Group)#" data-toggle="tile">
					<div class="tile-inner">
						<div class="text-overflow"><strong>#ucFirst(qryAllItems.Group)#</strong></div>
					</div>
				</div>
				<div class="tile-active-show collapse" id="function-#lCase(qryAllItems.Group)#">
					<cfloop>
						<span class="tile">
							<div class="tile-inner">
								<div class="text-overflow"><a href="objects.cfm?item=#qryAllItems.Group#.#qryAllItems.objects#">#qryAllItems.objects#</a></div>
							</div>
						</span>
					</cfloop>
				</div>
			</div>
		</cfloop>

	</cfif><!--- len( url.item) !--->


</cfoutput>


</cfmodule><!--- doc_layout !--->