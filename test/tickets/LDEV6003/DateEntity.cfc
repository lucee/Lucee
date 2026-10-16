component persistent="true" accessors="true" table="LDEV6003" output="false" {

	property name="pk" column="pk" type="numeric" fieldtype="id" generator="identity" ormtype="int" unsavedvalue="0";
	property name="recordDate" type="datetime" insert="false" update="false";
	property name="someInt";
	property name="someDate" type="datetime";

	public DateEntity function init() output=false {
		setPk( 0 );
		return this;
	}

}
