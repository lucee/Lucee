component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {

		describe('LDEV-5822 test property access metadata',function(){

			it( 'property with no access attribute should not have access in metadata' , function() {
				var testComponent = new component {
					property name='testProp' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).toHaveKey( 'name' );
				expect( prop ).toHaveKey( 'inject' );
				expect( prop ).notToHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
			});

			it( 'property with access="public" should have access in metadata' , function() {
				var testComponent = new component {
					property name='testProp' access='public' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).toHaveKey( 'name' );
				expect( prop ).toHaveKey( 'inject' );
				expect( prop ).toHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
				expect( prop.access ).toBe( 'public' );
			});

			it( 'property with access="private" should have access in metadata' , function() {
				var testComponent = new component {
					property name='testProp' access='private' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).toHaveKey( 'name' );
				expect( prop ).toHaveKey( 'inject' );
				expect( prop ).toHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
				expect( prop.access ).toBe( 'private' );
			});

			it( 'property with access="remote" should have access in metadata' , function() {
				var testComponent = new component {
					property name='testProp' access='remote' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).toHaveKey( 'name' );
				expect( prop ).toHaveKey( 'inject' );
				expect( prop ).toHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
				expect( prop.access ).toBe( 'remote' );
			});

			it( 'property with access="package" should have access in metadata' , function() {
				var testComponent = new component {
					property name='testProp' access='package' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).toHaveKey( 'name' );
				expect( prop ).toHaveKey( 'inject' );
				expect( prop ).toHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
				expect( prop.access ).toBe( 'package' );
			});

			it( 'multiple properties with different access levels' , function() {
				var testComponent = new component {
					property name='prop1' inject='value1';
					property name='prop2' access='public' inject='value2';
					property name='prop3' access='private' inject='value3';
					property name='prop4' access='remote' inject='value4';
				};
				var metadata = GetMetadata(testComponent);
				var props = metadata.properties;

				expect( props ).toHaveLength( 4 );

				// Property 1 - no access attribute
				expect( props[1] ).notToHaveKey( 'access' );
				expect( props[1].name ).toBe( 'prop1' );

				// Property 2 - public access
				expect( props[2] ).toHaveKey( 'access' );
				expect( props[2].access ).toBe( 'public' );
				expect( props[2].name ).toBe( 'prop2' );

				// Property 3 - private access
				expect( props[3] ).toHaveKey( 'access' );
				expect( props[3].access ).toBe( 'private' );
				expect( props[3].name ).toBe( 'prop3' );

				// Property 4 - remote access
				expect( props[4] ).toHaveKey( 'access' );
				expect( props[4].access ).toBe( 'remote' );
				expect( props[4].name ).toBe( 'prop4' );
			});

			it( 'property with no access in public component should not inherit component access' , function() {
				var testComponent = new component access='public' {
					property name='testProp' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).notToHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
			});

			it( 'property with no access in private component should not inherit component access' , function() {
				var testComponent = new component access='private' {
					property name='testProp' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).notToHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
			});

			it( 'property with no access in remote component should not inherit component access' , function() {
				var testComponent = new component access='remote' {
					property name='testProp' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).notToHaveKey( 'access' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
			});

			it( 'property with explicit access overrides component access' , function() {
				var testComponent = new component access='remote' {
					property name='testProp' access='private' inject='someValue';
				};
				var metadata = GetMetadata(testComponent);
				var prop = metadata.properties[1];

				expect( prop ).toHaveKey( 'access' );
				expect( prop.access ).toBe( 'private' );
				expect( prop.name ).toBe( 'testProp' );
				expect( prop.inject ).toBe( 'someValue' );
			});

			it( 'mixed properties in component with access attribute' , function() {
				var testComponent = new component access='public' {
					property name='prop1' inject='value1';
					property name='prop2' access='remote' inject='value2';
					property name='prop3' inject='value3';
				};
				var metadata = GetMetadata(testComponent);
				var props = metadata.properties;

				expect( props ).toHaveLength( 3 );

				// Property 1 - no access attribute, should not inherit component access
				expect( props[1] ).notToHaveKey( 'access' );
				expect( props[1].name ).toBe( 'prop1' );

				// Property 2 - explicit remote access
				expect( props[2] ).toHaveKey( 'access' );
				expect( props[2].access ).toBe( 'remote' );
				expect( props[2].name ).toBe( 'prop2' );

				// Property 3 - no access attribute, should not inherit component access
				expect( props[3] ).notToHaveKey( 'access' );
				expect( props[3].name ).toBe( 'prop3' );
			});

		});
	}
}