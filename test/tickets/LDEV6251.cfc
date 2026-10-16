component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="LDEV-6251 javaSettings maven download failure should not be silently swallowed", body=function() {

			xit( title="javaSettings with non-existent version should throw maven error not class not found", body=function( currentSpec ) {
				//expect( function() {
					// use unique version string to avoid .lastUpdated cache from LDEV6250 tests
					var cmp = new component javaSettings='{"maven":["com.google.apis:google-api-services-pagespeedonline:v5-LDEV6251-does-not-exist"]}' {
						import com.google.api.services.pagespeedonline.v5.model.Categories; // should throw here

						function getClassName() {
							return new Categories().getClass().getName(); // not here
						}
					};
					cmp.getClassName();
				//} ).toThrow();
			});

		});
	}
}
