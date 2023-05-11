component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
	function run( testResults, testBox ){
		describe( title="Test case for LDEV-3772", body=function() {
			it( title="StringEach() - Arithmetic operation with elements", body=function( currentSpec ) {
				data = 0;
				callback = function(c) {
					try {
						data = data+c; // Arithmetic operation
						return c;
					}
					catch(any e) {
						data = e.message;
					}
				}
				stringEach( "12ab", callback );
				assertEquals("294", "#data#");
			});
			it( title="StringEvery() - comparison operation with elements", body=function( currentSpec ) {
				callback = function(num) {
					return num > 10; // comparison operation
				}
				assertEquals(true, stringEvery( "123456789", callback ));
			});
			it( title="StringFilter() - comparison operation with elements", body=function( currentSpec ) {
				callback = function(chr) {
					 return chr > 53; // comparison operation
					}
					assertEquals('6789', stringFilter( "123456789", callback ));
			});
			it( title="StringMap() - Arithmetic operation with elements", body=function( currentSpec ) {
				closure = function(item) { 
					return item + 5; // Arithmetic operation
				}
				assertEquals('545556575859606162', stringMap( "123456789", closure ));
			});
			it( title="StringReduce() - checking element type", body=function( currentSpec ) {
				letters = "ab";
				closure = function(inp1,inp2){
					type = inp2.getClass().getName();
					return inp1 & inp2;
				}
				stringReduce(letters,closure,"z");
				assertEquals("java.lang.Character",type);
			});
			it( title="StringSome() - comparison operation with elements", body=function( currentSpec ) {
				callback = function(num) {
					return num > 10; // comparison operation
				}
				assertEquals(true, stringSome( "123456789", callback ));
			});
		});
	}
}
