component extends="org.lucee.cfml.test.LuceeTestCase" labels="content,mime,pdf"	{

	public void function testInvalidMimeTypes() skip=true {
		var mimeType = "";
		expect( testMimeType( mimeType ) ).toThrow();

		mimeType = "bananas/in-pajamas";
		expect( testMimeType( mimeType ) ).toBe( mimeType );

		mimeType = "this-should-never-work";
		expect( testMimeType( mimeType ) ).toBe( mimeType );

		mimeType = "works/on-my-machine";
		expect( testMimeType( mimeType ) ).toBe( mimeType );
	}

	public void function testValidMimeTypes() {
		var mimeType = "text/plain";
		expect( testMimeType( mimeType ) ).toBe( mimeType );

		mimeType = "text/html";
		expect( testMimeType( mimeType ) ).toBe( mimeType );

		mimeType = "application/json";
		expect( testMimeType( mimeType ) ).toBe( mimeType );
	}

	public void function testPDF() skip="isNotSupportedPDF" {
		var pdf = callTestPDF( mode="named" );
		expect( pdf.headers["content-type"] ).toBe( "application/pdf" );

		// TODO lucee.runtime.exp.Abort: Page request is aborted LDEV-3761
		//pdf = callTestPDF( mode="direct" );
		//expect( pdf.headers["content-type"] ).toBe( "application/pdf" );
	}

	private string function callTestPDF( required string mode ) {
		var res = _InternalRequest(
			template: createURI("content/pdf.cfm"),
			url: {
				mode: arguments.mode
			}
		);
		return res;
	}

	public boolean function isNotSupportedPDF(){
		return true;
		return !extensionExists( "66E312DD-D083-27C0-64189D16753FD6F0" );
	}

	private string function testMimeType( required string mimeType ) {
		var res = _InternalRequest(
			template: createURI("content/mimeType.cfm"),
			url: {
				mimeType: arguments.mimeType
			}
		);
		return trim( res.fileContent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
