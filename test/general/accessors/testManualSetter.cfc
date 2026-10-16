component accessors="true" {
	property name="value" default="0";

	function setValue( required numeric val ) {
		variables.value = arguments.val * 2;
	}
}
