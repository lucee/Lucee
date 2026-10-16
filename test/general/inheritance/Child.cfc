component extends="Parent" accessors="true" {

	property name="cprop" default="c-default";

	function init( string tag = "child" ) {
		this.tag = arguments.tag;
		return this;
	}

	function chain() {
		return super.chain() & ":child";
	}

}
