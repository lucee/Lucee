component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ){
		describe( "bytecode testing", function(){

			it( "endless loop with enormous cfs file", function(){
				var a = [];
				loop times=100000 {
					arrayAppend(a, "a=now()");
				}
				
				var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev4602-stackoverflow", "cfs");
				try {
					fileWrite( f, arrayToList(a, ";#chr(10)#" ) );  // approx 1.5mb of crap cfml
					systemOutput( f );
					silent {
						// remove cftimeout to test locally, it's an endless loop,not good for ci
						cftimeout( timespan=createTimeSpan(0, 0, 0, 5)){
							// cfinclude( template=listlast(f,"\/") ); // gets stuck in a loop here
						}
					}
				} finally {
					if (FileExists( f ) )
						FileDelete( f )
				}
			});

		} );
	}

}
