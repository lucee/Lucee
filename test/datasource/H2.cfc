component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
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