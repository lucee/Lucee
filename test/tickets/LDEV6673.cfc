component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {
	function run( testResults, testBox ) {
		describe( "LDEV-6673: reloading the logger config must not open a second appender per log", function() {
			it( "resetLoggers() keeps unchanged loggers instead of creating new ones", function() {
				var config = getPageContext().getConfig();
				var sys = createObject( "java", "java.lang.System" );
				var before = config.getLoggers().get( "application" );
				expect( isNull( before ) ).toBeFalse();
				config.resetLoggers();
				var after = config.getLoggers().get( "application" );
				expect( isNull( after ) ).toBeFalse();
				expect( sys.identityHashCode( after ) ).toBe( sys.identityHashCode( before ) );
			});
			it( "a logger created on demand is created once", function() {
				var config = getPageContext().getConfig();
				var sys = createObject( "java", "java.lang.System" );
				var name = "ldev6673_" & createUUID();
				var first = config.getLog( name, true );
				var second = config.getLog( name, true );
				expect( sys.identityHashCode( second ) ).toBe( sys.identityHashCode( first ) );
			});
		});
	}
}
