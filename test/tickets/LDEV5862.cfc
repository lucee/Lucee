component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title='LDEV-5862 Bad access to protected data in invokevirtual', body=function() {

			it( title='clone() on HashMap', body=function() {
				var map = createObject( "java", "java.util.HashMap" ).init();
				map.put( "key", "value" );
				var cloned = map.clone();
				expect( cloned.get( "key" ) ).toBe( "value" );
			});

			it( title='clone() on LinkedHashMap', body=function() {
				var map = createObject( "java", "java.util.LinkedHashMap" ).init();
				map.put( "key", "value" );
				var cloned = map.clone();
				expect( cloned.get( "key" ) ).toBe( "value" );
			});

			it( title='clone() on ArrayList', body=function() {
				var list = createObject( "java", "java.util.ArrayList" ).init();
				list.add( "item" );
				var cloned = list.clone();
				expect( cloned.get( 0 ) ).toBe( "item" );
			});

			it( title='clone() on SimpleDateFormat', body=function() {
				var sdf = createObject( "java", "java.text.SimpleDateFormat" ).init( "yyyy-MM-dd" );
				var cloned = sdf.clone();
				expect( cloned.toPattern() ).toBe( "yyyy-MM-dd" );
			});

			it( title='clone() on Date', body=function() {
				var d = createObject( "java", "java.util.Date" ).init();
				var cloned = d.clone();
				expect( cloned.getTime() ).toBe( d.getTime() );
			});

		});
	}
}