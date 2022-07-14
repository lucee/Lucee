<cfscript>
	cookieTestData = [
		{name="test_tag1", value=serializeJSON({"test_tag1":"somevar",rules:[]}) },
		{name="test_tag2", value="Plain ASCII text: no problem"},
		{name="test_tag3", value="Semicolon in ASCII text; problem!"},
		{name="test_tag4", value='Quotes in ASCII text "encoded"'},
		{name="test_tag5", value=serializeJSON({"test_tag5":"somevar",rules:[]}), encodevalue=false},
		{name="test_tag6", value="Plain ASCII text: no problem", encodevalue=false},
		{name="test_tag8", value='Quotes in ASCII text "encoded"', encodevalue=false},
		{name="test_tag9", value=serializeJSON({"test_tag9":"somevar",rules:[]}), encodevalue=true},
		{name="test_tag10", value="Plain ASCII text: no problem", encodevalue=true},
		{name="test_tag11", value="Semicolon in ASCII text; problem!", encodevalue=true},
		{name="test_tag12", value='Quotes in ASCII text "encoded"', encodevalue=true},
		{name="test_str1", value=serializeJSON( {"test_str1":"somevar", rules:[] }) },
		{name="test_str2", value="Plain ASCII text: no problem"},
		{name="test_str3", value="Semicolon in ASCII text; problem!"},
		{name="test_str4", value='Quotes in ASCII text "encoded"'}
	]
</cfscript>

