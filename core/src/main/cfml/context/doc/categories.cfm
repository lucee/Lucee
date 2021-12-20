<cfsetting showDebugOutput=false>
<cfinclude template="/lucee/admin/resources/text.cfm">

<cfset arrAllFunctions = Application.objects.utils.getAllFunctions()>
<cfset arrAllCategories = arrayNew(1)>

<cfloop array="#arrAllFunctions#" item="idx">
	<cfset data = getFunctionData( idx )>
	<cfif structKeyExists(data, "keywords")>
		<cfloop array="#data.keywords#" item="idx1">
			<cfif NOT arrayFindNoCase(arrAllCategories, idx1)>
				<cfset arrayAppend(arrAllCategories, idx1)>
			</cfif>
		</cfloop>
	</cfif>
</cfloop>
<cfset ArraySort(arrAllCategories, "textnocase" , "asc")>

<cfif len( url.item )>
	<cfset itemPos = arrAllCategories.findNoCase( url.item )>
	<cfif !itemPos>
		<cfset url.item = "">
	</cfif>

	<cfset prevLinkItem = itemPos GT 1 ? arrAllCategories[itemPos-1] : "">
	<cfset nextLinkItem = itemPos NEQ arrAllCategories.len() ? arrAllCategories[itemPos+1] : "">
<cfelse>
	<cfset prevLinkItem = "">
	<cfset nextLinkItem = "">
</cfif>

<cfif len(url.item)>
	<cfset currCategoryItems = arrayNew(1)>
	<cfloop array="#arrAllFunctions#" item="idx2">
		<cfset data = getFunctionData( idx2 )>
		<cfif structKeyExists(data, "keywords") AND data.keywords.findNoCase(url.item)>
			<cfset arrayAppend(currCategoryItems, idx2)>
		</cfif>
	</cfloop>
	<cfset ArraySort(currCategoryItems, "textnocase" , "asc")>
<cfelse>
	<cfset qryAllItems = queryNew("Category")>
	<cfloop array="#arrAllCategories#" index="ai">
		<cfset QueryAddRow(qryAllItems, ["#lCase(ai)#"])>
	</cfloop>
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
			var typeaheadData = #serializeJson( arrAllCategories )#;
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
				window.location.href = "categories.cfm?item=" + datum.toString();
			}
		});
	</script>
</cfsavecontent>

<cfmodule template="doc_layout.cfm" title="Lucee Tag Reference" prevLinkItem="#prevLinkItem#" nextLinkItem="#nextLinkItem#">
<cfoutput>
	<div class="tile-wrap">
		<div class="tile">
			<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
				<li><a href="index.cfm">Home</a></li>
				<cfif len( url.item )>
					<li><a href="categories.cfm">Categories</a></li>
					<li class="active">#ucFirst(url.item)#</li>
				<cfelse>
					<li class="active">Categories</li>
				</cfif>
			</ul>
		</div>
	</div>
	<cfif len( url.item )>

		<h2>Functions</h2>
		<ul class="list-unstyled">
			<cfloop array="#currCategoryItems#" item="currItem">
				<li><a href="functions.cfm?item=#currItem#">#ucFirst(currItem)#</a></li>
			</cfloop>
		</ul>
	<cfelse>
		<cfset myList = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z">

		<div class="tile-wrap tile-wrap-animation">
			<cfloop index="i"  list="#myList#">
				<cfquery name="queryList" dbtype="query" params=#["#i#%"]#>
					SELECT category FROM qryAllItems  WHERE category LIKE ?;
				</cfquery>
				<div class="tile tile-collapse tile-collapse-full">
					<div class="tile-toggle" data-target="##category-#lCase(i)#" data-toggle="tile">
						<div class="tile-inner">
							<div class="text-overflow"><strong>#uCase(i)#</strong></div>
						</div>
					</div>
					<div class="tile-active-show collapse" id="category-#lCase(i)#">
						<cfloop list="#valueList(queryList.category)#" index="currTag">
							<span class="tile">
								<div class="tile-inner">
									<div class="text-overflow"><a href="categories.cfm?item=#currTag#"> #ucFirst(currTag)# </a></div>
								</div>
							</span>
						</cfloop>
					</div>
				</div>
			</cfloop>
		</div>
	</cfif>
</cfoutput>
</cfmodule>