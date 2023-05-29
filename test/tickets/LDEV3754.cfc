component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp,mail" {

	// TODO ZAC once this is merged to 6.0, enable test and setup to use updated server.getTestService("smtp")
	// also fails due to https://luceeserver.atlassian.net/browse/LDEV-3431

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-3754", function() {
			it( title="send an email with debug enabled", skip=true, body = function( currentSpec ) {
				mail from="test@lucee.org" to="test@lucee.org" subject="test debug email" server="localhost" debug=true async=false {
					echo("dummy email with debug logging enabled");
				}
			});
		});
	}
}