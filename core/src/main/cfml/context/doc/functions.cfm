<cfinclude template="/lucee/admin/resources/text.cfm">
<cfset stText.doc.attr.default="Default Value">


<cfset arrAllItems = Application.objects.utils.getAllFunctions()>


<cfif len( url.item )>

	<cfif !arrAllItems.findNoCase( url.item )>

		<cfset url.item = "">
	</cfif>
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


<cfmodule template="doc_layout.cfm" title="Lucee Function Reference">


<cfoutput>


	<form id="form-item-selector" action="#CGI.SCRIPT_NAME#">
		<div class="centered x-large">

			#stText.doc.chooseFunction#:
			<input type="text" name="item" id="search-item" autocomplete="off">

			<input type="submit" value="#stText.Buttons.OK#">
		</div>
		<cfif len( url.item )>

			<div class="centered" style="padding: 0.5em;"><a href="#CGI.SCRIPT_NAME#">see all functions</a></div>
		</cfif>
	</form>



	<cfif len( url.item )>

		<cfset data = getFunctionData( url.item )>

		<h2>Function <em>#uCase( url.item )#</em></h2>
		<cfif data.status EQ "deprecated">
			<div class="warning nofocus">#stText.doc.depFunction#</div>
		</cfif>
		<!--- Desc --->
		<div class="text">
			<cfif not StructKeyExists(data, "description")>
				<em>No decription found</em>
			<cfelse>
				#replace( replace( data.description, '	', '&nbsp;&nbsp;&nbsp;', 'all' ), chr(10), '<br>', 'all' )#
			</cfif>
		</div>

		<cfset first=true>
		<cfset optCount=0>
		<h2>#stText.doc.example#</h2>
		<pre><span class="syntaxFunc">#data.name#(</span><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="syntaxFunc">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="syntaxFunc">[</span></cfif><span class="syntaxType">#item.type#</span> <span class="syntaxText">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType">#data.returntype#</span></pre>

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
			<table class="maintbl">
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
		<br>

		<cfset lastPrefix = left( arrAllItems[ 1 ], 1 )>
		<cfloop array="#arrAllItems#" index="ai">

			<cfif left( ai, 1 ) != lastPrefix>

				<div style="height: 0.65em;"></div>
				<cfset lastPrefix = left( ai, 1 )>
			</cfif>

			<a href="#CGI.SCRIPT_NAME#?item=#ai#" class="index-item">#ai#</a>
		</cfloop>

	</cfif><!--- len( url.item) !--->

</cfoutput>


</cfmodule><!--- doc_layout !--->