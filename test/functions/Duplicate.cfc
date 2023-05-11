component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function run( testResults , testBox ) {
		describe( "test case for Duplicate", function() {
			it(title = "Checking with Duplicate", body = function( currentSpec ) {
		 		cfapplication (action="update" clientmanagement="true");
				<!--- begin old test code --->
				<!--- String --->
					str="String";
					variables.test={};
					str2=duplicate(str);
					str="String 2";
					assertEquals("String", "#str2#");
				<!--- Number --->
					str=1+1;
					str2=duplicate(str);
					str=str+1;
					assertEquals("2", "#str2#");
				<!--- boolean --->
					str=true;
					str2=duplicate(str);
					str=false;
					assertEquals("true", "#str2#");
				<!--- struct --->
					str=structNew();
					str.data="aaaaa";
					str2=duplicate(str);
					str.data="bbbbb";
					assertEquals("aaaaa", "#str2.data#");
				<!--- array --->
					str=arrayNew(1);
					str[1]="aaaaa";
					str2=duplicate(str);
					str[1]="bbbbb";
					assertEquals("aaaaa", "#str2[1]#");
				<!--- query --->
					qry=queryNew("col");
					QueryAddRow(qry);
					QuerySetCell(qry,"col","aaaaa");
					qry2=duplicate(qry);
					QuerySetCell(qry,"col","bbbbb");
					assertEquals("aaaaa", "#qry2.col#");
				if(server.ColdFusion.ProductName eq "RAILO"){
					cfobject(type="component",name="c",component="duplicate.comps.some.Hello");
					assertEquals("0", "#c.get()#");
					d=duplicate(c);
					c.set(1);
					assertEquals("1", "#c.get()#");
					assertEquals("0", "#d.get()#");
				}
				<!--- not working in JSR223env --->
				if(server.lucee.environment=="servlet"){
					duplicate(client);
					duplicate(session);
					duplicate(application);
					duplicate(cgi);
				}
				duplicate(request);
				// duplicate(variables);
				duplicate(server);

				savecontent variable="xrds"{
					writeOutput('<?xml version="1.0" encoding="UTF-8"?><xrd><Service priority="10"><Type>http://openid.net/signon/1.0</Type><URI priority="15">http://resolve2.example.com</URI><URI priority="10">http://resolve.example.com</URI><URI>https://resolve.example.com</URI></Service></xrd>');
				}
				xrds = xmlParse(trim(xrds)).xmlRoot;
				xrdsService = xrds.xmlChildren[1];
				xrdsService.xmlChildren[2] = duplicate(xrdsService.URI[2]);
				xrdsService.URI[1] = duplicate(xrdsService.URI[2]);
				<!--- end old test code --->
			});
		});	
	}
}