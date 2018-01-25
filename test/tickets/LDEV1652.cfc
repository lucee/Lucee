component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1652", body=function() {
			it( title='Checking parseDateTime(), with input as invalid dateFormat',body=function( currentSpec ) {
				try{
					var ts = "2018-01-04-23.40.56";
					var result = parseDateTime(ts, "yyyy-MM-dd-HH.nn.ss");
				} catch( any e ){
					var result = e.message;
				}
				expect(result).toBe("{ts '2018-01-04 23:40:56'}");
			});

			it( title='Checking parseDateTime(), with input as valid dateFormat',body=function( currentSpec ) {
				try{
					var ts = "2018-01-04-23:40.56";
					var result = parseDateTime(ts, "yyyy-MM-dd-HH:nn.ss");
				} catch( any e ){
					var result = e.message;
				}
				expect(result).toBe("{ts '2018-01-04 23:40:56'}");
			});
			it( title='Checking parseDateTime(), input without any spaces or punctuation',body=function( currentSpec ) {
				try{
					var ts = "20180112";
					var result = ParseDateTime(ts, "yyyyMMdd");
				} catch( any e ){
					var result = e.message;
				}
				expect(result).toBe("{ts '2018-01-12 00:00:00'}");
			});
		});
	}
}