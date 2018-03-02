component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.orginal = getLocale();
	}

	function afterAll(){
		setLocale(variables.orginal);
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1539", function() {
			it(title="checking locales", body = function( currentSpec ) {
				cfloop( list = "#Server.Coldfusion.SupportedLocales#", index = "locale", delimiters = ","){
					try{
						setLocale(locale);
						var hasError =  "";
					} catch (any e){
						var hasError = e.message;
					}
					assertEquals("", hasError);
				}
			});
		});
	}
}