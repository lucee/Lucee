component extends="org.lucee.cfml.test.LuceeTestCase" labels="null" skip=true{
	function run( testResults , testBox ) {
		describe("Test suite for LDEV-1296", function() {
			it(title = "query.reduce member function, With default value on queryReduce", body = function( currentSpec ) {
				var qry = queryNew("id,en,mi", "integer,varchar,varchar", [
				    [1,"one","tahi"],
				    [2,"two","rua"],
				    [3,"three","toru"],
				    [4,"four","wha"]
				]).reduce(function(sum, row){
					return sum.append(row.id);
				},[]);
				expect(qry).toBeTypeOf("array");
			});

			it(title = "query.reduce member function, With default value on queryReduce and enableNullSupport", body = function( currentSpec ) {
				cfapplication(name="test", enableNullSupport=true);
				var qry = queryNew("id,en,mi", "integer,varchar,varchar", [
				    [1,"one","tahi"],
				    [2,"two","rua"],
				    [3,"three","toru"],
				    [4,"four","wha"]
				]).reduce(function(sum, row){
					return sum.append(row.id);
				},[]);
				expect(qry).toBeTypeOf("array");
			});

			it(title = "query.reduce member function, default value on arguments", body = function( currentSpec ) {
				cfapplication(name="test", enableNullSupport=false);
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

			it(title = "query.reduce member function, default value on arguments with enableNullSupport", body = function( currentSpec ) {
				try{
					cfapplication(name="test", enableNullSupport=true);
					var qry = queryNew("id,en,mi", "integer,varchar,varchar", [
					    [1,"one","tahi"],
					    [2,"two","rua"],
					    [3,"three","toru"],
					    [4,"four","wha"]
					]).reduce(function(sum=[], row){
						return sum.append(row.id);
					});		
				}
				catch(any e){
					qry = e.message;
				}
				expect(qry).toBeTypeOf("array");
			});
		});
	}
} 
