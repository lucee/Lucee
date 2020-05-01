component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		animals = {
			cow: {
				noise: "moo",
				size: "large"
			},
			pig: {
				noise: "oink",
				size: "MEDIUM"
			},
			cat: {
				noise: "MEOW",
				Size: "small"
			},
			bat: {
				Noise:"kee",
				size:"SMALL"
			}
		};
		describe( title = "Test suite for structsort", body = function() {

			it( title = 'Checking with structsort()',body = function( currentSpec ) {
				assertEquals('["CAT","BAT","COW","PIG"]',serialize(structsort(animals,"text","asc","noise")));
				assertEquals('["PIG","COW","BAT","CAT"]',serialize(structsort(animals,"text","desc","noise")));

				assertEquals('["BAT","CAT","COW","PIG"]',serialize(structsort(animals,"textnocase","asc","noise")));
				assertEquals('["PIG","COW","CAT","BAT"]',serialize(structsort(animals,"textnocase","desc","noise")));

				assertEquals('["PIG","BAT","COW","CAT"]',serialize(structsort(animals,"text","asc","size")));
				assertEquals('["CAT","COW","BAT","PIG"]',serialize(structsort(animals,"text","desc","size")));
				
				assertEquals('["COW","PIG","CAT","BAT"]',serialize(structsort(animals,"textnocase","asc","size")));
				assertEquals('["CAT","BAT","PIG","COW"]',serialize(structsort(animals,"textnocase","desc","size")));
			});

			it( title = 'Checking with struct.sort() member function',body = function( currentSpec ) {
				assertEquals('["CAT","BAT","COW","PIG"]',serialize(animals.sort("text","asc","noise")));
				assertEquals('["PIG","COW","BAT","CAT"]',serialize(animals.sort("text","desc","noise")));

				assertEquals('["BAT","CAT","COW","PIG"]',serialize(animals.sort("textnocase","asc","noise")));
				assertEquals('["PIG","COW","CAT","BAT"]',serialize(animals.sort("textnocase","desc","noise")));

				assertEquals('["PIG","BAT","COW","CAT"]',serialize(animals.sort("text","asc","size")));
				assertEquals('["CAT","COW","BAT","PIG"]',serialize(animals.sort("text","desc","size")));

				assertEquals('["COW","PIG","CAT","BAT"]',serialize(animals.sort("textnocase","asc","size")));
				assertEquals('["CAT","BAT","PIG","COW"]',serialize(animals.sort("textnocase","desc","size")));
			});
		});

	}
}