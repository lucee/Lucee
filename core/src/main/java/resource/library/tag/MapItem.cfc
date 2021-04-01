<cfcomponent name="Mapitem">

	<!--- Meta data --->
	<cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.attributes=[
	    "name":      	    {required:false,type:"string",default:""},
		"address":      	{required:false,type:"string",default:""},
		"latitude":     	{required:false,type:"string",default:""},
		"longitude" : 	{required:false,type:"string",default:""},
		"tip":		    {required:false,type:"string",default:""},
		"markerWindowContent":	{required:false,type:"string",default:""},
		"markerColor" : 	    {required:false,type:"string",default:''},
		"markerIcon" : 	    {required:false,type:"string",default:''}	
	]>
        
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
		
		<cfif not len(attributes.address) and (not len(attributes.latitude) or not len(attributes.longitude))>
			<cfthrow message="Attributes [address] is required if [longitude and latitude] are not provided." />
		</cfif>
		
		<!--- if name is not passed use the parent one ---> 
		<cfif not len(attributes.name)>
			<cfset attributes.name = parent.getAttribute('name') />
		</cfif>
		   				
		<!--- If there is no end tag add the attributes to tee parent collection --->
		<cfset parent.addChild(this) />
				
	    <cfreturn variables.hasEndTag>   
	</cffunction>

    <!---   parent   --->
	<cffunction name="getparent" access="public" output="false" returntype="map">
		<cfreturn variables.parent/>
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