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
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis.encoding.ser;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import lucee.runtime.type.StructImpl;

import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.utils.BeanPropertyDescriptor;
import org.apache.axis.utils.BeanUtils;
import org.apache.axis.utils.JavaUtils;

/**
 * DeserializerFactory for Bean
 *
 * @author Rich Scheuerle <scheu@us.ibm.com>
 * @author Sam Ruby <rubys@us.ibm.com>
 */
public class BeanDeserializerFactory extends BaseDeserializerFactory {

    /** Type metadata about this class for XML deserialization */
    protected transient TypeDesc typeDesc = null;
    protected transient Map propertyMap = null;

    public BeanDeserializerFactory(Class javaType, QName xmlType) {
        super(BeanDeserializer.class, xmlType, javaType);
        
        // Sometimes an Enumeration class is registered as a Bean.
        // If this is the case, silently switch to the EnumDeserializer
        if (JavaUtils.isEnumClass(javaType)) {
            deserClass = EnumDeserializer.class;
        }

        typeDesc = TypeDesc.getTypeDescForClass(javaType);
        propertyMap = getProperties(javaType, typeDesc);
    }

   /**
     * Get a list of the bean properties
     */
    public static Map getProperties(Class javaType, TypeDesc typeDesc ) {
        Map propertyMap = null;

        if (typeDesc != null) {
            propertyMap = typeDesc.getPropertyDescriptorMap();
        } else {
            BeanPropertyDescriptor[] pd = BeanUtils.getPd(javaType, null);
            propertyMap = new StructImpl();// use this to get rid of case sensitivity
            
            // loop through properties and grab the names for later
            for (int i = 0; i < pd.length; i++) {
                BeanPropertyDescriptor descriptor = pd[i];
                propertyMap.put(descriptor.getName(), descriptor);
            }
        }

        return propertyMap;
    }

   /**
     * Optimize construction of a BeanDeserializer by caching the
     * type descriptor and property map.
     */
    @Override
	protected Deserializer getGeneralPurpose(String mechanismType) {
        if (javaType == null || xmlType == null) {
           return super.getGeneralPurpose(mechanismType);
        }

        if (deserClass == EnumDeserializer.class) {
           return super.getGeneralPurpose(mechanismType);
        }
        return new BeanDeserializer(javaType, xmlType, typeDesc, propertyMap);
    }


    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        typeDesc = TypeDesc.getTypeDescForClass(javaType);
        propertyMap = getProperties(javaType, typeDesc);
    }
}