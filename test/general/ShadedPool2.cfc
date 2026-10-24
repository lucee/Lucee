component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi,datasource" {

	function beforeAll() {
		// OSGiUtil is a class of the core bundle
		variables.core = createObject( "java", "lucee.runtime.osgi.OSGiUtil" ).getClass().getClassLoader().getBundle();
	}

	private boolean function canLoad( required string className ) {
		try {
			variables.core.loadClass( arguments.className );
			return true;
		}
		catch ( any e ) {
			// only a class that does not exist is a "no", anything else is an error in the test
			if ( findNoCase( "ClassNotFoundException", e.type & " " & e.message & " " & e.stackTrace ) ) return false;
			rethrow;
		}
	}

	function run( testResults, testBox ) {
		describe( "commons-pool2 is shaded into the core", function() {

			it( title="the core has the commons-pool2 classes only under the relocated name", body=function( currentSpec ) {
				expect( canLoad( "lucee.shaded.pool2.impl.GenericObjectPool" ) ).toBeTrue();
				expect( canLoad( "lucee.shaded.pool2.BasePooledObjectFactory" ) ).toBeTrue();
				expect( canLoad( "org.apache.commons.pool2.impl.GenericObjectPool" ) ).toBeFalse();
				expect( canLoad( "org.apache.commons.pool2.BasePooledObjectFactory" ) ).toBeFalse();
			});

			it( title="the datasource pool of the core is based on the shaded commons-pool2", body=function( currentSpec ) {
				var pool = variables.core.loadClass( "lucee.runtime.config.DatasourceConnPool" );
				expect( pool.getSuperclass().getName() ).toBe( "lucee.shaded.pool2.impl.GenericObjectPool" );
				var factory = variables.core.loadClass( "lucee.runtime.db.DatasourceConnectionFactory" );
				expect( factory.getSuperclass().getName() ).toBe( "lucee.shaded.pool2.BasePooledObjectFactory" );
			});

			it( title="the core bundle contains no org/apache/commons/pool2 classes", body=function( currentSpec ) {
				var entries = variables.core.findEntries( "org/apache/commons/pool2", "*", true );
				expect( isNull( entries ) || !entries.hasMoreElements() ).toBeTrue();
				var shaded = variables.core.findEntries( "lucee/shaded/pool2", "*.class", true );
				var count = 0;
				while ( shaded.hasMoreElements() ) {
					shaded.nextElement();
					count++;
				}
				expect( count ).toBeGT( 20 );
			});

			it( title="the core does not require the commons-pool2 bundle anymore", body=function( currentSpec ) {
				var requires = variables.core.getHeaders().get( "Require-Bundle" ) ?: "";
				expect( requires ).notToInclude( "commons-pool2" );
				var imports = variables.core.getHeaders().get( "Import-Package" ) ?: "";
				expect( imports ).notToInclude( "org.apache.commons.pool2" );
			});

			it( title="the license and notice of commons-pool2 are in the core", body=function( currentSpec ) {
				var IOUtil = createObject( "java", "lucee.commons.io.IOUtil" );
				var entry = variables.core.getEntry( "META-INF/licenses/org.apache.commons.commons-pool2/LICENSE.txt" );
				expect( isNull( entry ) ).toBeFalse();
				expect( toString( IOUtil.toBytes( entry.openStream() ), "utf-8" ) ).toInclude( "Apache License" );
				entry = variables.core.getEntry( "META-INF/licenses/org.apache.commons.commons-pool2/NOTICE.txt" );
				expect( isNull( entry ) ).toBeFalse();
				expect( toString( IOUtil.toBytes( entry.openStream() ), "utf-8" ) ).toInclude( "Apache Commons Pool" );
			});

			it( title="a datasource connection is taken from the pool and given back", body=function( currentSpec ) {
				var ds = server.getDatasource( "h2", server._getTempDir( "shaded-pool2" ) );
				expect( structIsEmpty( ds ) ).toBeFalse();
				for ( var i = 1; i <= 3; i++ ) {
					query name="local.qry" datasource=ds {
						echo( "SELECT #i# AS nr" );
					}
					expect( qry.nr ).toBe( i );
				}
			});
		});
	}
}
