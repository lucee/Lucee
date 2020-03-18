component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV2767", function() {
			number = [34,55,656,1.8,-234342];
			it(title = "toString() function", body = function( currentSpec ) {
				expect(len(toString(int(number[1])))).toBe(2);
				expect(len(toString(int(number[2])))).toBe(2);
				expect(len(toString(int(number[3])))).toBe(3);
				expect(len(toString(int(number[4])))).toBe(1);
				expect(len(toString(int(number[5])))).toBe(7);
			});

			it(title = "toString() Member function", body = function( currentSpec ) {
				expect(len(int(number[1]).toString())).toBe(2);
				expect(len(int(number[2]).toString())).toBe(2);
				expect(len(int(number[3]).toString())).toBe(3);
				expect(len(int(number[4]).toString())).toBe(1);
				expect(len(int(number[5]).toString())).toBe(6);
			});

		});
	}

}