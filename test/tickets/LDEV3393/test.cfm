<cfscript>
    try{
        arr = [1,2,3,4];
        res = [];
    }
    catch(any e){}
    finally {
        for( item in arr){
            res.append(item);
        }
        writeoutput(serializeJSON(res));
    }
</cfscript>