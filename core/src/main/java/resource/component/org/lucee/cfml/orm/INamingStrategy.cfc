/**
 * Strategy to specify the table name for a CFC and column name for a property in the cfc.
 * This can be used to specify the application specific table and column naming convention.
 * This rule will be applied even if the user has specified the table/column name in the mapping so that
 * the name can be changed for any application at one place without changing the names in all the code. 
 */
interface
{
	/**
	 * Defines the table name to be used for a specified table name. The specified table name is either 
	 * the table name specified in the mapping or chosen using the entity name. 
	 */
	public string function getTableName(string tableName);
	
	/**
	 * Defines the column name to be used for a specified column name. The specified column name is either 
	 * the column name specified in the mapping or chosen using the proeprty name.  
	 */
	public string function getColumnName(string columnName);
	
}