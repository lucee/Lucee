component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ){

		describe( "test hsqldb qoq support ", function(){

			xit( "test qoq same column types ", function(){

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
					echo("SELECT news.title, news2.title FROM news, news2");
				}

				expect ( q.recordcount ).toBe( 4 );
			});

			it( "test qoq different column types ", function(){

				var news = queryNew("id,title","integer,varchar", [
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				var news2 = queryNew("id,title", "integer,varchar",[
					{"id":1,"title":"Dewey defeats Truman"},
					{"id":2,"title":"Man walks on Moon"}
				]);

				query name="local.q" dbtype="query" {
					echo("SELECT news.title, news2.title FROM news, news2");
				}

				expect ( q.recordcount ).toBe( 4 );
			});

		} );
	}

}
