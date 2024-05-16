<cfscript>
    param name = "FORM.scene" default = "";
    param name = "FORM.value" default = "";

    public any function insertVal( value ) {
        queryexecute("INSERT INTO ldev3097 VALUES('#arguments.value#')",{},{ datasource = "LDEV3097_DSN" });
    }

    public any function selectData(){
        qry = queryexecute("SELECT * FROM ldev3097",{},{ datasource = "LDEV3097_DSN" });
        return qry.recordcount;
    }

    transaction{
        try{
            insertVal("Test");
            queryexecute('CREATE TABLE ldev3097 (name VARCHAR(200))',{},{ datasource = "LDEV3097_DSN" });
        }
        catch(any e){
            transaction action = "rollback";
            if ( form.scene eq 1 ){
                insertVal( value = Form.value );
                getData = selectData();
                writeoutput(getData);
            }
            if( form.scene eq 2 ){
                insertVal( value = Form.value );
                getData = selectData();
                writeoutput(getData);
                exit "exitTemplate";
            }
        }
    }
</cfscript>