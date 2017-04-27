component persistent="true" table="users"{
	property name="id" fieldType="id" ormType="integer" generator="native";
	property name="name" ormtype="varchar" dbdefault="user";
}