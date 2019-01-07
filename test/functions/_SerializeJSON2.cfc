<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "test suite for SerializeJSON()", function() {
				it(title = "checking SerializeJSON() with their attributes", body = function( currentSpec ) {

					qry=queryNew('aaa,bbb');

					QueryAddRow(qry);
					querysetCell(qry,'aaa',"a");
					querysetCell(qry,'bbb',"b");

					QueryAddRow(qry);
					querysetCell(qry,'aaa',"c");
					querysetCell(qry,'bbb',"d");

					assertEquals(1, serializeJSON(1));
					assertEquals(true, serializeJSON(true));
					assertEquals('"susi"', serializeJSON('susi'));
					assertEquals('"Januar, 01 2017 01:01:01 +0530"', replace(serializeJSON(CreateDateTime(2017,1,1,1,1,1)),'January','Januar'));
					assertEquals('["a","b","c\"c"]', serializeJSON(listToArray('a,b,c"c')));

					var s.a = "x";
					assertEquals('{"A":"x"}', serializeJSON(s));
					assertEquals('{"COLUMNS":["AAA","BBB"],"DATA":[["a","b"],["c","d"]]}', serializeJSON(qry,false));
					assertEquals('{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}', serializeJSON(qry,true));

					sct=structNew();
					sct.aaa=true;
					assertEquals('{"AAA":true}', serializeJson(sct));

					sct=structNew();
					sct.aaa=true;
					sct['bbb']=true;
					assertEquals('AAA,bbb', listSort(structKeyList(deserializeJson(serializeJson(sct))),'text'));

					sct=structNew();
					sct['aaa']=true;
					assertEquals('{"aaa":true}', serializeJson(sct));



					qry=queryNew('aaa,BBB,cCc');

					assertEquals('{"COLUMNS":["AAA","BBB","CCC"],"DATA":[]}', serializeJson(qry));

					assertEquals('"susi\bsorglos"', serializeJSON('susi#chr(8)#sorglos'));
					assertEquals('"susi\tsorglos"', serializeJSON('susi#chr(9)#sorglos'));
					assertEquals('"susi\nsorglos"', serializeJSON('susi#chr(10)#sorglos'));
					assertEquals('"susi\fsorglos"', serializeJSON('susi#chr(12)#sorglos'));
					assertEquals('"susi\rsorglos"', serializeJSON('susi#chr(13)#sorglos'));
					assertEquals('"susi\\sorglos"', serializeJSON('susi\sorglos'));
					assertEquals('"susi\"sorglos"', serializeJSON('susi"sorglos'));
					assertEquals('"susi''sorglos"', serializeJSON("susi'sorglos"));

					assertEquals('"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo>bar</foo>"', serializeJSON( xmlParse ( "<foo>bar</foo>" ) ));

					var cfc = createObject('component','serializeJSON._Json');
					var ser = serializeJson(cfc);
					var cfc2 = deserializeJson(ser);
					assertEquals('A1,A2', ListSort(structKeyList(cfc2),'textnocase'));
					assertEquals("yes", IsStruct(cfc2));
					assertEquals("false", isValid('component',cfc2));

					cfc.susi = a;
					var ser = serializeJson(cfc);
					var cfc2 = deserializeJson(ser);
					assertEquals('A1,A2', ListSort(structKeyList(cfc2),'textnocase'));
					assertEquals("yes", IsStruct(cfc2));
					assertEquals("false", isValid('component',cfc2));

					var ser = serializeJson(a);
					var aa = deserializeJson(ser);
					assertEquals("public", aa.access);
					assertEquals(GetCurrentTemplatePath(), aa.PagePath);
					assertEquals("a", aa.Metadata.name);
					assertEquals(0, arrayLen(aa.Metadata.PARAMETERS));
					assertEquals(0, arrayLen(aa.MethodAttributes));

					<!--- UDF --->
					var ser=serializeJson(b);
					var aa=deserializeJson(ser);
					assertEquals("remote", "#aa.access#");
					assertEquals("#GetCurrentTemplatePath()#", "#aa.PagePath#");

					assertEquals("remote", "#aa.Metadata.access#");
					assertEquals("b", "#aa.Metadata.name#");
					assertEquals("wddx", "#aa.Metadata.RETURNFORMAT#");
					assertEquals("void", "#aa.Metadata.RETURNTYPE#");
					assertEquals("desc", "#aa.Metadata.DESCRIPTION#");
					assertEquals("dsp", "#aa.Metadata.DISPLAYNAME#");
					assertEquals("hi", "#aa.Metadata.HINT#");
					assertEquals("#false#", "#aa.Metadata.OUTPUT#");

					assertEquals("2", "#arrayLen(aa.Metadata.PARAMETERS)#");
					assertEquals("2", "#arrayLen(aa.MethodAttributes)#");
					assertEquals("b", "#aa.Metadata.PARAMETERS[2].name#");
					assertEquals("def", "#aa.Metadata.PARAMETERS[2].default#");
					assertEquals("ber", "#aa.Metadata.PARAMETERS[2].DISPLAYNAME#");
					assertEquals("h", "#aa.Metadata.PARAMETERS[2].HINT#");
					assertEquals("#false#", "#aa.Metadata.PARAMETERS[2].REQUIRED#");
					assertEquals("string", "#aa.Metadata.PARAMETERS[2].TYPE#");

					<!--- StringBuffer
					sb=createObject('java','java.lang.StringBuffer').init('susi');
					ser=serializeJson(sb);
					assertEquals('"susi"', ser)>
					 --->
					<!--- HashMap --->
					var sb = createObject('java','java.util.HashMap').init();
					sb.put(structNew(),"susi");
					var ser = serializeJson(sb);
					assertEquals('{"{}":"susi"}', ser);

					var sb = createObject('java','java.util.HashMap');
					var ser = serializeJson(sb);
					assertEquals('{}', ser);

					<!--- List --->
					var sb = createObject('java','java.util.ArrayList').init();
					sb.add("susi");
					var ser = serializeJson(sb);
					assertEquals('["susi"]', ser);
					var ser = serializeJson(sb.toArray());
					assertEquals('["susi"]', ser);

					<!--- char array --->
					str="ABC";
					var ser = serializeJson(str.toCharArray());
					assertEquals('"ABC"', ser);

					<!--- char array --->
					str="ABC";
					var ser = serializeJson(str.toCharArray());
					assertEquals('"ABC"', ser);

					<!--- java.lang.Math --->
					var sb = createObject('java','java.lang.Math');
					var ser = serializeJson(sb);
					var aa = deserializeJson(ser);
					assertEquals('E,PI', ListSort(structKeyList(aa),'text'));


					<!--- Locale --->
					setLocale('german (standard)');
					var sb = getLocale();
					var ser = serializeJson(sb);
					aa=deserializeJson(ser);
					assertEquals('"German (Standard)"', ser);

					<!--- java.util.Random --->
					var sb = createObject('java','java.util.Random');
					var ser = serializeJson(sb.init());
					aa=deserializeJson(ser);
					assertEquals('{"Seed":null}', ser);


					<!--- java.io.ByteArrayOutputStream --->
					var sb = createObject('java','java.io.ByteArrayOutputStream');
					var ser = serializeJson(sb);
					aa=deserializeJson(ser);
					assertEquals('{}', ser);



					<!--- java.io.File --->
					var sb = createObject('java','java.io.File').init(GetCurrentTemplatePath());
					var ser = serializeJson(sb);
					aa=deserializeJson(ser);
					assertEquals('"#replace(GetCurrentTemplatePath(),'\','\\','all')#"', ser);



					assertEquals("""html on a web page is enclosed in <html> and </html> tags""", serializeJSON('html on a web page is enclosed in <html> and </html> tags'));

					assertEquals("""javascript on a web page is enclosed in <script>and <\/script> tags""", serializeJSON('javascript on a web page is enclosed in <script>and </script> tags'));
					assertEquals("""/\\""", serializeJSON('/\'));
				});
			});

			describe( "test suite for serializeJSON with new arguments", function() {
				it(title="Checking serializeJSON() 1st argument data", body=function(){
					var serSettings =  getApplicationSettings().serialization;
					serSettings.serializeQueryAs = "column";
					application action="update" SerializationSettings=serSettings;
					var serdata = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
					var jsonObject = serializeJSON( data = serdata );
					local.tmpData = deserializeJSON(jsonObject);
					writeDump(local.tmpData);
					expect(local.tmpData).toBeTypeOf("struct");
					expect(listSort(structKeyList(local.tmpData), "text")).toBe("COLUMNS,DATA,ROWCOUNT");
				});

				it(title="Checking serializeJSON() 2nd argument queryFormat", body=function(){
					var serSettings =  getApplicationSettings().serialization;
					serSettings.serializeQueryAs = "column";
					application action="update" SerializationSettings=serSettings;
					var serdata = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
					var jsonObject = serializeJSON( data = serdata, queryFormat=false);
					local.tmpData = deserializeJSON(jsonObject);
					expect(local.tmpData).toBeTypeOf("struct");
					expect(listSort(structKeyList(local.tmpData), "text")).toBe("COLUMNS,DATA");
				});

				it(title="Checking serializeJSON() 3rd argument useSecureJSONPrefix", body=function(){
					var serdata = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
					var jsonObject = serializeJSON( data = serdata, queryFormat=false, useSecureJSONPrefix=true);
					expect(isJson(jsonObject)).toBeFalse();
				});

				it(title="Checking serializeJSON() 4th argument useCustomSerializer", body=function(){
					var uri = createURI("custom");
					local.result = _InternalRequest(
						template:"#uri#/index.cfm");
					expect(result.filecontent.trim()).toBe("SERIALISED");
				});
			});
		}

		private function a(){
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}

	</cfscript>

	<cffunction name="b" access="remote" description="desc" displayname="dsp" hint="hi" output="no" returntype="void" returnformat="wddx">
		<cfargument name="a" >
		<cfargument name="b" default="def" displayname="ber" hint="h" required="no" type="string">
	</cffunction>
</cfcomponent>