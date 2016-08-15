component persistent="true" table="Roster_Embargoes" output="false" readonly="true"
{
	property name="teamID" column="teamID" insert="false" update="false" type="string" ormtype="string" fieldtype="id" ;
	property name="seasonID" column="seasonID" insert="false" update="false" type="numeric" ormtype="int" fieldtype="id" elementtype="int";
	property name="seasonUID" column="seasonUID" insert="false" update="false" type="string" ormtype="string";
	property name="embargoDate" column="embargoDate" insert="false" update="false" type="date" ormtype="timestamp";
}