component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults , testBox ) {

        describe( 'LDEV-861' , function() {
            it( 'Able to invoke class via Java URL Class Loader' , function() {
				var urls = [];
				var file = createObject( "java", "java.io.File" ).init( '/tmp/' );
			    arrayAppend( urls, file.toURI().toURL() );
			    var threadProxy = createObject( "java", "java.lang.Thread" );
			    var appCL = threadProxy.currentThread().getContextClassLoader();
				var urlCLProxy = createObject( "java", "java.net.URLClassLoader" );
				var addURL = urlCLProxy.getClass().getDeclaredMethod( "addURL", this.__classes( "URL", 1, "java.net" ) );
				addUrl.setAccessible( true );
				for ( var newURL in urls.toArray() ) {
					addURL.invoke( appCL, [ newURL ] );
				}
                expect( addURL ).toBeInstanceOf( 'java.lang.reflect.Method' );
            });
        });
    }

    public any function __classes( string name, numeric n = 1, string prefix = "java.lang" ) {
	    var result = createObject( "java", "java.util.ArrayList" ).init();
	    var type = createObject( "java", prefix & "." & name ).getClass();
	    while ( n-- > 0 ) result.add( type );
	    var classType = createObject( "java", "java.lang.Class" );
	    var arrayType = createObject( "java", "java.lang.reflect.Array" );
	    var arrayInstance = arrayType.newInstance( classType.getClass(), result.size() );
	    return result.toArray( arrayInstance );
	}
}