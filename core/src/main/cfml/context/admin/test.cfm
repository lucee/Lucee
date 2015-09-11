<cffunction name="out" output="no" returntype="string">
	<cfargument name="namespace">
    <cfargument name="sct">
    
	<cfset var keys=StructKeyArray(sct)>
    <cfset ArraySort(keys,'textNocase')>
    <cfset var el="">
    <cfset var str="">
    
    <cfloop array="#keys#" index="key">
    	<cfset el=sct[key]>
        <cfif isStruct(el)>
        	<cfset str&=out(namespace&key& ".",el)>
        <cfelseif isArray(el)>
        	<xcfset out(namespace&key& ".",el)>
        <cfelseif isSimpleValue(el)>
            <cfset str&='	<custom key="#lCase(namespace)##lCase(key)#">#HTMLEditFormat(el)#</custom>
'>
		</cfif>
    </cfloop>
    <cfreturn str>
    
</cffunction>
<cfoutput>
<language key="#lCase(session.lucee_admin_lang)#">
#out('',stText)#
</language>
</cfoutput>
