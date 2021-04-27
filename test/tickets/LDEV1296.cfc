component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1296", body=function() {
			it(title="checking query.reduce member function, default value on arguments", body = function( currentSpec ) {
				var qry = queryNew("id,en,mi", "integer,varchar,varchar", [
				    [1,"one","tahi"],
				    [2,"two","rua"],
				    [3,"three","toru"],
				    [4,"four","wha"]
				]).reduce(function(sum=[], row){
					return sum.append(row.id);
				});
				expect(qry).toBeTypeOf("array");
			});
		});
	}
}