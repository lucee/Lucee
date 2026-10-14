component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="LDEV-6354 OSGiUtil.toVersion tolerates a malformed qualifier instead of throwing", body=function() {

			it( title="returns the default for a qualifier containing a space (the S3 'GA copy' entry)", body=function( currentSpec ) {
				var OSGiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				var fallback = createObject( "java", "org.osgi.framework.Version" ).init( "9.9.9" );
				// must NOT throw IllegalArgumentException; this overload contractually returns the default
				var result = OSGiUtil.toVersion( "3.9.0.GA copy", fallback );
				expect( result.toString() ).toBe( "9.9.9" );
			});

			it( title="still parses a well-formed OSGi version + qualifier", body=function( currentSpec ) {
				var OSGiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				var fallback = createObject( "java", "org.osgi.framework.Version" ).init( "9.9.9" );
				var result = OSGiUtil.toVersion( "3.9.0.GA", fallback );
				expect( result.toString() ).toBe( "3.9.0.GA" );
			});

		});
	}
}
