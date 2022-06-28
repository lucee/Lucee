<cfcomponent>
	<cfscript>
		this.name = "app3";
		this.serialization.serializeQueryAs = "struct";

		public function onRequestStart() {
			setting requesttimeout=10;
		}
	</cfscript>
</cfcomponent>
