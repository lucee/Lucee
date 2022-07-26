component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		application action="update" 
				datasource={
			class: 'org.h2.Driver'
			, bundleName: 'org.h2'
			, bundleVersion: '1.3.172'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
			, connectionLimit:100 // default:-1
		};

		try{
			query {
				echo("drop TABLE T2260");
			}
		}
		catch(local.e){}
		
		query  {
			echo("CREATE TABLE T2260 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255)");		
			echo(") ");
		}

		query  {
			echo("insert into T2260(id, i, vc) values(1,2,'3')");
		}
	}

		function afterAll(){
			try{
				query {
					echo("drop TABLE T2260");
				}
			}
			catch(local.e){}
			afterTests();
		}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV2260", function() {
			it( title='test int length', body=function( currentSpec ) {
			 	query name="local.q" {
					echo("select * from T2260 where id=");
					queryparam
						value = 1.23456789
						sqltype = 'integer'
						maxLength = 1;
				}
				//expect(isStruct(local.result)).tobe(true);
			});
		});
	}
		
	private function afterTests() {
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