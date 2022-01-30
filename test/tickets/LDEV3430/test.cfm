<cfparam name="FORM.scene" default="">
<cfscript>
    if( form.scene == 1 ){
        res = isArray(ORMExecuteQuery("From test"));
    }
    if( form.scene == 2 ){
        res = isArray(ORMExecuteQuery("From test", {}, false, {}));
    }
    if( form.scene == 3 ){
        try{
            res = isArray(ORMExecuteQuery(hql="From test", params={}, unique=false, queryOptions={}));
        }
        catch(any e){
            res = e.message;
        }
    }
    if( form.scene == 4 ){
        try{
            res = isArray(ORMExecuteQuery(hql="From test"));
        }
        catch(any e){
            res = e.message;
        }
    }
    if( form.scene == 5 ){
        try{
            res = isArray(ORMExecuteQuery(hql="From test", params={}));
        }
        catch(any e){
            res = e.message;
        }
    }
    if( form.scene == 6 ){
        try{
            res = isArray(ORMExecuteQuery(hql="From test", params={}, unique=false));
        }
        catch(any e){
            res = e.message;
        }
    }
    writeoutput(res);
</cfscript>
