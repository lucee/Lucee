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
package lucee.runtime.text.xml;

public interface XMLConstants {
	public final static String NON_VALIDATING_DTD_GRAMMAR="http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
	public final static String NON_VALIDATING_DTD_EXTERNAL="http://apache.org/xml/features/nonvalidating/load-external-dtd";
	
	public final static String VALIDATION_SCHEMA="http://apache.org/xml/features/validation/schema";
	public final static String VALIDATION_SCHEMA_FULL_CHECKING="http://apache.org/xml/features/validation/schema-full-checking";

	public static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
	public static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	public static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
	public static final String FEATURE_NONVALIDATING_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	// public static final String ACCESS_EXTERNAL_DTD = javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
	// public static final String ACCESS_EXTERNAL_SCHEMA =
	// javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;

	public static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
	public static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
}