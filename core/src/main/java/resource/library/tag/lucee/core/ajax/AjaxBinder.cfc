<cfcomponent>

	<cfset variables._LUCEE_AJAX_ALLOWED_BINDS = "cfc|javascript|url"/>
	<cfset variables._LUCEE_AJAX_DEFAULT_BINDING_EVENT = "change"/>
	<cfset variables._LUCEE_AJAX_ALLOWED_BINDING_EVENTS = "change|keyup|mousedown|none"/>
	<cfset variables._LUCEE_JS_BIND_HANDLER = 'Lucee.Bind.jsBindHandler' />
	<cfset variables._LUCEE_CFC_BIND_HANDLER = 'Lucee.Bind.cfcBindHandler' />
	<cfset variables._LUCEE_URL_BIND_HANDLER = 'Lucee.Bind.urlBindHandler' />

	<cfset variables.instance.proxyHelper = createObject('component','ajaxProxyHelper').init() />		
	
	<!--- Constructor --->
    <cffunction name="init" output="no" returntype="ajaxBinder">
		<cfreturn this/>
  	</cffunction>

	<cffunction name="parseParameters" type="array" output="false">
		<cfargument name="bindExpr" required="true" type="string" />
		<cfset var local = structNew()/>
		<cfset local.result = arrayNew(1)/>
			
		<cfset local.params = reFindNoCase('\(.*\)',arguments.bindExpr,1,true) />
		<cfif local.params.len[1] gt 0>
			<cfset local.params = mid(arguments.bindExpr,local.params.pos[1] + 1,local.params.len[1] -2 )/>	
		<cfelse>
			<cfthrow message="No parameters found in the bind expression #arguments.bindExpr#" type="cfajaxproxy.noParameterFound" />
		</cfif>	
		
		<cfloop list="#local.params#" index="i" delimiters=",">
			<cfset local.param = "" />
			<cfset local.event = "" />
			<cfset local.containerid = "" />
			<cfset local.label = "" />
			<!--- look for label --->
			<cfset local.matchlabel = reFindNoCase('\w*=',i,1,true) />
			<cfif local.matchlabel.len[1] gt 0>
				<!--- update the index value removing the label --->
				<cfset local.label = mid(i,local.matchlabel.pos[1],local.matchlabel.len[1] - 1)/>
				<cfset i = right(i,(len(i) - local.matchlabel.len[1]))/>
			</cfif>	
			<cfset local.param = rereplace(i,'\{|\}','','All')  />
			<!--- search for specific event --->
			<cfset local.event = reFindNoCase('(@)+(#_LUCEE_AJAX_ALLOWED_BINDING_EVENTS#)',local.param,1,true)/>
			<cfif local.event.len[1] gt 0>
				<cfset local.event = listLast(local.param,'@')/>
				<cfset local.param = listFirst(local.param,'@')/>
			<cfelse>
				<cfset local.event = _LUCEE_AJAX_DEFAULT_BINDING_EVENT />
			</cfif>
			<!--- check is passed an id of a dom container --->
			<cfif listLen(local.param,':') eq 2>
				<cfset local.containerId = listGetAt(local.param,1,':') />		
				<cfset local.param = listGetAt(local.param,2,':') />
			</cfif>		
			<cfset local.bind = arrayNew(1) />
			<cfset local.bind.append(local.param) />
			<cfset local.bind.append(local.event)/>
			<cfset local.bind.append(local.containerId)/>
			<!--- if no label refer to the dom name attribute --->
			<cfif local.label eq ""><cfset local.label = local.param/></cfif>
			<cfset local.bind.append(local.label)/>
			<cfset local.result.append(local.bind) />		
		</cfloop>
			
		<cfreturn local.result />
	</cffunction>
	
	<cffunction name="parseBind" returntype="struct" output="false">
		<cfargument name="bindExpr" required="true" type="string" />
		<cfargument name="listener" required="false" type="string" default="" />
		<cfargument name="errorHandler" required="true" type="string" default="" />
	
		<cfset var local = structNew() />
		<cfset local.hasParams = true />
		<cfset local.result = structNew() />
		<cfset local.result['bindExpr'] = [] />
		
		<!--- check if exists abc: --->
		<cfset local.bindType = reFindNoCase('(#_LUCEE_AJAX_ALLOWED_BINDS#)\:',arguments.bindExpr,1,true) />
	
		<cfif local.bindType.len[1] gt 0>
			<cfset local.bindType = mid(arguments.bindExpr,local.bindType.pos[1],local.bindType.len[1] - 1)/>	
		<cfelse>
			<cfthrow message="The Bind expression #arguments.bindExpr# is not supported." type="cfajaxproxy.BindExpressionNOtSupported">
		</cfif>	
			
		<!--- javascript --->
		<cfif local.bindType eq 'javascript'>
			<cfset local.jsFunction = reFindNoCase(':\w*\(',arguments.bindExpr,1,true) />
			<cfif local.jsFunction.len[1] gt 0>
				<cfset local.jsFunction = mid(arguments.bindExpr,local.jsFunction.pos[1] + 1,local.jsFunction.len[1] -2 )/>			
			<cfelse>
				<cfthrow message="The Bind expression #arguments.bindExpr# is not supported." type="cfajaxproxy.BindExpressionNOtSupported">
			</cfif>
			<cfset arguments.listener = local.jsFunction />
			<cfset local.result['handler'] = _LUCEE_JS_BIND_HANDLER />
		</cfif>	
	
		<!--- cfc--->
		<cfif local.bindType eq 'cfc'>
			<cfset local.cfcString = reFindNoCase(':.*\(',arguments.bindExpr,1,true) />
			<cfif local.cfcString.len[1] gt 0>
				<cfset local.cfcString = mid(arguments.bindExpr,local.cfcString.pos[1] + 1,local.cfcString.len[1] -2 )/>
				<cfset local.len = listlen(local.cfcString,'.') />
				<cfset local.result['method'] = listGetAt(local.cfcString,local.len,'.') /> 
				<cfset local.result['url'] = listDeleteAt(local.cfcString,local.len,'.') />
				<cfset local.result['url'] = variables.instance.proxyHelper.classToPath(local.result['url']) />
			<cfelse>
				<cfthrow message="The Bind expression #arguments.bindExpr# is not supported." type="cfajaxproxy.BindExpressionNOtSupported">
			</cfif>
			<cfset local.result['handler'] = _LUCEE_CFC_BIND_HANDLER />
		</cfif>	
	
		<!--- url--->
		<cfif local.bindType eq 'url'>
			
			<cfif refind('\?',arguments.bindExpr,1,false) eq 0>
				<cfset local.hasParams = false />
				<cfset local.result['url'] = rereplace(arguments.bindExpr,'url:','','one') & '?'/>
			</cfif>	
			
			<cfif local.hasParams>

				<cfset local.url = reFindNoCase(':.*\?',arguments.bindExpr,1,true) />

				<cfif local.url.len[1] gt 0>
					<cfset local.result['url'] = mid(arguments.bindExpr,local.url.pos[1] + 1,local.url.len[1] -2 )/>
				<cfelse>
					<cfthrow message="The Bind expression #arguments.bindExpr# is not supported." type="cfajaxproxy.BindExpressionNOtSupported">
				</cfif>			
				<!--- alter the bind Expression to fit the parseParameters --->
				<cfset local.queryString = reFindNoCase('\?.*',arguments.bindExpr,1,true) />
				<cfset local.queryString = mid(arguments.bindExpr,local.queryString.pos[1] + 1, local.queryString.len[1] -1) />
                <!--- looks for normal quesry string parameters that are not bindings and keep them with url --->
                <cfset local.qs = "" />
                <cfset local.binds = "" />
                <cfloop list="#local.queryString#" index="local.item" delimiters="&">
                    <cfif find("{",local.item) eq 0>
                       <cfset local.qs = listAppend(local.qs,local.item,"&")>
                    <cfelse>
                        <cfset local.binds = listAppend(local.binds,local.item,"&")>
                    </cfif>
                </cfloop>

                <!--- add qs to url--->
                <cfset local.result["url"] = "#local.result["url"]#?#local.qs#">

				<cfset arguments.bindExpr = reReplace('(' & local.binds &')','&',',','All') />
			</cfif>

			<cfset local.result['handler'] = _LUCEE_URL_BIND_HANDLER />

		</cfif>
	
		<cfset local.result['listener'] = arguments.listener />
		<cfset local.result['errorHandler'] = arguments.errorHandler />
		<!--- prevent operation if we already have assured there are no params to check --->
		<cfif local.hasParams>
			<cfset local.result['bindExpr'] = parseParameters(arguments.bindExpr) />
		</cfif>

		<cfreturn local.result />
	</cffunction>

</cfcomponent>