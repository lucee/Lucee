component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='LDEV-5459' , body=function(){

			// this test case can be removed in case the class get missing in the future
			it( title='missing object', skip=noOrm(), body=function() {
				var type = "org.hibernate.criterion.Restrictions";// must be a class with no empty constructor
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

				javaProxies.put(
					type,
					{
						type   : type,
						object : createObject( "java", type )
					}
				);
				var res = javaProxies.get( type );
				expect( isNull(res.object) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.object.getClass().getName() );

			});

			it( title='missing object deeper structure ConcurrentHashMap', body=function() {
				var type = "java.io.FileInputStream";// must be a class with no empty constructor
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

				javaProxies.put(
					type,
					{
						type   : type,
						object : createObject( "java", type )
					}
				);
				var res = javaProxies.get( type );
				expect( isNull(res.object) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.object.getClass().getName() );

			});

			it( title='missing object flat structure ConcurrentHashMap', body=function() {
				var type = "java.io.FileInputStream";// must be a class with no empty constructor
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

				javaProxies.put(
					type,createObject( "java", type )
				);
				var res = javaProxies.get( type );
				expect( isNull(res) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.getClass().getName() );

			});


			it( title='missing object deeper structure Struct', body=function() {
				var type = "java.io.FileInputStream";// must be a class with no empty constructor
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = {};

				javaProxies.put(
					type,
					{
						type   : type,
						object : createObject( "java", type )
					}
				);
				var res = javaProxies.get( type );
				expect( isNull(res.object) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.object.getClass().getName() );

			});

			it( title='missing object flat structure Struct', body=function() {
				var type = "java.io.FileInputStream"; // must be a class with no empty constructor
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = {};

				javaProxies.put(
					type,createObject( "java", type )
				);
				var res = javaProxies.get( type );
				expect( isNull(res) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.getClass().getName() );

			});
		});

	}

	private function noOrm() {
		return ( structCount( server.getTestService( "orm" ) ) eq 0 );
	}

}