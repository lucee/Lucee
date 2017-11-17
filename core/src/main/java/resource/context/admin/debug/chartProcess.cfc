<cfcomponent output="true">

	<cffunction name="printMemory" returntype="struct">
		<cfset pool['HEAP']="Heap">
		<cfset pool['NON_HEAP']="Non-Heap">
		<cfargument name="usage" type="query" required="yes">
		<cfargument name="showTitle" type="boolean" default="true" required="false">
		<cfset var used=evaluate(ValueList(arguments.usage.used,'+'))>
		<cfset var max=evaluate(ValueList(arguments.usage.max,'+'))>
		<cfset var init=evaluate(ValueList(arguments.usage.init,'+'))>
		<cfset var qry=QueryNew(arguments.usage.columnlist)>
		<cfset QueryAddRow(qry)>
	    <cfset QuerySetCell(qry,"type",arguments.usage.type)>
	    <cfset QuerySetCell(qry,"name",variables.pool[arguments.usage.type])>
	    <cfset QuerySetCell(qry,"init",init,qry.recordcount)>
	    <cfset QuerySetCell(qry,"max",max,qry.recordcount)>
	    <cfset QuerySetCell(qry,"used",used,qry.recordcount)>
	    <cfset arguments.usage=qry>
			<cfif arguments.showTitle><b>#pool[usage.type]#</b></cfif>
			<cfset str = {}>
			<cfloop query="usage">
				<cfset str.pused=int(100/arguments.usage.max*arguments.usage.used)>
	   			<cfset str.pfree=100-str.pused>
			</cfloop>
			<cfreturn str>
	</cffunction>


	<cffunction name="sysMetric" returnType="struct" returnformat="JSON" access="remote">
		<cfset systemInfo=GetSystemMetrics()>
		<cfset heap = printMemory(getmemoryUsage("heap"),false)>
		<cfset nonHeap = printMemory(getmemoryUsage("non_heap"),false)>
		<cfset result = {
			"heap":heap.pused ?: 0,
			"nonheap":nonHeap.pused ?: 0,
			"cpuSystem": int((systemInfo.cpuSystem ?: 0) * 100),
			"cpuProcess": int((systemInfo.cpuProcess ?: 0) *100)
		}>
		<cfreturn result>
	</cffunction>

	<cffunction name="functions" returnType="string" returnformat="JSON"  access="remote">
		<cfset data = getFunctionData( url.item )>
		<cfsavecontent variable="result">
			<cfoutput>
				<h1 class="header" style="text-align: center; color:##585857;">Lucee Functions</h1>
				<h2 class="header"><em>#uCase( url.item )#()</em></h2>
				<cfif data.status EQ "deprecated">
					<div class="warning">Deprecated Function</div>
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
				<h2 class="header">Example</h2>
				<pre class="panel"><span class="nf">#data.name#</span><span class="p">(</snap><cfloop array="#data.arguments#" index="item"><cfif item.status EQ "hidden"><cfcontinue></cfif><cfif not first><span class="nv">,</span></cfif><cfif not item.required><cfset optCount=optCount+1><span class="nv">[</span></cfif><span class="nv">#item.type#</span> <span class="nv">#item.name#</span><cfset first=false></cfloop><span class="syntaxFunc">#RepeatString(']',optCount)#):</span><span class="syntaxType">#data.returntype#</span></pre>

				<!--- Category --->
				<cfif structKeyExists(data, "keywords") AND !arrayIsEmpty(data.keywords)>
					<h2 class="header">Category</h2>
					<div class="text">#arraytolist(data.keywords)#</div>
				</cfif>
				<!--- Argumente --->
				<h2 class="header">Title</h2>
				<cfif data.argumentType EQ "fixed" and not arraylen(data.arguments)>
					<div class="text">Zero</div>
				<cfelse>
					<div class="text">
					<!--- 	#stText.doc.arg.type[data.argumentType]#
						<cfif data.argumentType EQ "dynamic">
							<cfif data.argMin GT 0 and data.argMax GT 0>
							#replace(replace(stText.doc.arg.minMax,"{min}",data.argMin),"{max}",data.argMax)#
							<cfelseif data.argMin GT 0>
							#replace(stText.doc.arg.min,"{min}",data.argMin)#
							<cfelseif data.argMax GT 0>
							#replace(stText.doc.arg.max,"{max}",data.argMax)#
							</cfif>

						</cfif> --->
					</div>
				</cfif>
				<cfif data.argumentType EQ "fixed" and arraylen(data.arguments)>
					<cfset hasdefaults=false>
					<cfloop array="#data.arguments#" index="key" item="val">
						<cfif !isNull(val.defaultValue)><cfset hasdefaults=true></cfif>
					</cfloop>
					<table class="tableStyle" cellpadding="8px">
						<tr>
							<th>Name</th>
							<th>Type</th>
							<th>Required</th>
							<cfif hasdefaults><th>default</th></cfif>
							<th>Description</th>
						</tr>
						<cfloop array="#data.arguments#" index="attr">
							<cfif attr.status EQ "hidden"><cfcontinue></cfif>
							<tr>
								<td>#attr.name	#</td>
								<td>#attr.type#&nbsp;</td>
								<td>#YesNoFormat(attr.required)#</td>
								<cfif hasdefaults><td><cfif isNull(attr.defaultValue)>&nbsp;<cfelse>#attr.defaultValue#</cfif></td></cfif>
								<td><cfif attr.status == "deprecated">
										<b class="error">deprcitedArguments</b>
									<cfelse>
										#formatAttrDesc(attr.description)#
									</cfif>
									&nbsp;
								</td>
							</tr>
						</cfloop>
					</table>
				</cfif>
			</cfoutput>
		</cfsavecontent>

		<cfreturn result>

	</cffunction>

	<cffunction name="tags"  returnType="string" returnformat="JSON"  access="remote">
	 	<cfsavecontent variable="result">
			<cfoutput>
			<cfset input = ReplaceNoCase(url.item, "cf", "")>
			<cfset data = getTagData( "cf", input )>
			<cfset tagName = data.namespace & data.namespaceseperator & data.name>

				<cfparam name="data.attributes" default="#{}#">
				<cfparam name="data.attributetype" default="fixed">
				
				<h1 class="header" style="text-align: center; color:##585857;">Lucee Tags </h1>
				<h2 class="header"><em>&lt;#uCase( tagName )#&gt;</em></h2>

				<cfif data.status == "deprecated">
					<div class="warning">Depreciated Tag</div>
				</cfif>

				<!--- Desc --->
				<div class="text">
					<cfif !data.keyExists( "description" ) || !len( data.description )>
						<em>No decription found</em>
					<cfelse>
						#data.description#
					</cfif>
				</div>

				<!--- Body --->
				<h2 class="header">Body</h2>
				<!--- <div class="text">#data.bodyType#</div> --->
				<cfif data.bodyType == "prohibited">
					<div class="text">This tag can&apos;t have a body. </div>
				<cfelseif data.bodyType == "free">
					<div class="text">This tag may have a body. </div>
				<cfelseif data.bodyType == "required">
					<div class="text">This tag must have a body. </div>
				</cfif>

				<h2 class="header">Usage</h2>
				<cfset arrAttrNames= data.attributes.keyArray().sort( 'textnocase' )>
				<cfif data.hasNameAppendix><cfset tagName &= "CustomName"></cfif>

				<!--- TODO: color coded example tag --->
				<div class="panel">
					<span>&lt;#tagName#</span>
					<cfif data.attributeType == "noname">
						 <div class="leftPadding">##
							<cfloop array="#arrAttrNames#" index="key">
								#data.attributes[key].type# <cfbreak>
							</cfloop>
							expression##
						</div>
					<cfelse>
						<cfloop array="#arrAttrNames#" index="key">
							<cfset attr = data.attributes[ key ]>
							<cfif attr.status EQ "hidden"><cfcontinue></cfif>
							
							<div class="leftPadding">
								<cfif !attr.required>
									<span >[</span>
								</cfif>
								<span>#key#=</span>
								<span>
									<cfif !attr.required><i></cfif>
									<cfif attr.keyExists("values")>#attr["values"].toList("|")#<cfelse>#attr.type#</cfif>
									<cfif !attr.required></i></cfif>
								</span>
								<cfif !attr.required>
									<span >]</span>
								</cfif>
							</div>
						</cfloop>
					</cfif>

					<cfif data.attributeType == "dynamic" || data.attributeType == "mixed"> 
						<div>...</div> 
					</cfif>

					<cfif data.bodyType == "prohibited">
						<span>&gt;</span>
					<cfelseif data.bodyType == "free">
						<span>&gt;</span>
						<span>[</span>
						<span>&lt;/#tagName#&gt;</span>
						<span>]</span>
					<cfelseif data.bodyType == "required">
						<span>&gt;
						&lt;/#tagName#&gt;</span>
					</cfif>
				</div>
				<cfif data.keyExists( "script" ) && data.script.type != "none">
					<cfset arrAttrNames = data.attributes.keyArray().sort( 'textnocase' )>
					<div class="text">Also in script</div>
							<!--- <cfabort showerror="Test"/> --->
					<div class="panel">
						<span>&lt;cfscript&gt;</span>
						<div class="leftPadding">#data.name#</div>
							<cfif data.attributeType == "noname">
								<div class="leftPadding">##
									<cfloop array="#arrAttrNames#" index="key">#data.attributes[ key ].type# <cfbreak></cfloop>
									expression##
								</div>
							<cfelseif data.script.type == "single">  
								<cfloop array="#arrAttrNames#" index="key">
								 	<div class="leftPadding">
										<cfset ss = data.attributes[ key ].scriptSupport>
										<cfset attr = data.attributes[ key ]>
										<cfif ss != "none">
											<cfif ss == "optional"><span>[</span></cfif>
											<cfif attr.keyExists("values")>#attr["values"].toList("|")#<cfelse>#attr.type#</cfif>
											<cfif data.script.rtexpr> expression</cfif>
											<cfif ss == "optional"><span>]</span></cfif>
											<cfbreak>
										</cfif>
									</div>
								</cfloop>
							<cfelse>
								<cfloop array="#arrAttrNames#" index="key">
									<div class="leftPadding">
										<cfset attr=data.attributes[key]>
										<cfif attr.status == "hidden"><cfcontinue></cfif>
										<cfif !attr.required><span>[</span></cfif>
										<span>#key#=</span>
										<span>
											<cfif !attr.required></cfif>
											<cfif attr.keyExists("values")>#attr["values"].toList("|")#<cfelse>#attr.type#</cfif>
											<cfif !attr.required>
												<span>]</span>
											</cfif>
										</span>
									</div>
								</cfloop>
							</cfif>

							<cfif data.attributeType == "dynamic" || data.attributeType == "mixed">
								<div>...</div>
							</cfif>
							<div>
								<cfif data.bodyType == "prohibited">
									<span>;</span>
								<cfelseif data.bodyType == "required" || data.bodyType == "free">
									<span> {
										[...]
									}</span>
								</cfif>
								<span>&lt;/cfscript&gt;</span>
							</div>
						</div>
					</div>
				</cfif>

				<!--- Attributes --->
				<h2 class="header">Attributes</h2>
				<cfif data.attributeType == "fixed" && !arrayLen( arrAttrNames )>
					<div class="text">Zero</div>
				<cfelse>
					<div class="text">The attributes for this tag are fixed. Except for the following attributes no other attributes are allowed. 
						<!--- <cfif data.attributeType == "dynamic">
							<cfif data.attrMin GT 0 && data.attrMax GT 0>
								#replace( replace( stText.doc.attr.minMax, "{min}", data.attrMin ), "{max}", data.attrMax )#
							<cfelseif data.attrMin GT 0>
								#replace( stText.doc.attr.min, "{min}", data.attrMin )#
							<cfelseif data.attrMax GT 0>
								#replace( stText.doc.attr.max, "{max}", data.attrMax )#
							</cfif>
						</cfif> --->
					</div>
				</cfif>
				<cfset isdefault = Findnocase('defaultValue', serializeJSON(data))>
				<cfif ( data.attributeType == "fixed" || data.attributeType == "mixed" ) && arrayLen( arrAttrNames )>
					<table class="tableStyle" cellpadding="8px">
						<tr>
							<th>Name</th>
							<th>Type</th>
							<th>Required</th>
							<cfif val(isdefault)><th>Default</th></cfif>
							<th>Description</th>
						</tr>
						<cfloop array="#arrAttrNames#" index="key">
							<cfset attr=data.attributes[key]>
							<cfif attr.status EQ "hidden"><cfcontinue></cfif>
							<tr>
								<td>#key#</td>
								<td><cfif attr.type EQ "object">any<cfelse>#attr.type#</cfif></td>
								<td>#YesNoFormat(attr.required)#</td>
								<cfif val(isdefault)><td><cfif structKeyExists(attr, "defaultValue")>#attr.defaultValue#</cfif></td></cfif>
								<td><cfif attr.status EQ "deprecated"><b class="warning">Depreciated</b><cfelse>#attr.description#</cfif>&nbsp;</td>
							</tr>
						</cfloop>
					</table>
				</cfif>
			</cfoutput>
		</cfsavecontent>
		<cfreturn result>
	</cffunction>

	<cffunction name="components"  returnType="string" returnformat="JSON"  access="remote">
		<cfsavecontent variable="result">
			<cfoutput>
				<cfset data = getComponentMetaData(url.item)>
					<h1 class="header" style="text-align: center; color:##585857;">Lucee Components</h1>
				<h2 class="header"><em>#(listLast(data.fullName, "."))#</em></h2>
				<!--- desc/hint --->
				<div class="text">
					<cfif !data.keyExists( "hint" ) || !len( data.hint )>
						<em>No description/hint found</em>
					<cfelse>
						#data.hint#
					</cfif>
				</div>
				<!--- Properties of the component --->
				<h2 class="header">Component properties</h2>
				<div class="text">
					<table class="tableStyle" cellpadding="8px">
						<tr>
							<th>Name</th>
							<th>Value</th>
						</tr>
						<cfset propertiesArray = ['accessors','persistent','synchronized','extends']>
						<cfloop array="#propertiesArray#" index="key">
							<cfif !structKeyExists(data, key)>
							<cfcontinue>
							</cfif>
							<tr>
								<td>#key#</td>
								<cfif key EQ "extends">
									<td>#data[key].fullname#</td>
								<cfelse>
									<td>#data[key]#</td>
								</cfif>
							</tr>
						</cfloop>
					</table>
				</div>
				<!--- functions --->
				<cfif structKeyExists(data, "functions")>
					<h2 class="header">Functions</h2>
					<cfset functionsArr = data.functions>
					<cfset functionsStruct = {}>
					<cfloop array="#functionsArr#" index="ai">
						<cfset functionsStruct[lCase(ai.name)] = ai>
					</cfloop>
					<cfset allCompFunctionsArr = structKeyArray(functionsStruct)>
					<cfset ArraySort(allCompFunctionsArr, "textnocase" , "asc")>
					<div class="text">
						<table cellpadding="8px" class="tableStyle" style="width: 50%;">
							<cfloop array="#allCompFunctionsArr#" item="currFuncName">
								<tr>
									<td>
										#ucFirst(currFuncName)#
									</td>
								<tr>
							</cfloop>
						</table>
					</div>
				</cfif>
			</cfoutput>
		</cfsavecontent>
		<cfreturn result>
	</cffunction>

	<cfscRIPT>
		
		function formatAttrDesc( desc ) {

			var NL = chr(10);

			desc = replace( trim( desc ), NL & "-", "<br><li>", "all" );
			desc = replace( desc, NL, "<br>", "all" );
		
			return desc;
		}
	</cfscRIPT>

</cfcomponent>

