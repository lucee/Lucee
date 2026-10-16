component persistent=true table="LDEV6139" {
	property name="id" generator="native" ormtype="integer";
	property name="name" ormtype="string" length="255" notnull=true unique=true;
}
