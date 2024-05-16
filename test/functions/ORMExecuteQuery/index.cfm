<cfscript>
if(isNull(form.scene)) form.scene="";

transaction {
    EntitySave( 
        entityNew( "Person", { name : "Susi", id : createUUID() } ) 
    );
}



// inline parameter
if(form.scene=="inline") {
    result = ormExecuteQuery(
        hql:"SELECT id FROM Person WHERE name = 'Susi'",
        unique:true
    );
    echo(result);
} 

// struct parameter
if(form.scene=="struct") {
    result = ormExecuteQuery(
        "SELECT id FROM Person WHERE name = :name",
        {name:"Susi" } ,
        true
    );
    echo(result);
} 

// array parameter
if(form.scene=="array") {
    result = ormExecuteQuery(
        "SELECT id FROM Person WHERE  name = ?1",
        [ "Susi" ],
        true
    );
    echo(result);
}  

// legacy parameter
if(form.scene=="legacy") {
    result = ormExecuteQuery(
        "SELECT id FROM Person WHERE  name = ?",
        [ "Susi" ],
        true
    );
    echo(result);
} 

       
</cfscript>