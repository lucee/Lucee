<cfcomponent>
	<cfscript>
		this.name = "app1";
		this.serialization.serializeQueryAs = "column";

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	</cfscript>
</cfcomponent>
