component extends = "org.lucee.cfml.test.LuceeTestCase" labels="struct" {
	function run( testResults , testBox ) {
		describe( title = "Test suite for structKeyTranslate", body = function() {
			var animals = {
				cow: {
					noise: "moo",
					size: "large"
				},
				"bird.noise": "chirp",
				"bird.size": "small"
			};
			it( title = 'Testcase for structKeyTranslate function',body = function( currentSpec ) {
				structKeyTranslate(animals);
				assertEquals('{"size":"small","noise":"chirp"}', serialize(animals.bird));
			});

			it( title = 'Test case for struct.KeyTranslate member function',body = function( currentSpec ) {
				animals.KeyTranslate();
				assertEquals('{"size":"small","noise":"chirp"}', serialize(animals.bird));
			}); 
		});
	}
}