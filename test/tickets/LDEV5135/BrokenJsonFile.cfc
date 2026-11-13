component javasettings="/test/tickets/LDEV5135/broken.json" {

	function test() {
		return createObject( "java", "java.lang.StringBuilder" ).init( "never reached" ).toString();
	}

}
