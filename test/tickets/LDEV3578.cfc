component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		// all your suites go here.
		describe( "bytecode testing", function(){

			it( "test writing out huge 1.5mb file for bytecode memory usuage", function(){
                var a = [];
                loop times=100*1000 {
                    arrayAppend(a, "<cfset a=now()>");
                }

                var f=getTempFile(getTempDirectory(), "bytecode", "cfm");
                fileWrite( f, arrayToList(a, chr(10) ) );
                debug(getTempDirectory());
                debug( f );
                debug(fileExists( f ) );
                silent {
                    include template=#f#;
                }
			});

		} );
	}

}
