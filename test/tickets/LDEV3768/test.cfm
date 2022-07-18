<cfparam name="FORM.scene" default="">
<cfscript>
    if( form.scene == 1 ){
        res = isArray(ORMExecuteQuery("From test where Ant = :ok",{"ok":'lucee'}));
    }
    if( form.scene == 2 ){
        res = isArray(ORMExecuteQuery("From test where ant = 'lucee'"));
    }
    if( form.scene == 3 ){
        try{
            res = isArray(ORMExecuteQuery("From test where ant = :ok",{"ok":'lucee'}));
        }
        catch(any e){
            res = e.message;
        }
    }
    writeoutput(res);
</cfscript>
