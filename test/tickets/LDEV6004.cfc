component extends="org.lucee.cfml.test.LuceeTestCase" labels="property" {

	function run( testResults, testBox ) {
		describe( "LDEV-6004 Property.getDefault() should return null when no default set", function() {

			it( title="Property.getDefault() should return null when no default is set", body=function( currentSpec ) {
				var comp = new LDEV6004.TestEntity();
				var ComponentUtil = createObject( "java", "lucee.runtime.type.util.ComponentUtil" );
				var properties = ComponentUtil.getProperties( comp, false, false, false, false );
				for ( var prop in properties ) {
					if ( prop.getName() == "stringNoDefault" ) {
						expect( isNull( prop.getDefault() ) ).toBeTrue( "Property.getDefault() should return null, not empty string" );
						return;
					}
				}
				fail( "Could not find stringNoDefault property" );
			});

		});
	}

}
