component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ){

		describe( "test hsqldb qoq support ", function(){

			xit( "test qoq different column types, duplicate columns ", function(){

				var news = queryNew("id,title","bit,varchar",[  // note bit
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				var news2 = queryNew("id,title", "integer,varchar",[
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				// throws  [incompatible data type in operation], but if you alias the second title column, we load a subset of data and it works
				query name="local.q" dbtype="query" {
					echo("SELECT news.title, news2.title FROM news, news2"); // duplicate column names
				}

				expect ( q.recordcount ).toBe( 4 );
			});

			it( "test qoq different column types, aliased duplicate columns ", function(){

				var news = queryNew("id,title","bit,varchar",[  // note bit
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				var news2 = queryNew("id,title", "integer,varchar",[
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				// throws  [incompatible data type in operation]
				query name="local.q" dbtype="query" {
					echo("SELECT news.title, news2.title as t2 FROM news, news2"); // aliased column names
				}

				expect ( q.recordcount ).toBe( 4 );
			});

			it( "test qoq same column types, aliased duplicate column names ", function(){

				var news = queryNew("id,title","bit,varchar",[  // note bit
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				var news2 = queryNew("id,title", "integer,varchar",[
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				// throws  [incompatible data type in operation]
				query name="local.q" dbtype="query" {
					echo("SELECT news.title, news2.title as t2 FROM news, news2"); // note alias
				}

				expect ( q.recordcount ).toBe( 4 );
			});

			it( "test qoq different column types, duplicate column names ", function(){

				var news = queryNew("id,title","integer,varchar", [
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				var news2 = queryNew("id,title", "integer,varchar",[
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				query name="local.q" dbtype="query" {
					echo("SELECT news.title, news2.title FROM news, news2"); // duplicate column names
				}

				expect ( q.recordcount ).toBe( 4 );
			});

			it( "test qoq different column types, aliased duplicate column names ", function(){

				var news = queryNew("id,title","integer,varchar", [
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				var news2 = queryNew("id,title", "integer,varchar",[
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				query name="local.q" dbtype="query" {
					echo("SELECT news.title, news2.title as t2 FROM news, news2"); // note alias
				}

				expect ( q.recordcount ).toBe( 4 );
			});

		} );
	}

}
