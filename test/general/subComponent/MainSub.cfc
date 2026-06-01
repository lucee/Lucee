component {

	function init( string tag = "main-default" ) {
		this.tag = arguments.tag;
		return this;
	}

	function whoami() {
		return "main:" & this.tag;
	}

}

component name="Sub" {

	function init( string tag = "sub-default" ) {
		this.tag = arguments.tag;
		return this;
	}

	function whoami() {
		return "sub:" & this.tag;
	}

}
