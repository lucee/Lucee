component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateTimeSpan()", body=function() {
			it(title="checking CreateTimeSpan() function", body = function( currentSpec ) {
				assertEquals("1.042372685185:","#CreateTimeSpan(1, 1, 1, 1)#:");
				assertEquals("12:30:00","#timeFormat(CreateTimeSpan(0,0,30,0),"hh:mm:ss")#");
				assertEquals("30.12.1899","#dateFormat(CreateTimeSpan(0,0,30,0),"dd.mm.yyyy")#");
			});
			it(title="toString() of a fractional timespan is stable across calls", body = function( currentSpec ) {
				var ts = createTimeSpan( 0, 1, 0, 0 );
				var first = toString( ts );
				expect( toString( ts ) ).toBe( first );
				expect( toString( ts ) ).toBe( toString( createTimeSpan( 0, 1, 0, 0 ) ) );
			});
			it(title="timespan survives objectSave() / objectLoad()", body = function( currentSpec ) {
				var ts = createTimeSpan( 0, 1, 0, 0 );
				var str = toString( ts );
				var loaded = objectLoad( objectSave( ts ) );
				expect( toString( loaded ) ).toBe( str );
				expect( loaded.getMillis() ).toBe( ts.getMillis() );
			});
		});
	}
}
