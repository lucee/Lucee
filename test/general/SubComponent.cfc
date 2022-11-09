component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
	}

	function afterAll(){
	}

	function run( testResults , testBox ) {
		describe( "test sub component", function() {
			it(title="tag based main component", body=function() {
				var cfc=new cfc.TestSubTag();
				expect(cfc.testtag()).toBe("tag:closure-insidetag:argclosuretag");
				expect(cfc.testscript()).toBe("script:closure-insidescript:argclosurescript");
			});
			it(title="tag based sub component", body=function() {
				var cfc=new cfc.TestSubTag$sub();
				expect(cfc.subtest()).toBe("subito");
			});
			it(title="script based main component", body=function() {
				var cfc=new cfc.TestSubScript();
				expect(cfc.test()).toBe("main:closure-insidemain:argclosuremain");
			});
			it(title="script based sub component", body=function() {
				var cfc=new cfc.TestSubScript$sub();
				expect(cfc.test()).toBe("sub:closure-insidesub:argclosuresub");
			});
		});
	}
}
