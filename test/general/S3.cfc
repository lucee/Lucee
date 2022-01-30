component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
	}

	function afterAll(){
	}

	function run( testResults , testBox ) {
		local.has=structCount(getCredencials());
		describe( "test suite for S3 VFS", function() {
			if(!has) return;
			it(title="test this.s3", body=function() {
				var uri=createURI("s3/default-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredencials());
				expect(trim(res.filecontent?:"")).toBe(true);
			});
			it(title="test this.vfs.s3", body=function() {
				var uri=createURI("s3/default-vfs-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredencials());
				expect(trim(res.filecontent?:"")).toBe(true);
			});
			it(title="test this.vfs.s3.lucee", body=function() {
				var uri=createURI("s3/mapping-vfs-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredencials());
				expect(trim(res.filecontent?:"")).toBe(true);
			});
			it(title="test this.vfs.s3[.lucee]", body=function() {
				var uri=createURI("s3/default-mapping-vfs-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredencials());
				expect(trim(res.filecontent?:"")).toBe("truetrue");
			});
		});
	}

	private struct function getCredencials() {
		return server.getTestService("s3");
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
