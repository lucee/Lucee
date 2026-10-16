component extends="org.lucee.cfml.test.LuceeTestCase" labels="scopes" {

	function run( testResults, testBox ) {
		describe( "LDEV-6539 - unary ops on a scoped key don't bind local / arguments", function() {

			it( title="baseline: a plain function doesn't bind", body=function( currentSpec ) {
				var res = plain( 1 );
				expect( res.local ).toBeFalse();
				expect( res.arguments ).toBeFalse();
			});

			it( title="local.i++ in a for loop doesn't bind local", body=function( currentSpec ) {
				var res = localPostIncrement();
				expect( res.i ).toBe( 4 );
				expect( res.local ).toBeFalse();
			});

			it( title="++local.n, +=, -=, *=, /= don't bind local", body=function( currentSpec ) {
				var res = localOtherUnary();
				expect( res.n ).toBe( 2 );
				expect( res.local ).toBeFalse();
			});

			it( title="local.s &= x doesn't bind local", body=function( currentSpec ) {
				var res = localConcat();
				expect( res.s ).toBe( "xx" );
				expect( res.local ).toBeFalse();
			});

			it( title="arguments.n++ and arguments.n += 1 don't bind arguments", body=function( currentSpec ) {
				var res = argumentsUnary( 1 );
				expect( res.n ).toBe( 3 );
				expect( res.arguments ).toBeFalse();
			});

			it( title="a bare local reference still binds", body=function( currentSpec ) {
				expect( bareLocal() ).toBeTrue();
			});

			it( title="a closure defined in the function still binds local", body=function( currentSpec ) {
				expect( withClosure() ).toBeTrue();
			});

			it( title="local.i++ doesn't stop the local scope being pooled", body=function( currentSpec ) {
				var first = localIdentity();
				var second = localIdentity();
				expect( second ).toBe( first );
			});

			it( title="local.i++ outside a function still works", body=function( currentSpec ) {
				local.i = 1;
				local.i++;
				local.s = "x";
				local.s &= "x";
				expect( local.i ).toBe( 2 );
				expect( local.s ).toBe( "xx" );
			});

		});
	}

	// localScope() / argumentsScope() with no argument don't bind, so inspecting doesn't change the result.
	// The check has to be inline: inside a helper UDF, localScope() is the helper's own scope.

	private struct function plain( n ) {
		var x = arguments.n + 1;
		return {
			"local": getPageContext().localScope().isBind(),
			"arguments": getPageContext().argumentsScope().isBind()
		};
	}

	private struct function localPostIncrement() {
		for ( local.i = 1; local.i <= 3; local.i++ ) {
		}
		return {
			"i": local.i,
			"local": getPageContext().localScope().isBind()
		};
	}

	private struct function localOtherUnary() {
		local.n = 0;
		++local.n;
		local.n += 1;
		local.n -= 1;
		local.n *= 2;
		local.n /= 2;
		local.n++;
		return {
			"n": local.n,
			"local": getPageContext().localScope().isBind()
		};
	}

	private struct function localConcat() {
		local.s = "x";
		local.s &= "x";
		return {
			"s": local.s,
			"local": getPageContext().localScope().isBind()
		};
	}

	private struct function argumentsUnary( n ) {
		arguments.n++;
		arguments.n += 1;
		return {
			"n": arguments.n,
			"arguments": getPageContext().argumentsScope().isBind()
		};
	}

	private boolean function bareLocal() {
		local.i = 1;
		var ref = local;
		return getPageContext().localScope().isBind();
	}

	private boolean function withClosure() {
		local.i = 1;
		var c = function() {
			return 1;
		};
		return getPageContext().localScope().isBind();
	}

	private numeric function localIdentity() {
		for ( local.i = 1; local.i <= 3; local.i++ ) {
		}
		return createObject( "java", "java.lang.System" ).identityHashCode( getPageContext().localScope() );
	}

}
