component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,scopes" {

	function run( testResults, testBox ) {
		describe( "LDEV-6544 - a thread sharing the caller's local / arguments must not see them recycled", function() {

			// the callers return straight away, other() then runs on the same PageContext while the thread sleeps,
			// the scope pool is LIFO, so other() gets the caller's scope back if it was recycled

			it( title="function listener: local is not reused by the next call", body=function( currentSpec ) {
				var res = listenerLocal();
				var otherHash = otherLocal();
				threadJoin( res.thread );
				expect( cfthread[ res.thread ].seen ?: "undefined" ).toBe( "A" );
				expect( otherHash ).notToBe( res.hash );
			});

			it( title="function listener: arguments is not reused by the next call", body=function( currentSpec ) {
				var res = listenerArguments( "A" );
				var otherHash = otherArguments( "B" );
				threadJoin( res.thread );
				expect( cfthread[ res.thread ].seen ?: "undefined" ).toBe( "A" );
				expect( otherHash ).notToBe( res.hash );
			});

			it( title="function listener: the caller's local and arguments are bound", body=function( currentSpec ) {
				var res = listenerBind( 1 );
				threadJoin( res.thread );
				expect( res.local ).toBeTrue();
				expect( res.arguments ).toBeTrue();
			});

			it( title="cfthread separateScopes=false: local is not reused by the next call", body=function( currentSpec ) {
				var res = sharedScopesThread();
				var otherHash = otherLocal();
				threadJoin( res.thread );
				expect( cfthread[ res.thread ].seen ?: "undefined" ).toBe( "A" );
				expect( otherHash ).notToBe( res.hash );
			});

			it( title="control: cfthread with separate scopes doesn't bind the caller's local", body=function( currentSpec ) {
				var res = separateScopesThread();
				threadJoin( res.thread );
				expect( res.local ).toBeFalse();
			});

		});
	}

	// these must be plain UDFs, not closures: a closure binds its scopes, which hides the bug

	private numeric function slow() {
		sleep( 300 );
		return 1;
	}

	private numeric function scopeHash( scope ) {
		return createObject( "java", "java.lang.System" ).identityHashCode( arguments.scope );
	}

	private struct function listenerLocal() {
		local.marker = "A";
		var t = slow():function( result, error ) {
			thread.seen = marker;
		};
		return {
			"thread": t,
			"hash": scopeHash( getPageContext().localScope() )
		};
	}

	private struct function listenerArguments( marker ) {
		// unscoped: inside the listener closure, arguments is the closure's own scope
		var t = slow():function( result, error ) {
			thread.seen = marker;
		};
		return {
			"thread": t,
			"hash": scopeHash( getPageContext().argumentsScope() )
		};
	}

	private struct function listenerBind( n ) {
		var t = slow():function( result, error ) {
		};
		return {
			"thread": t,
			"local": getPageContext().localScope().isBind(),
			"arguments": getPageContext().argumentsScope().isBind()
		};
	}

	private struct function sharedScopesThread() {
		local.marker = "A";
		var t = "LDEV6544_" & createUUID();
		thread name=t action="run" separateScopes=false {
			sleep( 300 );
			thread.seen = local.marker;
		}
		return {
			"thread": t,
			"hash": scopeHash( getPageContext().localScope() )
		};
	}

	private struct function separateScopesThread() {
		local.marker = "A";
		var t = "LDEV6544_" & createUUID();
		thread name=t action="run" {
			thread.seen = 1;
		}
		return {
			"thread": t,
			"local": getPageContext().localScope().isBind()
		};
	}

	private numeric function otherLocal() {
		local.marker = "B";
		var hash = scopeHash( getPageContext().localScope() );
		sleep( 600 );
		return hash;
	}

	private numeric function otherArguments( marker ) {
		var hash = scopeHash( getPageContext().argumentsScope() );
		sleep( 600 );
		return hash;
	}

}
