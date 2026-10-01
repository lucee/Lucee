component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialize,struct" {

	function run( testResults, testBox ) {
		describe( "LDEV-6476 Java serialisation round-trip of struct types", function() {

			// note: "weak" is left out on purpose, it uses java.util.WeakHashMap and was never Java-serializable (fails on 7.0 too)
			loop list="normal,soft,linked,ordered,synchronized,casesensitive,ordered-casesensitive" item="local.type" {
				it( title="#type# struct survives objectSave()/objectLoad()", data={ type: type }, body=function( data ) {
					var sct = structNew( data.type );
					sct.a = 1;
					sct.b = "two";
					sct.c = [ 1, 2, 3 ];

					var restored = objectLoad( objectSave( sct ) );

					expect( isStruct( restored ) ).toBeTrue();
					expect( structCount( restored ) ).toBe( 3 );
					expect( restored.a ).toBe( 1 );
					expect( restored.b ).toBe( "two" );
					expect( restored.c ).toBe( [ 1, 2, 3 ] );
					expect( structGetMetadata( restored ).type ?: data.type ).toBe( structGetMetadata( sct ).type ?: data.type );
				});
			}

			it( title="soft struct survives java.io.ObjectOutputStream/ObjectInputStream", body=function() {
				var sct = structNew( "soft" );
				sct.key = "value";

				var bos = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
				var oos = createObject( "java", "java.io.ObjectOutputStream" ).init( bos );
				oos.writeObject( sct );
				oos.close();

				var restored = objectLoad( bos.toByteArray() );
				expect( restored.key ).toBe( "value" );
			});

		});
	}
}
