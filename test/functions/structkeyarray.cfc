component extends = "org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {
		animals = {
			cow: "moo",
			pig: "oink",
			cat: "meow"
		};

		describe( title = "Test suite for structkeyarray", body = function() {

			it( title = 'Test case for structkeyarray in function',body = function( currentSpec ) {
				assertEquals("TRUE",isarray(structkeyarray(animals)));
				assertEquals("3",arraylen(structkeyarray(animals)));
				assertEquals("0",arrayfind(structkeyarray(animals),"cow"));
				assertEquals("2",arrayfindnocase(structkeyarray(animals),"COW"));
				assertEquals("FALSE",arrayindexexists(structkeyarray(animals),5));
				assertEquals("TRUE",arrayindexexists(structkeyarray(animals),3));
				assertEquals("FALSE",arrayisempty(structkeyarray(animals)));
				assertEquals("TRUE",arraycontainsnocase(structkeyarray(animals),"cat"));
				assertEquals("FALSE",arraycontains(structkeyarray(animals),"cat"));
			});

			it( title = 'Test case for structkeyarray in member-function',body = function( currentSpec ) {
				assertEquals("TRUE",isarray(animals.keyarray()));
				assertEquals("3",arraylen(animals.keyarray()));
				assertEquals("0",arrayfind(animals.keyarray(),"cat"));
				assertEquals("1",arrayfindnocase(animals.keyarray(),"CAT"));
				assertEquals("TRUE",arrayindexexists(animals.keyarray(),3));
				assertEquals("FALSE",arrayindexexists(animals.keyarray(),5));
				assertEquals("FALSE",arrayisempty(animals.keyarray()));
				assertEquals("TRUE",arraycontainsnocase(animals.keyarray(),"cat"));
				assertEquals("FALSE",arraycontains(animals.keyarray(),"cat"));
			});

		})
	}
}