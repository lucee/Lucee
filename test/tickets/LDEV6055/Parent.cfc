component {

	public function publicMethod() {
		return "publicMethod";
	}

	public function publicMethodWithArgs( required string arg1, string arg2="" ) {
		return "publicMethodWithArgs: #arguments.arg1# #arguments.arg2#";
	}

}
