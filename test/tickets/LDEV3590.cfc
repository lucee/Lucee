component extends = "org.lucee.cfml.test.LuceeTestCase" labels="application" {
	function testOnAppplicationStartVarExists(){
		var threadUnique = CreateUniqueId( );
		thread name="T1-#threadUnique#" action="run"{
			// sets an application var after a sleep(500) in onApplicationStart
			local.result = _InternalRequest(
				template: createURI("LDEV3590/test.cfm"),
				url: {
					first: true
				}
			);
		}
		thread name="T2-#threadUnique#" action="run"{
			sleep(10);
			// checks an application var is already set by onApplicationStart
			local.result = _InternalRequest(
				template: createURI("LDEV3590/test.cfm"),
				url: {
					second: true
				}
			); // returns true or false if the var exists
			variables.result = local.result;
		}
		thread action="join" name="T1-#threadUnique#,T2-#threadUnique#" timeout="5000";
		// stop the application
		local.result = _InternalRequest(
			template: createURI("LDEV3590/test.cfm"),
			url: {
				stop: true
			}
		);
		expect( trim( variables.result.filecontent ) ).toBe( "true" );
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}