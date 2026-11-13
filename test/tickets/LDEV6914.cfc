component extends="org.lucee.cfml.test.LuceeTestCase" labels="writer" {

	function run( testResults, testBox ) {
		describe( "LDEV-6914 WriterPool buffer release", function() {

			it( title="WriterPool.release() resets small responseBuffer for reuse", body=function( currentSpec ) {
				var pool = createObject( "java", "lucee.runtime.writer.WriterPool" );

				var sb = createObject( "java", "java.lang.StringBuilder" ).init( 1000 );
				sb.append( "hello" );
				pool.responseBuffer = sb;

				pool.release();

				expect( isNull( pool.responseBuffer ) ).toBeFalse( "small responseBuffer must be retained" );
				expect( pool.responseBuffer.length() ).toBe( 0, "retained responseBuffer must be empty after release" );
			});

			it( title="WriterPool.release() discards oversized responseBuffer and baos", body=function( currentSpec ) {
				var pool = createObject( "java", "lucee.runtime.writer.WriterPool" );

				var oversize = createObject( "java", "java.lang.StringBuilder" );
				oversize.append( repeatString( "x", 200000 ) ); // 200k > 128k MAX_REUSABLE_BUFFER_SIZE
				pool.responseBuffer = oversize;

				var baosLarge = createObject( "java", "java.io.ByteArrayOutputStream" ).init( 200000 );
				baosLarge.write( createObject( "java", "java.lang.String" ).valueOf( repeatString( "y", 200000 ) ).getBytes() );
				pool.baos = baosLarge;

				pool.release();

				expect( isNull( pool.responseBuffer ) ).toBeTrue( "oversized responseBuffer must be released" );
				expect( isNull( pool.baos ) ).toBeTrue( "oversized baos must be released" );
			});

			it( title="close() does not keep a response larger than MAX_REUSABLE_BUFFER_SIZE in pool.baos", body=function( currentSpec ) {
				var key = "LDEV6914_" & replace( createUUID(), "-", "", "all" );
				try {
					var result = _internalRequest(
						template: createURI( "LDEV6914/large_response.cfm" ),
						urls: { size: 200000, key: key }
					);
					expect( len( trim( result.fileContent ) ) ).toBeGTE( 200000, "sanity: response must be 200k+ chars" );
					expect( structKeyExists( server, key ) ).toBeTrue( "template must record the pool state" );

					var state = server[ key ];
					expect( state.isChild ).toBeFalse( "sanity: internal request must use a pooled (non-child) PageContext" );
					if ( !state.baosIsNull ) {
						expect( state.baosSize ).toBe( 0, "pool.baos must not hold the 200k response after close()" );
					}
				}
				finally {
					structDelete( server, key );
				}
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
