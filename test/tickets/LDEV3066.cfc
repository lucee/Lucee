component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV3066", function() {
			it(title = "test reReplace", body = function( currentSpec ) {
				var string = '---abc---asdasd';
				var regex = '---(.*)---(.*)';
				
				expect("abc").toBe(ReReplace( string, regex, "\1" ));
				expect("asdasd").toBe(ReReplace( string, regex, "\2" ));
				expect("").toBe(ReReplace( string, regex, "\3" ));

				writedump( ReFind( regex, string, 1, true ) );
			});


			it(title = "test reFind", body = function( currentSpec ) {
				var string = '---abc---asdasd';
				var regex = '---(.*)---(.*)';
				var res=ReFind( regex, string, 1, true );
				
				expect("15,3,6").toBe(arrayToList(res.len));
				expect("1,4,10").toBe(arrayToList(res.pos));
				expect("---abc---asdasd,abc,asdasd").toBe(arrayToList(res.match));
			});

			it(title = "test reFind", body = function( currentSpec ) {
				var string = '---abc---asdasd';
				var regex = '^---(.*)---(.*)$';
				var res=ReFind( regex, string, 1, true );
				
				expect("15,3,6").toBe(arrayToList(res.len));
				expect("1,4,10").toBe(arrayToList(res.pos));
				expect("---abc---asdasd,abc,asdasd").toBe(arrayToList(res.match));
			});

		});
	}
}