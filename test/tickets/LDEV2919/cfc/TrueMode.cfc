component localmode="true" {
	function probe() { unscoped = 1; return { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }; }
}
