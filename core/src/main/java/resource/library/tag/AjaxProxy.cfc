<cfcomponent extends="lucee.core.ajax.AjaxBase">

	<cfset variables.instance.proxyHelper = createObject('component','lucee.core.ajax.AjaxProxyHelper').init() />
	<cfset variables.instance.ajaxBinder = createObject('component','lucee.core.ajax.AjaxBinder').init() />

	<!--- Meta data --->
	<cfset this.metadata.hint="Creates a JavaScript proxy for a component, for use in an AJAX client. Alternatively, creates a proxy for a single CFC method, JavaScript function, or URL that is bound to one or more control attribute values.">
    <cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.attributes=[
		"cfc":		{required:false,type:"string",default:"",hint="the CFC for which to create a proxy. You must specify a dot-delimited path to the CFC. The path can be absolute or relative to location of the CFML page. For example, if the myCFC CFC is in the cfcs subdirectory of the Luceex page, specify cfcs.myCFC. On UNIX based systems, the tag searches first for a file who's name or path corresponds to the specified name or path, but is in all lower case. If it does not find it, Luceex then searches for a file name or path that coresponds to the attribute value exactly, with identical character casing."},
		"jsClassName":{required:false,type:"string",default:"",hint="The name to use for the JavaScript proxy class."},
		"bind":		{required:false,type:"string",default:"",hint="A bind expression that specifies a CFC method, JavaScript function, or URL to call. Cannot be used with the cfc attribute."},
		"onError":	{required:false,type:"string",default:"",hint="The name of a JavaScript function to invoke when a bind, specified by the bind attribute fails. The function must take two arguments: an error code and an error message."},
		"onSuccess":	{required:false,type:"string",default:"",hint="The name of a JavaScript function to invoke when a bind, specified by the bind attribute succeeds. The function must take one argument, the bind function return value. If the bind function is a CFC function, the return value is automatically converted to a JavaScript variable before being passed to the onSuccess function."},
		"extends":	{required:false,type:"boolean",default:true,hint="If true force ajaxproxy to look for remote methods in the cfc extensions chain. Any remote method found will be added to the proxy object. This attribute cannot be used with a bind attribute."},
		"methods":	{required:false,type:"string",default:"",hint="Comma delimited list of methods name. If exists only the method ( if remote ) specified will be exposed in the proxy object."}
	]>

    <cffunction name="init" output="no" returntype="void"
      hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">
      	<cfset super.init() />
  	</cffunction>

    <cffunction name="onStartTag" output="no" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">

		<!--- check --->
    	<cfset var hasCFC=len(trim(attributes.cfc))>
    	<cfset var hasBind=len(trim(attributes.bind))>
        <cfif hasCFC and hasBind>
        	<cfthrow message="you can not use attribute [cfc] and attribute [bind] at the same time">
        <cfelseif not hasCFC and not hasBind>
        	<cfthrow message="you must define at least one of the following attributes [cfc,bind]">
        </cfif>

        <cfif hasCFC>
        	<cfif len(trim(attributes.onError))>
        		<cfthrow message="in this context attribute [onError] is not allowed">
        	<cfelseif len(trim(attributes.onSuccess))>
        		<cfthrow message="in this context attribute [onSuccess] is not allowed">
        	</cfif>
        	<cfset doCFC(argumentCollection:arguments)>
        <cfelse>
        	<cfif len(trim(attributes.jsclassname))>
        		<cfthrow message="in this context attribute [jsclassname] is not allowed">
        	<cfelseif len(trim(attributes.methods))>
        		<cfthrow message="in this context attribute [methods] is not allowed">
        	</cfif>
        	<cfset doBind(argumentCollection:arguments)>
        </cfif>

        <cfreturn false>
    </cffunction>

    <cffunction name="doCFC" output="no" returntype="void">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">

   		<cfset var ph = getProxyHelper() />
		<cfset var js = "" />

		<!---
			CONVERT CFC PATH TO REALTIVE PATH.
			Relative path need to be created and passed to js proxy object to perform ajax calls.
			Es: mypath.components.mycfc  TO /mypath/components/mycfc.cfc
		--->
		<cfset cfcPath = ph.classToPath(attributes.cfc) />

		<!--- get the cfc meta data filtered by remote access only --->
		<cfset meta = ph.parseMetaData(attributes.cfc,attributes.methods,attributes.extends) />

		<cfsavecontent variable="js">
			<cfoutput>
			<script type="text/javascript">
			var _Lucee_#attributes.jsclassname# = Lucee.ajaxProxy.init('#cfcPath#','#attributes.jsClassName#');
			<cfloop array="#meta.functions#" index="method"><cfset args = ph.getArguments(method.parameters)/><cfset argsJson = ph.argsToJsMode(args)/>_Lucee_#attributes.jsclassname#.prototype.#method.name# = function(#args#){return Lucee.ajaxProxy.invokeMethod(this,'#method.name#',{#argsJson#});};
			</cfloop>
			</script>
            </cfoutput>
        </cfsavecontent>
		<cfset writeHeader(js,'_Lucee_#attributes.jsclassname#') />

	</cffunction>

    <cffunction name="doBind" output="no" returntype="void">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">

		<cfset bind = getAjaxBinder().parseBind(bindExpr=attributes.bind,listener=attributes.onSuccess,errorHandler=attributes.onError) />
		<cfset rand = "_Lucee_Bind_#randRange(1,99999999)#" />

		<cfsavecontent variable="js">
			<cfoutput>
			<script type="text/javascript">
			#rand# = function(){
				Lucee.Bind.register('_Lucee_Bind_#randRange(1,99999999)#',#serializeJson(bind)#);
			}
			Lucee.Events.subscribe(#rand#,'onLoad');
			</script>
            </cfoutput>
        </cfsavecontent>
		<cfset writeHeader(js,'#rand#') />

	</cffunction>


	<!--- Private --->

	<!---getProxyHelper--->
	<cffunction name="getProxyHelper" output="false" returntype="ajaxProxyHelper" access="private">
		<cfreturn variables.instance.proxyHelper />
	</cffunction>

	<!--- getAjaxBinder --->
	<cffunction name="getAjaxBinder" output="false" returntype="ajaxBinder" access="private">
		<cfreturn variables.instance.ajaxBinder />
	</cffunction>

</cfcomponent>