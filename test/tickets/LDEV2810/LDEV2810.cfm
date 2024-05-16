<cfoutput>
    <cftry>
        <cfxml variable="LOCAL.imageXml">
            <cfimage action="writeToBrowser" source="https://avatars1.githubusercontent.com/u/10973141?s=280&v=4'" format="png" />
        </cfxml>
        <cfoutput>#structKeyExists(LOCAL.imageXml.xmlRoot.xmlchildren[1].xmlchildren[1].xmlAttributes,"src")#</cfoutput>
        <cfcatch type="any">
            #cfcatch.message#
        </cfcatch>
    </cftry>
</cfoutput>