component {

	public string function getFoo() {
		return "getter-foo";
	}

	public function onMissingProperty( required string name ) {
		return "missing:" & arguments.name;
	}

}
