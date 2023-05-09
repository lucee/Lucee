component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.original = getLocale();
	}

	function afterAll(){
		setLocale(variables.original);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1539", body=function() {
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

		describe( title="Test suite for LSparseDateTime()", body=function() { 
			cfloop( list=#Server.Coldfusion.SupportedLocales#, index="locale"){
				describe(title="test LSparseDateTime locale format: [#locale#]", body=function(){
					it( title="test LSparseDateTime round trip with locale: [#locale#], ",
							data={ locale=locale },
							body=function( data ) {
						var testDate = parseDateTime("{ts '2008-04-06 01:02:03'}");
						expect (testDate).toBeDate();
						setLocale(data.locale);
						expect( lsParseDateTime( lsDateTimeFormat(testDate) ) ).toBe( testDate, "testing locale [#locale#]" );
					});
				});
				
			}
		});
	}
}