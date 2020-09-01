<cfcomponent extends="lucee.core.ajax.AjaxBase">
	
	<cfset variables._SUPPORTED_JSLIB = 'jquery' />
	<cfset variables.supported_types = 'tab' />	
	<cfset variables.children = [] />
	<cfset variables.instance.ajaxBinder = createObject('component','lucee.core.ajax.AjaxBinder').init() />
	
	<!--- Meta data --->
	<cfset this.metadata.attributetype="fixed">
	<cfset this.metadata.hint="">
    <cfset this.metadata.attributes={
		type:			{required:true,type:"string",hint=""},
		name:			{required:false,type:"string",default:"_cf_layout_#randRange(1,999999999)#",hint=""},
		style:      	{required:false,type:"string",default:"",hint=""},
		jsLib:  		{required:false,type:"string",default:"jquery",hint=""},	
		
		/* tab only */
		tabHeight : 	{required:false,type:"numeric",default:50,hint=""},
		tabsselect :	{required:false,type:"string",default:"",hint=""},
		tabsadd : 		{required:false,type:"string",default:"",hint=""},
		tabsremove :	{required:false,type:"string",default:"",hint=""},
		tabsenable :	{required:false,type:"string",default:"",hint=""},
		tabsdisable : 	{required:false,type:"string",default:"",hint=""},
		tabsload : 		{required:false,type:"string",default:"",hint=""}
								
	}>
         
    <cffunction name="init" output="no" returntype="void" hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">
      	<cfset var js = "" />
			
      	<cfset variables.hasEndTag = arguments.hasEndTag />
    	  	
		<!--- Cflayout cannot be empty --->
		<cfif not variables.hasEndTag>
			<cfthrow message="Tag cflayout must have at least one cflayoutarea child tag." />
		</cfif>
		
		<cfset super.init() />			
  	</cffunction> 
    
    <cffunction name="onStartTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">
   		
		<cfset var js = "" />
		<cfset variables.attributes = arguments.attributes />

		<!--- be sure library is supported ( if not we do not have resources to load ) --->
		<cfif listfind(variables._SUPPORTED_JSLIB,attributes.jsLib) eq 0>
			<cfthrow message="The js library [#attributes.jsLib#] is not supported for tag CFLAYOUT. Supported libraries are [#variables._SUPPORTED_JSLIB#]">
		</cfif>

		<cfif listFindNoCase(variables.supported_types,attributes.type) eq 0>
			<cfthrow message="type [#attributes.type#] is not a valid value. Valid types are are : #variables.supported_types#" />
		</cfif>
		
		<!--- do Attributes Check --->
		<cfset doAttributesCheck(attributes)/>
		
		<!--- Load Resources --->
		<cfif not structKeyExists(request,'Lucee_Ajax_Layout_#attributes.type#')>
		<cfsavecontent variable="js">
			<script type="text/javascript">Lucee.Ajax.importTag('CFLAYOUT-#uCase(attributes.type)#','#attributes.jsLib#');</script>
		</cfsavecontent>
		<cfhtmlhead text="#js#" />
		<cfset request['Lucee_Ajax_Layout_#attributes.type#'] = 'loaded' />
		</cfif>
					
	    <cfreturn variables.hasEndTag>   
	</cffunction>

    <cffunction name="onEndTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				
  		<cfargument name="generatedContent" type="string">
		
		<cfset var children = getChildren() />
  		<cfset var style = attributes.style />
				
  		<!--- Cflayout cannot be empty --->
		<cfif arrayIsEmpty(children)>
			<cfthrow message="Tag cflayout must have at least one cflayoutarea child tag." />
		</cfif>
		
		<cfswitch expression="#attributes.type#">        
			<cfcase value="tab">            
				<cfset tab = dotab(argumentCollection = arguments) />
			</cfcase>
		</cfswitch>
		
		<cfoutput>				
			<div id="#attributes.name#" style="#style#">
				#tab#
			</div>	
		</cfoutput>
		
		<cfreturn false/>	
	</cffunction>

	<!---   attributes   --->
	<cffunction name="getAttributes" access="public" output="false" returntype="struct">
		<cfreturn variables.Attributes/>
	</cffunction>

    <cffunction name="getAttribute" output="false" access="public" returntype="any">
		<cfargument name="key" required="true" type="String" />
    	<cfreturn variables.attributes[key] />
    </cffunction>

    <!---  children   --->
	<cffunction name="getChildren" access="public" output="false" returntype="array">
		<cfreturn variables.children/>
	</cffunction>
	
	<!---	addChild	--->
    <cffunction name="addChild" output="false" access="public" returntype="void">
    	<cfargument name="child" required="true" type="layoutarea" />
		<cfset children = getchildren() />
		<cfset children.append(arguments.child) />
    </cffunction>
	
	<!--- private -------------------------------------------------------------------------------->

	<!---doAttributesCheck--->
    <cffunction name="doAttributesCheck" output="false" access="private" returntype="void">
    	<cfargument name="attributes" type="struct">
		
		<cfswitch expression="#attributes.type#">        
			
			<cfcase value="tab">
				
			</cfcase>

		</cfswitch>
	
    </cffunction>

	<!---doTab--->
    <cffunction name="doTab" output="false" access="private" returntype="string">
     	<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				
  		<cfargument name="generatedContent" type="string">

  		<cfset var js = "" />
		<cfset var tab = "" />
		<cfset var rand = "_Lucee_Layout_#randRange(1,99999999)#" />
		<cfset var selected = "" />
		<cfset var disabled = "" />		
		<cfset var binds = [] />
		<cfset var bind = {} />
		<cfset var options = [] />
		<cfset var opt = {} />
		<cfset var layoutOptions = {} />
		
		
		<!--- make the html --->
		<cfsavecontent variable="tab">  
			<cfoutput>
				<ul></ul>
				<cfloop array="#getChildren()#" index="child">
					<div id="#child.getAttribute('name')#">#child.getGeneratedContent()#</div>
				</cfloop>
			</cfoutput>
		</cfsavecontent>	

		<!--- append js to head --->
		<cfsavecontent variable="js">            
			<cfoutput>
			<script type="text/javascript">
			_cf_layout_#rand# = function(){
				Lucee.Layout.initializeTabLayout('#attributes.name#',#serializeJson(attributes)#);
				<cfloop array="#getChildren()#" index="child">
					<cfsilent>
					<cfset randArea = 'cf_layout_tab_bind_#randRange(1,99999999)#' />
					<cfif len(child.getAttribute('source'))>
						<cfset bind = {} />
						<cfset bind = getAjaxBinder().parseBind('url:' & child.getAttribute('source')) />
						<cfset bind['bindTo'] = child.getAttribute('name') />	
						<cfset bind['listener'] = "Lucee.Ajax.innerHtml" />
						<cfset bind['errorHandler'] = child.getAttribute('onBindError') />
					</cfif>
					<cfset opt = {} />
					<cfset opt['refreshOnActivate'] = child.getAttribute('refreshOnActivate') />
					<cfset opt['selected'] = child.getAttribute('selected') />
					<cfset opt['disabled'] = child.getAttribute('disabled') />
					<cfset opt['overflow'] = child.getAttribute('overflow') />
					<cfset opt['style'] = "#child.getAttribute('style')#" />
					<cfset opt['tabHeight'] = attributes.tabHeight />
					<cfif len(child.getAttribute('source'))><cfset opt['bind'] = '#randArea#' /></cfif>
					</cfsilent>									
					<cfif len(child.getAttribute('source'))>Lucee.Bind.register('#randArea#',#serializeJson(bind)#,false);</cfif>
					Lucee.Layout.createTab('#attributes.name#','#child.getAttribute('name')#','#child.getAttribute('title')#','',#serializeJson(opt)#);						
				</cfloop>				
			}
			Lucee.Events.subscribe(_cf_layout_#rand#,'onLoad');	
			</script> 
			</cfoutput>
		</cfsavecontent>
		<cfset writeHeader(js,'_cf_layout_#rand#') /> 

		<cfreturn stripwhitespace(tab) />
		
    </cffunction>	

	<!--- getAjaxBinder --->
	<cffunction name="getAjaxBinder" output="false" returntype="ajaxBinder" access="private">
		<cfreturn variables.instance.ajaxBinder />    
	</cffunction>
				
</cfcomponent>
