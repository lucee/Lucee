component extends="Basic" {

	public function onMissingProperty( required string name ) {
		return "override:" & arguments.name;
	}

}
