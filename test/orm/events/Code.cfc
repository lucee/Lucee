component persistent="true" extends="eventHandler" {

	property name="ID" type="numeric" fieldtype="id" ormtype="long";// generator="identity";
	property name="code" type="string";

	this.name = "code";
}