component accessors="true" {

	property name="a" default="1";
	property name="b" default="1";

	function init( string tag = "tagged-default" ) {
		this.tag = arguments.tag;
		return this;
	}

}
