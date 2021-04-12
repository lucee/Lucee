component {
	this.name = "ac";

	mySQL = getCredentials();
	if(mySQL.count()!=0){
		this.datasource=mySQL;
	}

	public function onApplicationStart() {
		query {
			echo("DROP PROCEDURE IF EXISTS `LDEV1917SP`");
		}
		query {
			echo("DROP TABLE IF EXISTS `LDEV1917`");
		}
		query {
			echo("CREATE TABLE LDEV1917 (null_Value nvarchar(10))");
		}
		query {
			echo("
				CREATE PROCEDURE `LDEV1917SP`(IN null_Value nvarchar(10)) 
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