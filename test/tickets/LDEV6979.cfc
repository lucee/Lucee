component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( "LDEV-6979 try/catch nested inside a finally block", function() {

			it( title="cftry inside cfif inside cffinally (ticket pattern)", body=function( currentSpec ) {
				expect( runTemplate( "tagNestedTry", false ) ).toBe( "try,inner" );
				expect( runTemplate( "tagNestedTry", true ) ).toBe( "try,catch,inner" );
			});

			it( title="cftry with cfcatch and cffinally directly inside cffinally", body=function( currentSpec ) {
				expect( runTemplate( "tagNestedTryDirect", false ) ).toBe( "try,inner,innercatch,innerfinally" );
				expect( runTemplate( "tagNestedTryDirect", true ) ).toBe( "try,catch,inner,innercatch,innerfinally" );
			});

			it( title="cfscript try inside cffinally", body=function( currentSpec ) {
				expect( runTemplate( "tagScriptInFinally", false ) ).toBe( "try,inner" );
				expect( runTemplate( "tagScriptInFinally", true ) ).toBe( "try,catch,inner" );
			});

			it( title="script try on the same line as another statement inside finally (ticket pattern)", body=function( currentSpec ) {
				expect( runTemplate( "scriptNestedTry", false ) ).toBe( "try,inner" );
				expect( runTemplate( "scriptNestedTry", true ) ).toBe( "try,catch,inner" );
			});

			it( title="script try/catch/finally inside finally", body=function( currentSpec ) {
				expect( runTemplate( "scriptNestedTryFinally", false ) ).toBe( "try,inner,innercatch,innerfinally" );
				expect( runTemplate( "scriptNestedTryFinally", true ) ).toBe( "try,catch,inner,innercatch,innerfinally" );
			});

			it( title="cfscript block inside cffinally", body=function( currentSpec ) {
				expect( runTemplate( "tagScriptBlockInFinally", false ) ).toBe( "try,script" );
				expect( runTemplate( "tagScriptBlockInFinally", true ) ).toBe( "try,catch,script" );
			});

			it( title="cfsavecontent with cftry inside cffinally", body=function( currentSpec ) {
				expect( runTemplate( "tagSavecontentInFinally", false ) ).toBe( "try,xy" );
				expect( runTemplate( "tagSavecontentInFinally", true ) ).toBe( "try,catch,xy" );
			});

			it( title="cfretry inside cffinally", body=function( currentSpec ) {
				expect( runTemplate( "tagRetryInFinally", false ) ).toBe( "try,retry2" );
				expect( runTemplate( "tagRetryInFinally", true ) ).toBe( "try,catch,retry2" );
			});

		});
	}

	// the bug only shows on the first (cold) compile of a template, each spec includes its own template
	private string function runTemplate( required string name, required boolean throwInTry ) {
		request.ldev6979Throw = arguments.throwInTry;
		include "LDEV6979/#arguments.name#.cfm";
		return arrayToList( request.ldev6979 );
	}
}
