<cfcomponent extends="Layout">
	
    <cfset fields=array(
		field("Location Info","locationinfo","no",true,"If it is set to ""no"" this means there will be no location information output by this layout. If the the option is set to ""yes"", then the file name and line number of the statement at the origin of the log statement will be output.","radio","yes,no")
		,field("Title","title","",true,"This option sets the document title of the generated HTML document","text")
		
		)>
    
	<cffunction name="getClass" returntype="string" output="false">
    	<cfreturn "org.apache.log4j.HTMLLayout">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="false">
    	<cfreturn "HTML">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Creates a HTML Table">
    </cffunction>
    
</cfcomponent>