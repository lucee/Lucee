component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-278", function() {
			it('Casting CF array to java character array',  function( currentSpec ) {
				sampleArray = ["H","e","l","l","o"," ","W","o","r","l","d"];
				castedArray = javaCast("char[]", sampleArray);
				expect(arrayLen(castedArray)).toBe(11);
			});

			it('Casting string to java character array',  function( currentSpec ) {
				try{
					sampleString = "Hello World";
					castedArray = javaCast("char[]", sampleString);
					expect(arrayLen(castedArray)).toBe(11);
				} catch( any e ){
					expect(e.Message).toBe("");
				}
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}