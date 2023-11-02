<cfcomponent name="LayoutArea">

	<cfset variables['generatedContent'] = "" />

	<!--- Meta data --->
	<cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.attributes={
		name:      		{required:false,type:"string",default:"_cf_layout_#randRange(1,999999999)#"},
		title:     		{required:false,type:"string",default:""},
		selected : 		{required:false,type:"Boolean",default:"false"},
		disabled : 		{required:false,type:"Boolean",default:"false"},		
		source:			{required:false,type:"string",default:""},
		onBindError:	{required:false,type:"string",default:""},
		refreshOnActivate: {required:false,type:"Boolean",default:"false"},
		style:			{required:false,type:"string",default:""},
		overflow:		{required:false,type:"string",default:"auto"}
	}>
         
    <cffunction name="init" output="no" returntype="void"
      hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">
      	<cfset variables.hasEndTag = arguments.hasEndTag />
      	<cfset variables.parent = arguments.parent />
	</cffunction> 
    
    <cffunction name="onStartTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">
   		
   		<cfset var parent = getParent() />
 		<cfset variables.attributes = arguments.attributes />
   			
		<cfif parent.getAttribute('type') eq 'tab' and attributes.title eq "">
			<cfthrow message="Attributes [title] is required for a tag [layoutarea]." />
		</cfif>
   				
		<!--- If there is no end tag add the attributes to tee parent collection --->
		<cfif not variables.hasEndTag>
			<cfset parent.addChild(this) />
		</cfif>
				
	    <cfreturn variables.hasEndTag>   
	</cffunction>

    <cffunction name="onEndTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				
  		<cfargument name="generatedContent" type="string">
  		<cfset var parent = getParent() />
		<cfset variables['generatedContent'] = 	arguments.generatedContent />
		<cfset parent.addChild(this) />
		<cfreturn false/>	
	</cffunction>

    <!---   parent   --->
	<cffunction name="getparent" access="public" output="false" returntype="layout">
		<cfreturn variables.parent/>
	</cffunction>

	<!---getGeneratedContent--->
    <cffunction name="getGeneratedContent" output="false" access="public" returntype="string">
    	<cfreturn variables.generatedContent />
    </cffunction>
	
	<!---   attributes   --->
	<cffunction name="getAttributes" access="public" output="false" returntype="struct">
		<cfreturn variables.attributes/>
	</cffunction>

    <cffunction name="getAttribute" output="false" access="public" returntype="any">
		<cfargument name="key" required="true" type="String" />
    	<cfreturn variables.attributes[key] />
    </cffunction>

				
</cfcomponent>