package lucee.runtime.type.query;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;

/**
 * Shared handling of the attribute/argument "returntype" (and "columnKey") for every tag and
 * function that returns a query. It uses the same conversion as cfquery (see
 * {@link QueryArray#toQueryArray(QueryImpl)} and
 * {@link QueryStruct#toQueryStruct(QueryImpl, Key[])}), the default is still a query.
 */
public final class QueryReturnType {

	public static final int QUERY = 1;
	public static final int ARRAY = 2;
	public static final int STRUCT = 3;

	private QueryReturnType() {
	}

	/**
	 * translates the string value to one of the constants of this class
	 * 
	 * @param strReturntype value of the attribute/argument
	 * @return one of QUERY,ARRAY,STRUCT or -1 if the value is not valid; an empty value is QUERY
	 */
	public static int parse(String strReturntype) {
		if (StringUtil.isEmpty(strReturntype, true)) return QUERY;
		strReturntype = strReturntype.toLowerCase().trim();

		if (strReturntype.equals("query")) return QUERY;
		if (strReturntype.equals("struct")) return STRUCT;
		if (strReturntype.equals("array") || strReturntype.equals("array_of_struct") || strReturntype.equals("array-of-struct") || strReturntype.equals("arrayofstruct")
				|| strReturntype.equals("array_of_entity") || strReturntype.equals("array-of-entity") || strReturntype.equals("arrayofentities")
				|| strReturntype.equals("array_of_entities") || strReturntype.equals("array-of-entities")) {
			return ARRAY;
		}
		return -1;
	}

	/**
	 * same as parse(String), but throws an exception if the value is not valid
	 * 
	 * @param strReturntype value of the attribute/argument
	 * @param name name of the tag or function, used for the exception
	 */
	public static int parse(String strReturntype, String name) throws ApplicationException {
		int rt = parse(strReturntype);
		if (rt == -1) throw new ApplicationException("Attribute [returntype] of [" + name + "] has an invalid value", "valid values are [query,array,struct] but value was ["
				+ strReturntype.trim() + "]");
		return rt;
	}

	/**
	 * translates the value of the attribute/argument "columnKey", that can be a single column name or a
	 * comma separated list of column names
	 * 
	 * @param columnKey value of the attribute/argument
	 * @return the keys or null if no column is defined
	 */
	public static Key[] toColumnKeys(String columnKey) {
		if (StringUtil.isEmpty(columnKey, true)) return null;
		String[] arr = columnKey.split(",");
		List<Key> keys = new ArrayList<Key>(arr.length);
		for (String str: arr) {
			if (StringUtil.isEmpty(str, true)) continue;
			keys.add(KeyImpl.init(str.trim()));
		}
		if (keys.isEmpty()) return null;
		return keys.toArray(new Key[keys.size()]);
	}

	/**
	 * converts a query to the requested return type
	 * 
	 * @param qry the query
	 * @param returnType one of QUERY,ARRAY,STRUCT
	 * @param columnKey only used for STRUCT, the column(s) the value of is used as key, more than one column creates nested structs
	 * @return the query itself for QUERY, otherwise an array of structs or a struct of structs
	 */
	public static Object convert(Query qry, int returnType, Key[] columnKey) throws PageException {
		if (returnType != ARRAY && returnType != STRUCT) return qry;
		if (!(qry instanceof QueryImpl)) throw new ApplicationException("cannot convert a query of type [" + qry.getClass().getName() + "] to a return type other than query");
		if (returnType == ARRAY) return QueryArray.toQueryArray((QueryImpl) qry);
		return QueryStruct.toQueryStruct((QueryImpl) qry, columnKey);
	}

	public static Object convert(Query qry, String strReturnType, String columnKey, String name) throws PageException {
		return convert(qry, parse(strReturnType, name), toColumnKeys(columnKey));
	}
}
