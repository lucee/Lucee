component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function testReplaceNoCase(){
		var input = 'aaa bbb & İkra';
		var toReplace = 'aaa bbb & İkra';
		var newStr = "aaa bbb;İkra";

		expect( replaceNoCaseCustom(input, toReplace, newStr) ).toBe("aaa bbb;İkra"); // "aaa bbb;İkra"
		expect( replaceNoCase(input, toReplace, newStr) ).toBe( "aaa bbb;İkra" );  // aaa bbb & İkra
	}

	private function replaceNoCaseCustom(required string input, required string toReplace, required string newStr) {
		var offset = find( lcase( arguments.toReplace ), lcase( arguments.input ) );
		if ( offset == 0 ){
			return arguments.input;
		} else {
			if ( offset == 1 ){
			return arguments.newStr & mid( arguments.input, len( arguments.toReplace ) + 1 );
			} else {
				var s = left( arguments.input, offset - 1 ) & arguments.newStr;
				if ( ( len( arguments.toReplace ) + offset ) lt len( arguments.input ) ){
					s =s & mid( arguments.input, offset + len( arguments.toReplace ) ); 
				}
				return s;
			}
		}
	}

}