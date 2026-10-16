component extends="org.lucee.cfml.test.LuceeTestCase" {

	// onMissingMethod fallback through inheritance — a child that doesn't declare
	// the called method falls through to the parent's onMissingMethod, which reads
	// `this.tag` from the calling instance.

	function run( testResults, testBox ){
		describe( "onMissingMethod fallback through inheritance", function(){

			it( title="missing method on child falls through to parent's oMM", body=function( currentSpec ){
				var c = new onMissingMethod.OnMissingChild( tag="om1" );
				expect( c.declared() ).toBe( "declared:om1" );
				expect( c.somethingMissing() ).toBe( "om1:somethingMissing" );
			});
			it( title="duplicate of OnMissingChild — declared and oMM both still work", body=function( currentSpec ){
				var c = new onMissingMethod.OnMissingChild( tag="om2" );
				var d = duplicate( c );
				expect( d.declared() ).toBe( "declared:om2" );
				expect( d.somethingMissing() ).toBe( "om2:somethingMissing" );
			});
			it( title="oMM reads `this.tag` from the calling instance, not the base", body=function( currentSpec ){
				var a = new onMissingMethod.OnMissingChild( tag="aa" );
				var b = new onMissingMethod.OnMissingChild( tag="bb" );
				expect( a.fooBar() ).toBe( "aa:fooBar" );
				expect( b.fooBar() ).toBe( "bb:fooBar" );
			});

		});
	}
}
