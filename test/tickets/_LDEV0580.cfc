component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-580", function() {
			it(title="CFThread loses session in requests launched via cfhttp", body = function( currentSpec ) {
				var uri=createURI("LDEV0580/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms: {scene=1}
				);
				expect(result.filecontent.trim()).toBe(0);
			});

			it(title="CFThread loses session in requests launched via cfschdeule", body = function( currentSpec ) {
				var uri=createURI("LDEV0580/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms: {scene=2}
				);
				expect(result.filecontent.trim()).toBe(0);
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
