<cfcomponent extends="lucee.core.ajax.AjaxBase">

	<cfset variables._SUPPORTED_JSLIB = 'jquery,ext' />
	<cfset variables.instance.ajaxBinder = createObject('component','lucee.core.ajax.AjaxBinder').init() />
	
	<!--- Meta data --->
	<cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.hint="Creates a pop-up window in the browser. Does not create a separate browser pop-up instance. ">
    
    
    <cfset this.metadata.attributes=[
		"name":			{required:false,type:"string",default:"_cf_window_#randRange(1,999999999)#",hint:""},
		"title":      	{required:false,type:"string",default:"",hint:""},		
		"source":			{required:false,type:"string",default:"",hint:""},
		"onBindError":	{required:false,type:"string",default:"",hint:""},
		"modal":      	{required:false,type:"boolean",default:false,hint:""},
	  	"refreshOnShow": 	{required:false,type:"boolean",default:false,hint:""},
		"width":  		{required:false,type:"numeric",default:500,hint:""},
		"height":			{required:false,type:"numeric",default:300,hint:""},	
		"minWidth":  		{required:false,type:"numeric",default:150,hint:""},
		"minHeight":		{required:false,type:"numeric",default:150,hint:""},
		"initShow":   	{required:false,type:"boolean",default:false,hint:""},
		"resizable":  	{required:false,type:"boolean",default:true,hint:""},
		"draggable":  	{required:false,type:"boolean",default:true,hint:""},
		"onBindError":	{required:false,type:"string",default:"",hint:""},
		"jsLib":  		{required:false,type:"string",default:"jquery",hint:""},
		"x":		        {required:false,type:"numeric",default:-1,hint:""},
		"y":		        {required:false,type:"numeric",default:-1,hint:""},
		"buttons":        {required:false,type:"string",default:"{}",hint:""}					
	]>
         
    <cffunction name="init" output="no" returntype="void"
      hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">

      	<cfset var js = "" />     	
      	<cfset variables.hasEndTag = arguments.hasEndTag />				
		
		<cfset super.init() />
  	</cffunction> 
    
    <cffunction name="onStartTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				

		<!--- be sure library is supported ( if not we do not have resources to load ) --->
		<cfif listfind(variables._SUPPORTED_JSLIB,attributes.jsLib) eq 0>
			<cfthrow message="The js library [#attributes.jsLib#] is not supported for tag CFWINDOW. Supported libraries are [#variables._SUPPORTED_JSLIB#]">
		</cfif>

		<cfif not structKeyExists(request,'Lucee_Ajax_Window')>
		<cfsavecontent variable="js">
			<script type="text/javascript">Lucee.Ajax.importTag('CFWINDOW','#attributes.jsLib#');</script>
		</cfsavecontent>
		<cfhtmlhead text="#js#" />
		<cfset request.Lucee_Ajax_Window = 'loaded' />
		</cfif>

		<!--- checks --->
    	<cfset var hasRefreshOnShow=attributes.refreshOnShow />
    	<cfset var hasSource=len(trim(attributes.source))>
		
		<cfif not hasSource>
			<cfif hasRefreshOnShow>
				<cfthrow message="in this context attribute [hasRefreshOnShow] is not allowed">
			</cfif>
		</cfif>

		<cfset doWindow(argumentCollection=arguments)/>
		<cfoutput><div id="#attributes.name#"></cfoutput>
		<cfif not variables.hasEndTag>
			<cfoutput></div></cfoutput>
		</cfif>
	    <cfreturn variables.hasEndTag>   
	</cffunction>

    <cffunction name="onEndTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				
  		<cfargument name="generatedContent" type="string">						
			#arguments.generatedContent#</div>
		<cfreturn false/>	
	</cffunction>
	
	<!---doWindow--->		   
    <cffunction name="doWindow" output="no" returntype="void">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">
		
		<cfset var js = "" />
		<cfset var rand = "_Lucee_Win_#randRange(1,99999999)#" />		
		<cfset var bind = getAjaxBinder().parseBind('url:' & attributes.source) />
		
		<cfset bind['bindTo'] = attributes.name />	
		<cfset bind['listener'] = "Lucee.Ajax.innerHtml" />
		<cfset bind['errorHandler'] = attributes.onBindError />
		
		<cfsavecontent variable="js"><cfoutput>
		<script type="text/javascript">
		#rand#_on_Load = function(){
			<cfif len(attributes.source)>Lucee.Bind.register('#rand#',#serializeJson(bind)#,false);</cfif>
			Lucee.Window.create('#attributes.name#','#attributes.title#','#attributes.source#',{modal:#attributes.modal#,refreshOnShow:#attributes.refreshOnShow#,resizable:#attributes.resizable#,draggable:#attributes.draggable#,width:#attributes.width#,height:#attributes.height#,minWidth:#attributes.minWidth#,minHeight:#attributes.minHeight#,initShow:#attributes.initShow#,x:#attributes.x#,y:#attributes.y#,buttons:#attributes.buttons#}<cfif len(attributes.source)>,'#rand#'</cfif>);
		}		
		Lucee.Events.subscribe(#rand#_on_Load,'onLoad');	
		</script>		
		</cfoutput></cfsavecontent>
		<cfset writeHeader(js,'#rand#') /> 
	</cffunction>

	<!--- Private --->	
	<!--- getAjaxBinder --->
	<cffunction name="getAjaxBinder" output="false" returntype="ajaxBinder" access="private">
		<cfreturn variables.instance.ajaxBinder />    
	</cffunction>
		
</cfcomponent>