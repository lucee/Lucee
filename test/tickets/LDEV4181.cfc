component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" labels="qoq" {

	variables.mysql = server.getDatasource("mysql");
	
	function beforeAll(){
		if ( !hasMysql() )
			return;
		afterAll();
		queryExecute(
			sql="CREATE TABLE ldev4181 (
				id numeric(11,10) NOT NULL,
				price decimal(10,2)
			) ",
			options: {
				datasource: variables.mysql
			}
		);		
	};

	function afterAll(){
		if ( !hasMysql() )
			return;
		queryExecute(
			sql="drop table if exists ldev4181",
			options: {
				datasource: variables.mysql
			}
		);
	
	};

	private function hasMysql(){
		return !isEmpty(variables.mysql);
	}

	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4181", body=function() {
			it(title="Checking QoQ with numeric column, trailing 000s", body = function( currentSpec ) {
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

			it(title="Checking mysql query with numeric column, trailing 000s", body = function( currentSpec ) {
				var price = 3.14;
				queryExecute(
					sql="INSERT INTO ldev4181 ( id, price ) VALUES ( :id, :price )",
					params={
						id: { value: 1, type: "int" },
						price: { value: price, type: "decimal" }
					},
					options: {
						datasource: variables.mysql
					}
				);

				var qry = queryExecute(
					sql: "SELECT * from ldev4181",
					options: {
						datasource: variables.mysql
					}
				);

				expect( qry.recordCount ).toBe ( 1 );
				expect( qry.toJson() ).toBe('{"COLUMNS":["id","price"],"DATA":[[1,3.14]]}');
				expect( valueList(qry.id ) ) .toBe( "1" );
				expect( qry.id.toString() ).toBe( 1 );
				expect( qry.price ).toBe( price );
			});
		});
	}
}
