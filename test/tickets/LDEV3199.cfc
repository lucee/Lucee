component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testbox ){
		describe( "Testcase for LDEV-3199", function(){
			it( title="Check with week()", body=function( currentSpec ){
				expect(week("{ts '2020-01-01 0:00:00'}")).toBe(1);
				expect(week("{ts '2020-05-01 0:00:00'}")).toBe(18);
				expect(week("{ts '2020-12-28 12:08:27'}")).toBe(53);
				expect(week("{ts '2020-12-31 0:00:00'}")).tobe(53);
				expect(week("{ts '2020-12-27 0:00:00'}")).tobe(53);
				expect(week("{ts '2024-12-29 0:00:00'}")).toBe(53);
			});
			it( title="Check with week(), year with 54 weeks", body=function( currentSpec ){
				expect(week("{ts '2000-12-31 0:00:00'}")).tobe(54);
				expect(week("{ts '2028-12-31 0:00:00'}")).tobe(54);
				expect(week("{ts '2056-12-31 0:00:00'}")).tobe(54);
				expect(week("{ts '2084-12-31 0:00:00'}")).tobe(54);
			});
		});
	}
}