component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ){
		describe( "bytecode testing", function(){

			it( "parsing is expontentially slower with larger cfs files", function(){
				loop list="1000,2500,5000,7500,10000" item="local.lines" {
					var a = [];
					loop times=#lines# {
						arrayAppend(a, "a=now()");
					}

					var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev4603-slow-parsing-#lines#", "cfs");
					try {
						fileWrite( f, arrayToList(a, ";#chr(10)#" ) );  // approx 1.5mb of crap cfml
						// systemOutput( f );
						var s = getTickCount();
						silent {
							cfinclude( template=listlast(f,"\/") ); // gets stuck in a loop here
						}
						systemOutput( "#lines# lines took: " & (getTickCount() -s) & "ms", true );

					} finally {
						if (FileExists( f ) )
							FileDelete( f )
					}
				}
			});

		} );
	}

}
