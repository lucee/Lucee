<cfscript>
echo( serializeJSON( {
	"setting": getApplicationSettings().formUrlAsStruct,
	"urlKeys": structKeyList( url ),
	"urlIsStruct": structKeyExists( url, "a" ) && isStruct( url.a ),
	"formKeys": structKeyList( form ),
	"formIsStruct": structKeyExists( form, "f" ) && isStruct( form.f )
} ) );
</cfscript>
