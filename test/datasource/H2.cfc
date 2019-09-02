component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		
	}

	public void function testConnection13(){
		defineDatasource('org.h2','1.3.172');
		testConnection();
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
		var ds={
	  		class: 'org.h2.Driver'
	  		,bundleName:arguments.bundle
	  		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db#replace(arguments.version,'.','_','all')#;MODE=MySQL'
		};
		if(!isEmpty(version))
			ds['bundleVersion']=arguments.version;

		application action="update" datasource=ds;
	}
}