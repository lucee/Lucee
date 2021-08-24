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
		_test("sp0",0);
		_test("sp1",1);
		_test("sp2",2);
	}

	private void function _test(name,reccount){
		
		query name="qry" {
			echo("delete from testTransactionRollBack");
		}

		var count=0;
		transaction { 

			transaction action="SetSavePoint" savepoint="sp0"; 
	
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
		var ds={
	  		class: 'org.h2.Driver'
	  		,bundleName:arguments.bundle
	  		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/dbtrb#replace(arguments.version,'.','_','all')#;MODE=MySQL'
		};
		if(!isEmpty(version))
			ds['bundleVersion']=arguments.version;

		application action="update" datasource=ds;
	}

	public function afterTests() {
		var javaIoFile=createObject("java","java.io.File");
		loop array=DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
}