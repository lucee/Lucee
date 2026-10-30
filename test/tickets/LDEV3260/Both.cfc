component {

	public function onMissingMethod( required string missingMethodName, required struct missingMethodArguments ) {
		return "method:" & arguments.missingMethodName;
	}

	public function onMissingProperty( required string name ) {
		return "property:" & arguments.name;
	}

}
