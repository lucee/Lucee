component extends = "org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults, testBox ){
		describe( "bytecode testing", function() {
			loop list="100,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000" item="local.lines" {
				it( title="parsing is expontentially slower with larger cfs files single method [lines:#local.lines#]",data={"lines":local.lines}, body=function(data){
						var a = [];
						loop times=#data.lines# {
							arrayAppend(a, "a=now();counter++;");
						}

						var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev4603-slow-parsing-#data.lines#", "cfs");
						try {
							debug(arrayToList(a, "#chr(10)#" ));
							fileWrite( f, "counter=0;"&arrayToList(a, "#chr(10)#" ) );  // approx 1.5mb of crap cfml
							// systemOutput( f );
							var s = getTickCount();
							silent {
								cfinclude( template=listlast(f,"\/") ); // gets stuck in a loop here
							}
							expect( counter?:-1 ).toBe( data.lines );
							debug(counter);
							//debug( "#data.lines# lines took: " & (getTickCount() -s) & "ms" );

						} finally {
							if (FileExists( f ) )
								FileDelete( f )
						}
					
				});
			}

			loop list="1000,1500,1700" item="local.lines" {
				it( title="parsing is expontentially slower with larger cfs files multiple methods [lines:#local.lines#]",data={"lines":local.lines}, body=function(data){
						var a = [];
						loop from ="1" to=data.lines index="local.i" {
							arrayAppend(a, "function test#i#(){a=now();}");
						}

						var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev4603-slow-parsing-multi-#data.lines#", "cfs");
						try {
							fileWrite( f, arrayToList(a, "#chr(10)#" ) );  // approx 1.5mb of crap cfml
							debug(arrayToList(a, "#chr(10)#" ));
							// systemOutput( f );
							var s = getTickCount();
							silent {
								cfinclude( template=listlast(f,"\/") ); // gets stuck in a loop here
							}
							//debug( "multi #data.lines# lines took: " & (getTickCount() -s) & "ms" );

						}
						finally {
							if (FileExists( f ) )
								FileDelete( f )
						}
					
				});
			}

		} );
	}

}
