component {

	variables.self = this;
	variables.calls = 0;

	public function onMissingProperty( required string name ) {
		variables.calls++;
		if ( arguments.name == "viaThis" ) return this.neverDefined;
		if ( arguments.name == "viaSelf" ) return variables.self.neverDefined;
		if ( arguments.name == "viaBracket" ) return this[ "never" & "Defined" ];
		if ( arguments.name == "viaOther" ) return new Recursion().neverDefined;
		return "ok:" & arguments.name;
	}

	public numeric function getCalls() {
		return variables.calls;
	}

}
