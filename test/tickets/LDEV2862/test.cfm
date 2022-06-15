 <cfscript>
    try {
        res = entityLoad("test");
        ok = duplicate(res);
        writeoutput("success");
    }
    catch(any e) {
        writeoutput(e.message);
    } 
</cfscript>