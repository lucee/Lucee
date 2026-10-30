component accessors=true {

	property name="title" type="string" default="the-title";

	public function onMissingProperty( required string name ) {
		return "missing:" & arguments.name;
	}

}
