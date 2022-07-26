<cfcomponent>
	<cfscript>
		this.name = "App1";
		this.serialization.serializeQueryAs = "column";
		this.customSerializer="custom.Serialize";
	
		public function onRequestStart() {
			setting requesttimeout=10;
		}
	
	</cfscript>
</cfcomponent>
