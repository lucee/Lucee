component {
	this.name = "LDEV-4017";

	param name="form.dbfile" default="";

	this.datasource= server.getDatasource("H2", form.dbfile);

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropCreate",
		dialect = "H2"
	};

	function onRequestStart(){
		query {
			echo("INSERT INTO persons(id, name) VALUES('#form.uuid#','lucee')");
		}
		query {
			echo("INSERT INTO thoughts(id, body, FK_person) VALUES('#createUUID()#','lazy-loaded works outside of transcation', '#form.uuid#')");
		}
	}
}