component extends="org.lucee.cfml.test.LuceeTestCase" {

	/*
	 * io.grpc:grpc-context:1.60.1 is a 295-byte stub jar (only META-INF/MANIFEST.MF, zero classes).
	 * Its only dependency is io.grpc:grpc-api:1.60.1 declared as <scope>runtime</scope>.
	 * All io.grpc.* classes live in grpc-api.jar.
	 *
	 * On broken 6.2 (SCOPE_COMPILE only), the runtime-scoped grpc-api is dropped:
	 *   mavenLoad returns 1 jar (the empty stub) — no io.grpc.* class is loadable.
	 *
	 * After the fix (SCOPES_FOR_RUNTIME), grpc-api is included:
	 *   mavenLoad returns >=2 jars and io.grpc.* classes resolve.
	 */
	public function run( testResults, testBox ) {
		describe( title="LDEV-6325 maven resolution includes runtime-scoped transitive deps", body=function() {

			it( title="mavenLoad pulls runtime-scoped transitive jar", body=function( currentSpec ) {
				var jars = mavenLoad( [ "io.grpc:grpc-context:1.60.1" ] );
				expect( arrayLen( jars ) ).toBeGT( 1 );

				var foundApi = false;
				for ( var jar in jars ) {
					if ( find( "grpc-api", jar ) ) {
						foundApi = true;
						break;
					}
				}
				expect( foundApi ).toBeTrue();
			});

			it( title="javaSettings.maven loads class from runtime-scoped transitive jar", body=function( currentSpec ) {
				var cmp = new component javaSettings='{"maven":["io.grpc:grpc-context:1.60.1"]}' {
					function getClassName() {
						return createObject( "java", "io.grpc.StatusRuntimeException" ).getClass().getName();
					}
				};
				expect( cmp.getClassName() ).toBe( "io.grpc.StatusRuntimeException" );
			});

		});
	}
}
