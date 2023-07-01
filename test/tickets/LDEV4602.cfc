component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "bytecode testing", function(){

			it( "stackoverflow with enormous cfml file", function(){
				var a = [];
				loop times=100000 {
					arrayAppend(a, "<cfset a=now()>");
				}
				
				var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev4602-stackoverflow", "cfm");
				try {
					fileWrite( f, arrayToList(a, chr(10) ) );  // approx 1.5mb of crap cfml
					systemOutput( f );
					silent {
						cfinclude( template=listlast(f,"\/") ); // errors
					}
				} finally {
					if (FileExists( f ) )
						FileDelete( f )
				}
			});

		} );
	}

}
