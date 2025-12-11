<cfscript>
code = fileRead( getCurrentTemplatePath().replace("debug_ast.cfm", "dump.cfm") );
ast = astFromString( code, "tag" );
systemOutput( serialize( ast ), true );
</cfscript>
