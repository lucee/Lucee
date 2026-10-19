component accessors=true {
    property name="literalHello"   type="any" default="hello";
    property name="literalEmpty"   type="any" default="";
    property name="noDefault"      type="any";
    property name="nullExprDef"    type="any" default="#nullValue()#";
    property name="emptyStructDef" type="any" default='#{}#';
    property name="emptyArrayDef"  type="any" default='#[]#';
    property name="oneKeyStruct"   type="any" default='#{a:1}#';
    property name="twoKeyStruct"   type="any" default='#{a:1,b:2}#';
    property name="oneItemArray"   type="any" default='#[42]#';
    property name="threeItemArray" type="any" default='#["x","y","z"]#';
    property name="funcCallDef"    type="any" default="#repeatString( 'x', 5 )#";
    property name="nestedFuncDef"  type="any" default="#listToArray( 'a,b,c' )#";

    // Expose variables scope for the probe — that's the scope ORM CFCSetter actually reads.
    function _probe( required string key ) {
        return {
              "varsHasKey"  : structKeyExists( variables, arguments.key )
            , "varsValueIsNull" : structKeyExists( variables, arguments.key ) ? isNull( variables[ arguments.key ] ) : "<absent>"
            , "varsValue"   : structKeyExists( variables, arguments.key ) ? ( isNull( variables[ arguments.key ] ) ? "<null>" : "[" & ( isSimpleValue( variables[ arguments.key ] ) ? variables[ arguments.key ] : serializeJson( variables[ arguments.key ] ) ) & "]" ) : "<absent>"
        };
    }
}
