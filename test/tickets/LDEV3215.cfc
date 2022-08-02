component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {
	function beforeAll(){
		variables.uri = createURI("LDEV3215");
		if(!directoryExists("#uri#/test")) {
			directoryCreate("#uri#/test");
		}
	}

	function run( testResults , testbox ){
		describe( "Testcase for LDEV-3215", function(){
		    it( title="PDF merge with normal pdf files", body=function(){
			cfdocument( filename="#uri#/test/test1.pdf"){ writeOutput("<p>test PDF file</p>"); };
			cfdocument( filename="#uri#/test/test2.pdf"){ writeOutput("<p>test PDF file</p>"); };
			cfpdf( action="merge", source="#uri#/test/test1.pdf,#uri#/test/test2.pdf", destination="#uri#/test/merge.pdf" );
			res = fileExists("#uri#/test/merge.pdf");
			expect(res).toBe(true);
		    });
		    it( title="PDF merge with copy restricted pdf files", body=function(){
			try{
			    cfpdf( action="merge", source="#uri#/sample.pdf,#uri#/sample2.pdf", destination="#uri#/test/mergeCopy.pdf" );
			    res = fileExists("#uri#/test/mergeCopy.pdf");
			}
			catch(any e){
			    res = e.message;
			}
			expect(res).toBe(true);
		    });
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	function afterAll() {
		if(directoryExists("#uri#/test")) {
			directoryDelete("#uri#/test",true);
		}
	}
	}
