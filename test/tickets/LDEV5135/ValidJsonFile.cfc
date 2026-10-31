component javasettings="/test/tickets/LDEV5135/valid.json" {

	function test() {
		return createObject( "java", "java.lang.StringBuilder" ).init( "ok" ).toString();
	}

}
