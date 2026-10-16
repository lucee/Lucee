component extends="org.lucee.cfml.test.LuceeTestCase" labels="guard" {
	function run( testResults , testBox ) {
		describe( title="Test suite for Canonicalize()", body=function() {
			it(title="checking Canonicalize() function", body = function( currentSpec ) {
				assertEquals('<',canonicalize("&lt;",false,false));
				assertEquals('< < < <<',canonicalize("%26lt; %26lt; %2526lt%253B %2526lt%253B%2526lt%253B",false,false));
				assertEquals('<',canonicalize("&##X25;3c",false,false));
			});


            it( "reduces double-encoded characters (Standard Unmasking)", function() {
                // %25 is '%', so %253c becomes %3c which becomes <
                var input = "Hello %253cscript%253e";
                expect( canonicalize( input, false, false ) ).toBe( "Hello <script>" );
            });


            it( "preserves the plus sign (No URL-space conversion)", function() {
                var input = "1+1=2";
                // Our Lucee-native decoder ensures + does not become a space
                expect( canonicalize( input, false, false ) ).toBe( "1+1=2" );
            });

            it( "preserves all special characters when simplify is disabled", function() {
                var input = "!@##$&*()_+{}[]:;''<>, .?/|~`.";
                
                // When simplify is false, carets and backslashes remain
                expect( canonicalize( input, false, false ) ).toBe( input );
            });

            it( "handles mixed HTML and URL encoding", function() {
                // %26 is '&', so %26lt; becomes &lt; which becomes <
                var input = "Mixed: %26lt;script%26gt;";
                expect( canonicalize( input, false, false ) ).toBe( "Mixed: <script>" );
            });

		});
	}
}