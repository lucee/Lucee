component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function run( testResults , testBox ) {

		describe( title="Test suite for LDEV-3296, check this vs variables scope resolution", body=function() {
			var sample = new LDEV3296.sample();

			it(title="check func() comes from 'variables' scope (when same name)", body=function( currentSpec ) {
				str = sample.getFuncScope();
				expect(str).toBe("variables");
			});
			it(title="check this.func() comes from 'this' scope (when same name)", body=function( currentSpec ) {
				str = sample.getThisFuncScope();
				expect(str).toBe("this");
			});
			it(title="check 'variable' comes from 'variables' scope (when same name)", body=function( currentSpec ) {
				str = sample.getVariableScope();
				expect(str).toBe("variables");
			});
			it(title="check 'this.variable' comes from 'this' scope (when same name)", body=function( currentSpec ) {
				str = sample.getThisVariableScope();
				expect(str).toBe("this");
			});
		});
	}
}
