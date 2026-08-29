<cfscript>
// LDEV-6303 null-edge probe. Exercises each form of default to record actual current behavior
// under both NULLSupport modes. Pure observation — no assertions.
// Run via: http://7.localhost:7888/test71/tickets/LDEV6303/null-probe.cfm

setting showdebugoutput="false";

function probeOnce( required string label ) {
    var inst = new NullProbeCfc();
    var props = getMetadata( inst ).properties;
    var rows = [];
    for ( var p in props ) {
        var sub = inst._probe( p.name );
        var row = {
              "name"            : p.name
            , "metaHasDefault"  : structKeyExists( p, "default" )
            , "metaDefaultValue": structKeyExists( p, "default" ) ? ( isNull( p.default ) ? "<null>" : "[" & ( isSimpleValue( p.default ) ? p.default : serializeJson( p.default ) ) & "]" ) : "<absent>"
            , "metaIsSimple"    : structKeyExists( p, "default" ) ? ( isNull( p.default ) ? "<null>" : isSimpleValue( p.default ) ) : "<absent>"
            , "metaIsObject"    : structKeyExists( p, "default" ) ? ( isNull( p.default ) ? "<null>" : isObject( p.default ) ) : "<absent>"
            , "varsHasKey"      : sub.varsHasKey
            , "varsValueIsNull" : sub.varsValueIsNull
            , "varsValue"       : sub.varsValue
        };
        arrayAppend( rows, row );
    }
    return { label: arguments.label, rows: rows };
}

scenarios = [];

// Default mode (probably fullNullSupport=false depending on Application.cfc inheritance)
arrayAppend( scenarios, probeOnce( "default mode" ) );

// Force fullNullSupport=true for the second probe
application action="update" nullSupport=true;
arrayAppend( scenarios, probeOnce( "fullNullSupport=true" ) );

// Force fullNullSupport=false for the third probe
application action="update" nullSupport=false;
arrayAppend( scenarios, probeOnce( "fullNullSupport=false" ) );

// Render
echo( "<h2>LDEV-6303 null-edge probe — engine: " & server.lucee.version & "</h2>" );
for ( s in scenarios ) {
    echo( "<h3>scenario: " & s.label & "</h3>" );
    echo( "<table border=1 cellpadding=4 style='border-collapse:collapse'>" );
    echo( "<tr><th>property</th><th>metaHasDefault</th><th>metaDefaultValue</th><th>metaIsSimple</th><th>metaIsObject</th><th>varsHasKey</th><th>varsValueIsNull</th><th>varsValue</th></tr>" );
    for ( r in s.rows ) {
        echo( "<tr>" );
        for ( k in [ "name", "metaHasDefault", "metaDefaultValue", "metaIsSimple", "metaIsObject", "varsHasKey", "varsValueIsNull", "varsValue" ] ) {
            echo( "<td>#encodeForHtml( "" & r[ k ] )#</td>" );
        }
        echo( "</tr>" );
    }
    echo( "</table>" );
}

echo( "<pre>" );
echo( chr( 10 ) & "engine: " & server.lucee.version & chr( 10 ) );
for ( s in scenarios ) {
    echo( chr( 10 ) & "===== scenario: " & s.label & " =====" & chr( 10 ) );
    for ( r in s.rows ) {
        echo( chr( 10 ) & "[#r.name#]" & chr( 10 ) );
        for ( k in [ "metaHasDefault", "metaDefaultValue", "metaIsSimple", "metaIsObject", "varsHasKey", "varsValueIsNull", "varsValue" ] ) {
            echo( "  #k# = #r[k]#" & chr( 10 ) );
        }
    }
}
echo( "</pre>" );
</cfscript>
