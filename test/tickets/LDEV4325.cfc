component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.mySQL = server.getDatasource("mysql");
		if( structCount(mySQL) ) {
			application action="update"  datasource=mySQL;

			query{
				echo( "DROP TABLE IF EXISTS testnotes" );
			}
			query{
				echo( "CREATE TABLE `testnotes` (
					`id` INT(11) NOT NULL AUTO_INCREMENT,
					`notes` MEDIUMTEXT NOT NULL COLLATE utf16_bin,
					PRIMARY KEY (`id`) USING BTREE
				) COLLATE=latin1_swedish_ci ENGINE=InnoDB" );
			}
		}
	}

	function afterAll() {
		if (!notHasMysql()) {
			queryExecute( sql = "DROP TABLE IF EXISTS testnotes" );
		}
	}

	function run( testResults, testBox ) {
		describe( title = "Testcase for LDEV-4325", body = function() {
			it( title = "Checking Cf_sql_clob", skip = "#notHasMysql()#", body = function( currentSpec ) {
				st = structNew();
				st.ID = 1;
				st.NAME = "Water";
				st.DESIGNATION = "Important source for all";
				st.DATA = 1;
				notes = st.toJson();
				QueryExecute(
					sql = "INSERT INTO `testnotes` ( notes ) VALUES ( :notes )",
					params = {
						notes: { value: notes, type: "clob" }
					}
				);
				qry = QueryExecute(
					sql: "SELECT notes from `testnotes`"
				);
				expect( qry.notes ).toBe( '{"DATA":1,"DESIGNATION":"Important source for all","NAME":"Water","ID":1}' );
			});
		});
	}

	private function notHasMysql() {
		return structCount( server.getDatasource("mysql") ) == 0;
	}
}
