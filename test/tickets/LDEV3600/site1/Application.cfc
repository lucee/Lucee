<cfcomponent extends ="RootProxy" hint="Sub Application">
    <cftry>
        <cfoutput>#this.test#</cfoutput>
        <cfcatch>
            <cfoutput>#cfcatch.message#</cfoutput>
        </cfcatch>
    </cftry>
<cfscript>
	public function onRequestStart() {
		setting requesttimeout=10;
	}
</cfscript>
</cfcomponent>