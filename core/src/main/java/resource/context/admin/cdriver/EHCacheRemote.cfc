<cfcomponent extends="Cache">

	
    <cfset fields=array(
		field("URL","url","http://",true,"","text")
		,field("Remote Cache Name","remoteCacheName","",true,"","text")
	)>

	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.extension.io.cache.eh.remote.EHCacheRemote">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="no">
    	<cfreturn "EHCache Remote">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Remote Connection (Soap, RESTFul) ">
    </cffunction>
</cfcomponent>