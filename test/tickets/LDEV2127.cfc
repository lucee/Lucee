component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "bytecode testing", function(){

			it( "test writing out huge 1.5mb file for bytecode memory usuage", function(){
				var a = [];
				loop times=100*1000 {
					arrayAppend(a, "<cfset a=now()>");
				}
				
				var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev2127-bytecode", "cfm");
				try {
					fileWrite( f, arrayToList(a, chr(10) ) );
					systemOutput( f );
					timer variable="local.compileExecutionTime" {
						silent {
							cfinclude( template=listlast(f,"\/") ); // errors
						}
					}
					systemOutput("compileExecutionTime: #compileExecutionTime#", true );
				} finally {
					if (FileExists( f ) )
						FileDelete( f )
				}
			});

		} );
	}

}
