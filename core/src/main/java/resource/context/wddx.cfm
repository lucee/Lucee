<cfsetting showdebugoutput="no">
function WddxRecordset(data)	{
	var priv={};
	var pub=this;
	priv.data=data;
	
	pub.wddxSerialize=function (serializer) {
		alert("not supported at the moment");
	}
	
	/**
	* set Value of a Field of the WDDXResultset
	* @param row row to get
	* @param column column to get
	* @param value value of the object
	* @return value of the field
	*/
	pub.setField=function (row, column, value) {
		return priv.data[column][row]=value;
	}
	
	/**
	* get Value of a Field of the WDDXResultset
	* @param row row to get
	* @param column column to get
	* @return value of the field
	*/
	pub.getField=function (row, column) {
		return priv.data[column][row];
	}
	
	/**
	* is a Column Name or not
	* @param name Name of the column to check
	* @return boolean is a column or not
	*/
	pub.isColumn=function (name) {
		return priv.data[name]?true:false;
	}
	
	/**
	* adds a new column to the WDDXResultset
	* @param name Name of the new column
	* @return void
	*/
	pub.addRows=function (count) {
		if(!count)count=1;
		for(var column in priv.data) {
			var col=priv.data[column];
			for(var i=0;i<count;i++) {
				col[col.length]=null;
			}
		}
	}
	
	/**
	* adds a new column to the WDDXResultset
	* @param name Name of the new column
	* @return void
	*/
	pub.addColumn=function (name) {
		priv.data[name]=new Array(this.getRowCount());
	}
	
	/**
	* returns the number of rows of the WDDXRecordset
	* @return row count
	*/
	pub.getColumnCount=function () {
		var count=0;
		for(var column in priv.data) count++;
		return count;
	}
	
	/**
	* returns the number of rows of the WDDXRecordset
	* @return row count
	*/
	pub.getRowCount=function () {
		var count=0;
		for(var column in priv.data) {
			for(var row in priv.data[column])count++;
			break;
		}
		return count;
	}
	
	/**
	* dump the content as a HTML table
	* @param escape strings or not
	* @return HTML String
	*/
	pub.dump= function (escapeString) {
		return priv._dump(priv.data,escapeString);
	}
	priv._dump= function (obj,escapeString) {
		var type=typeof(obj);
		// String
		if(type=='string') return escapeString?priv.escapeHTML(obj):obj;
		// Number
		else if(type=='number') return obj;
		// Object
		else if(type=='object') {
			var rtn='<table border="1">';
			for(var key in obj) {
				rtn+='<tr><td bgcolor="cccccc">'+key+'</td><td>'+priv._dump(obj[key],escapeString)+'</td></tr>';
			}
			return rtn+'</table>';
		}
		// All others
		else return obj.toString();
	}
	pub.toString = pub.dump;
	
	/**
	* escapes HTML 
	* @param str HTML String to escape
	* @return HTML escaped String
	*/
	priv.escapeHTML=function (str) {
		var nstr='';
		for (var i=0;i<str.length;i++) {
			var c=str.charAt(i);
			if(c=='&') nstr+='&amp;';
			else if(c=='&') nstr+='&amp;';
			else if(c=='<') nstr+='&lt;';
			else if(c=='>') nstr+='&gt;';
			else nstr+=c;
		}            
		return nstr;
	}
}