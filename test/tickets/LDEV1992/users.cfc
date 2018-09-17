component persistent="true" accessors="true" table="users"{
	property name="id" column="user_id" fieldType="id" generator="uuid";
	property name="firstName";
	property name="role" cfc="Role" fieldtype="many-to-one" fkcolumn="FKRoleID" lazy="true" notnull="false";
}

   