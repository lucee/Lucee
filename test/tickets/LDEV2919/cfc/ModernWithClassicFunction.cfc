component localmode="modern" {
	function probeDefault() { unscoped = 1; return { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }; }
	function probeClassic() localmode="classic" { unscoped = 1; return { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }; }
}
