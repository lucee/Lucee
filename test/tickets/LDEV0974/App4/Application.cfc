<cfcomponent>
	<cfscript>
		this.name = "app4";
		this.serialization.preservecaseforstructkey = true;
		// this.serialization.structmetadata={firstname: {type:"string",name:"fname"},lastname:{name:"lname"}};
		// this.customSerializer="CustomSerializer";

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	</cfscript>
</cfcomponent>
