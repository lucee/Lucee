component extends="org.lucee.cfml.test.LuceeTestCase" labels="interface" {

	function run( testResults, testBox ) {
		describe( title="LDEV-1812 covariant return types", body=function() {

			describe( "narrower return type is accepted", function() {

				it( title="interface function implemented with a sub component as return type", body=function( currentSpec ) {
					var f = new LDEV1812.DogFactory();
					var a = f.make();
					expect( a ).toBeInstanceOf( "test.tickets.LDEV1812.Dog" );
					expect( a ).toBeInstanceOf( "test.tickets.LDEV1812.Animal" );
					expect( a.sound() ).toBe( "Woof" );
				});

				it( title="return type 2 levels below the declared type", body=function( currentSpec ) {
					expect( new LDEV1812.PuppyFactory().make().sound() ).toBe( "Yip" );
				});

				it( title="same return type still works", body=function( currentSpec ) {
					expect( new LDEV1812.AnimalFactory().make().sound() ).toBe( "..." );
				});

				it( title="return type defined with the full path", body=function( currentSpec ) {
					expect( new LDEV1812.QualifiedFactory().make().sound() ).toBe( "Woof" );
				});

				it( title="return type is resolved relative to the declaring component", body=function( currentSpec ) {
					expect( new LDEV1812.sub.SubFactory().make().sound() ).toBe( "Woof" );
				});

				it( title="return type implementing the declared interface", body=function( currentSpec ) {
					expect( new LDEV1812.SquareFactory().make().area() ).toBe( "square" );
				});

				it( title="return type is an interface extending the declared interface", body=function( currentSpec ) {
					expect( new LDEV1812.ISquareFactory().make().area() ).toBe( "square" );
				});

				it( title="abstract function implemented with a sub component as return type", body=function( currentSpec ) {
					expect( new LDEV1812.AbstractImpl().make().sound() ).toBe( "Woof" );
				});

				it( title="function of a super component overridden with a sub component as return type", body=function( currentSpec ) {
					expect( new LDEV1812.OverrideFactory().make().sound() ).toBe( "Woof" );
				});

			});

			describe( "incompatible return type is rejected", function() {

				it( title="return type that is not a sub component", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.RockFactory();
					}).toThrow( type="application" );
				});

				it( title="return type that does not implement the declared interface", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.RockShapeFactory();
					}).toThrow( type="application" );
				});

				it( title="base type instead of a component", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.StringFactory();
					}).toThrow( type="application" );
				});

				it( title="unknown type", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.MissingTypeFactory();
					}).toThrow( type="application" );
				});

				it( title="wider return type than the interface", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.WideningFactory();
					}).toThrow( type="application" );
				});

				it( title="abstract function implemented with an unrelated component", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.RockAbstractImpl();
					}).toThrow( type="application" );
				});

				it( title="different base types", body=function( currentSpec ) {
					expect( function() {
						new LDEV1812.NumberImpl();
					}).toThrow( type="application" );
				});

			});

		});
	}

}
