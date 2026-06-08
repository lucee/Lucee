component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults, testBox ) {

		describe( "LDEV-5941 sessionCommit() / sessionTouch()", function() {

			it( title="sessionTouch() is callable and does not throw", body=function() {
				var uri = createURI( "LDEV5941" );
				var resp = _InternalRequest( template: "#uri#/testTouchCausesWrite.cfm", url: { useTouch: true } );
				expect( resp.fileContent ).toBeJson();
				expect( deserializeJSON( resp.fileContent ).useTouch ).toBe( true );
			});

			// Post-commit nested mutation IS persisted at request end — content-aware ComponentImpl.hashCode
			// (LDEV-5930) means the hash check catches CFC internal changes. That's intentional behaviour.
			it( title="post-commit nested CFC mutation is persisted at request end (hash detection)", body=function() {
				var uri = createURI( "LDEV5941" );

				var first = _InternalRequest( template: "#uri#/testPostCommitMutation.cfm" );
				expect( first.fileContent ).toBeJson();

				var cookies = {
					cfid: first.session.cfid,
					cftoken: first.session.cftoken
				};

				_InternalRequest( template: "#uri#/stopApp.cfm", cookies: cookies );

				var second = _InternalRequest( template: "#uri#/testPostCommitMutationCheck.cfm", cookies: cookies );
				expect( second.fileContent ).toBeJson();
				expect( deserializeJSON( second.fileContent ).nestedValue ).toBe( "changedAfterCommit" );
			});

			// True no-double-write: mutate, sessionCommit, NO further mutations, request ends.
			// Request-end touchAfterRequest should see isDirty=false (markStored cleared the flag + rehashed)
			// and isStale=false (lastStored just bumped). So no redundant write fires.
			// We verify by comparing the cache entry's lastModified before and after request end.
			it( title="sessionCommit + no further mutations = no redundant write at request end", body=function() {
				var uri = createURI( "LDEV5941" );

				var first = _InternalRequest( template: "#uri#/testCommitOnly.cfm" );
				expect( first.fileContent ).toBeJson();
				var firstData = deserializeJSON( first.fileContent );

				var cookies = {
					cfid: first.session.cfid,
					cftoken: first.session.cftoken
				};

				// brief pause so a redundant write at request end would produce a distinguishable timestamp
				sleep( 50 );

				var second = _InternalRequest( template: "#uri#/testCommitOnlyCheck.cfm", cookies: cookies );
				expect( second.fileContent ).toBeJson();
				var secondData = deserializeJSON( second.fileContent );

				expect( secondData.cacheLastModifiedAfter ).toBe(
					firstData.cacheLastModifiedAtCommit,
					"BUG LDEV-5941: cache entry was rewritten after sessionCommit despite no further mutations. "
					& "markStored() must clear the dirty flag and rebaseline the hash."
				);
			});

			// Regression: sessionCommit mid-request must NOT trigger touchAfterRequest's metadata churn
			// (_lastvisit / _timecreated bumps, csrf cleanup). The bare commit path writes data0 as-is.
			it( title="sessionCommit does not bump _lastvisit mid-request", body=function() {
				var uri = createURI( "LDEV5941" );

				var first = _InternalRequest( template: "#uri#/testCommitOnly.cfm" );
				expect( first.fileContent ).toBeJson();

				var cookies = {
					cfid: first.session.cfid,
					cftoken: first.session.cftoken
				};

				// sleep so this request's _lastvisit is distinguishable from the prior request-end's persisted _lastvisit
				sleep( 100 );

				var second = _InternalRequest( template: "#uri#/testCommitNoMetadataBump.cfm", cookies: cookies );
				expect( second.fileContent ).toBeJson();
				var data = deserializeJSON( second.fileContent );

				expect( data.lastvisitAfter ).toBe(
					data.lastvisitBefore,
					"BUG LDEV-5941: sessionCommit() mid-request bumped persisted _lastvisit from #data.lastvisitBefore# to #data.lastvisitAfter#. "
					& "Should match prior request-end's _lastvisit (#data.lastvisitBefore#) — sessionCommit must not call touchAfterRequest mid-request."
				);
				// sanity: the cache write actually happened (lastModified bumped) — guards against trivial pass when no write fires
				expect( data.lastModifiedAfter ).notToBe(
					data.lastModifiedBefore,
					"Test fixture issue: sessionCommit did not fire a cache write — before/after _lastvisit comparison is meaningless."
				);
			});
		});
	}

	private string function createURI( required string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
