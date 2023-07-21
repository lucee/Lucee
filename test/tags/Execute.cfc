<cfscript>
	component extends="org.lucee.cfml.test.LuceeTestCase"	{
		
		
		//public function afterTests(){}
		
		public function setUp(){}
	
		public function testName() {
			cfexecute(name="curl https://update.lucee.org/rest/update/provider/echoGet" ,variable="local.x");
			assertTrue(find('"session"',x)>0);
		}
	
		public function testNameStringArg() {
			cfexecute(name="curl", arguments="https://update.lucee.org/rest/update/provider/echoGet" ,variable="variables.x");
			assertTrue(find('"session"',x)>0);
		}
		
		public function testNameStringArrayArg() {
			cfexecute(name="curl", arguments=["https://update.lucee.org/rest/update/provider/echoGet"] ,variable="variables.x");
			assertTrue(find('"session"',x)>0);
		}
	
		
		public function testTimeout() skip=true {
			try {
				cfexecute(name="curl", timeout="0.02", arguments="https://update.lucee.org/rest/update/provider/echoGet" ,variable="variables.x");
			} catch(e) {
				expect(e.message).toInclude('expired', e.message); // this fails sometimes on CI
			}
		}
	
	} 
	</cfscript>
	