component extends = "org.lucee.cfml.test.LuceeTestCase" labels="h2" skip="false" {

	function beforeAll(){
		application action="update" datasource=server.getDatasource( 
			service="h2",
			dbFile=server._getTempDir( "h24320" ),
			options={ connectionLimit:2 }
		);
	}
	
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4320", function() {
			it(title="testing connection limit", body=function( currentSpec ) {
				var names="";
				loop from=1 to=20 index="local.i" {
					var name="LDEV4320-"&createUniqueID()&"-"&i;
					names=listAppend(names, name);
					thread name=name {
						query {
							echo("show tables");
						}
					}
				}
				thread action="join" name=names;
				var hasError=false;
				loop array=listToArray(names) item="local.k" {
					var v=cfthread[k];
					if(v.status!="completed"){
						systemOutput(v, true);
						hasError=true;
						break;
					}
				}
				expect(hasError).toBe(false);
			});
		});
	}
} 
