<cfscript>
    function isPositive(required numeric input, boolean debug=false) {
        absValue = abs(arguments.input);
        return (absValue == arguments.input);
    }
    testValue = "0.08263888888888889";
    writeOutput("#isPositive(testValue)#");
</cfscript>