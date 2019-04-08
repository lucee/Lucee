<cfinclude template="/lucee/admin/resources/text.cfm">


<cf_doc_layout title="Lucee Object Methods Reference">


<cfoutput>

	<cfset itemList = Application.objects.utils.getMemberFunctionList()>
	<cfset arr = itemList.keyArray().sort( 'textnocase' )>
	
	<form id="form-item-selector" action="#CGI.SCRIPT_NAME#">
		<div class="centered x-large">
			
			#stText.doc.chooseFunction#: 
			<select id="select-item" name="item">
				<option value=""> -------------- </option>

				<cfloop array="#arr#" index="obj">
					<cfif left( obj, 1 ) != "_">
						<optgroup label="#obj#">

							<cfset arrObjMethods = itemList[ obj ].keyArray().sort( 'textnocase' )>

							<cfloop array="#arrObjMethods#" index="key">
								<option value="#obj#.#key#" <cfif url.item == "#obj#.#key#">selected="selected"</cfif>>#ucFirst( obj )#.#key#</option>
							</cfloop>
						</optgroup>
					</cfif>
				</cfloop>
			</select>

			<input type="submit" value="#stText.Buttons.OK#"> 
		</div>
		<cfif len( url.item )>
				
			<div class="centered" style="padding: 0.5em;"><a href="#CGI.SCRIPT_NAME#">see all object methods</a></div>
		</cfif>
	</form>


	<cfif len( url.item )>

		<cfset data = getFunctionData( Application.objects.utils.getBIFName( url.item ) )>

		<h2>Documentation for object method <em>#ucFirst(data.member.type)#.#data.member.name#</em></h2>
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
		<pre><span class="syntaxFunc">#ucFirst(data.member.type)#.#data.member.name#(</span><cfloop array="#data.arguments#" index="index" item="item"><cfif index EQ 1 or item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="syntaxFunc">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="syntaxFunc">[</span></cfif><span class="syntaxType">#item.type#</span> <span class="syntaxText">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType">#data.returntype#</span></pre>

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
		<cfset arrNamespaces = itemList.keyArray().sort( 'textnocase' )>

		<br>

		<cfloop array="#arrNamespaces#" index="ns">
			
			<cfif arrNamespaces.len() GT 1>
			
				<h3 style="margin-top: 1.75em;">#ucFirst( ns )#</h3>
			</cfif>
			
			<cfset arrTags = itemList[ ns ].keyArray().sort( 'textnocase' )>

			<cfset lastPrefix = left( arrTags[ 1 ], 1 )>
			<cfloop array="#arrTags#" index="ai">

				<cfif left( ai, 1 ) != lastPrefix>
					
					<div style="font-size: 0.65em;">&nbsp;</div>
					<cfset lastPrefix = left( ai, 1 )>
				</cfif>

				<a href="#CGI.SCRIPT_NAME#?item=#ns#.#ai#" class="index-item">#ai#</a>
			</cfloop>
		</cfloop>


	</cfif><!--- len( url.item) !--->


</cfoutput>



</cf_doc_layout>
