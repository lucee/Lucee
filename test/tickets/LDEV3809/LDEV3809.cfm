<cfscript>
    num = 3;
    writeoutput("dateAdd a result:" & dateFormat(DateAdd("d", -num, "01/04/2022"), "dd/mm/yyyy"));
</cfscript>