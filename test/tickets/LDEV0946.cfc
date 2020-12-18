component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-946 ( checking GetMetaData() for a component )", body=function() {
			it(title="Component with functions defined", body=function( currentSpec ) {
				b = createObject("component", "LDEV0946.b");
				B_Meta = GetMetaData(b);
				expect(structKeyExists(B_Meta, "functions")).toBeTrue();
			});

			it(title="Component with no functions defined", body=function( currentSpec ) {
				a = createObject("component", "LDEV0946.a");
				A_Meta = GetMetaData(a);
				expect(structKeyExists(A_Meta, "functions")).toBeTrue();
				expect(arrayIsEmpty(A_Meta.functions)).toBetrue();
			});
		});
	}
}