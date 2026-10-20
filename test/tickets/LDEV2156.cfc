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
				var rgxDomain = "(<(a|img)[ \t].{0,}(href|src).{0,})(=(" & '"' & "|')" & domainSubRGX & subRGX & ")(.{0,})";
				var matches = ReMatchNoCase(regex:rgxDomain, string:renderedHTML,multiline:true);
				expect(matches).toBeTypeOf('Array');
			});

			// perl engine semantics ("." matches a line break), the engine is provided by the Perl Regex extension (LDEV-6632)
			it(title = "ReMatchNoCase() davoid regression of fix LDEV-1467", skip=!perlAvailable(), body = function( currentSpec ) {
				var orig = getApplicationSettings().regex;
				application action="update" regex={ type: "perl" };
				try {
					var REMatchTest = ArrayLen( REMatch( '.',Chr( 10 ) ) );
					expect(REMatchTest).toBe(1);
					var REFindTest = REFind( '.',Chr( 10 ) );
					expect(REFindTest).toBe(1);
				}
				finally {
					application action="update" regex=orig;
				}
			});
		});
	}
	
	private boolean function perlAvailable() {
		var orig = getApplicationSettings().regex;
		try {
			application action="update" regex={ type: "perl" };
			reFind( "a", "a" );
			return true;
		}
		catch ( any e ) {
			return false;
		}
		finally {
			application action="update" regex=orig;
		}
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
