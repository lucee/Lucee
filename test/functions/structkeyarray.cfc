component extends = "org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {
		variables.animals = [
			cow: "moo",
			pig: "oink",
			cat: "meow"
		];

		describe( title = "Test suite for structkeyarray", body = function() {

			it( title = 'Test case for structkeyarray in function',body = function( currentSpec ) {
				assertEquals("TRUE",isarray(structkeyarray(animals)));
				assertEquals("3",arraylen(structkeyarray(animals)));
				assertEquals("0",arrayfind(structkeyarray(animals),"cow"));
				assertEquals("1",arrayfindnocase(structkeyarray(animals),"COW"));
				assertEquals("FALSE",arrayindexexists(structkeyarray(animals),5));
				assertEquals("TRUE",arrayindexexists(structkeyarray(animals),3));
				assertEquals("FALSE",arrayisempty(structkeyarray(animals)));
				assertEquals("TRUE",arraycontainsnocase(structkeyarray(animals),"cat")>0);
				assertEquals("FALSE",arraycontains(structkeyarray(animals),"cat")>0);
			});

			it( title = 'Test case for structkeyarray in member-function',body = function( currentSpec ) {
				assertEquals("TRUE",isarray(animals.keyarray()));
				assertEquals("3",arraylen(animals.keyarray()));
				assertEquals("0",arrayfind(animals.keyarray(),"cat"));
				assertEquals("3",arrayfindnocase(animals.keyarray(),"CAT"));
				assertEquals("TRUE",arrayindexexists(animals.keyarray(),3));
				assertEquals("FALSE",arrayindexexists(animals.keyarray(),5));
				assertEquals("FALSE",arrayisempty(animals.keyarray()));
				assertEquals("TRUE",arraycontainsnocase(animals.keyarray(),"cat")>0);
				assertEquals("FALSE",arraycontains(animals.keyarray(),"cat")>0);
			});

		})
	}
}