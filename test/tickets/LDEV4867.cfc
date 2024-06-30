component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4867" ) );
	variables.params = { a:1, b:2 };
	variables.LF = chr( 10 );

	function run( testResults , testBox ) {
		describe( title='LDEV-4867' , body=function(){

			it( title='test query parsing, removing comments' , body=function() {
				doTest( ["-- foo", "/* bar */", "SELECT 'test'"]
					,[ "-- foo" , "/* bar */" ]);
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				doTest( [ "-- foo", "/* bar? */", "SELECT 'test'" ]
					,[ "-- foo", "/* bar? */" ]
				);
			});

			it( title='test query parsing, with a ? in a /* */ comment' , body=function() {
				doTest( [ "-- foo", "/* bar? */", "SELECT 'test'" ],
					[ "-- foo", "/* bar? */" ] );
			});
		
			it( title='test query parsing, with a ? and : in a comment' , body=function() {
				doTest( [ "-- foo ? :do", "/* bar? :*/", "SELECT 'test'" ],
					[ "-- foo ? :do", "/* bar? :*/" ]
				);
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				doTest( [ "-- foo ? :do", "/* bar? :*/", "SELECT 'test'" ],
					[ "-- foo ? :do", "/* bar? :*/" ]
				);
			});

		});
	}

	private function doTest( array sql, array comments ){
		var newlines = ArrayToList( arguments.sql, chr( 10 ) );
		executeTest( newlines, comments, "[LF]" );

		var paddedNewlines = ArrayToList( arguments.sql, " " & chr( 10 ) & " " );
		executeTest( paddedNewlines, comments, "[ LF ]" );

		var leading =  ArrayToList( arguments.sql, " " & chr( 10 ) );
		executeTest( leading, comments, "[ LF]" );

		var trailing =  ArrayToList( arguments.sql, chr( 10 ) & " " );
		executeTest( trailing, comments, "[LF ]" );
		
		var emptySqlComment =  ArrayToList( arguments.sql, "--" & chr( 10 ) & " " );
		executeTest( emptySqlComment, comments, "[--LF ]" );

		var emptySqlCommentParam =  ArrayToList( arguments.sql, "--?" & chr( 10 ) & " " );
		executeTest( emptySqlCommentParam, comments, "[--?LF ]" );

		var emptySqlCommentParam2 =  ArrayToList( arguments.sql, "--:foo" & chr( 10 ) & " " );
		executeTest( emptySqlCommentParam2, comments, "[--:fooLF ]" );
	}

	private function executeTest( string sql, array comments, string seperatorWhitespace ){
		_executeTest( arguments.sql, arguments.comments, 
			arguments.seperatorWhitespace & ", no extra" );
		_executeTest( " " & chr( 10 ) & arguments.sql, arguments.comments, 
			arguments.seperatorWhitespace & ", leading SPACE LF" );
		_executeTest( " " & chr( 10 ) & arguments.sql & " " & chr( 10 ), arguments.comments, 
			arguments.seperatorWhitespace & ", leading and trailing SPACE LF" );
		_executeTest( arguments.sql & " " & chr( 10 ), arguments.comments, 
			arguments.seperatorWhitespace & ", trailing SPACE LF" );
		_executeTest( chr( 10 ) & arguments.sql, arguments.comments,
			arguments.seperatorWhitespace & ",  leading LF");
		_executeTest( chr( 10 ) & arguments.sql & chr( 10 ), arguments.comments, 
			arguments.seperatorWhitespace & ", leading and trailing LF" );
		_executeTest( arguments.sql & chr( 10 ), arguments.comments, 
			arguments.seperatorWhitespace & ", trailing LF" );
	}

	private function _executeTest( string sql, array comments, string whitespaceDesc ){
		var result = doQuery( arguments.sql, arguments.whitespaceDesc );
		for ( var comment in arguments.comments ){
			expect ( result.sql ).toInclude( comment );
		}
	}

	private function doQuery( string sql, string whitespaceDesc ){
		try {
			query name="local.test" datasource="#ds#" params="#params#" dbtype="parseOnly" result="local.result" {
				echo( sql );
			}
		} catch (e) {
			systemOutput( "WHITESPACE: " & arguments.whitespaceDesc, true );
			if ( e.stackTrace.indexOf("lucee.runtime.exp.DatabaseException:") neq 0 )
				rethrow;
			systemOutput(e.stackTrace, true);
		}
	//	systemOutput("", true);
	//	systemOutput("Parsed: " & result.sql, true);
	//	systemOutput("Source: "& result.source, true);
		return result;
	}

}