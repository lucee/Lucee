component localmode="modern" {
	function closureProbe() {
		var fn = function() { unscoped = 1; return { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }; };
		return fn();
	}
}
