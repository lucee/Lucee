// Non-literal hint — Lucee substitutes "[runtime expression]" rather than evaluating it.
component hint="#now()#" {
	function test() {}
}
