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
package lucee.commons.io.res.type.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public final class ACLFactory extends S3Factory {

	private boolean insideAccessControlList=false; 
	private boolean insideGrant=false; 
	private boolean insideGrantee=false; 
	
	private boolean insideOwners=false;

	private AccessControl ac; 
	private AccessControlPolicy acp=new AccessControlPolicy();
	private List<AccessControl> acl=acp.getAccessControlList();
	private String type;
	

	/**
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param file File Objekt auf die TLD.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public ACLFactory(InputStream in,S3 s3) throws IOException, SAXException {
		super();
		init(in);
	}

	@Override
	public void doStartElement(String uri, String name, String qName, Attributes atts) {
		if(insideGrant) {
			if(qName.equals("Grantee")) {
				for(int i=atts.getLength()-1;i>=0;i--){
					if("type".equalsIgnoreCase(atts.getLocalName(i)))
						type=atts.getValue(i);
				}
				
				
				insideGrantee=true;
			}
		}
		else if(insideAccessControlList) {
			if(qName.equals("Grant")) startGrant();
		}
		else if(qName.equals("AccessControlList")) insideAccessControlList=true;
		else if(qName.equals("Owner")) insideOwners=true;
		
	}
    
	@Override
	public void doEndElement(String uri, String name, String qName) throws SAXException {
		if(insideGrant) {
			if(qName.equals("Grant")) endGrant();
			else if(qName.equals("Grantee")) insideGrantee=false;
		}
		else if(qName.equals("AccessControlList")) insideAccessControlList=false;
		else if(qName.equals("Owner")) insideOwners=false;
	}
	
	
	@Override
	protected void setContent(String value) throws SAXException 	{
		if(insideGrant)	{
			if(insideGrantee){
				if(inside.equals("ID")) 					ac.setId(value);
				else if(inside.equals("DisplayName")) 		ac.setDisplayName(value);
				else if(inside.equals("URI")) 				ac.setUri(value);
				else if(inside.equals("Email")) 			ac.setEmail(value);
				else if(inside.equals("EmailAddress")) 		ac.setEmail(value);
				
			}
			else {
				if(inside.equals("Permission")) 			ac.setPermission(value);
			}
    	}
		else if(insideOwners) {
			if(inside.equals("ID")) 					acp.setId(value);
			else if(inside.equals("DisplayName")) 		acp.setDisplayName(value);
		}
    }
	
	
	/**
	 * Wird jedesmal wenn das Tag attribute beginnt aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void startGrant()	{
    	insideGrant=true;
    	ac=new AccessControl(); 
    }
	
	
	/**
	 * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void endGrant()	{
		ac.setType(AccessControl.toType(type,AccessControl.TYPE_CANONICAL_USER));
		acl.add(ac);
		insideGrant=false;
    }

	public AccessControlPolicy getAccessControlPolicy() {
		return acp;
	}


}