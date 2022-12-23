component extends = "org.lucee.cfml.test.LuceeTestCase" labels="h2" {

	function beforeAll(){
		variables.dir="#getDirectoryFromPath(getCurrentTemplatePath())#h24320";
   				if(!directoryExists(variables.dir)) directoryCreate(variables.dir);

		application action="update" datasource = {
				class: 'org.h2.Driver'
			, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#variables.dir#;MODE=MySQL'
			, username: 'root'
			, password: "encrypted:bfc0a1c52d845676830d40bcbf6fedcf8801ab2dd37fdbc513ff3c9b9a3c77b3"
			, connectionLimit:2 // THIS IS IMPORTANT, regular datasource provided does not have it
			};
	}
	
	function afterALL(){
		if(directoryExists(variables.dir)){
			directoryDelete(variables.dir,true);
		}
	}

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4320", function() {
			it(title="testing connection limit", body=function( currentSpec ) {
				var names="";
				loop from=1 to=20 index="local.i" {
					var name="twdasfd"&i;
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
						hasError=true;
						break;
					}
				}
				expect(hasError).toBe(false);
			});
		});
	}
} 
