<cfscript>
obj = invoke("ReturnsString", "returnsAny");
writeoutPut(isObject(obj));
</cfscript>