component extends="org.lucee.cfml.test.LuceeTestCase" {

	// `synchronized="true"` wraps method dispatch in `synchronized(this)` at runtime.
	// The flag is set at compile time on the class's ComponentProperties — it is NOT
	// inherited from a parent class at instance-init time. These tests pin both the
	// per-class semantics and the runtime serialisation behaviour under stress.

	function run( testResults, testBox ){
		describe( "synchronized component dispatch", function(){

			it( title="single-thread bump() increments and persists across calls", body=function( currentSpec ){
				var s = new synchronized.Synced();
				expect( s.callCount ).toBe( 0 );
				s.bump();
				expect( s.callCount ).toBe( 1 );
				s.bump();
				s.bump();
				expect( s.callCount ).toBe( 3 );
			});
			it( title="getMetaData reflects synchronized=true on the declaring class", body=function( currentSpec ){
				var s = new synchronized.Synced();
				expect( getMetaData( s ).synchronized ).toBeTrue();
			});
			it( title="getMetaData on a child that doesn't redeclare synchronized — child class itself is NOT synchronized", body=function( currentSpec ){
				// Per-class semantics: SyncedChild extends Synced but doesn't declare
				// synchronized="true" itself, so its own metadata reports synchronized=false.
				// The flag IS visible on the parent via getMetaData(child).extends.synchronized.
				var sc = new synchronized.SyncedChild();
				expect( getMetaData( sc ).synchronized ).toBeFalse();
				expect( getMetaData( sc ).extends.synchronized ).toBeTrue();
			});
			it( title="synchronized parent — concurrent bumps on shared instance don't lose updates", body=function( currentSpec ){
				request.syncTarget = new synchronized.Synced();
				var threadCount = 20;
				var threadNames = [];
				for ( var t=1; t<=threadCount; t++ ) {
					var tname = "synchronized-sync-" & t;
					arrayAppend( threadNames, tname );
					thread name="#tname#" {
						request.syncTarget.bump();
					}
				}
				thread action="join" name="#arrayToList( threadNames )#";
				expect( request.syncTarget.callCount ).toBe( threadCount );
			});
			it( title="duplicate of synchronized parent — duplicate retains the synchronized flag", body=function( currentSpec ){
				var s = new synchronized.Synced();
				request.syncDup = duplicate( s );
				expect( getMetaData( request.syncDup ).synchronized ).toBeTrue();
				var threadCount = 20;
				var threadNames = [];
				for ( var t=1; t<=threadCount; t++ ) {
					var tname = "synchronized-syncdup-" & t;
					arrayAppend( threadNames, tname );
					thread name="#tname#" {
						request.syncDup.bump();
					}
				}
				thread action="join" name="#arrayToList( threadNames )#";
				expect( request.syncDup.callCount ).toBe( threadCount );
				expect( s.callCount ).toBe( 0 );
			});

		});
	}
}
