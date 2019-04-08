<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
	function run( testResults , testBox ) {
		describe( title="Test suite for DeserializeJson()", body=function() {
			it(title="checking testNumber", body = function( currentSpec ) {
				var sct=deserializeJson('{a:1}');
				assertEquals(1,sct.a);
			});
			it(title="checking testBoolean", body = function( currentSpec ) {
				var sct=deserializeJson('{a:true}');
				assertTrue(sct.a);
			});
			it(title="checking testNull", body = function( currentSpec ) {
				var sct=deserializeJson('{a:null}');
				assertTrue(isNull(sct.a));
			});
			it(title="checking testString", body = function( currentSpec ) {
				var sct=deserializeJson('{a:"Susi"}');
				assertEquals("Susi",sct.a);
				var sct=deserializeJson('{a:"##susi##"}');
				assertEquals("##susi##",sct.a);
			});
			it(title="checking testMustFail", body = function( currentSpec ) {
				var failed=false;
				try {
					var sct=deserializeJson('{a:susi}');
				}
				catch(local.e){
					failed=true;
				}
				if(!failed) throw "{a:susi} must fail";


				var failed=false;
				try {
					var sct=deserializeJson('{a:susi=1}');
				}
				catch(local.e){
					failed=true;
				}
				if(!failed) throw "{a:susi} must fail";


				var failed=false;
				try {
					var sct=deserializeJson('{a:[a,b,c]}');
				}
				catch(local.e){
					failed=true;
				}
				if(!failed) throw "{a:[a,b,c]} must fail";
			});

			it(title="checking testNumbersBreakingInByteForm1", body = function( currentSpec ) {
				var i=0;
				assertEquals("9465656331668701",deserializeJson('9465656331668701'));
			});
			it(title="checking testNumbersBreakingInByteForm2", body = function( currentSpec ) {
				var i=0;
				assertEquals("9465656331668701",Evaluate('"9465656331668701"'));
			});
			it(title="checking DeSerializeJSON() function", body = function( currentSpec ) {
				server.enable21=1;

				sct.a=listToArray('a,b,c,d');
				sct.b=true;
				sct['susi sorglos']="""";
				sct.d=[1,2,"qq""qq",arrayNew(1),23];

				qry=queryNew('aaa,bbb');

				QueryAddRow(qry);
				querysetCell(qry,'aaa',"a");
				querysetCell(qry,'bbb',"b");

				QueryAddRow(qry);
				querysetCell(qry,'aaa',"c");
				querysetCell(qry,'bbb',"d");

				assertEquals("1x", "#deserializeJSON(1)#x");
				assertEquals("true", "#deserializeJSON(true)#");
				assertEquals('su##si', "#deserializeJSON('"su##si"')#");
				assertEquals('sus"i', "#deserializeJSON('"sus\"i"')#");
				assertEquals('Januar, 01 2000 01:01:01', "#deserializeJSON('"Januar, 01 2000 01:01:01"')#");
				assertEquals('true', "#isArray(deserializeJSON('["a","b","c\"c"]'))#");

				s.a="x";
				assertEquals(true,"#isStruct(deserializeJSON('{"A":"x"}'))#");

				qry1='{"COLUMNS":["AAA","BBB"],"DATA":[["a","b"],["c","d"]]}';
				qry2='{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}';

				assertEquals('true',"#isQuery(deserializeJSON(qry1,false))#");
				assertEquals('false',"#isQuery(deserializeJSON(qry1,true))#");
				assertEquals('true',"#isQuery(deserializeJSON(qry2,false))#");
				assertEquals('false',"#isQuery(deserializeJSON(qry2,true))#");

				qry2='{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]},"XYZ":1}';
				assertEquals('false',"#isQuery(deserializeJSON(qry2,false))#");
				qry2='{"ROWCOUNT":2,"COLUMNS":1,"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}';
				assertEquals('false',"#isQuery(deserializeJSON(qry2,false))#");
				qry2='{"ROWCOUNT":2,"COLUMNS":[[1,2],"BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}';
				assertEquals('false',"#isQuery(deserializeJSON(qry2,false))#");

				q1='{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":[1.0,3.0],"bbb":[2.0,4.0]}}';
				q2='{"COLUMNS":["AAA","BBB"],"DATA":[[1.0,2.0],[3.0,4.0]]}';
				q3='{"susi":[[[{"COLUMNS":["AAA","BBB"],"DATA":[[1.0,2.0],[3.0,4.0]]}]]]}';
				q4='{"susi":[[[{"COLUMNS":["AAA","BBB"],"DATA":[[{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":[1.0,3.0],"bbb":[{"COLUMNS":["AAA","BBB"],"DATA":[[1.0,2.0],[3.0,4.0]]},4.0]}},2.0],[3.0,4.0]]}]]]}';

				str = savecontent1();
				sct=DeserializeJSON(str);
				assertEquals("\//-//''-''""""	","#sct.profile.identifier#");

				str = savecontent2();
				sct=DeserializeJSON(str);
				assertEquals("//-//''-''","#sct.profile.identifier#");

				str = savecontent3();
				sct=DeserializeJSON(str);
				assertEquals("//-//""-""","#sct.profile.identifier#");


				content = savecontent4();
				data=deserializejson(content);
				str="";
				loop index="i" from="1" to="#len(data.text)#" {
					str&=asc(mid(data.text,i,1));
				}
				assertEquals("98149813981298249830982718918883648252983642982510085978617417410006","#str#");

				json = savecontent5();
				str = savecontent6();
				assertEquals("#str#","#toAsc(deserializejson(json))#");
				str='"\u2765\u263a\u00ae\u00ae\u2716"';
				data=deserializejson(str);
				assertEquals("#toAsc(serializeJson(data,false,"us-ascii"))#","#toAsc(str)#");
			});
			
		});
	}

	private function toHex(nbr){
		var Integer=createObject('java','java.lang.Integer');
		var str=Integer.toHexString(nbr);
		while(len(str) LT 4)str=0&str;
		return str;
	}

	private function toAsc(str){
		var length=len(str);
		var res='';
		var i=1;
		for(;i<=length;i++){
			res&=asc(mid(str,i,1));
		}
		return res;
	}
	</cfscript>

	<cffunction name="savecontent1">
		<cfsavecontent variable="str">
			{"profile":{"identifier":"\\\/\/-//\'\'-''\"\"\t"}} 
		</cfsavecontent>
		<cfreturn str>
	</cffunction>

	<cffunction name="savecontent2">
		<cfsavecontent variable="str">
			{"profile":{"identifier":"//-//''-''"}} 
		</cfsavecontent>
		<cfreturn str>
	</cffunction>

	<cffunction name="savecontent3">
		<cfsavecontent variable="str">
			{"profile":{"identifier":'//-//"-"'}} 
		</cfsavecontent>
		<cfreturn str>
	</cffunction>

	<cffunction name="savecontent4">
		<cfsavecontent variable="content">
			{"geo":null,"truncated":false,"source":"web","created_at":"Thu Oct 0101:02:51 +00002009","in_reply_to_status_id":null,"favorited":false,"user":{"profile_background_image_url":"http://s.twimg.com/a/1254344155/images/themes/theme6/bg.gif","description":null,"profile_link_color":"FF3300","followers_count":8,"url":null,"following":null,"profile_background_tile":false,"friends_count":3,"profile_background_color":"709397","verified":false,"time_zone":null,"created_at":"SatMay 16 23:52:45 +00002009","statuses_count":5,"favourites_count":0,"profile_sidebar_fill_color":"A0C5C7","profile_sidebar_border_color":"86A4A6","protected":false,"profile_image_url":"http://a1.twimg.com/profile_images/266659234/5776822-2_normal.jpg","notifications":null,"location":null,"name":"brandonsloan","screen_name":"crookedbrandon","id":40568411,"geo_enabled":false,"utc_offset":null,"profile_text_color":"333333"},"in_reply_to_user_id":null,"in_reply_to_screen_name":null,"id":4512512993,"text":"\u2656\u2655\u2654\u2660\u2666\u2663\u00bd\u00bc\u20ac\u203c\u266c*\u2661\u2765\u263a\u00ae\u00ae\u2716"}
		</cfsavecontent>
		<cfreturn content>
	</cffunction>

	<cffunction name="savecontent5">
		<cfoutput>
			<cfsavecontent variable="json">"<cfloop index="i" from="1" to="9814">\u#toHex(i)#</cfloop>"</cfsavecontent>
		</cfoutput>
		<cfreturn json>
	</cffunction>

	<cffunction name="savecontent6">
		<cfoutput>
			<cfsavecontent variable="str"><cfloop index="i" from="1" to="9814">#i#</cfloop></cfsavecontent>
		</cfoutput>
		<cfreturn str>
	</cffunction>

</cfcomponent>