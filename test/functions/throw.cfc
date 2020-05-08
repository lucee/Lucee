component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for throw", body=function() {
			it( title='Test case for throw function  ',body=function( currentSpec ) {

				<!--- begin old test code --->
				try {

					throw("msg");
				}
				catch(any e) {

		    	assertEquals("#cfcatch.message#","msg");
		    	assertEquals("#cfcatch.detail#","");
				}


				try {
					throw("a","b","c","d","e");
				}
				catch (any e) {
			    	assertEquals("#cfcatch.message#","a");
			    	assertEquals("#cfcatch.type#","b");
			    	assertEquals("#cfcatch.detail#","c");
			    	assertEquals("#cfcatch.ErrorCode#","d");
			    	assertEquals("#cfcatch.ExtendedInfo#","e");
				    	
				}

				try {
					throw(message:"msg");
				}
				catch (any e) {
			    	assertEquals("#cfcatch.message#","msg");
			    	assertEquals("#cfcatch.detail#","");
			    	assertEquals("#cfcatch.type#","application");
				}

				try {
					throw(detail:"msg");
				}
				catch (any e) {

			    	assertEquals("#cfcatch.message#","");
			    	assertEquals("#cfcatch.detail#","msg");
			    	assertEquals("#cfcatch.type#","application");
				}

			});
		});
	}
}