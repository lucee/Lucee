component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function beforeAll(){
		variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4867" ) );
		variables.params = {
			id: {
				value: 1, sqltype="numeric"
			}
		};

		variables.LF = chr( 10 );
		variables.qry = QueryNew( "engine,id", "varchar,numeric", [
			[ "lucee", 1 ],
			[ "ralio" , 2 ]
		]);
		_setupDatabase();
	};

	function run( testResults , testBox ) {
		describe( title='LDEV-4867', body=function(){

			it( title='test query parsing, removing comments', body=function() {
				doTest( ["-- foo", "/* bar */", "SELECT engine from qry where id = :id "]
					,[ "-- foo" , "/* bar */" ]
				);
			});

			it( title='test query parsing, mixed nested comments', body=function() {
				doTest( ["/* bar -- foo */", "SELECT engine from qry where id = :id ", "/* bar -- foo */"]
					,[ "/* bar -- foo */" ]
				);
				doTest( ["--foo /* bar */", "SELECT engine from qry where id = :id ", "--foo /* bar */"]
					,[ "--foo /* bar */" ]
				);
			});

			it( title='test query parsing, nested comment blocks', skip=true, body=function() {
				// Nested comments aren't generally supported anyway....
				doTest( ["/* bar /* #LF# -- foo #LF# */ */", "SELECT engine from qry where id = :id "]
					,[ "/* bar /* #LF# -- foo #LF# */ */" ]
				);
			});

			it( title='test query parsing, with a ? in a comment', body=function() {
				doTest( [ "-- foo", "/* bar? */", "SELECT engine"," from qry", "where id = :id" ]
					,[ "-- foo", "/* bar? */" ]
				);
			});

			it( title='test query parsing, with a ? in a /* */ comment', body=function() {
				doTest( [ "-- foo", "/* bar? */", "SELECT engine"," from qry", "where id = :id" ],
					[ "-- foo", "/* bar? */" ] );
			});

			it( title='test query parsing, with a ? and : in a comment', body=function() {
				doTest( [ "-- foo ? :do", "/* bar? :*/", "SELECT engine"," from qry", "where id = :id" ],
					[ "-- foo ? :do", "/* bar? :*/" ]
				);
			});

			it( title='test query parsing, with a ? in a comment', body=function() {
				doTest( [ "-- foo ? :do", "/* bar? :*/", "SELECT engine"," from qry", "where id = :id" ],
					[ "-- foo ? :do", "/* bar? :*/" ]
				);
			});

			it( title='test query parsing, with a ? in a trailing line comment', body=function() {
				doTest( [ "SELECT engine"," from qry", "where id = :id",  "-- foo ? :do" ],
					[ "-- foo ? :do" ]
				);
			});

			it( title='test query parsing, with a ? in a trailing comment block', body=function() {
				doTest( [ "SELECT engine"," from qry", "where id = :id",  "/* foo ? :do */" ],
					[ "/* foo ? :do */" ]
				);
			});

			// this fails in the old zzsql parser, works fine with h2
			it( title='test query parsing, with comments in quotes ', body=function() {
				doTest( [ "SELECT engine, '/* multi */  // -- single' AS comments FROM qry WHERE id = :id"],
					[ "/* multi */  // -- single" ]
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

		// real database, h2
		var db = doQuery( arguments.sql, arguments.whitespaceDesc, "" );
		for ( var comment in arguments.comments ){
			expect ( db.result.sql ).toInclude( comment );
		}
		expect( db.rs.engine ).toBe( "lucee" );

		// QoQ
		var qoq = doQuery( arguments.sql, arguments.whitespaceDesc, "query" );
		for ( var comment in arguments.comments ){
			expect ( qoq.result.sql ).toInclude( comment );
		}
		expect( qoq.rs.engine ).toBe( "lucee" );
		// systemOutput( out.result, true );
	}

	private function doQuery( string sql, string whitespaceDesc, string dbtype ){
		try {
			query name="local.rs" datasource="#ds#" params="#params#" dbtype="#arguments.dbtype#" result="local.result" {
				echo( sql );
			}
		} catch (e) {
			systemOutput( "WHITESPACE: " & arguments.whitespaceDesc, true );
			systemOutput("Source: "& sql, true);
		//	if ( e.stackTrace.indexOf("lucee.runtime.exp.DatabaseException:") neq 0 )
			rethrow;
			//systemOutput(e.stackTrace, true);
		}
		/*
		systemOutput("", true);
		systemOutput("Parsed: " & result.sql, true);
		systemOutput("Source: "& sql, true);
		*/
		return {
			result: result,
			rs: rs
		}
	}

	private function _setupDatabase() {
		query datasource=ds {
			echo("CREATE TABLE qry ( id int NOT NULL, engine varchar(255) ) ");
		}
		var delim = "";
		query datasource=ds {
			echo("insert into qry ( id, engine ) VALUES ");
			loop query="qry" {
				echo(" #delim# ( #qry.id#, '#qry.engine#' ) ");
				delim = ",";
			}
		}
	}

}