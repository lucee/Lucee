<cfsetting showDebugOutput=false>
<cfinclude template="/lucee/admin/resources/text.cfm">
<cfset stText.doc.attr.default="Default Value">


<cfset arrAllItems = Application.objects.utils.getAllFunctions()>


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
				window.location.href = "functions.cfm?item=" + datum.toString();
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
<cfsavecontent variable="a">
<cfoutput>
	<cfif len( url.item )>

		<cfset data = getFunctionData( url.item )>
		<cfif !structKeyExists(url, "isAjaxRequest")>
			<div class="tile-wrap">
				<div class="tile">
					<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
						<li><a href="index.cfm">Home</a></li>
						<li><a href="functions.cfm">Lucee functions</a></li>
						<li class="active">#data.name#</li>
					</ul>
				</div>
			</div>
		<cfelse>
			<h2 style="text-align: center;">Lucee Functions</h2>
		</cfif>

		<h2>Function <em>#uCase( url.item )#</em></h2>
		<cfif data.status EQ "deprecated">
			<div class="warning nofocus">#stText.doc.depFunction#</div>
		</cfif>
		<!--- Desc --->
		<div class="text">
			<cfif not StructKeyExists(data, "description")>
				<em>No description found</em>
			<cfelse>
				#replace( replace( data.description, '	', '&nbsp;&nbsp;&nbsp;', 'all' ), chr(10), '<br>', 'all' )#
			</cfif>
		</div>

		<cfset first=true>
		<cfset optCount=0>
		<h2>#stText.doc.example#</h2>
		<pre><span class="nf">#data.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType">#data.returntype#</span></pre>

		<!--- Category --->
		<cfif structKeyExists(data, "keywords") AND !arrayIsEmpty(data.keywords)>
			<h2>#stText.doc.category#</h2>
			<div class="text">#arraytolist(data.keywords)#</div>
		</cfif>
		<!--- Argumente --->
		<h2>#stText.doc.argTitle#</h2>
		<cfif data.argumentType EQ "fixed" and not arraylen(data.arguments)>
			<div class="text">#stText.doc.arg.zero#</div>
		<cfelse>
			<div class="text">
				#stText.doc.arg.type[data.argumentType]#
				<cfif data.argumentType EQ "dynamic">
					<cfif data.argMin GT 0 and data.argMax GT 0>
					#replace(replace(stText.doc.arg.minMax,"{min}",data.argMin),"{max}",data.argMax)#
					<cfelseif data.argMin GT 0>
					#replace(stText.doc.arg.min,"{min}",data.argMin)#
					<cfelseif data.argMax GT 0>
					#replace(stText.doc.arg.max,"{max}",data.argMax)#
					</cfif>

				</cfif>
			</div>
		</cfif>


		
		<cfif data.argumentType EQ "fixed" and arraylen(data.arguments)>
			<cfset hasdefaults=false>
			<cfloop array="#data.arguments#" index="key" item="val">
				<cfif !isNull(val.defaultValue)><cfset hasdefaults=true></cfif>
			</cfloop>
			<table class="table maintbl">
				<thead>
					<tr>
						<th width="21%">#stText.doc.arg.name#</th>
						<th width="7%">#stText.doc.arg._type#</th>
						<th width="7%">#stText.doc.arg.required#</th>
						<cfif hasdefaults><th width="7%">#stText.doc.attr.default#</th></cfif>
						<th width="65%">#stText.doc.arg.description#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop array="#data.arguments#" index="attr">
						<cfif attr.status EQ "hidden"><cfcontinue></cfif>
						<tr>
							<td>#attr.name	#</td>
							<td>#attr.type#&nbsp;</td>
							<td>#YesNoFormat(attr.required)#</td>
							<cfif hasdefaults><td><cfif isNull(attr.defaultValue)>&nbsp;<cfelse>#attr.defaultValue#</cfif></td></cfif>
							<td><cfif attr.status == "deprecated">
									<b class="error">#stText.doc.depArg#</b>
								<cfelse>
									#Application.objects.utils.formatAttrDesc( attr.description )#
								</cfif>
								&nbsp;
								</td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfif>

	<cfelse><!--- len( url.item) !--->

		<!--- render index !--->

		<div class="tile-wrap">
			<div class="tile">
				<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
					<li><a href="index.cfm">Home</a></li>
					<li class="active">Lucee functions</li>
				</ul>
			</div>
		</div>

		<p>Functions are at the core of Lucee Server's templating language. You can check out every function available using the A-Z index below.</p>

		<cfset qryAllItems = queryNew("Functions")>
		<cfloop array="#arrAllItems#" index="ai">
			<cfset QueryAddRow(qryAllItems, ["#lCase(ai)#"])>
		</cfloop>

		<cfset list = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z">
		<cfset myarray= {}>

		<div class="tile-wrap tile-wrap-animation">
			<cfloop index="i"  list="#list#">
					<cfquery name="queryList" dbtype="query" params=#["#i#%"]#>
						SELECT functions FROM qryAllItems  WHERE functions LIKE ?;
					</cfquery>
				<div class="tile tile-collapse tile-collapse-full">
					<div class="tile-toggle" data-target="##function-#lCase(i)#" data-toggle="tile">
						<div class="tile-inner">
							<div class="text-overflow"><strong>#uCase(i)#</strong></div>
						</div>
					</div>
					<div class="tile-active-show collapse" id="function-#lCase(i)#">
						<cfloop list="#valueList(queryList.functions)#" index="currFunc">
							<span class="tile">
								<div class="tile-inner">
									<div class="text-overflow"><a href="functions.cfm?item=#currFunc#">#currFunc#</a></div>
								</div>
							</span>
						</cfloop>
					</div>
				</div>
			</cfloop>
		</div>
	</cfif><!--- len( url.item) !--->

</cfoutput>
</cfsavecontent>

<!--- <cfif !structKeyExists(url, "isAjaxRequest")> --->
<cfmodule template="doc_layout.cfm" title="Lucee Function Reference" prevLinkItem="#prevLinkItem#" nextLinkItem="#nextLinkItem#">
	<cfoutput>#a#</cfoutput>
</cfmodule><!--- doc_layout !--->
<!--- <cfelse>
	<link href="/context/doc/assets/css/base.min.css.cfm" rel="stylesheet">
	<link href="/context/doc/assets/css/highlight.css.cfm" rel="stylesheet">
	<cfoutput>#a#</cfoutput>
</cfif> --->
