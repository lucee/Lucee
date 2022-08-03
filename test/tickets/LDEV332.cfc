component extends="org.lucee.cfml.test.LuceeTestCase"{
	function testLenClassic( required STRING arg ){
		test1 = Len( arg ) ;
		return test1;
	}
	function testLenMemberFunction( required STRING arg ){
		test2 = arg.Len();
		return test2;
	}

	function run( testResults , testBox ) {
		describe( title="Checking string member function with string and numeric values", body=function() {
			var numberAsString = "123";
			var number = 123;
			it( title='Checking Len of number, like Len(number)', body=function( currentSpec ) {
				result1 = Len( number );
				expect(result1).toBe('3');
			});
			it( title='Checking Len of numberAsString to member function, like numberAsString.Len()', body=function( currentSpec ) {
				result2 = numberAsString.Len();
				expect(result2).toBe('3');
			});
			it( title='Checking Len of number to member function, like number.Len()', body=function( currentSpec ) {
				result3 = "";
				try {
					result3 = number.Len();
				} catch( any e ) {
					result3 = e.Message;
				}
				expect(result3).toBe('3');
			});
			it( title='Checking numberAsString by using function, like testLenClassic(numberAsString)', body=function( currentSpec ) {
				result4 = testLenClassic( numberAsString );
				expect(result4).toBe('3');
			});
			it( title='Checking number by using function, like testLenClassic(number)', body=function( currentSpec ) {
				result5 = testLenClassic( number );
				expect(result5).toBe('3');
			});
			it( title='Checking numberAsString by using MemberFunction, like testLenMemberFunction(numberAsString)', body=function( currentSpec ) {
				result6 = testLenMemberFunction( numberAsString );
				expect(result6).toBe('3');
			});
			it( title='Checking number by using MemberFunction, like testLenMemberFunction(number)', body=function( currentSpec ) {
				result7 = "";
				try {
					result7 = testLenMemberFunction( number );
				} catch( any e ) {
					result7 = e.Message;
				}
				expect(result7).toBe('3');
			});
		});

		describe( title="Checking number member function with string and numeric values", body=function() {
			var num = 123.000;
			var numAsString = "123.000";
			var str = "test";
			it( title='Checking round of number, like round(number)', body=function( currentSpec ) {
				expect(round(num)).toBe(123);
			});
			it( title='Checking round of stringAsNumber, like round(stringAsNumber)', body=function( currentSpec ) {
				expect(round(numAsString)).toBe(123);
			});
			it( title='Checking round of number to MemberFunction, like number.round()', body=function( currentSpec ) {
				expect(num.round()).toBe(123);
			});
			it( title='Checking round of number numberAsString to MemberFunction, like numberAsString.round()', body=function( currentSpec ) {
				expect(numAsString.round()).toBe(123);
			});
			it( title='Checking round of number string to MemberFunction, like string.round()', body=function( currentSpec ) {
				try {
					hasError = false;
					result = str.round();
				}
				catch(any e){
					hasError = true;
				}
				expect(hasError).toBe(true);
			});
		});
	}
}