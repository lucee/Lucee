<cfparam name="url.func" default="">
<cfparam name="url.keyword" default="">

<cfset stText.doc.filterByKeyword="Filter by Keyword">
<cfset stText.doc.attr.default="Default Value">
<cfset funcList=getFunctionList()>
<cfset keywords=getFunctionKeywords()>

<cfscript>
	NL="
";

	function formatDesc(string desc){
		desc=replace(trim(desc),NL&"-","<br><li>","all");
		desc=replace(desc,NL,"<br>","all");
	
		return desc;
	}
</cfscript>

<cfoutput>
	<script type="text/javascript">
		function goToKeyword(field){
			var value=field.options[field.selectedIndex].value;
			var path="#request.self#?action=#url.action#&keyword="+value;
			window.location=(path);
		}
		function detail(field){
			var value=field.options[field.selectedIndex].value;
			var path="#request.self#?action=#url.action#&keyword=#url.keyword#&func="+value;
			window.location=(path);
		}
	</script>

	<form action="#request.self#">
		<input type="hidden" name="action" value="#url.action#" />
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.doc.filterByKeyword#</th>
					<td>
						<select name="keyword" onchange="goToKeyword(this)" class="large">
							<option value=""> -------------- </option>
							<cfloop array="#keywords#" index="key">
								<option value="#key#" <cfif url.keyword EQ key>selected="selected"</cfif>>#key#</option>
							</cfloop>
						</select>
					</td>
				</tr>


				<tr>
					<th scope="row">#stText.doc.choosefunction#</th>
					<td>
						<select name="func" onchange="detail(this)" class="large">
							<option value=""> -------------- </option>
							<cfset arr=StructKeyArray(funcList)>
							<cfset ArraySort(arr,'textnocase')>
							<cfloop array="#arr#" index="key">
								<cfif left(key,1) NEQ "_">
									<cfif len(url.keyword)>
										<cfset _data=getFunctionData(key)>
										<cfif !structKeyExists(_data,"keywords") || !ArrayFindNoCase(_data.keywords,url.keyword)>
											<cfcontinue>
										</cfif>
									</cfif>
									<option value="#key#" <cfif url.func EQ key>selected="selected"</cfif>>#key#</option>
								</cfif>
							</cfloop>
						</select>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input class="bs button submit" type="submit" name="mainAction" value="#stText.Buttons.OK#"> 
					</td>
				</tr>
			</tfoot>
		</table>
	</form>

	<cfif len(url.func)>
		<cfset data=getFunctionData(url.func)>
		<h2>Documentation for function <em>#uCase(replace(url.func, ',', ''))#</em></h2>
		<cfif data.status EQ "deprecated">
			<div class="warning nofocus">#stText.doc.depFunction#</div>
		</cfif>
		<!--- Desc --->
		<div class="text">
			<cfif not StructKeyExists(data, "description")>
				<em>No decription found</em>
			<cfelse>
				#replace(replace(data.description,'	','&nbsp;&nbsp;&nbsp;','all'),'
','<br />','all')#
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

			#hasdefaults#
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
							<td><cfif attr.status EQ "deprecated"><b class="error">#stText.doc.depArg#</b><cfelse>#formatDesc(attr.description)#</cfif>&nbsp;</td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfif>
	</cfif>
</cfoutput>