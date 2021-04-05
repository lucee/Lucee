component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for getMetaData", body = function() {

			it( title = 'Checking regular array',body = function( currentSpec ) {
				var data=[1,2,3];
				var meta=data.getmetadata();

				assertEquals("datatype,dimensions,type",listSort(meta.keyList()));
				assertEquals("any",meta.datatype);
				assertEquals(1,meta.dimensions);
				assertEquals("unsynchronized",meta.type);
			});

			it( title = 'Checking 2 dim array',body = function( currentSpec ) {
				var data=arrayNew(2);
				var meta=data.getmetadata();

				assertEquals("datatype,dimensions,type",listSort(meta.keyList()));
				assertEquals("any",meta.datatype);
				assertEquals(2,meta.dimensions);
				assertEquals("unsynchronized",meta.type);
			});

			it( title = 'Checking typed array',body = function( currentSpec ) {
				var data=arrayNew(1,"string");
				var meta=data.getmetadata();

				assertEquals("datatype,dimensions,type",listSort(meta.keyList()));
				assertEquals("string",meta.datatype);
				assertEquals(1,meta.dimensions);
				assertEquals("unsynchronized",meta.type);
			});


			it( title = 'Checking regular struct',body = function( currentSpec ) {
				var data={a:1};
				var meta=data.getmetadata();

				assertEquals("ordered,type",listSort(meta.keyList()));
				assertEquals("unordered",meta.ordered);
				assertEquals("unsynchronized",meta.type);
			});

			it( title = 'Checking ordered struct',body = function( currentSpec ) {
				var data=[a:1];
				var meta=data.getmetadata();

				assertEquals("ordered,type",listSort(meta.keyList()));
				assertEquals("ordered",meta.ordered);
				assertEquals("ordered",meta.type);
			});

			it( title = 'Checking soft struct',body = function( currentSpec ) {
				var data=structNew("soft");
				var meta=data.getmetadata();

				assertEquals("ordered,type",listSort(meta.keyList()));
				assertEquals("unordered",meta.ordered);
				assertEquals("soft",meta.type);
			});

		});

	}
}