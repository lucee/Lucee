component output="false" accessors="true"{
	/**
	 * @hint Constructor
	 * @dataSource The name of the data source from which this query should retrieve data.
 	*/
	public void function init(string dataSource){
		variables.datasource = "";
		if(!isNull(arguments.dataSource)){
			variables.datasource = arguments.dataSource;
		}
		variables.select = [];
		variables.from = [];
		variables.where = "";
		variables.join = "";
	}

	/**
	 * @hint To execute queries you need a datasource
	 * @dataSource The name of the data source from which this query should retrieve data.
 	*/
	public void function setDatasource(required string dataSource){
		variables.datasource = arguments.dataSource;
	}

	/**
	 * @hint Select the columns available in table
	 * @colNames Column names which is present in table, You can passed it by array or string
	 * @overwrite Overwite clears existing selected column
 	*/

	public void function select(any colNames, boolean overwrite=false){
		if(overwrite) {
			if(isSimpleValue(arguments.colNames))
			variables.select=[arguments.colNames];
			else
			variables.select = arguments.colNames;
		}
		else {
			if(isSimpleValue(arguments.colNames))
				arrayAppend(variables.select, arguments.colNames);
			else {
				loop array=arguments.colNames item="local.el" {
					arrayAppend(variables.select, el);
				}
			}
		}
	}

	/**
	 * @hint entered the table name
	 * @table table name to execute the query
 	*/

	public void function from(required any table, boolean overwrite=false){
		if(overwrite) {
			if(isSimpleValue(arguments.table))
			variables.from=[arguments.table];
			else
			variables.from = arguments.table;
		}
		else {
			if(isSimpleValue(arguments.table))
				arrayAppend(variables.from, arguments.table);
			else {
				loop array=arguments.table item="local.el" {
					arrayAppend(variables.from, el);
				}
			}
		}
	}

	/**
	 * @hint where clause used to filer out the records
	 * @condString passed the condtion to filter out the data
 	*/

	public void function where(string condString){
		if(arguments.condString NEQ ""){
			variables.where = " where #arguments.condString#";
		}
	}

	/**
	 * @hint Joins used for inner join
	 * @table table which you want to join
	 * @on Joins table based on their column
 	*/

	public void function join(required string table, required string on){
		if(!arrayisEmpty(variables.from)){
			variables.Join &= " JOIN #arguments.table# #arguments.on#"
		} else {
			throw(type="Invalid SQL statment", message="Please provide the table name");
		}
	}

	/**
	 * @hint Joins used for inner join
	 * @table table which you want on Left join
	 * @on Joins table based on their column
 	*/
	public void function leftJoin(required string table, required string on){
		if(!arrayisEmpty(variables.from)){
			variables.Join &= " LEFT JOIN #arguments.table# #arguments.on#"
		} else {
			throw(type="Invalid SQL statment", message="Please provide the table name");
		}
	}

	/**
	 * @hint Joins used for inner join
	 * @table table which you want on Right join
	 * @on Joins table based on their column
 	*/

	public void function rightJoin(required string table, required string on){
		if(!arrayisEmpty(variables.from)){
			variables.Join &= " Right JOIN #arguments.table# #arguments.on#"
		} else {
			throw(type="Invalid SQL statment", message="Please provide the table name");
		}
	}

	/**
	 * @hint get the information of datasource
	 * @datasource dataSource name which you want info
 	*/

	public any function getDatasourceInfo( required string datasource ) {
		var pc = getPageContext();
		var ds = pc.getDatasource(arguments.dataSource);

		var sct=structNew("linked");
		try{sct['host']=ds.getHost();}catch(e){}
		try{sct['port']=ds.getPort();}catch(e){}
		try{sct['database']=ds.getDatabase();}catch(e){}
		sct['connectionString']=ds.getConnectionStringTranslated();
		try{sct['literalTimestampWithTSOffset']=ds.getLiteralTimestampWithTSOffset();}catch(e){}
		try{sct['alwaysSetTimeout']=ds.getAlwaysSetTimeout();}catch(e){}
		try{sct['dbDriver']=ds.getDBDriver();}catch(e){}
		sct['connectionLimit']=ds.getConnectionLimit();
		sct['connectionTimeout']=ds.getConnectionTimeout();
		sct['timeZone']=ds.getTimeZone();
		var cd=ds.getClassDefinition();

		sct['class']=cd.getClassName();
		sct['bundleName']=cd.getName();
		sct['bundleVersion']=cd.getVersion()&"";

		return sct;
	}

	/**
	 * @hint Execute the query
	*/
	public any function execute(){
		if(variables.datasource NEQ ""){
			var q = new Query(datasource=variables.dataSource);
			if(ArrayisEmpty(variables.select)){
				var _sql = " * ";
			} else {
				var _sql = arrayToList(variables.select);
			}
			if( ArrayisEmpty(variables.from)){
				throw(type="Invalid SQL statment", message="Please provide the table name");
			} else {
				var _from = arrayToList(variables.from);
				q.Setsql("SELECT #_sql# FROM #_from# #variables.join# #variables.where#" );
			}
			var result = q.execute().getResult();
			return result;
		} else {
			throw(type="DataSource Exeception", message="Data Source is required to execute the query");
		}
	}
}