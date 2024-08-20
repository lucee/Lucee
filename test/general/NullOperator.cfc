component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	

	function run( testResults , testBox ) {
		describe( "test new operator", function() {
			it(title="component: full name", body=function() {
				var cfc=new  org.lucee.cfml.Query();
				cfc.getName();
			});
			it(title="component: implicit org.lucee.cfml", body=function() {
				var cfc=new Query();
				cfc.getName();
			});

			it(title="class: full name", body=function() {
				var sb= new java.lang.StringBuilder("Susi");
				expect(sb.toString()).toBe("Susi");
			});

			it(title="class: implicit java.lang", body=function() {
				var sb= new StringBuilder("Susi");
				expect(sb.toString()).toBe("Susi");
			});

			it(title="class: implicit with asterix import", body=function() {
				import java.util.*;
				var map=new HashMap();
				expect(map.size()).toBe(0);
			});

			it(title="class: implicit with explicit import", body=function() {
				import java.util.HashMap;
				var map=new HashMap();
				expect(map.size()).toBe(0);
			});
			
		});
	}
}