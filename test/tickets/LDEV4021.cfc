component extends="org.lucee.cfml.test.LuceeTestCase" labels="throw" skip=true{

	function beforeAll() {
		try {
			throw(message="custom error message", type="custom error type", detail="custom type detail");
		}
		catch(any e) {
			variables.exception = e;
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4021", function() {
			it( title="Checking stacktrace for rethrown original exception", body=function( currentSpec ) {
				try {
					throw(object=variables.exception);
				}
				catch(any e) {
					var exp = e;
				}
				expect(exp.message).toBe(variables.exception.message);
				expect(exp.detail).toBe(variables.exception.detail);
				expect(exp.type).toBe(variables.exception.type);
				expect(exp.TagContext[1].line).toBe(variables.exception.TagContext[1].line);
			});
			it( title="Checking stacktrace for serialize/deSerialize struct of the original exception", body=function( currentSpec ) {
				try {
					var serializedExp = deserializeJSON(serializeJSON(variables.exception));
					throw(object=serializedExp);
				}
				catch(any e) {
					var exp = e;
				}
				expect(exp.message).toBe(variables.exception.message);
				expect(exp.detail).toBe(variables.exception.detail);
				expect(exp.type).toBe(variables.exception.type);
				expect(exp.TagContext[1].line).toBe(variables.exception.TagContext[1].line);
			});
		});
	}
}