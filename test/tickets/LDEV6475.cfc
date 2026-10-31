component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialize" {

	// objectSave( [ 1, "two", [ 3 ] ] ) written by Lucee 7.0.6.8-SNAPSHOT
	variables.LUCEE7_ARRAY = "rO0ABXNyABxsdWNlZS5ydW50aW1lLnR5cGUuQXJyYXlJbXBsqh/UFrRWceMCAAB4cgAjbHVjZWUucnVudGltZS50eXBlLndyYXAuTGlzdEFzQXJyYXnXG6zmWlcUdwIAAUwABGxpc3R0ABBMamF2YS91dGlsL0xpc3Q7eHIAJGx1Y2VlLnJ1bnRpbWUudHlwZS51dGlsLkFycmF5U3VwcG9ydL/8CqHAfIR5AgAAeHBzcgAmamF2YS51dGlsLkNvbGxlY3Rpb25zJFN5bmNocm9uaXplZExpc3SUY+/jg0QQfAIAAUwABGxpc3RxAH4AAnhyACxqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU3luY2hyb25pemVkQ29sbGVjdGlvbiph+E0JnJm1AwACTAABY3QAFkxqYXZhL3V0aWwvQ29sbGVjdGlvbjtMAAVtdXRleHQAEkxqYXZhL2xhbmcvT2JqZWN0O3hwc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAN3BAAAAANzcgAOamF2YS5sYW5nLkxvbmc7i+SQzI8j3wIAAUoABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAAAAAAAF0AAN0d29zcQB+AABzcQB+AAVzcQB+AAoAAAABdwQAAAABc3EAfgAMAAAAAAAAAAN4cQB+ABF4cQB+ABJ4cQB+AAl4cQB+AAs=";

	// objectSave( [ 1, "two", [ 3 ], { four: 4 }, null, 6 ] ) written by Lucee 7.0.2.106, a synchronized array
	variables.LUCEE7_SYNC_ARRAY = "rO0ABXNyABxsdWNlZS5ydW50aW1lLnR5cGUuQXJyYXlJbXBsqh/UFrRWceMCAAB4cgAjbHVjZWUucnVudGltZS50eXBlLndyYXAuTGlzdEFzQXJyYXnXG6zmWlcUdwIAAUwABGxpc3R0ABBMamF2YS91dGlsL0xpc3Q7eHIAJGx1Y2VlLnJ1bnRpbWUudHlwZS51dGlsLkFycmF5U3VwcG9ydL/8CqHAfIR5AgAAeHBzcgAmamF2YS51dGlsLkNvbGxlY3Rpb25zJFN5bmNocm9uaXplZExpc3SUY+/jg0QQfAMAAUwABGxpc3RxAH4AAnhyACxqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU3luY2hyb25pemVkQ29sbGVjdGlvbiph+E0JnJm1AwACTAABY3QAFkxqYXZhL3V0aWwvQ29sbGVjdGlvbjtMAAVtdXRleHQAEkxqYXZhL2xhbmcvT2JqZWN0O3hwc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAZ3BAAAAAZzcgAOamF2YS5sYW5nLkxvbmc7i+SQzI8j3wIAAUoABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAAAAAAAF0AAN0d29zcQB+AABzcQB+AAVzcQB+AAoAAAABdwQAAAABc3EAfgAMAAAAAAAAAAN4cQB+ABF4cQB+ABJ4c3IAHWx1Y2VlLnJ1bnRpbWUudHlwZS5TdHJ1Y3RJbXBsE7sPJSFK5LkCAAJJAAR0eXBlTAADbWFwdAAPTGphdmEvdXRpbC9NYXA7eHIAJWx1Y2VlLnJ1bnRpbWUudHlwZS51dGlsLlN0cnVjdFN1cHBvcnRnKbKJGDmF4wIAAHhwAAAAAnNyAEBsdWNlZS5jb21tb25zLmNvbGxlY3Rpb24uY29uY3VycmVudC5Db25jdXJyZW50SGFzaE1hcE51bGxTdXBwb3J0ZJneEp2HKT0DAANJAAtzZWdtZW50TWFza0kADHNlZ21lbnRTaGlmdFsACHNlZ21lbnRzdABLW0xsdWNlZS9jb21tb25zL2NvbGxlY3Rpb24vY29uY3VycmVudC9Db25jdXJyZW50SGFzaE1hcE51bGxTdXBwb3J0JFNlZ21lbnQ7eHAAAAAAAAAAIHVyAEtbTGx1Y2VlLmNvbW1vbnMuY29sbGVjdGlvbi5jb25jdXJyZW50LkNvbmN1cnJlbnRIYXNoTWFwTnVsbFN1cHBvcnQkU2VnbWVudDtwkN7o9kof6gIAAHhwAAAAAXNyAEhsdWNlZS5jb21tb25zLmNvbGxlY3Rpb24uY29uY3VycmVudC5Db25jdXJyZW50SGFzaE1hcE51bGxTdXBwb3J0JFNlZ21lbnQfNkyQWJMpPQIAAUYACmxvYWRGYWN0b3J4cgAoamF2YS51dGlsLmNvbmN1cnJlbnQubG9ja3MuUmVlbnRyYW50TG9ja2ZVqCwsyGrrAgABTAAEc3luY3QAL0xqYXZhL3V0aWwvY29uY3VycmVudC9sb2Nrcy9SZWVudHJhbnRMb2NrJFN5bmM7eHBzcgA0amF2YS51dGlsLmNvbmN1cnJlbnQubG9ja3MuUmVlbnRyYW50TG9jayROb25mYWlyU3luY2WIMudTe78LAgAAeHIALWphdmEudXRpbC5jb25jdXJyZW50LmxvY2tzLlJlZW50cmFudExvY2skU3luY7geopSqRFp8AgAAeHIANWphdmEudXRpbC5jb25jdXJyZW50LmxvY2tzLkFic3RyYWN0UXVldWVkU3luY2hyb25pemVyZlWoQ3U/UuMCAAFJAAVzdGF0ZXhyADZqYXZhLnV0aWwuY29uY3VycmVudC5sb2Nrcy5BYnN0cmFjdE93bmFibGVTeW5jaHJvbml6ZXIz36+5rW1vqQIAAHhwAAAAAD9AAABzcgAabHVjZWUucnVudGltZS50eXBlLktleUltcGyE+b8vafRvZwwAAHhwdAAERk9VUnhzcQB+AAwAAAAAAAAABHBweHBzcQB+AAwAAAAAAAAABnhxAH4ACXhxAH4AC3g=";

	// objectSave( arrayNew( 1, "", false ) ) holding 1 and "two", written by Lucee 7.0.2.106
	variables.LUCEE7_UNSYNC_ARRAY = "rO0ABXNyABxsdWNlZS5ydW50aW1lLnR5cGUuQXJyYXlJbXBsqh/UFrRWceMCAAB4cgAjbHVjZWUucnVudGltZS50eXBlLndyYXAuTGlzdEFzQXJyYXnXG6zmWlcUdwIAAUwABGxpc3R0ABBMamF2YS91dGlsL0xpc3Q7eHIAJGx1Y2VlLnJ1bnRpbWUudHlwZS51dGlsLkFycmF5U3VwcG9ydL/8CqHAfIR5AgAAeHBzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAAAncEAAAAAnNyAA5qYXZhLmxhbmcuTG9uZzuL5JDMjyPfAgABSgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAAAAAAAAAXQAA3R3b3g=";

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

			it( title="a synchronized array with a null serialised by Lucee 7 keeps its values and stays synchronized", body=function( currentSpec ) {
				var arr = objectLoad( toBinary( variables.LUCEE7_SYNC_ARRAY ) );
				expect( arrayLen( arr ) ).toBe( 6 );
				expect( arr[ 1 ] ).toBe( 1 );
				expect( arr[ 2 ] ).toBe( "two" );
				expect( arr[ 3 ] ).toBe( [ 3 ] );
				expect( arr[ 4 ] ).toBe( { four: 4 } );
				expect( arrayIsDefined( arr, 5 ) ).toBeFalse();
				expect( arr[ 6 ] ).toBe( 6 );
				// Lucee 7 reported every array as unsynchronized, whatever its list
				expect( getMetadata( arr ).type ).toBe( "synchronized" );
			});

			it( title="an unsynchronized array serialised by Lucee 7 stays unsynchronized", body=function( currentSpec ) {
				var arr = objectLoad( toBinary( variables.LUCEE7_UNSYNC_ARRAY ) );
				expect( arr ).toBe( [ 1, "two" ] );
				expect( getMetadata( arr ).type ).toBe( "unsynchronized" );
			});

			it( title="array round-trips objectSave()/objectLoad()", body=function( currentSpec ) {
				var arr = [ 1, "two", [ 3 ], { a: 1 } ];
				expect( objectLoad( objectSave( arr ) ) ).toBe( arr );
			});

		});
	}
}
