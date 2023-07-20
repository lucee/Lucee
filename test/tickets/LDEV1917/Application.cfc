component {
	this.name = "ldev-1917";

	param name="form.datatype";
	param name="form.notNull" default="false";


	if (form.datatype neq "char" and form.datatype neq "nvarchar")
		throw "bad datatype [#form.datatype#]";

	mySQL = getCredentials();
	if(mySQL.count()!=0){
		this.datasource=mySQL;
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;

		var extra= form.notNull ? " NOT NULL" : "";

		query {
			echo("DROP PROCEDURE IF EXISTS `LDEV1917SP`");
		}
		query {
			echo("DROP TABLE IF EXISTS `LDEV1917`");
		}
		query {
			echo("CREATE TABLE LDEV1917 (null_Value #form.datatype#(10) #extra# )");
		}
		query {
			echo("
				CREATE PROCEDURE `LDEV1917SP`(IN null_Value #form.datatype#(10)) 
				BEGIN
					INSERT INTO LDEV1917 VALUE(null_Value);
				END
			");
		}
	}
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}