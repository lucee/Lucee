component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title='LDEV-5519 wrong constructor called - exact match should win over assignable', body=function() {

			it( title='JSONObject constructor with String should parse JSON, not treat as bean', body=function() {
				var jsonString = '{"name":"test"}';
				var helper = new component javasettings='{
					"maven": ["org.json:json:20240303"]
				}' {
					import org.json.JSONObject;
					function parse( jsonString ) {
						var jsonObj = new JSONObject( javacast( "String", jsonString.toString() ) );
						return jsonObj.getString( "name" );
					}
				};
				expect( helper.parse( jsonString ) ).toBe( "test" );
			});

			it( title='StringBuilder constructor with String vs CharSequence', body=function() {
				// StringBuilder has StringBuilder(String) and StringBuilder(CharSequence)
				// String should match StringBuilder(String) exactly
				var sb = createObject( "java", "java.lang.StringBuilder" ).init( "hello" );
				expect( sb.toString() ).toBe( "hello" );
			});

			it( title='BigDecimal constructor with String vs Object', body=function() {
				// BigDecimal(String) should be called, not some other constructor
				var bd = createObject( "java", "java.math.BigDecimal" ).init( "123.45" );
				expect( bd.toString() ).toBe( "123.45" );
			});

			it( title='HashMap constructor with Map argument', body=function() {
				// HashMap(Map) should work when passing a LinkedHashMap
				var source = createObject( "java", "java.util.LinkedHashMap" ).init();
				source.put( "key", "value" );
				var copy = createObject( "java", "java.util.HashMap" ).init( source );
				expect( copy.get( "key" ) ).toBe( "value" );
			});

			it( title='ArrayList constructor with Collection argument', body=function() {
				// ArrayList(Collection) should work when passing a LinkedList
				var source = createObject( "java", "java.util.LinkedList" ).init();
				source.add( "item1" );
				source.add( "item2" );
				var copy = createObject( "java", "java.util.ArrayList" ).init( source );
				expect( copy.size() ).toBe( 2 );
				expect( copy.get( 0 ) ).toBe( "item1" );
			});

			it( title='Integer constructor with String', body=function() {
				// Integer(String) should parse the string
				var i = createObject( "java", "java.lang.Integer" ).init( "42" );
				expect( i.intValue() ).toBe( 42 );
			});

		});
	}

}
