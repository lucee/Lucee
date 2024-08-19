component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) { 

		describe( title='LDEV-5010' , body=function(){

			it( title='check fileWrite charset exception makes sense' , body=function() {
				try {
					fileWrite(getTempFile(getTempDirectory(), "test"), "test", true);
				} catch ( e ){
					expect( e.message ).notToInclude( "Invalid file" ); // should be invalid encoding
				}
			});

			it( title='check fileAppend charset exception makes sense' , body=function() {
				try {
					fileAppend(getTempFile(getTempDirectory(), "test"), "test", true);
				} catch ( e ){
					expect( e.message ).notToInclude( "Invalid file" ); // should be invalid encoding
				}
				
			});


		});
	}
}