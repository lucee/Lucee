<cfscript>
setting showdebugoutput="false";
testString = "Hello {%1%} ! You like {%2%} ?";
result = refind("{%\d%}", testString, 1 ,  true, "all");
writeOutput(IsArray(result));
if(IsArray(result)){
    writeOutput("||");
    writeOutput(arrayLen(result));
}
</cfscript>