component extends = "org.lucee.cfml.test.LuceeTestCase" label="json" {

	function run( testResults, testBox ){
		describe( "LDEV-5069", function(){

			it( "isJson allows json", function(){
				var zoneId      = createObject( "java", "java.time.ZoneId" );
				var chronoField = createObject( "java", "java.time.temporal.ChronoField" );
				now().toInstant().atZone( zoneId.of( "US/Central" ) ).toLocalDateTime().with( ChronoField.DAY_OF_WEEK, javacast( "long", 1 ) );
			});


		} );
	}
}