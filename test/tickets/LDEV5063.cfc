component extends="org.lucee.cfml.test.LuceeTestCase" labels="java,proxy" {

	function run( testResults, testBox ) {
		describe( "LDEV-5063 - PhysicalClassLoader proxy class loading", function() {

			it( "can create Java proxy from component implementing interface", function() {
				// Create a component that implements a Java interface
				var testCFC = new LDEV5063.TestComponent();

				// This should work - creating a Java proxy from the component
				var proxy = createDynamicProxy( testCFC, [ "java.lang.Runnable" ] );

				expect( proxy ).toBeInstanceOf( "java.lang.Runnable" );
			});

			it( "can load proxy classes from PhysicalClassLoader", function() {
				// Create multiple proxies to test classloader behavior
				var testCFC1 = new LDEV5063.TestComponent();
				var testCFC2 = new LDEV5063.TestComponent();

				var proxy1 = createDynamicProxy( testCFC1, [ "java.lang.Runnable" ] );
				var proxy2 = createDynamicProxy( testCFC2, [ "java.lang.Runnable" ] );

				// Both should be valid instances
				expect( proxy1 ).toBeInstanceOf( "java.lang.Runnable" );
				expect( proxy2 ).toBeInstanceOf( "java.lang.Runnable" );

				// They should be different instances
				expect( proxy1 ).notToBe( proxy2 );
			});

			it( "proxy methods are callable", function() {
				var testCFC = new LDEV5063.TestComponent();
				var proxy = createDynamicProxy( testCFC, [ "java.lang.Runnable" ] );

				// Should be able to call the run() method without error
				proxy.run();

				expect( testCFC.getWasCalled() ).toBe( true );
			});
		});

		describe( "LDEV-5063 - Maven support with pagePoolClear", function() {

			it( "can reload inline component with Maven dependencies after pagePoolClear", function() {
				// First load - inline component with Maven deps
				var aws1 = new Component javasettings='{maven:["software.amazon.awssdk:auth:2.31.16", "software.amazon.awssdk:identity-spi:2.31.16"]}'{
					import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

					function test(){
						return AwsBasicCredentials::create( "test", "test" ).getClass().getName();
					}
				};
				var result1 = aws1.test();
				expect( result1 ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );

				// Clear the page pool
				pagePoolClear( true );

				// Second load - create same inline component again
				var aws2 = new Component javasettings='{maven:["software.amazon.awssdk:auth:2.31.16", "software.amazon.awssdk:identity-spi:2.31.16"]}'{
					import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

					function test(){
						return AwsBasicCredentials::create( "test", "test" ).getClass().getName();
					}
				};
				var result2 = aws2.test();
				expect( result2 ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );
			});

			it( "can reload multiple times with Maven dependencies", function() {
				// Load and clear 10 times to simulate real-world scenario
				for ( var i = 1; i <= 10; i++ ) {
					var aws = new Component javasettings='{maven:["software.amazon.awssdk:auth:2.31.16", "software.amazon.awssdk:identity-spi:2.31.16"]}'{
						import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

						function test(){
							return AwsBasicCredentials::create( "test", "test" ).getClass().getName();
						}
					};
					var result = aws.test();
					expect( result ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );

					if ( i < 10 ) {
						pagePoolClear( true );
					}
				}
			});

			it( "can reload template with Maven dependencies via _InternalRequest after pagePoolClear", function() {
				var uri = createURI( "/LDEV5063" );

				// First load - template with Maven dependencies
				var result1 = _InternalRequest( template="#uri#/maven-test.cfm" );
				expect( trim( result1.filecontent ) ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );

				// Clear the page pool
				pagePoolClear( true );

				// Second load - should still work after pagePoolClear
				var result2 = _InternalRequest( template="#uri#/maven-test.cfm" );
				expect( trim( result2.filecontent ) ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );
			});

			it( "can reload component with Maven dependencies via _InternalRequest after pagePoolClear", function() {
				var uri = createURI( "/LDEV5063" );

				// First load
				var result1 = _InternalRequest( template="#uri#/maven-component-test.cfm" );
				expect( trim( result1.filecontent ) ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );

				// Clear the page pool
				pagePoolClear( true );

				// Second load - should still work
				var result2 = _InternalRequest( template="#uri#/maven-component-test.cfm" );
				expect( trim( result2.filecontent ) ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );
			});

			it( "can reload template multiple times via _InternalRequest with Maven dependencies", function() {
				var uri = createURI( "/LDEV5063" );

				// Load and clear 10 times to simulate real-world scenario
				for ( var i = 1; i <= 10; i++ ) {
					var result = _InternalRequest( template="#uri#/maven-test.cfm" );
					expect( trim( result.filecontent ) ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );

					if ( i < 10 ) {
						pagePoolClear( true );
					}
				}
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/")#/";
		return baseURI & "" & calledName;
	}

}
