component {
	this.name="LDEV5094";

	public function onRequestStart() {
		setting requesttimeout=10;
	}

	public function onMissingFunction( functionName, functionArguments ){
		return "Function name: #arguments.functionName#. Arguments: #SerializeJson( arguments.functionArguments )#";
	}
}
