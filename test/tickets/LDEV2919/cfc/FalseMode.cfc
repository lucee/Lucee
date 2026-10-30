component localmode="false" {
	function probe() { unscoped = 1; return { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }; }
}
