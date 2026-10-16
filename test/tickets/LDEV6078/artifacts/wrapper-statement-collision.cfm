<cfscript>
// LDEV-6078: Wrapper statement line collision
// Bug: bc.visitLine(start) then _writeOut(bc) where child also calls visitLine
// The wrapper's line and first child's line share same bytecode label

// Pattern 1: for loop - for keyword line collides with init statement
for ( var i = 1; i <= 3; i++ ) {
	systemOutput( i, true );
}

// Pattern 2: while loop - while keyword line collides with condition
var count = 0;
while ( count < 3 ) {
	count++;
}

// Pattern 3: if statement - if keyword line collides with condition
var value = 10;
if ( value > 5 ) {
	systemOutput( "big", true );
}

// Pattern 4: nested wrappers - each level can collide
for ( var x = 1; x <= 2; x++ ) {
	for ( var y = 1; y <= 2; y++ ) {
		if ( x == y ) {
			systemOutput( "#x#,#y#", true );
		}
	}
}
</cfscript>
