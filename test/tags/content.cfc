component extends="org.lucee.cfml.test.LuceeTestCase" labels="content,mime"	{

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
