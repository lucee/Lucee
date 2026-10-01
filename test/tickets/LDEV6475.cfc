component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialize,array" {

	// objectSave( [ 1, "two", [ 3 ] ] ) written by Lucee 7.0.6.8-SNAPSHOT
	variables.LUCEE7_ARRAY = "rO0ABXNyABxsdWNlZS5ydW50aW1lLnR5cGUuQXJyYXlJbXBsqh/UFrRWceMCAAB4cgAjbHVjZWUucnVudGltZS50eXBlLndyYXAuTGlzdEFzQXJyYXnXG6zmWlcUdwIAAUwABGxpc3R0ABBMamF2YS91dGlsL0xpc3Q7eHIAJGx1Y2VlLnJ1bnRpbWUudHlwZS51dGlsLkFycmF5U3VwcG9ydL/8CqHAfIR5AgAAeHBzcgAmamF2YS51dGlsLkNvbGxlY3Rpb25zJFN5bmNocm9uaXplZExpc3SUY+/jg0QQfAIAAUwABGxpc3RxAH4AAnhyACxqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU3luY2hyb25pemVkQ29sbGVjdGlvbiph+E0JnJm1AwACTAABY3QAFkxqYXZhL3V0aWwvQ29sbGVjdGlvbjtMAAVtdXRleHQAEkxqYXZhL2xhbmcvT2JqZWN0O3hwc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAN3BAAAAANzcgAOamF2YS5sYW5nLkxvbmc7i+SQzI8j3wIAAUoABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAAAAAAAF0AAN0d29zcQB+AABzcQB+AAVzcQB+AAoAAAABdwQAAAABc3EAfgAMAAAAAAAAAAN4cQB+ABF4cQB+ABJ4cQB+AAl4cQB+AAs=";

	// serialVersionUID Java computes for lucee.runtime.type.wrap.ListAsArray on 6.2/7.0/7.1 (no explicit field)
	variables.LIST_AS_ARRAY_SUID = "-2946571425825745801"; // string, a long this size does not survive as a CFML number

	function run( testResults, testBox ) {
		describe( "LDEV-6475 Java serialisation of arrays across Lucee versions", function() {

			it( title="ListAsArray keeps the serialVersionUID used by Lucee 7", body=function( currentSpec ) {
				var osc = createObject( "java", "java.io.ObjectStreamClass" ).lookup( [].getClass().getSuperclass() );
				expect( osc.getName() ).toBe( "lucee.runtime.type.wrap.ListAsArray" );
				expect( toString( osc.getSerialVersionUID() ) ).toBe( variables.LIST_AS_ARRAY_SUID );
			});

			it( title="an array serialised by Lucee 7 can be deserialised", body=function( currentSpec ) {
				var arr = objectLoad( toBinary( variables.LUCEE7_ARRAY ) );
				expect( isArray( arr ) ).toBeTrue();
				expect( arr ).toBe( [ 1, "two", [ 3 ] ] );
			});

			it( title="array round-trips objectSave()/objectLoad()", body=function( currentSpec ) {
				var arr = [ 1, "two", [ 3 ], { a: 1 } ];
				expect( objectLoad( objectSave( arr ) ) ).toBe( arr );
			});

		});
	}
}
