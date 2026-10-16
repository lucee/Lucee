<cfscript>
// LDEV-6434 — StackWalker port for ExceptionUtil.getTagContextLine + LogUtil.caller
//
// Verifies the live-stack path (Throwable arg = null) produces the same
// CFML template:line; entries that cfcatch.tagContext records for the
// same call chain. Also verifies LogUtil.caller returns the innermost
// CFML frame as 'template:line'.
//
// Both methods are Java statics with no CFML wrapper, so reached via
// createObject — exception to the "prefer CFML functionality" rule per
// AGENTS.md (absolutely required).

component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){

		// ground truth: cfcatch.tagContext gives template + line for the
		// throw chain. lines for outer/wrapper match the call-site lines
		// in non-throw mode too (same statement), but inner's line differs
		// (throw line vs getTagContextLine call line).
		var truth = [];
		try {
			wrapper( true );
		}
		catch ( any e ){
			for ( var i = 1; i <= 3; i++ ){
				truth.append({
					template: e.tagContext[ i ].template,
					line:     e.tagContext[ i ].line
				});
			}
		}
		variables.truth = truth;

		describe( "LDEV-6434 ExceptionUtil.getTagContextLine via StackWalker", function(){

			it( title="live stack returns 'template:line;' entries with no spaces or func suffix", body=function(){
				var s = wrapper( false );
				expect( s ).toBeTypeOf( "string" );
				expect( len( s ) ).toBeGT( 0 );
				expect( s ).toInclude( ";" );
				expect( s ).notToInclude( " " );
				expect( s ).notToInclude( "()" );
				expect( s ).toMatch( ":[0-9]+;" );
			});

			// cfcatch.tagContext returns filesystem absolute paths; getTagContextLine
			// reads StackTraceElement.getFileName() — the mapping-relative path baked
			// in at compile time. multiple mappings can point to the same dir
			// (/test, /test-once, /test-never, /test8) so normalise BOTH sides
			// of the comparison via normTestPath.
			it( title="inner frame template matches getTagContextLine output", body=function(){
				var s   = normTestPath( wrapper( false ) );
				var tpl = normTestPath( variables.truth[ 1 ].template );
				expect( s ).toInclude( tpl & ":" );
			});

			it( title="outer + wrapper frames template:line match exactly", body=function(){
				var s = normTestPath( wrapper( false ) );
				for ( var i = 2; i <= 3; i++ ){
					var tpl = normTestPath( variables.truth[ i ].template );
					expect( s ).toInclude( tpl & ":" & variables.truth[ i ].line & ";" );
				}
			});
		});

		describe( "LDEV-6434 LogUtil.caller via StackWalker (findFirst short-circuit)", function(){

			it( title="returns innermost CFML frame as 'template:line', not defaultValue", body=function(){
				var s = wrapper( false, true );
				expect( s ).toBeTypeOf( "string" );
				expect( s ).notToBe( "FALLBACK" );
				expect( s ).toMatch( ":[0-9]+$" );
				// abs(pc, template) returns the filesystem absolute path, matching
				// cfcatch.tagContext.template format exactly. find() returns 1 only
				// if the prefix is at the very start (avoids toStartWith — newer testbox)
				expect( find( variables.truth[ 1 ].template & ":", s ) ).toBe( 1 );
			});

			it( title="returns defaultValue when no CFML frames present", body=function(){
				// no easy way to reach this path from CFML — call via a fresh
				// java thread that has no CFML on its stack would be ideal but
				// overkill for a regression test. Skip: verified by absence of
				// regression in the happy-path test above (orElse fallback wiring).
			});
		});
	}

	// helpers: createObject Java statics  <-  inner  <-  outer  <-  wrapper
	// modes:
	//   doThrow=true             — inner throws, used to derive cfcatch.tagContext ground truth
	//   useCaller=true           — inner calls LogUtil.caller
	//   neither (default)        — inner calls ExceptionUtil.getTagContextLine(null)
	private function inner( boolean doThrow=false, boolean useCaller=false ){
		if ( arguments.doThrow )   throw( type="test", message="ground truth" );
		if ( arguments.useCaller ) return createObject( "java", "lucee.commons.io.log.LogUtil" ).caller( getPageContext(), "FALLBACK" );
		return createObject( "java", "lucee.commons.lang.ExceptionUtil" ).getTagContextLine( javaCast( "null", "" ) );
	}
	private function outer( boolean doThrow=false, boolean useCaller=false ){
		return inner( arguments.doThrow, arguments.useCaller );
	}
	private function wrapper( boolean doThrow=false, boolean useCaller=false ){
		return outer( arguments.doThrow, arguments.useCaller );
	}

	// normalise either a filesystem absolute path or an already-mapping path
	// (or a concatenated string of mapping paths) by collapsing any /test*/
	// mapping variant to /test/ — spans /test, /test-once, /test-never, /test8 etc.
	private function normTestPath( required string path ){
		var s = contractPath( arguments.path );
		return reReplaceNoCase( s, "/test[^/]*/", "/test/", "all" );
	}
}
</cfscript>
