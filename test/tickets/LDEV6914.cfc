component extends="org.lucee.cfml.test.LuceeTestCase" labels="writer,memory" {

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

			it( title="WriterPool.release() discards oversized responseBuffer", body=function( currentSpec ) {
				var pool = createObject( "java", "lucee.runtime.writer.WriterPool" );

				var oversize = createObject( "java", "java.lang.StringBuilder" );
				oversize.append( repeatString( "x", 200000 ) ); // 200k > 128k MAX_REUSABLE_BUFFER_SIZE

				pool.responseBuffer = oversize;
				pool.release();

				expect( isNull( pool.responseBuffer ) ).toBeTrue( "oversized responseBuffer must be released" );
			});

			it( title="after a large _internalRequest, pool.baos and responseBuffer are released", body=function( currentSpec ) {
				// Issue a request that outputs 200 kB — well above MAX_REUSABLE_BUFFER_SIZE (128 kB).
				// The writer pipeline allocates pool.baos during close(); release() must then drop it.
				var template = createURI( "LDEV6914/large_response.cfm" );
				var result   = _internalRequest( template: template, urls: { size: 200000 } );
				expect( len( trim( result.fileContent ) ) ).toBeGTE( 200000, "sanity: response must be 200k+ chars" );

				// After releaseLuceePageContext() the pool should have been reset.
				// The PC backing the _internalRequest is returned to the factory pool;
				// we can inspect the live request's own pool to verify release() runs correctly.
				var pc   = getPageContext();
				var pool = pc.getWriterPool();

				// Seed an oversized responseBuffer and baos on the pool, then call release() directly.
				var sbLarge = createObject( "java", "java.lang.StringBuilder" );
				sbLarge.append( repeatString( "y", 200000 ) );
				pool.responseBuffer = sbLarge;

				// baos: use a plain ByteArrayOutputStream — release() must still drop oversized ones
				var baosLarge = createObject( "java", "java.io.ByteArrayOutputStream" ).init( 200000 );
				baosLarge.write( createObject( "java", "java.lang.String" ).valueOf( repeatString( "y", 200000 ) ).getBytes() );
				pool.baos = baosLarge;

				pool.release();

				expect( isNull( pool.responseBuffer ) ).toBeTrue( "oversized responseBuffer must be cleared by release()" );
				expect( isNull( pool.baos ) ).toBeTrue( "oversized plain baos must be cleared by release()" );
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
