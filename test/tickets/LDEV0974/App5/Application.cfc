<cfcomponent>
	<cfscript>
		this.name = "app5";
		this.serialization.structmetadata={firstname: {type:"string",name:"fname"},lastname:{name:"lname"}, id:{type:"string", name:"id"}};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
		</cfscript>
</cfcomponent>
