component {

	function init( string tag = "om" ) {
		this.tag = arguments.tag;
		return this;
	}

	function onMissingMethod( required string missingMethodName, required struct missingMethodArguments ) {
		return this.tag & ":" & arguments.missingMethodName;
	}

}
