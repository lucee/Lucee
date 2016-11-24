component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( 'Test suite for LDEV-1084 ( cfmljure )' , function() {
			it( 'Able to invoke class via Java URL Class Loader' , function() {
				var addURL = invokeClasses();
				expect( addURL ).toBeInstanceOf( 'java.lang.reflect.Method' );
			});

			it( 'Able to load clojure via Java Class Loader' , function() {
				hasError1 = false;
				hasError2 = false;
				try {
					var clj6 = loadClasses("clojure.java.api.Clojure");
					this._clj_var  = clj6.getMethod( "var", __classes( "Object", 2 ) );
					this._clj_read = clj6.getMethod( "read", __classes( "String" ) );
				} catch ( any e ) {
					hasError1 = true;
					try {
						var clj5 = loadClasses("clojure.lang.RT");
						this._clj_var  = clj5.getMethod( "var", __classes( "String", 2 ) );
						this._clj_read = clj5.getMethod( "readString", __classes( "String" ) );
					} catch ( any e ) {
						hasError2 = true;
					}
				}finally{
					if( !hasError1 && !hasError2 ){
						// if clojure 1.6 or later found
						expect( clj6 ).toBeInstanceOf( 'clojure.java.api.Clojure' );
					}else if( hasError1 && !hasError2 ){
						// if clojure 1.5 or earlier found
						expect( clj5 ).toBeInstanceOf( 'clojure.lang.RT' );
					}else{
						// if clojure version ( atleast one must be false )
						expect( hasError1 && hasError2 ).toBeFalse();
					}

				}
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

	// Private functions
	private any function invokeClasses(){
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
		return addURL;
	}

	private any function loadClasses(className){
		var urls = [];
		var file = createObject( "java", "java.io.File" ).init( '/tmp/' );
		arrayAppend( urls, file.toURI().toURL() );
		var threadProxy = createObject( "java", "java.lang.Thread" );
		var appCL = threadProxy.currentThread().getContextClassLoader();
		var urlCLProxy = createObject( "java", "java.net.URLClassLoader" );
		var addURL = urlCLProxy.getClass().getDeclaredMethod( "addURL", this.__classes( "URL", 1, "java.net" ) );
		addUrl.setAccessible( true );

		var cljObj = appCL.loadClass( className );
		return cljObj;
	}
}