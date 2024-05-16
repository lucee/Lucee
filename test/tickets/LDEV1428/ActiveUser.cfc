component persistent="true" table="users1428" {
	property name="id" column="id" ormtype="int" notnull="true" fieldtype="id" generator="native";
	property name="firstName" column="firstName" ormtype="string" length="50";
	property name="Lastname" column="Lastname" ormtype="string" length="50";
	property name="UserName" column="UserName" ormtype="string" length="50";
	property name="Password" column="Password" ormtype="string" length="50";
}