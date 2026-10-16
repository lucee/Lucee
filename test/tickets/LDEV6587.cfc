component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults, testBox ) {
		describe( "LDEV-6587 CFC returned where the Java method declares an interface", function() {

			it( title="createDynamicProxy, iterator() returns this, Java calls it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterSelf( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				expect( joiner().join( ",", p ) ).toBe( "a,b" );
			});

			it( title="createDynamicProxy, iterator() returns this, CFML calls it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterSelf( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				var r = p.iterator();
				expect( isInstanceOf( r, "java.util.Iterator" ) ).toBeTrue();
				expect( r.next() ).toBe( "a" );
			});

			it( title="implementsJava component passed to Java, iterator() returns this", body=function( currentSpec ) {
				expect( joiner().join( ",", new LDEV6587.ImplIterSelf( [ "a", "b" ] ) ) ).toBe( "a,b" );
			});

			it( title="javaCast to interface, iterator() returns this", body=function( currentSpec ) {
				var p = javaCast( "java.lang.Iterable", new LDEV6587.IterSelf( [ "a", "b" ] ) );
				expect( joiner().join( ",", p ) ).toBe( "a,b" );
			});

			it( title="no PageContext on the calling thread, iterator() returns this", body=function( currentSpec ) {
				var tlpc = createObject( "java", "lucee.runtime.engine.ThreadLocalPageContext" );
				var p = createDynamicProxy( new LDEV6587.IterSelf( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				var j = joiner();
				var outerPC = getPageContext();
				var joined = "";
				tlpc.release();
				try {
					joined = j.join( ",", p );
				}
				finally {
					tlpc.register( outerPC );
				}
				expect( joined ).toBe( "a,b" );
			});

			it( title="closure javaCast to Iterable returns a CFC", body=function( currentSpec ) {
				var iter = new LDEV6587.IterSelf( [ "a", "b" ] );
				var itb = javaCast( "java.lang.Iterable", function() {
					return iter;
				} );
				expect( joiner().join( ",", itb ) ).toBe( "a,b" );
			});

		});

		describe( "LDEV-6587 CFML array returned where the Java method declares a typed array", function() {

			it( title="Collection.toArray() returns a CFML array, Java calls it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.CollArray(), [ "java.util.Collection" ] );
				var l = createObject( "java", "java.util.ArrayList" ).init( p );
				expect( l.size() ).toBe( 2 );
				expect( l.get( 0 ) ).toBe( "a" );
			});

			it( title="Collection.toArray() returns a CFML array, CFML calls it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.CollArray(), [ "java.util.Collection" ] );
				var r = p.toArray();
				expect( arrayLen( r ) ).toBe( 2 );
				expect( r[ 2 ] ).toBe( "b" );
			});

			it( title="String[] from a CFML array of strings", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.KeyManager(), [ "javax.net.ssl.X509KeyManager" ] );
				var r = p.getClientAliases( "RSA", javaCast( "null", "" ) );
				expect( arrayLen( r ) ).toBe( 2 );
				expect( r[ 1 ] ).toBe( "alias1" );
			});

			it( title="Annotation[] from a CFML array of CFCs", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.AnnotatedCfc(), [ "java.lang.reflect.AnnotatedElement" ] );
				var r = p.getAnnotations();
				expect( arrayLen( r ) ).toBe( 1 );
				expect( isInstanceOf( r[ 1 ], "java.lang.annotation.Annotation" ) ).toBeTrue();
			});

			it( title="int[] and long[] from CFML arrays of numbers", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.BatchStatement(), [ "java.sql.Statement" ] );
				var ints = p.executeBatch();
				expect( ints.getClass().getName() ).toBe( "[I" );
				expect( arrayToList( ints ) ).toBe( "1,2,3" );
				var longs = p.executeLargeBatch();
				expect( longs.getClass().getName() ).toBe( "[J" );
				expect( arrayToList( longs ) ).toBe( "4,5" );
			});

			it( title="byte[] from a base64 string, converted as binary", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.KeyEncoded(), [ "java.security.Key" ] );
				var r = p.getEncoded();
				expect( isBinary( r ) ).toBeTrue();
				expect( toBase64( r ) ).toBe( "AQID" );
			});

		});

		describe( "LDEV-6587 CFML value returned where the Java method declares a boxed type", function() {

			it( title="Integer from a CFML number", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.SqlTypeNumber(), [ "java.sql.SQLType" ] );
				var r = p.getVendorTypeNumber();
				expect( r.getClass().getName() ).toBe( "java.lang.Integer" );
				expect( r ).toBe( 4 );
			});

			it( title="Integer from a numeric string", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.SqlTypeString(), [ "java.sql.SQLType" ] );
				var r = p.getVendorTypeNumber();
				expect( r.getClass().getName() ).toBe( "java.lang.Integer" );
				expect( r ).toBe( 4 );
			});

		});

		describe( "LDEV-6587 a return that can't convert fails with an actionable message", function() {

			it( title="non-numeric string where Integer is declared, CFML calls it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.SqlTypeBad(), [ "java.sql.SQLType" ] );
				var e = "";
				try {
					p.getVendorTypeNumber();
				}
				catch ( any ex ) {
					e = ex;
				}
				expect( e ).notToBe( "", "expected an exception" );
				// the whole message: no createDynamicProxy hint for a value that isn't a component
				expect( e.message ).toMatch( "^function \[getVendorTypeNumber\] of component \[[\w.]*LDEV6587\.SqlTypeBad\] returned \[string abc\], but the Java method \[getVendorTypeNumber\] declares return type \[java\.lang\.Integer\]$" );
				expect( e.cause.message ).toInclude( "abc" );
			});

			it( title="struct where Iterator is declared, Java calls it: the error crosses the Java frames with its cause", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterStruct( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				var j = joiner();
				var e = "";
				try {
					j.join( ",", p );
				}
				catch ( any ex ) {
					e = ex;
				}
				expect( e ).notToBe( "", "expected an exception" );
				expect( e.message ).toMatch( "^function \[iterator\] of component \[[\w.]*LDEV6587\.IterStruct\] returned \[struct\], but the Java method \[iterator\] declares return type \[java\.util\.Iterator\]$" );
				expect( e.cause.message ).toInclude( "java.util.Iterator" );
				// thrown inside String.join, so it reached the Java caller unchecked
				expect( e.stacktrace ).toInclude( "java.lang.String.join" );
			});

		});

		describe( "LDEV-6587 returns that already work stay unchanged", function() {

			it( title="CFC already wrapped with createDynamicProxy", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterWrapped( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				expect( joiner().join( ",", p ) ).toBe( "a,b" );
			});

			it( title="iterator() returns arr.iterator()", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterArrayIterator(), [ "java.lang.Iterable" ] );
				expect( joiner().join( ",", p ) ).toBe( "a,b" );
			});

			it( title="null returned, CFML calls it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterNull( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				expect( isNull( p.iterator() ) ).toBeTrue();
			});

			it( title="null returned, Java calls it and fails where it uses it", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.IterNull( [ "a", "b" ] ), [ "java.lang.Iterable" ] );
				var j = joiner();
				expect( function() {
					j.join( ",", p );
				} ).toThrow( type="java.lang.NullPointerException" );
			});

			it( title="Collection.toArray() returns a Java array", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.CollJavaArray(), [ "java.util.Collection" ] );
				var l = createObject( "java", "java.util.ArrayList" ).init( p );
				expect( l.size() ).toBe( 2 );
			});

			it( title="CFML array returned where List is declared", body=function( currentSpec ) {
				var p = createDynamicProxy( new LDEV6587.ListSub(), [ "java.util.List" ] );
				var r = p.subList( 1, 3 );
				expect( arrayLen( r ) ).toBe( 2 );
				expect( r[ 1 ] ).toBe( "b" );
			});

			it( title="closure javaCast to Iterable returns arr.iterator()", body=function( currentSpec ) {
				var itb = javaCast( "java.lang.Iterable", function() {
					return [ "a", "b" ].iterator();
				} );
				expect( joiner().join( ",", itb ) ).toBe( "a,b" );
			});

			it( title="closure javaCast to Iterable returns a wrapped CFC", body=function( currentSpec ) {
				var iter = new LDEV6587.IterSelf( [ "a", "b" ] );
				var itb = javaCast( "java.lang.Iterable", function() {
					return createDynamicProxy( iter, [ "java.util.Iterator" ] );
				} );
				expect( joiner().join( ",", itb ) ).toBe( "a,b" );
			});

		});
	}

	private function joiner() {
		return createObject( "java", "java.lang.String" );
	}

}
