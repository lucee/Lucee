<cfcomponent extends ="RootProxy" hint="Sub Application">
    <cftry>
        <cfoutput>#this.test#</cfoutput>
        <cfcatch>
            <cfoutput>#cfcatch.message#</cfoutput>
        </cfcatch>
    </cftry>
</cfcomponent>