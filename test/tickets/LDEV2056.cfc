component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2056", body=function() {
			it( title='checking closure after using dynamic proxy function',body=function( currentSpec ) {
				var aNames = [ 
					new Base( "luis" ),
					new Base( "joe" ),
					new Base( "majano" ),
					new Base( "brad" )
				];

				var castType 			= "java.lang.Object[]";
				variables.Arrays 		= createObject( "java", "java.util.Arrays" );
				variables.Collectors 	= createObject( "java", "java.util.stream.Collectors" );
				variables.jStream 		= variables.Arrays.stream( javaCast( castType, aNames ) );

				// If we call a mapping process via dynamic proxy, it looses scope
				mapper( function( item ){
				return item.getMemento();
				} );

				var results = variables.jStream.collect( variables.Collectors.toList() );
				results.each( function( item ){
					assertEquals(true, (isStruct( item )));
				});
			});
		});
	}

	private function mapper( required fnc ){
		variables.jStream = variables.jStream.map(
			createDynamicProxy( 
				new LDEV2056.proxies.Function( arguments.fnc ), 
				[ "java.util.function.Function" ] 
			)
		);
	}
}
