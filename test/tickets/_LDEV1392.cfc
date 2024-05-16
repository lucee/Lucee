component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1392", function() {
			it(title="checking getThreadGroup()", body = function( currentSpec ) {
				var thrGrp1 = getPageContext().getThread().getThreadGroup();
				var thrGrp2 = getPageContext().getThread().getThreadGroup();
				assertEquals(true,isInstanceOf(thrGrp1, "java.lang.ThreadGroup"));
				assertEquals(true,isInstanceOf(thrGrp2, "java.lang.ThreadGroup"));
			});

			it(title="checking getThreadGroup(), after HTTP call", body = function( currentSpec ) {
				var thrGrp1 = getPageContext().getThread().getThreadGroup();
				http url="https://www.lucee.org" method="post" result="result";
				var thrGrp2 = getPageContext().getThread().getThreadGroup();
				try{
					var result = isInstanceOf(thrGrp2, "java.lang.ThreadGroup")
				} catch( any e ){
					var thrGrp2 = false;
				}
				assertEquals(true,isInstanceOf(thrGrp1, "java.lang.ThreadGroup"));
				assertEquals(true,thrGrp2);
			});
		});
	}
}