<cfscript>
final x = 1;
writeOutput( x );
final s = { a: 1 };
s.b = 2;
s.a = 5;
writeOutput( "," & listSort( structKeyList( s ), "text" ) & "," & s.a );
final arr = [ 1 ];
arrayAppend( arr, 2 );
arr[ 1 ] = 9;
writeOutput( "," & arrayToList( arr ) );
// final is still usable as a name
final = 7;
writeOutput( "," & final );
final += 1;
writeOutput( "," & final );
st = { final: 3 };
st.final++;
writeOutput( "," & st.final );
</cfscript>
