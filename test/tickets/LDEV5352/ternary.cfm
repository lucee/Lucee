<cfscript>
	results = (1 == 1)
		? function (input){
			return true;
		}
		// by setting to an empty string, we cause the standard total formatting to be applied
		: "";
</cfscript><cfoutput>ok</cfoutput>