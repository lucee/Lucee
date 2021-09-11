component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function testQuery() {
		try{
			application action="update" NULLSupport=false;
			query name="local.res" {
				echo("SELECT null AS value");
			}
			var r=res.value;
			assertFalse(isNull(r));

			application action="update" NULLSupport=true;
			query name="local.res" {
				echo("SELECT null AS value");
			}
			var r=res.value;
			assertTrue(isNull(r));
		}
		finally {
			application action="update" NULLSupport=false;
		}
	}

	function testArray() {
		try{
			application action="update" NULLSupport=false;
			query name="local.res" returntype="array" {
				echo("SELECT null AS value");
			}
			assertFalse(isNull(res[1].value));

			application action="update" NULLSupport=true;
			query name="local.res" returntype="array" {
				echo("SELECT null AS value");
			}
			assertTrue(isNull(res[1].value));
		}
		finally {
			application action="update" NULLSupport=false;
		}
	}

	function testStruct() {
		try{
			application action="update" NULLSupport=false;
			query name="local.res" returntype="struct" columnKey="id" {
				echo("SELECT 'a' as id, null AS value");
			}
			assertFalse(isNull(res.a.value));

			application action="update" NULLSupport=true;
			query name="local.res" returntype="struct" columnKey="id" {
				echo("SELECT 'a' as id, null AS value");
			}
			assertTrue(isNull(res.a.value));
		}
		finally {
			application action="update" NULLSupport=false;
		}
	}


	public function setUp(){
		defineDatasource();
	}

	private string function defineDatasource(){
		application 
			action="update" 
			datasource="#{
		  		class: 'org.h2.Driver'
		  		, bundleName: 'org.h2'
				, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/ldev2236;MODE=MySQL'
			}#"
		;
	}

	function afterTests() {
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