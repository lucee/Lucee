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

			it('Casting CF array to java string array',  function( currentSpec ) {
				sampleArray = ["H","e","l","l","o"];
				castedArray = javaCast("String[]", sampleArray);
				expect(arrayLen(castedArray)).toBe(5);
			});

			it('Casting CF array to java string array',  function( currentSpec ) {
				sampleArray = ["H","e","l","l","o"];
				castedArray = javaCast("String[]", sampleArray);
				expect(arrayLen(castedArray)).toBe(5);
			});

			it('Casting deep CF array to java string array',  function( currentSpec ) {
				sampleArray = [[["a","b"]]];
				castedArray = javaCast("String[][][]", sampleArray);
				expect(arrayLen(castedArray)).toBe(1);
				expect(arrayLen(castedArray[1])).toBe(1);
				expect(arrayLen(castedArray[1][1])).toBe(2);
				expect(getMetaData(castedArray).getName()).toBe('[[[Ljava.lang.String;');
			});

			it('Casting deep CF array to java BigDecimal array',  function( currentSpec ) {
				sampleArray = [[["1","1"]]];
				castedArray = javaCast("java.math.BigDecimal[][][]", sampleArray);
				expect(arrayLen(castedArray)).toBe(1);
				expect(arrayLen(castedArray[1])).toBe(1);
				expect(arrayLen(castedArray[1][1])).toBe(2);
				expect(getMetaData(castedArray).getName()).toBe('[[[Ljava.math.BigDecimal;');
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}