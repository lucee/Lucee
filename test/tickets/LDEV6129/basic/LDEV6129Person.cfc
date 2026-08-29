component persistent="true" entityname="LDEV6129Person" {

	property name="id"   fieldtype="id" type="numeric" ormtype="long" generator="increment";
	property name="name" type="string" unique="true";

}
