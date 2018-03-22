component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1751", function() {
			it( title='Checking string value into forLoop', body=function( currentSpec ) {
			    for (a in "1"){
					local.result = a ;
			    }
				expect(local.result).toBe('1');
			});

			it( title='Checking number value into forLoop', body=function( currentSpec ) {
				try{
					var foo = 1 ;
					for (a in foo ) {
						local.result = a ;
					} 
				}
				catch(any e){
					local.result = e.message ;
				}
				expect(local.result).toBe('1');
			});
		});
	}
}
