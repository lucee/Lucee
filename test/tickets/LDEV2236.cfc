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
				echo("SELECT 'a' as id, null AS val");
			}
			assertFalse(isNull(res.a.val));

			application action="update" NULLSupport=true;
			query name="local.res" returntype="struct" columnKey="id" {
				echo("SELECT 'a' as id, null AS val");
			}
			assertTrue(isNull(res.a.val));
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
			datasource="#server.getDatasource( "h2", server._getTempDir( "LDEV2236" ) )#";
	}


}