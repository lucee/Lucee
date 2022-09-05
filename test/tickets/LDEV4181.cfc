component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4181", body=function() {
			it(title="Checking QoQ with numeric column", body = function( currentSpec ) {
				var qry = queryNew( 'id,test', 'numeric,string', [ [1,',1,10'],[2,',2,20'],[3,',3,30'],[4,',4,40'],[5,',5,50'],[10,',10,100'],[15,',15,150'] ] );
				var queryResult = queryExecute("
					SELECT id 
					FROM qry 
					where ','||test||',' like ('%1%')",
					[],
					{ dbType='query' }
				);
				expect(valueList(queryResult.id)).tobe("1,10,15");
			});
		});
	}
}