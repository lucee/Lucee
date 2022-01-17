<cfcomponent extends="Layout">
	
    <cfset fields=array(
		field("Pattern","pattern","%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n",true,"This is the string which controls formatting and consists of a mix of literal content and conversion specifiers. for more details see: http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html","text")
		
		)>
    
	<cffunction name="getClass" returntype="string" output="false">
    	<cfreturn left(getConfigSettings().log4j.version,1)==1?"org.apache.log4j.PatternLayout":"org.apache.logging.log4j.core.layout.PatternLayout">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="false">
    	<cfreturn "Pattern">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "A flexible layout configurable with pattern string">
    </cffunction>
    
</cfcomponent>