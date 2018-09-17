component persistent="true" table="roles"{

	property name="roleID" column="roleID" fieldType="id" generator="native";
	property name="role";
	
	// O2M -> Users
	property name="users" singularName="user" fieldtype="one-to-many" type="array" lazy="extra"
			  cfc="users" fkcolumn="FKRoleID" inverse="true" cascade="all-delete-orphan"; 
	
}