<cfcomponent>
	<cfscript>
		this.name = "App1";
		this.serialization.serializeQueryAs = "column";
		this.customSerializer="custom.Serialize";
	</cfscript>
</cfcomponent>
