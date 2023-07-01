component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "bytecode testing", function(){

			it( "test writing out huge 1.5mb file for bytecode memory usuage", function(){
				var a = [];
				loop times=100*1000 {
					arrayAppend(a, "<cfset a=now()>");
				}

				var f=getTempFile(getTempDirectory(), "bytecode", "cfm");
				fileWrite( f, arrayToList(a, chr(10) ) );
				systemOutput( f );
				timer variable="local.compileExecutionTime" {
					silent {
						cfinclude(template="c:\temp\zac.cfm");
						cfinclude(template=f);
					}
				}
				systemOutput("compileExecutionTime: #compileExecutionTime#", true );
			});

		} );
	}

}
