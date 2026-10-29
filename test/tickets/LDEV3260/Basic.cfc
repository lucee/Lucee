component {

	this.existing = "present";
	variables.hidden = "in-variables";

	public function onMissingProperty( required string name ) {
		return "missing:" & arguments.name;
	}

	public string function readVariables( required string name ) {
		return variables[ arguments.name ];
	}

	public string function readThis( required string name ) {
		return this[ arguments.name ];
	}

}
