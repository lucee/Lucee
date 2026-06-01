component {

	function init( string tag = "subject-default" ) {
		this.tag = arguments.tag;
		return this;
	}

	function whoAmI() {
		return this.tag;
	}

}
