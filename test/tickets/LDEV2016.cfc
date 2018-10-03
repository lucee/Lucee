component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2016", function() {
			it(title = "next() throwing NPE", body = function( currentSpec ) {
				var result='';
				var news = queryNew("id,title", "integer,varchar",[[1,"Dewey defeats Truman"],[2,"Men walk on Moon"]]);
				while(news.next()){
					result=news.title;
					break;
				}
				expect(result).toBe("Dewey defeats Truman");
			});
		});
	}
}