component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2156");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2156", function() {
			it(title = "ReMatchNoCase() doesn't work as expected when handling with vast data", body = function( currentSpec ) {
				var renderedHTML = fileRead(variables.uri &"/test.cfm");
				var subRGX = "(\/(includes|files)\/)";
				var domainSubRGX = "(http|https):\/\/(dev|test|staging|www)\." & replaceNoCase("dev.testingsite.com", ".", "\.", "all");
				var rgxDomain = "(<(a|img)[[:blank:]].{0,}(href|src).{0,})(=(" & '"' & "|')" & domainSubRGX & subRGX & ")(.{0,})";
				var matches = ReMatchNoCase(regex:rgxDomain, string:renderedHTML,multiline:true);
				expect(matches).toBeTypeOf('Array');
			});

			it(title = "ReMatchNoCase() davoid regression of fix LDEV-1467", body = function( currentSpec ) {
					
				REMatchTest = ArrayLen( REMatch( '.',Chr( 10 ) ) );
				expect(REMatchTest).toBe(1);
				REFindTest = REFind( '.',Chr( 10 ) );
				expect(REFindTest).toBe(1);
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
