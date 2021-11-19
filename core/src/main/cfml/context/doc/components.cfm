<!--- <cfset data = getComponentMetaData(request.componentDetails.pack&"."&url.item)>
<cfdump var="#data#" /><cfabort /> --->
<cfsetting showDebugOutput=false>
<cfset arrAllItems = request.componentDetails.cfcs>
<cfset arrAllPacks=request.componentDetails.pack>

<cfinclude template="/lucee/admin/resources/text.cfm">

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
	<cfif !structKeyExists(url, "isAjaxRequest")>
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
		.m-t-15{
			margin-top: 15px;
		}
		.alert-danger, .alert-error{
			background-color: #f2dede;
			border-color: #eed3d7;
			color: #b94a48;
		}
		.alert{
			border: 1px solid #fbeed5;
			border-radius: 4px;
			margin-bottom: 20px;
			padding: 8px 35px 8px 14px;
			text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
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
				window.location.href = "components.cfm?item=" + datum.toString();
			}

			$(".tile.tile-collapse.tile-collapse-full,body").on("click", function(event){
				var clickedOn = $(event.target);
				if (clickedOn.parents().andSelf().is('.tile.tile-collapse.tile-collapse-full')){
					if($(this).prop("tagName") == "DIV"){
						$(".tile.tile-collapse.tile-collapse-full").not($(this)).removeClass("active");
						$(".tile.tile-collapse.tile-collapse-full").not($(this)).find(".tile-toggle").each(function(idx,elem){
							$($(elem).data("target")).removeClass("in");
						});
						// toggle prop and name for currently clicked accordion
						$(this).find("div.funcName").toggle();
						$(this).find("div.funcProp").toggle();
						// show name and hide prop for other accordions
						$(".tile.tile-collapse.tile-collapse-full").not($(this)).each(function(idx,elem){
							$(elem).find("div.funcName").show();
							$(elem).find("div.funcProp").hide();
						});
					}
				}else{
					$(".tile.tile-collapse.tile-collapse-full").removeClass("active");
					$(".tile.tile-collapse.tile-collapse-full").find(".tile-toggle").each(function(idx,elem){
						$($(elem).data("target")).removeClass("in");
					});
						// show name and hide prop for all accordions
					$(".tile.tile-collapse.tile-collapse-full").find("div.funcName").show();
					$(".tile.tile-collapse.tile-collapse-full").find("div.funcProp").hide();
				}
			});
		});
	</script>
	</cfif>
</cfsavecontent>

<cfmodule template="doc_layout.cfm" title="Components" prevLinkItem="#prevLinkItem#" nextLinkItem="#nextLinkItem#">
	<cfif len(url.item)>
		<!--- details for a specific component --->
		<cfoutput>
			<cftry>
				<cfsavecontent variable="compDetailsBody">
					<cfset data = getComponentMetaData(url.item)>

					<cfif !structKeyExists(url, "isAjaxRequest")>
						<div class="tile-wrap">
								<div class="tile">
										<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
											<li><a href="index.cfm">Home</a></li>
											<li><a href="components.cfm">Components</a></li>
											<li class="active">#data.fullName#</li>
										</ul>
								</div>
						</div>
					<cfelse>
						<h2 style="text-align: center;">Lucee Components</h2>
					</cfif>

					<h2>Component <em>#(listLast(data.fullName, "."))#</em></h2>

					<!--- desc/hint --->
					<span style="padding-left: 2em;">
						<cfif !data.keyExists( "hint" ) || !len( data.hint )>
							<em>No description/hint found</em>
						<cfelse>
							#data.hint#
						</cfif>
					</span>

					<!--- Properties of the component --->
					<cfscript>
					tmp = ['accessors':false,'persistent':false,'synchronized':false,'extends':'lucee.Component'];
					propertiesStruct=structNew('linked');
					loop struct=tmp index="key" item="def" {
						if(!structKeyExists(data, key)) continue;
						if(key EQ "extends") val=data[key].fullname;
						else val=data[key];
						if(def!=val) propertiesStruct[key]=val;
					}
					</cfscript>
								

					<cfset stText.doc.attr.type="Type">
					<cfset stText.doc.attr.value="Value">
					<cfset stText.doc.attr.hint="Hint">
					<cfset stText.doc.attr.access="Access">
					<cfset stText.doc.attr.required="Required">
					<cfif structCount(propertiesStruct)>
					<div class="text" style="width: 90%; margin: 0 auto;">
						<table class="table maintbl">
							<thead>
								<tr>
									<th width="50%">#stText.doc.attr.name#</th>
									<th width="50%">#stText.doc.attr.value#</th>
								</tr>
							</thead>
							<tbody>
								<cfloop struct="#propertiesStruct#" index="key" item="val">
									<tr>
										<td>#key#</td>
										<td>#val#</td>
									</tr>
								</cfloop>
							</tbody>
						</table>
					</div>
					</cfif>
					<!--- properties --->
					<cfif structKeyExists(data, "properties") and arrayLen(data.properties)>
					<h2>Properties</h2>
					
					<div class="text">
						<table class="table maintbl">
							<thead>
								<tr>
									<th>#stText.doc.attr.access#</th>
									<th>#stText.doc.attr.required#</th>
									<th>#stText.doc.attr.name#</th>
									<th>#stText.doc.attr.type#</th>
									<th>#stText.doc.attr.hint#</th>
								</tr>
							</thead>
							<tbody>
								<cfloop array="#data.properties#" item="prop">
									<tr>
										<td><cfif structKeyExists(prop, "access")>#prop.access#<cfelse>public</cfif></td>
										<td><cfif structKeyExists(prop, "required")>#prop.required#<cfelse>no</cfif></td>
										<td>#prop.name#</td>
										<td>#prop.type#</td>
										<td><cfif structKeyExists(prop, "hint")>#prop.hint#</cfif></td>
										
									</tr>
								</cfloop>
							</tbody>
						</table>
					</div>
						
					</cfif>
					<!--- functions --->
					<cfif structKeyExists(data, "functions") and arrayLen(data.functions)>
						<h2>Functions</h2>
						<cfset functionsArr = data.functions>
						<cfset functionsStruct = {}>
						<cfloop array="#functionsArr#" index="ai">
							<cfset functionsStruct[lCase(ai.name)] = ai>
						</cfloop>
						<cfset allCompFunctionsArr = structKeyArray(functionsStruct)>
						<cfset ArraySort(allCompFunctionsArr, "textnocase" , "asc")>
						<cfif !structKeyExists(url, "isAjaxRequest")>
							<div class="tile-wrap tile-wrap-animation">
								<cfloop array="#allCompFunctionsArr#" item="currFuncName">
									<cfset currFunc = functionsStruct[currFuncName]>
									<!--- properties for the function --->
									<cfset functionProperties = "#currFunc['access']# #currFunc['returnType']# #currFunc['name']#(" >
									<cfif !currFunc.parameters.isEmpty()>
										<cfloop from="1" to="#arrayLen(currFunc.parameters)#" index="i">
											<cfif i!=1>
												<cfset functionProperties = functionProperties & ", " >
											</cfif>
											<cfset currArg = currFunc.parameters[i]>
											<cfset functionArgs = currArg.required ? "required ":"" >
											<cfset functionArgs = functionArgs & "#currArg.type# #currArg.name#" >
											<cfset functionProperties = functionProperties & functionArgs >
										</cfloop>
									</cfif>
									<cfset functionProperties = functionProperties & ")" >
									<div class="tile tile-collapse tile-collapse-full">
										<div class="tile-toggle" data-target="##api-#lCase(currFunc.name)#" data-toggle="tile">
											<div class="tile-inner">
												<div class="text-overflow funcName"><strong>#(currFunc.name)#</strong></div>
												<div class="text-overflow funcProp" style="display: none;"><strong>#functionProperties#</strong></div>
											</div>
										</div>
										<div class="tile-active-show collapse" id="api-#lCase(currFunc.name)#" style="padding: 0em 2em;">
											<!--- desc/hint --->
											<cfif structKeyExists(currFunc, "hint")>
												<span style="padding-left: 3em;">#currFunc.hint#</span>
											</cfif>
											<!--- arguments for the function --->
											<cfif !currFunc.parameters.isEmpty()>
												<h3 style="padding-left: 1em; margin-top: 24px;">Arguments</h3>
												<span style="padding-left: 3em;">The arguments for this function are set. You can not use other arguments except the following ones.</span>
												<div class="text" style="width: 90%; margin: 0 auto;">
													<table class="maintbl">
														<thead>
															<tr>
																<th>#stText.doc.attr.name#</th>
																<th width="10%">#stText.doc.attr._type#</th>
																<th width="10%">#stText.doc.attr.required#</th>
																<th width="50%">#stText.doc.attr.description#</th>
															</tr>
														</thead>
														<tbody>
															<cfloop array="#currFunc.parameters#" index="currArg">
																<tr>
																	<td>#currArg.name#</td>
																	<td>#currArg.type#</td>
																	<td>#currArg.required#</td>
																	<td><cfif structKeyExists(currArg, "hint")>#currArg.hint#</cfif></td>
																</tr>
															</cfloop>
														</tbody>
													</table>
												</div>
											</cfif>
										</div>
									</div>
								</cfloop>
							</div>
						<cfelse>
							<style type="text/css">
							.modal { overflow: auto !important;}
							</style>
							<div class="tile-wrap">
								<cfloop array="#allCompFunctionsArr#" item="currFuncName">
									<cfset currFunc = functionsStruct[currFuncName]>
									<!--- properties for the function --->
									<cfset functionProperties = "#currFunc['access']# #currFunc['returnType']# #currFunc['name']#(" >
									<cfif !currFunc.parameters.isEmpty()>
										<cfloop from="1" to="#arrayLen(currFunc.parameters)#" index="i">
											<cfif i!=1>
												<cfset functionProperties = functionProperties & ", " >
											</cfif>
											<cfset currArg = currFunc.parameters[i]>
											<cfset functionArgs = currArg.required ? "required ":"" >
											<cfset functionArgs = functionArgs & "#currArg.type# #currArg.name#" >
											<cfset functionProperties = functionProperties & functionArgs >
										</cfloop>
									</cfif>
									<cfset functionProperties = functionProperties & ")" >
									<div class="tile tile-collapse">
										<div class="tile-toggle" data-toggle="tile">
											<div class="tile-inner">
												<div class="text-overflow funcName"><strong>#(currFunc.name)#</strong></div>
												<div class="text-overflow funcProp"><strong>#functionProperties#</strong></div>
											</div>
										</div>
										<div class="tile-active-show"  style="padding: 0em 2em;">
											<!--- desc/hint --->
											<cfif structKeyExists(currFunc, "hint")>
												<span style="padding-left: 3em;">#currFunc.hint#</span>
											</cfif>
											<!--- arguments for the function --->
											<cfif !currFunc.parameters.isEmpty()>
												<h3 style="padding-left: 1em; margin-top: 24px;">Arguments</h3>
												<span style="padding-left: 3em;">The arguments for this function are set. You can not use other arguments except the following ones.</span>
												<div class="text" style="width: 90%; margin: 0 auto;">
													<table class="table maintbl">
														<thead>
															<tr>
																<th>#stText.doc.attr.name#</th>
																<th width="10%">#stText.doc.attr._type#</th>
																<th width="10%">#stText.doc.attr.required#</th>
																<th width="50%">#stText.doc.attr.description#</th>
															</tr>
														</thead>
														<tbody>
															<cfloop array="#currFunc.parameters#" index="currArg">
																<tr>
																	<td>#currArg.name#</td>
																	<td>#currArg.type#</td>
																	<td>#currArg.required#</td>
																	<td><cfif structKeyExists(currArg, "hint")>#currArg.hint#</cfif></td>
																</tr>
															</cfloop>
														</tbody>
													</table>
												</div>
											</cfif>
										</div>
									</div>
								</cfloop>
							</div>
						</cfif>
					</cfif>
				</cfsavecontent>
				<cfcatch type="any"><cfrethrow>
					<cfset hasError = true>
					<div class="alert alert-danger m-t-15">
						<strong>Error!</strong> We are not able to give detailed information about this component, because this component cannot be loaded without an exception: <br>
						<strong>#cfcatch.message#</strong>
						<!--- <cfdump var="#cfcatch#" expand="true" /> --->
					</div>
					<center>Back to <a href="index.cfm" class="alert-link">Home</a> or <a href="components.cfm" class="alert-link">Components</a>.</center>
				</cfcatch>
			</cftry>
			<!--- printing details --->
			<cfif !structKeyExists(variables, "hasError")>
				#compDetailsBody#
			</cfif>
		</cfoutput>
	<cfelse>
		<!--- list all components --->
		<div class="tile-wrap">
			<div class="tile">
				<ul class="breadcrumb margin-no-top margin-right margin-no-bottom margin-left">
					<li><a href="index.cfm">Home</a></li>
					<li class="active">Components</li>
				</ul>
			</div>
		</div>
		<p>The packages listed here are based on the component mappings defined in the Lucee Administrator under  "Archives & Resources/Component". Add your own packages here by register your components with a component mapping. What makes it easier to access your components.</p>

		<cfset qryAllItems = queryNew("component")>
		<cfloop array="#arrAllItems#" index="ai">
			<cfset QueryAddRow(qryAllItems, ["#(ai)#"])>
		</cfloop>

		<cfoutput>
			<div class="tile-wrap tile-wrap-animation">
				<cfloop array="#arrAllPacks#" index="i">
					<div class="tile tile-collapse tile-collapse-full">
						<div class="tile-toggle" data-target="##api-#lCase(replaceNoCase(i, '.', '-','ALL'))#" data-toggle="tile">
							<div class="tile-inner">
								<div class="text-overflow"><strong>#i#</strong></div>
							</div>
						</div>
						<div class="tile-active-show collapse" id="api-#lCase(replaceNoCase(i, '.', '-','ALL'))#">
							<cfloop query="#qryAllItems#">
								<cfset comp=qryAllItems.component>
								<cfset pack=reverse(listRest(reverse(comp),'.'))>
								<cfif i!=pack><cfcontinue></cfif>

								<span class="tile">
									<div class="tile-inner">
										<div class="text-overflow"><a href="components.cfm?item=#comp#">#listLast(comp, ".")#</a></div>
									</div>
								</span>
							</cfloop>
						</div>
					</div>
				</cfloop>
			</div>
		</cfoutput>
	</cfif>
</cfmodule>
