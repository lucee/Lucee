component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( "LDEV-3069 script include with a path followed by named attributes", function() {

			it( "include ""path"" cachedwithin=... caches the output", function() {
				expect( scriptIncludeShort() ).toBe( scriptIncludeShort() );
			});

			it( "include ""path"" runonce=true includes the file only once", function() {
				request.ldev3069Counter = 0;
				include "LDEV3069/counter.cfm" runonce=true;
				include "LDEV3069/counter.cfm" runonce=true;
				expect( request.ldev3069Counter ).toBe( 1 );
			});

			it( "include template=... cachedwithin=... caches the output", function() {
				expect( scriptIncludeTemplate() ).toBe( scriptIncludeTemplate() );
			});

			it( "cfinclude( template=..., cachedwithin=... ) caches the output", function() {
				expect( scriptCfinclude() ).toBe( scriptCfinclude() );
			});

			it( "include ""path"" without cachedwithin is not cached", function() {
				savecontent variable="local.a" { include "LDEV3069/uuid.cfm"; }
				savecontent variable="local.b" { include "LDEV3069/uuid.cfm"; }
				expect( trim( a ) ).notToBe( trim( b ) );
			});
		});
	}

	private function scriptIncludeShort() {
		savecontent variable="local.res" {
			include "LDEV3069/uuid.cfm" cachedwithin=createTimeSpan( 0, 0, 1, 2 );
		}
		return trim( res );
	}

	private function scriptIncludeTemplate() {
		savecontent variable="local.res" {
			include template="LDEV3069/uuid.cfm" cachedwithin=createTimeSpan( 0, 0, 1, 1 );
		}
		return trim( res );
	}

	private function scriptCfinclude() {
		savecontent variable="local.res" {
			cfinclude( template="LDEV3069/uuid.cfm", cachedwithin=createTimeSpan( 0, 0, 1, 3 ) );
		}
		return trim( res );
	}
}
