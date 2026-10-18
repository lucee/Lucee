component extends = "org.lucee.cfml.test.LuceeTestCase" labels="datetime" {
	function run( testResults, testBox ){
		describe( "Test for LDEV-5173", function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("IST");
			});
			afterEach( function(){
				setTimeZone(variables.startingTZ?:"UTC");
			});
			it( title="round trip DateTimeFormat/ParseDateTime with format 'epoch'", body=function( currentSpec ) {
				var epoch = DateTimeFormat( datetime="2024/12/11 16:19:54", mask="epoch" );
				expect( toString( parseDateTime( date=epoch, format="epoch" ) ) ).toBe( "{ts '2024-12-11 16:19:54'}" );
			});

			it( title="round trip DateTimeFormat/ParseDateTime with format 'epochms'", body=function( currentSpec ) {
				var epochms = DateTimeFormat( datetime="2024/12/11 16:19:54", mask="epochms" );
				expect( toString( parseDateTime( date=epochms, format="epochms" ) ) ).toBe( "{ts '2024-12-11 16:19:54'}" );
			});

			it( title="epochms keeps the milliseconds", body=function( currentSpec ) {
				expect( parseDateTime( date="1733914194123", format="epochms" ).getTime() ).toBe( 1733914194123 );
			});

			it( title="an epoch is an absolute instant, independent of the timezone", body=function( currentSpec ) {
				expect( parseDateTime( date=0, format="epoch" ).getTime() ).toBe( 0 );
				setTimeZone( "UTC" );
				expect( toString( parseDateTime( date=0, format="epoch" ) ) ).toBe( "{ts '1970-01-01 00:00:00'}" );
				expect( toString( parseDateTime( date=86400, format="EPOCH" ) ) ).toBe( "{ts '1970-01-02 00:00:00'}" );
			});

			it( title="a value that is no number throws", body=function( currentSpec ) {
				expect( function(){
					parseDateTime( date="not a number", format="epoch" );
				}).toThrow();
			});
		} );
	}
}
