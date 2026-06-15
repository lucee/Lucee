/**
 * Serialization helpers for MCP tool responses.
 */
component {

	public any function toSerializable(required any value) {
		if (isNull(arguments.value)) {
			return javacast("null", "");
		}

		if (isSimpleValue(arguments.value)) {
			return arguments.value;
		}

		if (isQuery(arguments.value)) {
			return queryToArray(arguments.value);
		}

		if (isArray(arguments.value)) {
			var arr = [];
			for (var item in arguments.value) {
				arrayAppend(arr, toSerializable(item));
			}
			return arr;
		}

		if (isStruct(arguments.value)) {
			var sct = {};
			for (var key in arguments.value) {
				sct[key] = toSerializable(arguments.value[key]);
			}
			return sct;
		}

		return arguments.value;
	}

	private array function queryToArray(required query qry) {
		var rows = [];
		var cols = listToArray(arguments.qry.columnList);

		for (var row = 1; row <= arguments.qry.recordCount; row++) {
			var item = {};
			for (var col in cols) {
				item[col] = toSerializable(arguments.qry[col][row]);
			}
			arrayAppend(rows, item);
		}

		return rows;
	}

	public string function toJsonText(required any value) {
		return serializeJSON(toSerializable(arguments.value));
	}

}
