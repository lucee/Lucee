component persistent="true" table="supportticket"{

	property name="ID" column="id" fieldtype="id" generator="identity" html="text|hidden";
	property name="createdOn" column="createdOn" ormtype="timestamp" html="date|picker";
	property name="userID" column="userID" ormtype="integer" default="0";
	property name="status" column="status" ormtype="integer" default="1";
	property name="companyUserID" column="companyUserID" ormtype="integer" default="0";
	property name="companyID" column="companyID" ormtype="integer" default="0";
	property name="subject" column="subject" ormtype="string" default="";
	property name="lastUpdatedOn" column="lastUpdatedOn" ormtype="timestamp";
	property name="messageCount" column="messageCount" ormtype="integer" default="0";
	property name="s3Folder"  column="s3Folder" type="string" default="true";

	property name="closedOn" column="closedOn" ormtype="timestamp";
	property name="closedBy" column="closedBy" ormtype="integer" default="0";

	property name="fullName"  column="fullName" type="string" default="";
	property name="email"  column="email" type="string" default="";
	property name="skypeID"  column="skypeID" type="string" default="";

	property name="phone"  column="phone" type="string" default="";

	property name="user" fieldtype="many-to-one" cfc="Users" fkcolumn="userID" insert="false" update="false";

	this._isNewTicket = true;
}