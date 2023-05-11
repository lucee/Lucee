component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" skip=true {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2930", function() {
			it(title="break should work inside a times loop LDEV-2930", body = function( currentSpec ) {
				var uri=createURI("LDEV2930/timesLoopBreak.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="continue should work inside a times loop LDEV-2931", body = function( currentSpec ) {
				var uri=createURI("LDEV2930/timesLoopContinue.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
