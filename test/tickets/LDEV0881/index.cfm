

<cfscript>
code = entityNew( 'Code' );
ref = entityNew( 'Ref' );

code.setId( 1 );
code.setCode( 'a' );

ref.setId( 1 );
ref.setCode(code);  

entitySave( code );
entitySave( ref );
	
ormFlush();

arr = entityLoad("Code", {code = "a"});
res=ormExecuteQuery("FROM Ref WHERE code IN (:codes)", {codes = arr});
echo(serialize(res));
</cfscript>
