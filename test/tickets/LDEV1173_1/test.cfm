<cfparam name="form.scene" default="">
<cfscript>
	qry = queryNew( 'foo,bar' );
	qry.addRow( {foo:1, bar:2} );
	qry.addRow( {foo:3, bar:4} );
	if(form.scene EQ 1){
		writeOutput(serializeJSON( qry, 'true' ) );
	}else if(form.scene EQ 2){
		writeOutput(serializeJSON( qry, 'struct' ) );
	}else if(form.scene EQ 3){
		writeOutput(serializeJSON( qry, 'row' ) );
	}else{
		writeOutput(serializeJSON( qry, 'column' ) );
	}
</cfscript>