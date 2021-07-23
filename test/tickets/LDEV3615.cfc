component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" skip=true {

	function testQoQunion (){
		var q1 = queryNew(
			"subtype",
			"varchar",
			[["RECORD3_TEMPLATE"]]
		);
		var q2 = queryNew(
			"id",
			"int",
			[[1]]
		);
			
		```
			<cfquery name="local.result" dbtype="query">
					SELECT 	subtype,
							subtype AS subject
					FROM 	q1
					UNION
					SELECT 	NULL AS subtype,
							NULL AS subject
					FROM	q2
			</cfquery>
		
		```
		expect( serializeJson( local.result ) ).toBe('{"COLUMNS":["subtype","subject"],"DATA":[["RECORD3_TEMPLATE","RECORD3_TEMPLATE"],["",""]]}');
	}
	
}