component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-4151", function() {
			it( title="concatenation operator(&) in finally without local scope prefix", body = function( currentSpec ) {
				expect(testLocalPrefixInFinally(1)).toBe("Start-Try-Finally-End");
			});

			it( title="concatenation operator(&) in finally with local scope prefix", body = function( currentSpec ) {
				expect(testLocalPrefixInFinally(2)).toBe("Start-Try-Finally-End");
			});

			it( title="compound concatenation operator(&=) in finally without local scope prefix", body = function( currentSpec ) {
				expect(testLocalPrefixInFinally(3)).toBe("Start-Try-Finally-End");
			});
			
			it( title="compound concatenation operator(&=) in finally with local scope prefix", body = function( currentSpec ) {
				try {
					var res = testLocalPrefixInFinally(4);
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("Start-Try-Finally-End");
			});
		});
	}

	private any function testLocalPrefixInFinally(scene=0) {
		local.thing = "";
		local.thing &= "Start";
		try {
			local.thing &= "-Try";
		}
		catch(any e) {
			local.thing &= "-Catch";
		}
		finally {
			if (arguments.scene == 1) thing = thing & "-Finally"; // & operator without prefix local
			else if (arguments.scene == 2) local.thing = local.thing & "-Finally"; // & operator with prefix local
			else if (arguments.scene == 3) thing &= "-Finally"; // &= operator without prefix local
			else if (arguments.scene == 4) local.thing &= "-Finally"; // &= operator with prefix local
		}
		local.thing &= "-End";
		return local.thing;
	}
} 