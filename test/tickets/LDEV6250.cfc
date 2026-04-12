component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="LDEV-6250 maven version starting with a letter", body=function() {

			it( title="mavenLoad with v-prefixed version", body=function( currentSpec ) {
				var l = len( mavenLoad( [
					"com.google.apis:google-api-services-pagespeedonline:v5-rev20240829-2.0.0"
				] ) );
				expect( l ).toBeGT( 0 );
			});

			it( title="javaSettings with v-prefixed version via createObject", body=function( currentSpec ) {
				var cmp = new component javaSettings='{"maven":["com.google.apis:google-api-services-pagespeedonline:v5-rev20240829-2.0.0"]}' {

					function getClassName() {
						return createObject( "java", "com.google.api.services.pagespeedonline.v5.model.Categories" ).getClass().getName();
					}
				};
				expect( cmp.getClassName() ).toBe( "com.google.api.services.pagespeedonline.v5.model.Categories" );
			});

			it( title="javaSettings with v-prefixed version via import", body=function( currentSpec ) {
				var cmp = new component javaSettings='{"maven":["com.google.apis:google-api-services-pagespeedonline:v5-rev20240829-2.0.0"]}' {
					import com.google.api.services.pagespeedonline.v5.model.Categories;

					function getClassName() {
						return new Categories().getClass().getName();
					}
				};
				expect( cmp.getClassName() ).toBe( "com.google.api.services.pagespeedonline.v5.model.Categories" );
			});

		});

		describe( title="LDEV-6250 invalid non existing maven version", body=function() {

			it( title="mavenLoad with non-existent version throws", body=function( currentSpec ) {
				expect( function() {
					mavenLoad( [
						"com.google.apis:google-api-services-pagespeedonline:v5-this-version-does-not-exist"
					] );
				} ).toThrow();
			});

		});
	}
}
