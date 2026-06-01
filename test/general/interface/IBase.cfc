component implements="IFace" {

	function init( string tag = "ibase" ) {
		this.tag = arguments.tag;
		return this;
	}

	function ifaceMethod() {
		return "ibase:" & this.tag;
	}

}
