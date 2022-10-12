component extends = "org.lucee.cfml.test.LuceeTestCase" labels="application" {

	function run( testResults , testBox ) {
		describe("testcase for LDEV-3613", function( currentSpec ){
			it(title="Checking applicationStop() with multiple requests", body=function( currentSpec ){
				threadNames = "";
				loop list="true,false,true,false", item="e" {
					var threadUnique ="i_#createUUID()#";
					var threadNames = listAppend(threadNames, threadUnique)
					thread name="#threadUnique#" action="run" e="#e#" {
						thread.result = _InternalRequest(
							template: createURI("LDEV3613/LDEV3613.cfm"),
							forms = {"sleep":e}
						).fileContent;
					}
				}

				thread action="join" name=threadNames;

				var results="";
				cfloop (struct=cfthread, index="local.name", item="local.threads") {
					results=listAppend(results,threads.result);
				}
				expect(results).toBe("success,success,success,success");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
} 