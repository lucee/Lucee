component extends="org.lucee.cfml.test.LuceeTestCase" labels="component" {

	function run( testResults, testBox ) {
		describe( "LDEV-6574 getComponentMetadata lists the sub components in a subComponents key", function() {

			it( "lists the sub components of a component that defines some, in declaration order", function() {
				var md = getComponentMetadata( "LDEV6574.WithSubs" );
				expect( md ).toHaveKey( "subComponents" );
				expect( isArray( md.subComponents ) ).toBeTrue();
				expect( md.subComponents ).toHaveLength( 2 );
				expect( md.subComponents[ 1 ].subname ).toBe( "First" );
				expect( md.subComponents[ 2 ].subname ).toBe( "Second" );
			});

			it( "each entry holds name, fullname and subname that match the sub component's own metadata", function() {
				var md = getComponentMetadata( "LDEV6574.WithSubs" );
				loop array="#md.subComponents#" item="local.entry" {
					expect( listSort( structKeyList( entry ), "textnocase" ) ).toBe( "fullname,name,subname" );
					expect( entry.name ).toBe( entry.fullname );
					expect( entry.fullname ).toInclude( "$" & entry.subname );

					var subMd = getComponentMetadata( entry.fullname );
					expect( subMd.name ).toBe( entry.name );
					expect( subMd.fullname ).toBe( entry.fullname );
					expect( subMd.subname ).toBe( entry.subname );
					expect( subMd.sub ).toBeTrue();
				}
			});

			it( "the listed full name can be used to create the sub component", function() {
				var md = getComponentMetadata( "LDEV6574.WithSubs" );
				var first = createObject( "component", md.subComponents[ 1 ].fullname );
				expect( first.firstFunction() ).toBe( "first" );
				var second = createObject( "component", md.subComponents[ 2 ].fullname );
				expect( second.secondFunction() ).toBe( "second" );
			});

			it( "works the same for an instance of the component", function() {
				var cfc = new LDEV6574.WithSubs();
				var md = getComponentMetadata( cfc );
				expect( md.subComponents ).toHaveLength( 2 );
				expect( getMetadata( cfc ).subComponents ).toHaveLength( 2 );
			});

			it( "a sub component does not list sub components itself", function() {
				var md = getComponentMetadata( "LDEV6574.WithSubs$First" );
				expect( md ).notToHaveKey( "subComponents" );
				expect( md.sub ).toBeTrue();
			});

			it( "a component without sub components has no subComponents key", function() {
				var md = getComponentMetadata( "LDEV6574.WithoutSubs" );
				expect( md ).notToHaveKey( "subComponents" );
			});

			it( "all existing keys are unchanged, subComponents is the only addition", function() {
				var withSubs = getComponentMetadata( "LDEV6574.WithSubs" );
				var withoutSubs = getComponentMetadata( "LDEV6574.WithoutSubs" );

				var keysWith = structKeyArray( withSubs );
				arrayDelete( keysWith, "subComponents" );
				arraySort( keysWith, "textnocase" );
				var keysWithout = structKeyArray( withoutSubs );
				arraySort( keysWithout, "textnocase" );
				expect( keysWith ).toBe( keysWithout );

				expect( withSubs.name ).toInclude( "LDEV6574.WithSubs" );
				expect( withSubs.name ).notToInclude( "$" );
				expect( withSubs.fullname ).toBe( withSubs.name );
				expect( withSubs.subname ).toBe( "" );
				expect( withSubs.sub ).toBeFalse();
				expect( withSubs.type ).toBe( "component" );
				expect( withSubs.hint ).toBe( withoutSubs.hint );
				expect( withSubs.path ).toInclude( "WithSubs.cfc" );
				expect( withSubs.persistent ).toBe( withoutSubs.persistent );
				expect( withSubs.accessors ).toBe( withoutSubs.accessors );

				var functionNames = [];
				loop array="#withSubs.functions#" item="local.f" {
					arrayAppend( functionNames, f.name );
				}
				arraySort( functionNames, "textnocase" );
				expect( functionNames ).toBe( [ "init", "mainFunction" ] );
			});

		});
	}
}
