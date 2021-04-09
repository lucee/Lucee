<cfcomponent extends="lucee.core.ajax.AjaxBase">

	<cfset variables.instance.ajaxBinder = createObject('component','lucee.core.ajax.AjaxBinder').init() />

	<!--- Meta data --->
    <cfset this.metadata.hint="Creates an HTML tag with specified contents and lets you to use bind expressions to dynamically control the tag contents.">
	<cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.attributes=[
		"id":			{required:false,type:"string",default:"",hint="The HTML ID attribute value to assign to the generated container tag."},
		"bindOnLoad": {required:false,type:"boolean",default:true,hint="- true (executes the bind attribute expression when first loading the tag. 
		- false (does not execute the bind attribute expression until the first bound event)
		To use this attribute, you must also specify a bind attribute"},		
		"bind":		{required:false,type:"string",hint="A bind expression that returns the container contents. Note: If a CFML page specified in this attribute contains tags that use AJAX features, such as cfform, cfgrid, and cfwindow, you must use a tag on the page with the tag. For more information, see cfajaximport."},
		"onBindError":{required:false,type:"string",default:"",hint="The name of a JavaScript function to execute if evaluating a bind expression results in an error. The function must take two attributes: an HTTP status code and a message."},
		"tagName":	{required:false,type:"string",default:"div",hint="The HTML container tag to create."}
	]/>
         
    <cffunction name="init" output="no" returntype="void" hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">
		<cfset variables.hasEndTag = arguments.hasEndTag />
		<cfset super.init() />
  	</cffunction> 
    
    <cffunction name="onStartTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				

		<!--- check --->
    	<cfset var hasBindError=len(trim(attributes.onBindError))>
 				
        <cfif hasBindError>
        	<cfif IsDefined("attributes.bind") and not len(trim(attributes.bind))>
        		<cfthrow message="in this context attribute [onBindError] is not allowed">
        	</cfif>
        </cfif>
		<!--- Don't bind if the argument is not provided, just render the tag. 
			  Function doBind will validate the bind expression if provided ---> 
        <cfif IsDefined("attributes.bind")>
			<cfset doBind(argumentCollection=arguments) />
		</cfif>
		<cfoutput><#attributes.tagname# id="#attributes.id#"></cfoutput>
		<cfif not variables.hasEndTag>
			<cfoutput></#attributes.tagname#></cfoutput>
		</cfif>
	    <cfreturn variables.hasEndTag>   
	</cffunction>

    <cffunction name="onEndTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				
  		<cfargument name="generatedContent" type="string">						
			#arguments.generatedContent#</#attributes.tagname#>
		<cfreturn false/>	
	</cffunction>
	
	<!---doBind--->		   
    <cffunction name="doBind" output="no" returntype="void">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">
		
		<cfset var js = "" />				
		<cfset var bind = getAjaxBinder().parseBind(attributes.bind) />
		
		<cfif not structKeyExists(attributes,'id') or not len(trim(attributes.id))>
			<cfset attributes.id = 'lucee_#randRange(1,99999999)#'/>
		</cfif>
		<cfset bind['bindTo'] = attributes.id />	
		<cfset bind['listener'] = "Lucee.Ajax.innerHtml" />
		<cfset bind['errorHandler'] = attributes.onBindError />
		<cfset rand = "_Lucee_Bind_#randRange(1,99999999)#" />
		<cfsavecontent variable="js"><cfoutput>
		<script type="text/javascript">
		#rand# = function(){
			Lucee.Bind.register('#attributes.id#',#serializeJson(bind)#,#attributes.bindOnLoad#);
		}		
		Lucee.Events.subscribe(#rand#,'onLoad');	
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