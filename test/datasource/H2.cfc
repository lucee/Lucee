component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public function setUp(){
		
	}

	public void function testConnectionLatest(){
		defineDatasource('org.h2');
		testConnection();
	}

	private void function testConnection(){
		query name="local.qry" {
			echo("show tables");
		}
	}

	private void function testNull(){
		query name="local.qry" {
			echo("SELECT null as _null");
		}
		assertTrue(isNull(qry._null));
	}

	private void function defineDatasource(required bundle,version=""){
		var ds = server.getDatasource( "h2", server._getTempDir( "h2-" & replace(arguments.version,'.','_','all') ) );
		
		if(!isEmpty(version))
			ds['bundleVersion']=arguments.version;

		application action="update" datasource=ds;
	}
	
}