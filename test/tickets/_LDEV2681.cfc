component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		struct1 = {
		  	user_name: "Lucee",
		  	company_name: "Lucee",
  			company_url: "Lucee.org"
		};

		struct2 = {
			userName: "Lucee",
			companyname: "Lucee",
  			companyurl: "Lucee.org"
		};

		struct1.each(function(key,value){
			struct1["{{" & key & "}}"] = value;
			struct1.delete(key);
		});

		struct2.each(function(key,value){
			struct2["{{" & key & "}}"] = value;
			struct2.delete(key);
		});
		describe( "test suite for LDEV2681", function() {
			it(title = "structkey with '_' in structeach function", body = function( currentSpec ) {
				expect("true").toBe(structkeyexists(struct1,"{{user_name}}"));
				expect("true").toBe(structkeyexists(struct1,"{{company_name}}"));
				expect("true").toBe(structkeyexists(struct1,"{{company_url}}"));
			});

			it(title = "structkey without '_' in structeach function", body = function( currentSpec ) {
				expect("true").toBe(structkeyexists(struct2,"{{username}}"));
				expect("true").toBe(structkeyexists(struct2,"{{companyname}}"));
				expect("true").toBe(structkeyexists(struct2,"{{companyurl}}"));
			});
		});
	} 
}
