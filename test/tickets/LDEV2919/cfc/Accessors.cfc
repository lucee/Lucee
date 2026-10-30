component localmode="modern" accessors="true" {
	property name="name";
	function probe() { unscoped = 1; return { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }; }
}
