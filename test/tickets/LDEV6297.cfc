component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6297: extension manifest 'maven:' field accepts JSON and gradle GAV-comma forms", function() {

			it( title="single GAV gradle string", body=function() {
				var result = parseManifest( "org.postgresql:postgresql:42.7.1" );
				expect( result.size() ).toBe( 1 );
				expect( result.get( 0 ).get( "groupId" ) ).toBe( "org.postgresql" );
				expect( result.get( 0 ).get( "artifactId" ) ).toBe( "postgresql" );
				expect( result.get( 0 ).get( "version" ) ).toBe( "42.7.1" );
			});

			it( title="multiple GAVs comma-separated", body=function() {
				var result = parseManifest( "org.postgresql:postgresql:42.7.1,org.apache.poi:poi:5.5.1" );
				expect( result.size() ).toBe( 2 );
				expect( result.get( 0 ).get( "groupId" ) ).toBe( "org.postgresql" );
				expect( result.get( 0 ).get( "version" ) ).toBe( "42.7.1" );
				expect( result.get( 1 ).get( "groupId" ) ).toBe( "org.apache.poi" );
				expect( result.get( 1 ).get( "artifactId" ) ).toBe( "poi" );
				expect( result.get( 1 ).get( "version" ) ).toBe( "5.5.1" );
			});

			it( title="JSON struct (single entry)", body=function() {
				var result = parseManifest( "{'groupId':'org.postgresql','artifactId':'postgresql','version':'42.7.1'}" );
				expect( result.size() ).toBe( 1 );
				expect( result.get( 0 ).get( "groupId" ) ).toBe( "org.postgresql" );
				expect( result.get( 0 ).get( "artifactId" ) ).toBe( "postgresql" );
				expect( result.get( 0 ).get( "version" ) ).toBe( "42.7.1" );
			});

			it( title="JSON array of structs", body=function() {
				var result = parseManifest( "[{'groupId':'org.postgresql','artifactId':'postgresql','version':'42.7.1'},{'groupId':'org.apache.poi','artifactId':'poi','version':'5.5.1'}]" );
				expect( result.size() ).toBe( 2 );
				expect( result.get( 0 ).get( "groupId" ) ).toBe( "org.postgresql" );
				expect( result.get( 1 ).get( "groupId" ) ).toBe( "org.apache.poi" );
				expect( result.get( 1 ).get( "version" ) ).toBe( "5.5.1" );
			});

			it( title="JSON array with short-key aliases (g/a/v)", body=function() {
				// short keys are passed through as-is; downstream MavenUtil.toGAVSO( map ) reads both forms
				var result = parseManifest( "[{'g':'org.postgresql','a':'postgresql','v':'42.7.1'}]" );
				expect( result.size() ).toBe( 1 );
				expect( result.get( 0 ).get( "g" ) ).toBe( "org.postgresql" );
				expect( result.get( 0 ).get( "a" ) ).toBe( "postgresql" );
				expect( result.get( 0 ).get( "v" ) ).toBe( "42.7.1" );
			});

			it( title="whitespace-padded GAV-comma form", body=function() {
				var result = parseManifest( "  org.postgresql:postgresql:42.7.1  ,  org.apache.poi:poi:5.5.1  " );
				expect( result.size() ).toBe( 2 );
				expect( result.get( 0 ).get( "groupId" ) ).toBe( "org.postgresql" );
				expect( result.get( 0 ).get( "version" ) ).toBe( "42.7.1" );
				expect( result.get( 1 ).get( "artifactId" ) ).toBe( "poi" );
			});

			it( title="GAV with scope token", body=function() {
				var result = parseManifest( "org.postgresql:postgresql:42.7.1:runtime" );
				expect( result.size() ).toBe( 1 );
				expect( result.get( 0 ).get( "groupId" ) ).toBe( "org.postgresql" );
				expect( result.get( 0 ).get( "version" ) ).toBe( "42.7.1" );
				expect( result.get( 0 ).get( "scope" ) ).toBe( "runtime" );
			});

			it( title="garbage value parses as neither, returns empty list", body=function() {
				// a single 'Could not parse maven manifest field' error is logged; mavens stays empty
				var result = parseManifest( "this is not a parseable manifest value" );
				expect( result.size() ).toBe( 0 );
			});

		});
	}

	private any function parseManifest( required string str ) {
		var classCls = createObject( "java", "java.lang.Class" );
		var rhExtClass = classCls.forName( "lucee.runtime.extension.RHExtension" );
		var logClass = classCls.forName( "lucee.commons.io.log.Log" );
		var stringClass = classCls.forName( "java.lang.String" );

		var method = rhExtClass.getDeclaredMethod( "toMavenSettings", classArray( [ logClass, stringClass ] ) );
		method.setAccessible( true );

		var log = getPageContext().getConfig().getLog( "application" );
		return method.invoke( nullValue(), [ log, arguments.str ] );
	}

	// build a real java.lang.Class[] array for getDeclaredMethod's param-types argument
	private any function classArray( required array classes ) {
		var classCls = createObject( "java", "java.lang.Class" );
		var arrayType = createObject( "java", "java.lang.reflect.Array" );
		var arr = arrayType.newInstance( classCls.getClass(), arrayLen( arguments.classes ) );
		for ( var i = 1; i <= arrayLen( arguments.classes ); i++ ) {
			arrayType.set( arr, i - 1, arguments.classes[ i ] );
		}
		return arr;
	}

}
