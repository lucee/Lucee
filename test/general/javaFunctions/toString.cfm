<cfscript>
	String function to_string(String str1, String str2) type="java" {
		return new java.lang.StringBuilder(str1).append(str2).toString();
   	}
	echo(to_string("a","b"));
</cfscript>