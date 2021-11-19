<cfoutput>
	<cfset navigation = stText.MenuStruct[request.adminType]>
	<cfset arr = []>
	<cfset adminKey = []>
	<cfset str = {}>
	<cfscript>
		for(key in navigation){
			for(act in key.children){
				if(structKeyExists(act, "hidden") && act.hidden) cfcontinue;
				tmpStr = structNew();
				arrayAppend(adminKey, act.action);
				tmpStr[act.action] = "#key.action#.#act.action#";
				arrayAppend(arr, tmpStr);
			}
		}
	</cfscript>
	<cfset otherLinks = {"overView": "overView",  "Performance/Caching" : "server.cache", "Language/Compiler" : "server.compiler" , "CFXtags": "resources.cfx_tags"}>

	<cfscript>
		for(key in otherLinks){
			tmpStr = structNew();
			tmpStr[key] = otherLinks[key];
			arrayAppend(adminKey, key);
			arrayAppend(arr, tmpStr);
		}
	</cfscript>


	<cfhtmlbody>
		<script type="text/javascript">
			var allArr = #serializeJson(arr)#;
			var adminKeys = #serializeJson(adminKey)#;
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

				$( '##lucee-admin-search-input' ).typeahead(
					{
						hint: true,
						highlight: true,
						minLength: 1
					},
					{
					  name: 'keyWords',
					  source: substringMatcher(adminKeys),
					   templates: {
						    empty:  '<div class="moreResults"><a><span onclick="moreInfo()">Click here for More Results</a></span></div>'
					  	}
				}
			).on('typeahead:selected', typeaheadSelected);
				function typeaheadSelected($e, datum){
					$.each(allArr, function(i, data) {
						$.each(data, function(x, y){
							if(datum.toString() == x){
								action = data[x];
							}
						});
					});
					window.location.href = '#request.self#?action='+ action;
				}
			});
			function moreInfo() {
				val = $( '##lucee-admin-search-input' ).val();
				window.location.href = '#request.self#?action=admin.search&q='+ val;
			}
		</script>

		<style type="text/css">
			.twitter-typeahead{
				width: 94% !important;
			}
			.tt-suggestion.tt-selectable p{
				margin: 0px !important;
			}
			.tt-suggestion.tt-selectable{
				cursor: pointer;
			}
			/*show suggestion words */
			.tt-menu.tt-open{
				background-color: white !important;
				width: 110% !important;
				font-size:14px !important; 
				padding:2% 1% 2% 2% !important;
			}
			.tt-suggestion.tt-selectable:hover{
				background-color: #request.singleMode?'##666':(request.adminType=="web"?'##39c':'##BF4F36')# !important;
				color: white;
			}
			/*show more Results*/
			.moreResults{
				font-size: 10px;
				font-style: italic;
				padding: 2% 1% 2% 1% ;
			}
			.navSearch{
				border-color:  #request.singleMode?'##666':(request.adminType=="web"?'##39c':'##BF4F36')# !important;
				background-color: ##fff !important;
				width: 89% !important;
				padding: 1px 1px 1px 1px;
			}
		</style>
	</cfhtmlbody>
</cfoutput>