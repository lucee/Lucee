component extends="Grandparent" accessors="true" {

	property name="pprop" default="p-default";

	function chain() {
		return super.chain() & ":parent";
	}

}
