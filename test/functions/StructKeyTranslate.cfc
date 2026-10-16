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
				assertEquals("small", animals.bird.size);
				assertEquals("chirp", animals.bird.noise);
			});

			it( title = 'Test case for struct.KeyTranslate member function',body = function( currentSpec ) {
				animals.KeyTranslate();
				assertEquals("small", animals.bird.size);
				assertEquals("chirp", animals.bird.noise);
			});

			it( title = 'Test case for structKeyTranslate with ordered struct',body = function( currentSpec ) {
				// Ordered struct using [:] notation should maintain insertion order
				var orderedAnimals = [
					"cow": [
						"noise": "moo",
						"size": "large"
					],
					"bird.noise": "chirp",
					"bird.size": "small"
				];
				structKeyTranslate(orderedAnimals);
				assertEquals("chirp", orderedAnimals.bird.noise);
				assertEquals("small", orderedAnimals.bird.size);
			});
		});
	}
}