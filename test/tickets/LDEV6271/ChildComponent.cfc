component extends="SupertypeComponent" accessors="true" {

	property name="properties" type="struct";

	function init() {
		variables.properties = {};
		super.init();
		return this;
	}

}
