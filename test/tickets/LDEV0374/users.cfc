component persistent="true" table="users"{
	property name="id" fieldType="id" ormType="integer" generator="native";
	property name="DateJoined" ormtype="datetime" dbdefault="2016-10-10";
}