<cfcomponent>
	<cfscript>
		this.name = "app2";
		this.serialization.serializeQueryAs = "row";

		public function onRequestStart() {
			setting requesttimeout=10;
		}
	</cfscript>
</cfcomponent>
