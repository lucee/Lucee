component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public function setUp(){
		defineDatasource('org.h2');

		// create the table if not exist
		query {
			echo("CREATE TABLE IF NOT EXISTS testTransactionRollBack ( 
				id INT NOT NULL, 
				title VARCHAR(50) NOT NULL, 
				author VARCHAR(20) NOT NULL, 
				submission_date DATE 
			 );");
		}
	}


	public void function testRollBack(){
		_test("sp1",1);
		_test("sp2",2);
	}

	private void function _test(name,reccount){
		
		query name="qry" {
			echo("delete from testTransactionRollBack");
		}

		var count=0;
		transaction { 

			query name="qry" {
				echo("insert into testTransactionRollBack(id,title,author,submission_date)
					values(#++count#,'test #getTickCount()#','Michael',#CreateODBCDate(now())#)");
			}
	
			transaction action="SetSavePoint" savepoint="sp1"; 
	
			query name="qry" {
				echo("insert into testTransactionRollBack(id,title,author,submission_date)
					values(#++count#,'test #getTickCount()#','Michael',#CreateODBCDate(now())#)");
			}
	
			//transaction action="SetSavePoint" savepoint="sp2"; 
			TransactionSetsavepoint("sp2");
	
			query name="qry" {
				echo("insert into testTransactionRollBack(id,title,author,submission_date)
					values(#++count#,'test #getTickCount()#','Michael',#CreateODBCDate(now())#)");
			}
	
			//transaction action="rollback" savepoint="sp2";
			TransactionRollBack(arguments.name);
		} 

		query name="local.qry" {
			echo("select * from testTransactionRollBack");
		}
		assertEquals(arguments.reccount,qry.recordcount);
	}

	private void function defineDatasource(required bundle,version=""){
		var ds= server.getDatasource( "h2", 
			server._getTempDir( "transRollBack" & replace(arguments.version,'.','_','all') ) 
		);
		
		if(!isEmpty(version))
			ds['bundleVersion']=arguments.version;

		application action="update" datasource=ds;
	}
}