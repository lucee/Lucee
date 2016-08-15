component persistent="true" {

	property name="ID" type="numeric" fieldtype="id" ormtype="long";
	property name="code" 
		 	 fieldtype="many-to-one" 
		     fkcolumn="codeID"
		     cfc="Code";

}