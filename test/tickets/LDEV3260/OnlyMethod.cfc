component {

	public function onMissingMethod( required string missingMethodName, required struct missingMethodArguments ) {
		return "method:" & arguments.missingMethodName;
	}

}
