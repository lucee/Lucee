component extends = "org.lucee.cfml.test.LuceeTestCase" labels="h2" {

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
					var name=createUniqueID()&"-"&i;
					names=listAppend(names, name);
					thread name=name {
						query {
							echo("show tables");
						}
					}
				}
				thread action="join" name=names;
				var hasError=false;
				loop struct=cfthread index="k" item="v" {
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
