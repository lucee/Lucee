<cfsetting showdebugoutput="false"><cfscript>
code = entityNew( 'Code' );
code.setId( 1 );
code.setCode( 'a' );
entitySave( code );
ormFlush();
arr = entityLoad("Code", {code = "a"});
</cfscript>
