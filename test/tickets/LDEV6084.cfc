component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults, testBox ) {
		describe( "LDEV-6084 - IllegalAccessError when dynamic invocation wrapper not in target's ClassLoader", function() {

			it( "should invoke builder pattern on Maven library without IllegalAccessError", function() {

				// IllegalAccessError  is logged to tomcat system log / console, passes tho as it falls back to reflection

				var msal = new component javaSettings='{"maven":["com.microsoft.azure:msal4j:1.23.1"]}' {
					import com.microsoft.aad.msal4j.ClientCredentialFactory;
					import com.microsoft.aad.msal4j.ConfidentialClientApplication;

					function buildApp() {
						var clientCredential = ClientCredentialFactory::createFromSecret( "test-secret" );
						var app = ConfidentialClientApplication::builder( "test-client-id", clientCredential )
							.authority( "https://login.microsoftonline.com/test-tenant" )
							.build();
						return app;
					}

					function getAppClassName() {
						return buildApp().getClass().getName();
					}
				};

				var result = msal.getAppClassName();
				expect( result ).toBe( "com.microsoft.aad.msal4j.ConfidentialClientApplication" );
			});

		});
	}

}
