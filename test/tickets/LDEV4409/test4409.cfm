<cfscript>
	num = 123;
	value = numberFormat(1/num, '999.99');
	writeOutput(num * value);
</cfscript>