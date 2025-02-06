component extends="org.lucee.cfml.test.LuceeTestCase" { 

	function run( testResults, testBox ){ 
		describe( "tests for FileModeToSymbolic", function(){
			it( title="test converting masks to symbolic", body=function(){
				var stringModes = {
					644: "rw-r--r--",
					755: "rwxr-xr-x",
					777: "rwxrwxrwx",
					400: "r--------"
				};
				for (var mode in stringModes ){
					//systemOutput( "mode: #mode# " & FileModeToSymbolic(mode), true );
					expect( FileModeToSymbolic( mode ) ).toBe( stringModes[ mode ] );
				}
			});
		} );
	}

}
