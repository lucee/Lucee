component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6942: duplicating a CatchBlock that references itself", function() {

			it( title="duplicate() of a CatchBlock with a self member does not throw StackOverflowError", body=function() {
				try {
					throw "boom";
				}
				catch ( any ex ) {
					var caught = ex;
				}
				caught.self = caught;
				var copy = duplicate( caught );
				expect( isStruct( copy ) ).toBeTrue();
				expect( copy.message ).toBe( "boom" );
				expect( isSame( copy.self, copy ) ).toBeTrue();
			});

			it( title="duplicate() of a CatchBlock with cause set to itself does not throw StackOverflowError", body=function() {
				try {
					throw "boom";
				}
				catch ( any ex ) {
					var caught = ex;
				}
				caught.cause = caught;
				var copy = duplicate( caught );
				expect( isStruct( copy ) ).toBeTrue();
				expect( isSame( copy.cause, copy ) ).toBeTrue();
			});

			it( title="duplicate() of a struct holding a self-referencing CatchBlock does not throw StackOverflowError", body=function() {
				try {
					throw "boom";
				}
				catch ( any ex ) {
					var caught = ex;
				}
				caught.self = caught;
				var copy = duplicate( { error: caught } );
				expect( isStruct( copy.error ) ).toBeTrue();
				expect( copy.error.message ).toBe( "boom" );
				expect( isSame( copy.error.self, copy.error ) ).toBeTrue();
			});

			it( title="an indirect cycle still copies", body=function() {
				try {
					throw "boom";
				}
				catch ( any ex ) {
					var caught = ex;
				}
				caught.holder = { inner: caught };
				var copy = duplicate( caught );
				expect( isStruct( copy ) ).toBeTrue();
				expect( isSame( copy.holder.inner, copy ) ).toBeTrue();
			});

		});
	}

	// TestBox toBe() stringifies both sides on failure, which StackOverflows on a cycle.
	private boolean function isSame( required any a, required any b ) {
		return createObject( "java", "java.lang.System" ).identityHashCode( arguments.a )
			== createObject( "java", "java.lang.System" ).identityHashCode( arguments.b );
	}

}
