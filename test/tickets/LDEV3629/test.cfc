<cfcomponent>
    <cffunction name="getCustomer" returntype="xml" access="remote">
        <cfsavecontent variable="returnBody">
            <cfoutput><result><customers><customer>test</customer></customers></result></cfoutput>
        </cfsavecontent>
        <cfreturn XmlParse(returnBody)>
    </cffunction>
</cfcomponent>