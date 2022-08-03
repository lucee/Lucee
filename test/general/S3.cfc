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
			it(title="test access 100 threads at the same time", body=function() {
				var cred=getCredencials();
				var dir="s3://#cred.ACCESS_KEY_ID#:#cred.SECRET_KEY#@/lucee-s3-#lcase( hash( CreateGUID() ) )#/";
				var file=dir&"testmultithread.txt";
				try {
					if(!directoryExists(dir))directoryCreate(dir);
					fileWrite(file, "Susi sorglos foehnte Ihr Haar!");
					
					var arr=[];
					loop times=100 {
						arrayAppend(arr, file);
					}

					arr.each(
					function(el, ix, arr) localMode=true {
						var res=fileRead(el);
					}, true, 100);
				}
				finally {
					if(directoryExists(dir))directoryDelete(dir, true);
				}
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
