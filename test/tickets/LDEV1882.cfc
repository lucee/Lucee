component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" skip=true {
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1882", function() {
			it(title = "checking cfcontinue within script", body = function( currentSpec ) {
				var myQry = queryNew(	'id,title,content','integer,string,string',
								[	{"id":1, "title":"sas", "content":"sample"},
									{"id":2, "title":"sas", "content":"lucee"},
									{"id":3, "title":"arg", "content":"test"},
									{"id":4, "title":"arg", "content":"case"},
									{"id":5, "title":"arg", "content":"result"} ]);

				var result="";
				var temp= "";
				cfloop(query=myQry,group='title'){
					if(myQry.title eq 'sas'){
						continue;;
					}
					temp&=myQry.title;
				}
				expect(temp).toBe('arg');
			});

			it(title = "Checking cfcontinue with tag", body = function( currentSpec ) {
				var uri = createURI("LDEV1882");
				local.result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('arg');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}