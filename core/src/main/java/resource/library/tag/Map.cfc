<cfcomponent name="Map" extends="mapping-tag.lucee.core.ajax.AjaxBase">

	<cfset variables.instance._SUPPORTED_MAP_TYPES  = 'map,satellite,hybrid,terrain' />
	<cfset variables.instance._SUPPORTED_TYPES_CONTROL  = 'none,basic,advanced' />
	<cfset variables.instance._SUPPORTED_ZOOM_CONTROL  = 'none,small,large,large3d,small3d' />
	<cfset variables.instance.ajaxBinder = createObject('component','mapping-tag.lucee.core.ajax.AjaxBinder').init() />
	<cfset variables.children = [] />
	
	<!--- Meta data --->
	<cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.attributes=[
		"name":				{required:false,type:"string",default:"_cfmap_#randRange(1,9999999)#"},
		"onLoad" : 			{required:false,type:"string",default:""},
		"onNotFound" : 		{required:false,type:"string",default:""},
		"onError" :		 	{required:false,type:"string",default:""},
		"centerAddress" : 	{required:false,type:"string",default:""},
		"centerLatitude" : 	{required:false,type:"string",default:""},
		"centerLongitude" : 	{required:false,type:"string",default:""},
		"height" : 			{required:false,type:"numeric",default:400},
		"width" : 			{required:false,type:"numeric",default:400},
		"zoomLevel" :   		{required:false,type:"numeric",default:3},
		"overview"  :         {required:false,type:"boolean",default:false},
		"showScale"  :        {required:false,type:"boolean",default:false},	
		"type" :				{required:false,type:"string",default:"map"},
		"showCenterMarker" :  {required:false,type:"boolean",default:true},
		"markerWindowContent":{required:false,type:"string",default:""},
		"tip" :				{required:false,type:"string",default:""},
		"typeControl" : 		{required:false,type:"string",default:"basic"},
		"zoomControl" : 		{required:false,type:"string",default:"small"},
		"continuousZoom" :    {required:false,type:"boolean",default:true},
		"doubleClickZoom" :	{required:false,type:"boolean",default:true},
		"markerColor" : 	    {required:false,type:"string",default:''},
		"markerIcon" : 	    {required:false,type:"string",default:''}	
	]>
     
    <cffunction name="init" output="no" returntype="void"
      hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">

      	<cfset var js = "" />
		<cfset var str = {} />
		<cfset var mappings = getPageContext().getApplicationContext().getMappings() />
				
		<cfset variables.hasEndTag = arguments.hasEndTag />
		<cfset super.init() />

		<cfsavecontent variable="js">
			<cfoutput><script type="text/javascript">
				Lucee.Ajax.importTag('CFMAP',null,'google','#variables.instance.LUCEEJSSRC#');
				</script>
				</cfoutput>
		</cfsavecontent>

		<cfset writeHeader(js,'_cf_map_import') /> 

  	</cffunction> 
    
    <cffunction name="onStartTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				

		<cfset variables.attributes = arguments.attributes />
		
		<!--- checks --->		
		<cfif attributes.centeraddress eq "" and (attributes.centerlatitude eq "" or attributes.centerlongitude eq "")>
			<cfthrow message="Attributes [centeraddress] or  [centerlatitude and centerlongitude] are required.">
		</cfif>

		<cfif not listFindNoCase(variables.instance._SUPPORTED_TYPES_CONTROL,attributes.typecontrol)>
			<cfthrow message="Attributes [typecontrol] supported values are [#variables.instance._SUPPORTED_TYPES_CONTROL#].">
		</cfif>

		<cfif not listFindNoCase(variables.instance._SUPPORTED_MAP_TYPES,attributes.type)>
			<cfthrow message="Attributes [type] supported values are [#variables.instance._SUPPORTED_MAP_TYPES#].">
		</cfif>

		<cfif not listFindNoCase(variables.instance._SUPPORTED_ZOOM_CONTROL,attributes.zoomcontrol)>
			<cfthrow message="Attributes [zoomcontrol] supported values are [#variables.instance._SUPPORTED_ZOOM_CONTROL#].">
		</cfif>
		
		<cfif len(attributes.markercolor) and len(attributes.markercolor) neq 6>
			<cfthrow message="Attribute [markercolor] must be in hexadecimal format es : FF0000.">
		</cfif>
		
		<cfoutput><div id="#attributes.name#" style="height:#attributes.height#px;width:#attributes.width#px"> </div></cfoutput>
		
		<cfif not variables.hasEndTag>
 			
		</cfif>

	    <cfreturn variables.hasEndTag />   
	</cffunction>

    <cffunction name="onEndTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">				
  		<cfargument name="generatedContent" type="string">
		
		<cfset doMap(argumentCollection=arguments)/>

		<cfreturn false/>	
	</cffunction>
	
    <!---  children   --->
	<cffunction name="getChildren" access="public" output="false" returntype="array">
		<cfreturn variables.children/>
	</cffunction>
	
	<!---	addChild	--->
    <cffunction name="addChild" output="false" access="public" returntype="void">
    	<cfargument name="child" required="true" type="mapitem" />
		<cfset children = getchildren() />
		<cfset children.add(arguments.child) />
    </cffunction>

	<!---   attributes   --->
	<cffunction name="getAttributes" access="public" output="false" returntype="struct">
		<cfreturn variables.attributes/>
	</cffunction>

    <cffunction name="getAttribute" output="false" access="public" returntype="any">
		<cfargument name="key" required="true" type="String" />
    	<cfreturn variables.attributes[key] />
    </cffunction>

	<!---doMap--->		   
    <cffunction name="doMap" output="no" returntype="void">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">
		
		<cfset var js = "" />
		<cfset var rand = "_Lucee_Map_#randRange(1,99999999)#" />	
		
		<cfset var options = duplicate(attributes) />
		<cfset var children = getChildren() />
		
		<cfset structDelete(options,'name') />
					
		<cfsavecontent variable="js"><cfoutput>
		<script type="text/javascript">
		#rand#_on_Load = function(){
			Lucee.Map.init('#attributes.name#',#this.serializeJsonSafe(options)#);
			<cfloop array="#children#" index="child">Lucee.Map.addMarker('#attributes.name#',#serializeJsonSafe(child.getAttributes())#);</cfloop>
		}		
		Lucee.Events.subscribe(#rand#_on_Load,'onLoad');	
		</script>		
		</cfoutput></cfsavecontent>

		<cfset writeHeader(js,'#rand#') /> 
			
	</cffunction>

    <cffunction name="serializeJsonSafe" output="false" access="private" returntype="string">
    	<cfargument name="str" required="true"/>
		<cfscript>
		 var rtn={};
			 loop collection="#str#" item="local.k" {
			 rtn[lcase(k)]=str[k];
		}
		return serializeJson(rtn);
		</cfscript>    			
    </cffunction>
		
</cfcomponent>