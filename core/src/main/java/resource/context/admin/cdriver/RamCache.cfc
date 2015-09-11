<cfcomponent extends="Cache">
	
    <cfset fields=array(
		field("Time to idle in seconds","timeToIdleSeconds","0",true,"Sets the time to idle for an element before it expires. If all fields are set to 0 the element live as long the server live.","time"),
		field("Time to live in seconds","timeToLiveSeconds","0",true,"Sets the timeout to live for an element before it expires. If all fields are set to 0 the element live as long the server live.","time")
	)>
    
	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.runtime.cache.ram.RamCache">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string">
    	<cfreturn "RamCache">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Create a Ram Cache (in Memory Cache)">
    </cffunction>
    
</cfcomponent>