component extends="org.lucee.cfml.test.LuceeTestCase" {

	private any function aiUtil() {
		return createObject( "java", "lucee.runtime.ai.AIUtil" );
	}

	function run( testResults, testBox ) {
		describe( title="AI session multipart serialize and load", body=function() {

			it( title="toConversations restores multipart text answers", body=function() {
				var conversations = aiUtil().toConversations( [
					{
						question: "prompt",
						answer: [
							{ type: "text", contenttype: "text/plain", content: "Line one." },
							{ type: "text", contenttype: "text/plain", content: " Line two." }
						]
					}
				] );
				var resp = conversations[ 1 ].getResponse();
				expect( resp.getAnswer() ).toBe( "Line one. Line two." );
				expect( resp.isMultiPart() ).toBeTrue();
			});

			it( title="toConversations restores multipart answers with binary parts", body=function() {
				var conversations = aiUtil().toConversations( [
					{
						question: "prompt",
						answer: [
							{ type: "text", contenttype: "text/plain", content: "Caption:" },
							{ type: "binary", contenttype: "image/png", content: toBase64( "png-bytes" ) }
						]
					}
				] );
				var resp = conversations[ 1 ].getResponse();
				var parts = resp.getAnswers();
				expect( resp.isMultiPart() ).toBeTrue();
				expect( resp.getAnswer() ).toBe( "Caption:" );
				expect( parts.size() ).toBe( 2 );
				expect( parts[ 1 ].getAsString() ).toBe( "Caption:" );
				expect( parts[ 2 ].isImage() ).toBeTrue();
				expect( toString( parts[ 2 ].getAsBinary() ) ).toBe( "png-bytes" );
			});

			it( title="toConversations restores structured JSON answer parts", body=function() {
				var conversations = aiUtil().toConversations( [
					{
						question: "prompt",
						answer: [
							{
								type: "struct",
								contenttype: "application/json",
								content: { name: "get_weather", input: { location: "Vienna" } }
							}
						]
					}
				] );
				var part = conversations[ 1 ].getResponse().getAnswers()[ 1 ];
				expect( part.isStructured() ).toBeTrue();
				expect( part.getAsStruct().name ).toBe( "get_weather" );
			});

			it( title="toConversations keeps plain string answers", body=function() {
				var conversations = aiUtil().toConversations( [
					{ question: "ping", answer: "pong" }
				] );
				var resp = conversations[ 1 ].getResponse();
				expect( resp.getAnswer() ).toBe( "pong" );
				expect( resp.isMultiPart() ).toBeFalse();
			});

			it( title="toConversations round-trips multipart question and answer", body=function() {
				var conversations = aiUtil().toConversations( [
					{
						question: [
							{ type: "text", contenttype: "text/plain", content: "Describe this" },
							{ type: "binary", contenttype: "image/png", content: toBase64( "png-bytes" ) }
						],
						answer: [
							{ type: "text", contenttype: "text/plain", content: "A small image." }
						]
					}
				] );
				expect( conversations[ 1 ].getRequest().isMultiPart() ).toBeTrue();
				expect( conversations[ 1 ].getResponse().getAnswer() ).toBe( "A small image." );
			});

		});
	}

}
