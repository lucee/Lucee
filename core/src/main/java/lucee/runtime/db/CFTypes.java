/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.db;

import java.sql.Types;

public interface CFTypes {
	// public static final int BFILE=-13;//OracleTypes.BFILE;
	public static final int CURSOR = -10;// OracleTypes.CURSOR;
	public static final int BFILE = -13;
	public static final int BINARY_DOUBLE = 101;
	public static final int BINARY_FLOAT = 100;
	public static final int FIXED_CHAR = 999;
	public static final int INTERVALDS = -104;
	public static final int INTERVALYM = -103;
	public static final int JAVA_STRUCT = 2008;
	public static final int NUMBER = Types.NUMERIC;
	public static final int PLSQL_INDEX_TABLE = -14;
	public static final int RAW = -2;
	public static final int ROWID = -8;
	public static final int ORACLE_TIMESTAMPLTZ = -102;
	public static final int ORACLE_TIMESTAMPNS = -100;
	public static final int ORACLE_TIMESTAMPTZ = -101;
	public static final int VARCHAR2 = -100;

	public static final int ORACLE_OPAQUE = 2007;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>BIT</code>.
	 */
	public final static int BIT = -7;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>TINYINT</code>.
	 */
	public final static int TINYINT = -6;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>SMALLINT</code>.
	 */
	public final static int SMALLINT = 5;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>INTEGER</code>.
	 */
	public final static int INTEGER = 4;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>BIGINT</code>.
	 */
	public final static int BIGINT = -5;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>FLOAT</code>.
	 */
	public final static int FLOAT = 6;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>REAL</code>.
	 */
	public final static int REAL = 7;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>DOUBLE</code>.
	 */
	public final static int DOUBLE = 8;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>NUMERIC</code>.
	 */
	public final static int NUMERIC = 2;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>DECIMAL</code>.
	 */
	public final static int DECIMAL = 3;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>CHAR</code>.
	 */
	public final static int CHAR = 1;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>VARCHAR</code>.
	 */
	public final static int VARCHAR = 12;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>LONGVARCHAR</code>.
	 */
	public final static int LONGVARCHAR = -1;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>DATE</code>.
	 */
	public final static int DATE = 91;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>TIME</code>.
	 */
	public final static int TIME = 92;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>TIMESTAMP</code>.
	 */
	public final static int TIMESTAMP = 93;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>BINARY</code>.
	 */
	public final static int BINARY = -2;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>VARBINARY</code>.
	 */
	public final static int VARBINARY = -3;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>LONGVARBINARY</code>.
	 */
	public final static int LONGVARBINARY = -4;

	/**
	 * <P>
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>NULL</code>.
	 */
	public final static int NULL = 0;

	/**
	 * The constant in the Java programming language that indicates that the SQL type is
	 * database-specific and gets mapped to a Java object that can be accessed via the methods
	 * <code>getObject</code> and <code>setObject</code>.
	 */
	public final static int OTHER = 1111;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>JAVA_OBJECT</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_JAVA_OBJECT = 2000;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>DISTINCT</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_DISTINCT = 2001;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>STRUCT</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_STRUCT = 2002;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>ARRAY</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_ARRAY = 2003;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>BLOB</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_BLOB = 2004;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>CLOB</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_CLOB = 2005;

	public static final int ORACLE_NCLOB = 2011;

	/**
	 * The constant in the Java programming language, sometimes referred to as a type code, that
	 * identifies the generic SQL type <code>REF</code>.
	 * 
	 * @since 1.2
	 */
	public final static int ORACLE_REF = 2006;

	/**
	 * The constant in the Java programming language, somtimes referred to as a type code, that
	 * identifies the generic SQL type <code>DATALINK</code>.
	 *
	 * @since 1.4
	 */
	public final static int DATALINK = 70;

	/**
	 * The constant in the Java programming language, somtimes referred to as a type code, that
	 * identifies the generic SQL type <code>BOOLEAN</code>.
	 *
	 * @since 1.4
	 */
	public final static int BOOLEAN = 16;

	public static final int IDSTAMP = CHAR;// TODO is this right?
}