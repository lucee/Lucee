<cfscript>
    param name="form.undefined" default=false;
    param name="form.initialValue" default=false;
    param name="form.defaultValue" default="";
    try{
        if(form.undefined){ 
            defaultValue = evaluate('undefinedVar ?? form.defaultValue');
        }
        if(form.initialValue != false){
            defaultValue = evaluate('form.initialValue ?? form.defaultValue');
        }
        else{
            defaultValue = evaluate('null ?? form.defaultValue');
        }
        
        writeOutput(defaultValue);
    } catch (any e) {
        writeOutput(e.message);
    }
</cfscript>

    
