component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "bytecode testing", function(){

			it( "endless loop with enormous cfs file", function(){
				var a = [];
				// 10000 takes 2s
				// 20000 takes 9s to 16s
				// 50000 takes 63s
				var times=20000;
				loop times=times {
					arrayAppend(a, "a=now()");
				}
				
				var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev4603-endlessloop-#times#-", "cfs");
				try {
					fileWrite( f, arrayToList(a, ";#chr(10)#" ) );  // approx 1.5mb of crap cfml
					systemOutput( f, true );
					systemOutput( fileInfo(f).size, true);
					silent {
						// remove cftimeout to test locally, it's an endless loop,not good for ci
						//cftimeout( timespan=createTimeSpan(0, 0, 0, 5)){
							 cfinclude( template=listlast(f,"\/") ); // gets stuck in a loop here
						//}
					}
					expect( a ).toBeDate();
				} finally {
					if (FileExists( f ) )
						FileDelete( f )
				}
			});

		} );
	}

}
