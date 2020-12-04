<cfscript>
    try{
        arr = ["one","two","three","four","five"];
        res = arrayPop(arr,arr);
        writeOutput(res);
    }
    catch(any e){
        res = e.message;
        writeOutput(res);
    }
</cfscript>