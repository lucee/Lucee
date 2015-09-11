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
 * Copyright 2001-2002,2004 The Apache Software Foundation.
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

import java.io.CharArrayWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.ConstructorTarget;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.Target;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHandler;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.BeanPropertyDescriptor;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * General purpose deserializer for an arbitrary java bean.
 *
 * @author Sam Ruby <rubys@us.ibm.com>
 * @author Rich Scheuerle <scheu@us.ibm.com>
 * @author Tom Jordahl <tomj@macromedia.com>
 */
public class BeanDeserializer extends DeserializerImpl implements Serializable
{
    protected static Log log =
        LogFactory.getLog(BeanDeserializer.class.getName());

    private final CharArrayWriter val = new CharArrayWriter();

    QName xmlType;
    Class javaType;
    protected Map propertyMap = null;
    protected QName prevQName;

    /**
     *  Constructor if no default constructor
     */
    protected Constructor constructorToUse = null;

    /**
     * Constructor Target object to use (if constructorToUse != null)
     */
    protected Target constructorTarget = null;
    
    /** Type metadata about this class for XML deserialization */
    protected TypeDesc typeDesc = null;

    // This counter is updated to deal with deserialize collection properties
    protected int collectionIndex = -1;

    protected SimpleDeserializer cacheStringDSer = null;
    protected QName cacheXMLType = null;

    // Construct BeanSerializer for the indicated class/qname
    public BeanDeserializer(Class javaType, QName xmlType) {
        this(javaType, xmlType, TypeDesc.getTypeDescForClass(javaType));
    }

    // Construct BeanDeserializer for the indicated class/qname and meta Data
    public BeanDeserializer(Class javaType, QName xmlType, TypeDesc typeDesc ) {
        this(javaType, xmlType, typeDesc,
             BeanDeserializerFactory.getProperties(javaType, typeDesc));
    }

    // Construct BeanDeserializer for the indicated class/qname and meta Data
    public BeanDeserializer(Class javaType, QName xmlType, TypeDesc typeDesc,
                            Map propertyMap ) {
        this.xmlType = xmlType;
        this.javaType = javaType;
        this.typeDesc = typeDesc;
        this.propertyMap = propertyMap;

        // create a value
        try {
            value=javaType.newInstance();
        } catch (Exception e) {
            // Don't process the exception at this point.
            // This is defered until the call to startElement
            // which will throw the exception.
        }
    }

    /**
     * startElement
     * 
     * The ONLY reason that this method is overridden is so that
     * the object value can be set or a reasonable exception is thrown
     * indicating that the object cannot be created.  This is done
     * at this point so that it occurs BEFORE href/id processing.
     * @param namespace is the namespace of the element
     * @param localName is the name of the element
     * @param prefix is the prefix of the element
     * @param attributes are the attributes on the element...used to get the
     *                   type
     * @param context is the DeserializationContext
     */
    @Override
	public void startElement(String namespace, String localName,
                             String prefix, Attributes attributes,
                             DeserializationContext context)
        throws SAXException
    {
        // Create the bean object if it was not already
        // created in the constructor.
        if (value == null) {
            try {
                value=javaType.newInstance();
            } catch (Exception e) {
                // Use first found constructor.
                // Note : the right way is to use XML mapping information
                // for example JSR 109's constructor-parameter-order
                Constructor[] constructors = javaType.getConstructors();
                if (constructors.length > 0) {
                    constructorToUse = constructors[0];
                }

                // Failed to create an object if no constructor
                if (constructorToUse == null) {
                    throw new SAXException(Messages.getMessage("cantCreateBean00", 
                                                            javaType.getName(), 
                                                            e.toString()));
                }
            }
        }
        // Invoke super.startElement to do the href/id processing.
        super.startElement(namespace, localName, 
                           prefix, attributes, context);
    }

    /**
     * Deserializer interface called on each child element encountered in
     * the XML stream.
     * @param namespace is the namespace of the child element
     * @param localName is the local name of the child element
     * @param prefix is the prefix used on the name of the child element
     * @param attributes are the attributes of the child element
     * @param context is the deserialization context.
     * @return is a Deserializer to use to deserialize a child (must be
     * a derived class of SOAPHandler) or null if no deserialization should
     * be performed.
     */
    @Override
	public SOAPHandler onStartChild(String namespace,
                                    String localName,
                                    String prefix,
                                    Attributes attributes,
                                    DeserializationContext context)
        throws SAXException
    {
        handleMixedContent();

        BeanPropertyDescriptor propDesc = null;
        FieldDesc fieldDesc = null;

        SOAPConstants soapConstants = context.getSOAPConstants();
        String encodingStyle = context.getEncodingStyle();
        boolean isEncoded = Constants.isSOAP_ENC(encodingStyle);

        QName elemQName = new QName(namespace, localName);
        // The collectionIndex needs to be reset for Beans with multiple arrays
        if ((prevQName == null) || (!prevQName.equals(elemQName))) {
            collectionIndex = -1;
        }  

        boolean isArray = false;
        QName itemQName = null;
        if (typeDesc != null) {
            // Lookup the name appropriately (assuming an unqualified
            // name for SOAP encoding, using the namespace otherwise)
            String fieldName = typeDesc.getFieldNameForElement(elemQName, 
                                                               isEncoded);
            propDesc = (BeanPropertyDescriptor)propertyMap.get(fieldName);
            fieldDesc = typeDesc.getFieldByName(fieldName);

            if (fieldDesc != null) {
               ElementDesc element = (ElementDesc)fieldDesc;
               isArray = element.isMaxOccursUnbounded();
               itemQName = element.getItemQName();
           }
        }

        if (propDesc == null) {
            // look for a field by this name.
            propDesc = (BeanPropertyDescriptor) propertyMap.get(localName);
        }
        
        // Workaround
        if (propDesc == null) {
        	StringBuilder sb=new StringBuilder();
            sb.append(Character.toLowerCase(localName.charAt(0)));
        	if(localName.length()>1)sb.append(localName.substring(1));
            // look for a field by this name.
            propDesc = (BeanPropertyDescriptor) propertyMap.get(sb.toString());
            
        }

        // try and see if this is an xsd:any namespace="##any" element before
        // reporting a problem
        if (propDesc == null || 
        		(((prevQName != null) && prevQName.equals(elemQName) &&
        				!(propDesc.isIndexed()||isArray)
						&& getAnyPropertyDesc() != null ))) {
            // try to put unknown elements into a SOAPElement property, if
            // appropriate
        	prevQName = elemQName;
            propDesc = getAnyPropertyDesc();
            if (propDesc != null) {
                try {
                    MessageElement [] curElements = (MessageElement[])propDesc.get(value);
                    int length = 0;
                    if (curElements != null) {
                        length = curElements.length;
                    }
                    MessageElement [] newElements = new MessageElement[length + 1];
                    if (curElements != null) {
                        System.arraycopy(curElements, 0,
                                         newElements, 0, length);
                    }
                    MessageElement thisEl = context.getCurElement();

                    newElements[length] = thisEl;
                    propDesc.set(value, newElements);
                    // if this is the first pass through the MessageContexts
                    // make sure that the correct any element is set,
                    // that is the child of the current MessageElement, however
                    // on the first pass this child has not been set yet, so
                    // defer it to the child SOAPHandler
                    if (!localName.equals(thisEl.getName())) {
                        return new SOAPHandler(newElements, length);
                    }
                    return new SOAPHandler();
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
        }


        if (propDesc == null) {
            // No such field
            throw new SAXException(
                    Messages.getMessage("badElem00", javaType.getName(), 
                                         localName));
        }

        prevQName = elemQName;
        // Get the child's xsi:type if available
        QName childXMLType = context.getTypeFromAttributes(namespace, 
                                                            localName,
                                                            attributes);
        String href = attributes.getValue(soapConstants.getAttrHref());
        Class fieldType = propDesc.getType();

        // If no xsi:type or href, check the meta-data for the field
        if (childXMLType == null && fieldDesc != null && href == null) {
            childXMLType = fieldDesc.getXmlType();
            if (itemQName != null) {
                // This is actually a wrapped literal array and should be
                // deserialized with the ArrayDeserializer
                childXMLType = Constants.SOAP_ARRAY;
                fieldType = propDesc.getActualType();
            } else {
                childXMLType = fieldDesc.getXmlType();
            }
        }
        
        // Get Deserializer for child, default to using DeserializerImpl
        Deserializer dSer = getDeserializer(childXMLType,
                                            fieldType,
                                            href,
                                            context);

        // It is an error if the dSer is not found - the only case where we
        // wouldn't have a deserializer at this point is when we're trying
        // to deserialize something we have no clue about (no good xsi:type,
        // no good metadata).
        if (dSer == null) {
            dSer = context.getDeserializerForClass(propDesc.getType());
        }

        // Fastpath nil checks...
        if (context.isNil(attributes)) {
            if ((propDesc.isIndexed()||isArray)) {
                if (!((dSer != null) && (dSer instanceof ArrayDeserializer))) {
                    collectionIndex++;
                    dSer.registerValueTarget(new BeanPropertyTarget(value,
                            propDesc, collectionIndex));
                    addChildDeserializer(dSer);
                    return (SOAPHandler)dSer;
                }
            }
            return null;
        }            
          
        if (dSer == null) {
            throw new SAXException(Messages.getMessage("noDeser00",
                                                       childXMLType.toString()));
        }

        if (constructorToUse != null) {
            if (constructorTarget == null) {
                constructorTarget = new ConstructorTarget(constructorToUse, this);
            }
            dSer.registerValueTarget(constructorTarget);
        } else if (propDesc.isWriteable()) {
            // If this is an indexed property, and the deserializer we found
            // was NOT the ArrayDeserializer, this is a non-SOAP array:
            // <bean>
            //   <field>value1</field>
            //   <field>value2</field>
            // ...
            // In this case, we want to use the collectionIndex and make sure
            // the deserialized value for the child element goes into the
            // right place in the collection.

            // Register value target
            if ((itemQName != null || propDesc.isIndexed() || isArray) && !(dSer instanceof ArrayDeserializer)) {
                collectionIndex++;
                dSer.registerValueTarget(new BeanPropertyTarget(value,
                        propDesc, collectionIndex));
            } else {
                // If we're here, the element maps to a single field value,
                // whether that be a "basic" type or an array, so use the
                // normal (non-indexed) BeanPropertyTarget form.
                collectionIndex = -1;
                dSer.registerValueTarget(new BeanPropertyTarget(value,
                                                                propDesc));
            }
        }
        
        // Let the framework know that we need this deserializer to complete
        // for the bean to complete.
        addChildDeserializer(dSer);
        
        return (SOAPHandler)dSer;
    }

    /**
     * Get a BeanPropertyDescriptor which indicates where we should
     * put extensibility elements (i.e. XML which falls under the
     * auspices of an &lt;xsd:any&gt; declaration in the schema)
     *
     * @return an appropriate BeanPropertyDescriptor, or null
     */
    public BeanPropertyDescriptor getAnyPropertyDesc() {
        if (typeDesc == null)
            return null;
        
       return typeDesc.getAnyDesc();
    }

    /**
     * Set the bean properties that correspond to element attributes.
     * 
     * This method is invoked after startElement when the element requires
     * deserialization (i.e. the element is not an href and the value is not
     * nil.)
     * @param namespace is the namespace of the element
     * @param localName is the name of the element
     * @param prefix is the prefix of the element
     * @param attributes are the attributes on the element...used to get the
     *                   type
     * @param context is the DeserializationContext
     */
    @Override
	public void onStartElement(String namespace, String localName,
                               String prefix, Attributes attributes,
                               DeserializationContext context)
            throws SAXException {

        // The value should have been created or assigned already.
        // This code may no longer be needed.
        if (value == null && constructorToUse == null) {
            // create a value
            try {
                value=javaType.newInstance();
            } catch (Exception e) {
                throw new SAXException(Messages.getMessage("cantCreateBean00", 
                                                            javaType.getName(), 
                                                            e.toString()));
            }
        }

        // If no type description meta data, there are no attributes,
        // so we are done.
        if (typeDesc == null)
            return;

        // loop through the attributes and set bean properties that 
        // correspond to attributes
        for (int i=0; i < attributes.getLength(); i++) {
            QName attrQName = new QName(attributes.getURI(i),
                                        attributes.getLocalName(i));
            String fieldName = typeDesc.getFieldNameForAttribute(attrQName);
            if (fieldName == null)
                continue;

            FieldDesc fieldDesc = typeDesc.getFieldByName(fieldName);
            
            // look for the attribute property
            BeanPropertyDescriptor bpd =
                    (BeanPropertyDescriptor) propertyMap.get(fieldName);
            if (bpd != null) {
                if (constructorToUse == null) {
                    // check only if default constructor
                    if (!bpd.isWriteable() || bpd.isIndexed()) continue ;
                }
                
                // Get the Deserializer for the attribute
                Deserializer dSer = getDeserializer(fieldDesc.getXmlType(),
                                                    bpd.getType(), 
                                                    null,
                                                    context);
                if (dSer == null) {
                    dSer = context.getDeserializerForClass(bpd.getType());

                    // The java type is an array, but the context didn't
                    // know that we are an attribute.  Better stick with
                    // simple types..
                    if (dSer instanceof ArrayDeserializer)
                    {
                        SimpleListDeserializerFactory factory =
                            new SimpleListDeserializerFactory(bpd.getType(),
                                    fieldDesc.getXmlType());
                        dSer = (Deserializer)
                            factory.getDeserializerAs(dSer.getMechanismType());
                    }
                }

                if (dSer == null)
                    throw new SAXException(
                            Messages.getMessage("unregistered00",
                                                 bpd.getType().toString()));
                
                if (! (dSer instanceof SimpleDeserializer))
                    throw new SAXException(
                            Messages.getMessage("AttrNotSimpleType00", 
                                                 bpd.getName(), 
                                                 bpd.getType().toString()));
                
                // Success!  Create an object from the string and set
                // it in the bean
                try {
                    dSer.onStartElement(namespace, localName, prefix,
                                        attributes, context);
                    Object val = ((SimpleDeserializer)dSer).
                        makeValue(attributes.getValue(i));
                    if (constructorToUse == null) {
                        bpd.set(value, val);
                    } else {
                        // add value for our constructor
                        if (constructorTarget == null) {
                            constructorTarget = new ConstructorTarget(constructorToUse, this);
                        }
                        constructorTarget.set(val);
                    }
                } catch (Exception e) {
                    throw new SAXException(e);
                }
                
            } // if
        } // attribute loop
    }

    /**
     * Get the Deserializer for the attribute or child element.
     * @param xmlType QName of the attribute/child element or null if not known.
     * @param javaType Class of the corresponding property
     * @param href String is the value of the href attribute, which is used
     *             to determine whether the child element is complete or an 
     *             href to another element.
     * @param context DeserializationContext
     * @return Deserializer or null if not found.
    */
    protected Deserializer getDeserializer(QName xmlType, 
                                           Class javaType, 
                                           String href,
                                           DeserializationContext context) {
        if (javaType.isArray()) {
            context.setDestinationClass(javaType);
        } 
        // See if we have a cached deserializer
        if (cacheStringDSer != null) {
            if (String.class.equals(javaType) &&
                href == null &&
                (cacheXMLType == null && xmlType == null ||
                 cacheXMLType != null && cacheXMLType.equals(xmlType))) {
                cacheStringDSer.reset();
                return cacheStringDSer;
            }
        }
        
        Deserializer dSer = null;

        if (xmlType != null && href == null) {
            // Use the xmlType to get the deserializer.
            dSer = context.getDeserializerForType(xmlType);
        } else {
            // If the xmlType is not set, get a default xmlType
            TypeMapping tm = context.getTypeMapping();
            QName defaultXMLType = tm.getTypeQName(javaType);
            // If there is not href, then get the deserializer
            // using the javaType and default XMLType,
            // If there is an href, the create the generic
            // DeserializerImpl and set its default type (the
            // default type is used if the href'd element does 
            // not have an xsi:type.
            if (href == null) {
                dSer = context.getDeserializer(javaType, defaultXMLType);
            } else {
                dSer = new DeserializerImpl();
                context.setDestinationClass(javaType);
                dSer.setDefaultType(defaultXMLType);
            }
        }
        if (javaType.equals(String.class) &&
            dSer instanceof SimpleDeserializer) {
            cacheStringDSer = (SimpleDeserializer) dSer;
            cacheXMLType = xmlType;
        }
        return dSer;
    }

    @Override
	public void characters(char[] chars, int start, int end) throws SAXException {
        val.write(chars, start, end);
    }

    @Override
	public void onEndElement(String namespace, String localName,
                             DeserializationContext context) throws SAXException {
        handleMixedContent();
    }

    protected void handleMixedContent() throws SAXException {
        BeanPropertyDescriptor propDesc = getAnyPropertyDesc();
        if (propDesc == null || val.size() == 0) {
            return;
        }
        String textValue = val.toString().trim();
        val.reset();
        if (textValue.length() == 0) {
            return;
        }
        try {
            MessageElement[] curElements = (MessageElement[]) propDesc.get(value);
            int length = 0;
            if (curElements != null) {
                length = curElements.length;
            }
            MessageElement[] newElements = new MessageElement[length + 1];
            if (curElements != null) {
                System.arraycopy(curElements, 0,
                        newElements, 0, length);
            }
            MessageElement thisEl = new MessageElement(new org.apache.axis.message.Text(textValue));
            newElements[length] = thisEl;
            propDesc.set(value, newElements);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }
}