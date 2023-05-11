component persistent="true" table="persons" {
	property name="id" type="string" ormtype="string" fieldtype="id";
	property name="name" type="string" ormtype="string";

	property name="thoughts"
		cfc="Thought"
		fieldtype="one-to-many"
		fkcolumn="FK_person"
		lazy="true"
		inverse=true;
}