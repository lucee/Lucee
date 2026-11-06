component extends="org.lucee.cfml.test.LuceeTestCase" labels="component" {

	function run( testResults, testBox ) {
		describe( title="LDEV-5926 the base component is never checked for changes", body=function() {

			it( title="a component without extends still works", body=function() {
				loop times=50 {
					var cfc = new LDEV5926.Simple();
					expect( cfc.hello() ).toBe( "hello" );
				}
			});

			it( title="a component extending another component still works", body=function() {
				loop times=50 {
					var cfc = new LDEV5926.Child();
					expect( cfc.hello() ).toBe( "hello child" );
				}
			});

			it( title="static access on a component still works", body=function() {
				expect( LDEV5926.Simple::ping() ).toBe( "pong" );
				expect( LDEV5926.Child::ping() ).toBe( "pong" );
			});

			it( title="the metadata of a component still contains the base component", body=function() {
				var meta = getMetadata( new LDEV5926.Simple() );
				expect( meta.extends.fullname ).toBe( "org.lucee.cfml.Component" );
			});

			it( title="the base component page source is immutable and stays loaded", body=function() {
				var pc = getPageContext();
				var base = pc.getConfig().getBaseComponentPageSource( pc, false );
				expect( isNull( base ) ).toBeFalse();
				expect( base.isImmutable() ).toBeTrue();

				var before = base.loadPage( pc, false );
				var cfc = new LDEV5926.Simple();
				var after = base.loadPage( pc, false );
				expect( after.getCompileTime() ).toBe( before.getCompileTime() );
				expect( after.getSourceLastModified() ).toBe( before.getSourceLastModified() );
			});

			it( title="the page source of a regular component is not immutable", body=function() {
				var pc = getPageContext();
				expect( pc.getCurrentPageSource().isImmutable() ).toBeFalse();
			});

		});
	}

}
