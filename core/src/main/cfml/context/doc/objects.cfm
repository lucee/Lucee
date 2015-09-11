<cfinclude template="/lucee/admin/resources/text.cfm">


<cfset itemList = Application.objects.utils.getMemberFunctions()>

<cfset arrAllItems = itemList.keyArray().sort( 'textnocase' )>

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



<cfmodule template="doc_layout.cfm" title="Lucee Object Methods Reference">


<cfoutput>


	<form id="form-item-selector" action="#CGI.SCRIPT_NAME#">
		<div class="centered x-large">

			#stText.doc.chooseFunction#:
			<input type="text" name="item" id="search-item" autocomplete="off">

			<input type="submit" value="#stText.Buttons.OK#">
		</div>
		<cfif len( url.item )>

			<div class="centered" style="padding: 0.5em;"><a href="#CGI.SCRIPT_NAME#">see all object methods</a></div>
		</cfif>
	</form>


	<cfif len( url.item )>

		<cfset data = getFunctionData( Application.objects.utils.getBIFName( url.item ) )>

		<h2>Object Method <em>#ucFirst(data.member.type)#.#data.member.name#</em></h2>
		<cfif data.status EQ "deprecated">
			<div class="warning nofocus">#stText.doc.depFunction#</div>
		</cfif>
<!--- Desc --->
		<div class="text">
			<cfif not StructKeyExists(data, "description") or data.description eq "">
				<em>No decription found</em>
			<cfelse>
				#replace(replace(data.description,'	','&nbsp;&nbsp;&nbsp;','all'), server.separator.line,'<br />','all')#
			</cfif>
		</div>

		<cfset first=true>
		<cfset optCount=0>
		<pre><span class="syntaxFunc">#ucFirst(data.member.type)#.#data.member.name#(</span><cfloop array="#data.arguments#" index="index" item="item"><cfif index EQ 1 or item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="syntaxFunc">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="syntaxFunc">[</span></cfif><span class="syntaxType">#item.type#</span> <span class="syntaxText">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType">#data.returntype#</span></pre>

		<!--- Argumente --->
		<h2>#stText.doc.argTitle#</h2>
		<div class="itemintro">
			<cfif data.argumentType EQ "fixed" and arraylen(data.arguments) LTE 1>
				#stText.doc.arg.zero#
			<cfelse>
				#stText.doc.arg.type[data.argumentType]#
			</cfif>
		</div>
		<cfif data.argumentType EQ "fixed" and arraylen(data.arguments) GT 1>
			<table class="maintbl">
				<thead>
					<tr>
						<th width="20%">#stText.doc.arg.name#</th>
						<th width="7%">#stText.doc.arg._type#</th>
						<th width="7%">#stText.doc.arg.required#</th>
						<th width="66%">#stText.doc.arg.description#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop array="#data.arguments#" index="index" item="attr">
						<cfif index EQ 1 or attr.status EQ "hidden"><cfcontinue></cfif>
						<tr>
							<td>#attr.name	#</td>
							<td>#attr.type#&nbsp;</td>
							<td>#YesNoFormat(attr.required)#</td>
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

		<cfset lastObj    = "">
		<cfset lastPrefix = "">
		<cfloop array="#arrAllItems#" item="objMethod">

			<cfset obj    = listFirst( objMethod, '.' )>
			<cfset method = listLast( objMethod, '.' )>

			<cfif obj != lastObj>

				<h3 style="margin-top: 1.0em;">#listFirst( objMethod, '.' )#</h3>
				<cfset lastObj = obj>
			</cfif>

			<cfif left( method, 1 ) != lastPrefix>

				<div style="height: 0.65em;">&nbsp;</div>
				<cfset lastPrefix = left( method, 1 )>
			</cfif>

			<a href="#CGI.SCRIPT_NAME#?item=#objMethod#" class="index-item">#method#</a>
		</cfloop>

	</cfif><!--- len( url.item) !--->


</cfoutput>


</cfmodule><!--- doc_layout !--->