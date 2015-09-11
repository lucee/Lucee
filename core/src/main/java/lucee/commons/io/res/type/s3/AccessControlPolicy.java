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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccessControlPolicy {
	
	private String id;
	private String displayName;
	
	private List<AccessControl> accessControlList=new ArrayList<AccessControl>();

	/**
	 * @param accessControlList the accessControlList to set
	 */
	public void setAccessControlList(List<AccessControl> accessControlList) {
		this.accessControlList = accessControlList;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return the accessControlList
	 */
	public List<AccessControl> getAccessControlList() {
		return accessControlList;
	}
	
	

	@Override
	public String toString(){
		return toXMLString();
	}
	
	public String toXMLString(){
		StringBuilder sb=new StringBuilder("<AccessControlPolicy xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">\n");
		
		// Owner
		sb.append("\t<Owner>\n");
		sb.append("\t\t<ID>"+getId()+"</ID>\n");
		sb.append("\t\t<DisplayName>"+getDisplayName()+"</DisplayName>\n");
		sb.append("\t</Owner>\n");
		
		// ACL
		sb.append("\t<AccessControlList>\n");
		AccessControl ac;
		Iterator<AccessControl> it = accessControlList.iterator();
		while(it.hasNext()){
			ac=it.next();
			sb.append("\t\t<Grant>\n");
			
			// Grantee
			sb.append("\t\t\t<Grantee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\""+AccessControl.toType(ac.getType(),"Group")+"\">\n");
			
			switch(ac.getType()){
			case AccessControl.TYPE_CANONICAL_USER:
				sb.append("\t\t\t\t<ID>"+ac.getId()+"</ID>\n");
				sb.append("\t\t\t\t<DisplayName>"+ac.getDisplayName()+"</DisplayName>\n");
			break;
			case AccessControl.TYPE_GROUP:
				sb.append("\t\t\t\t<URI>"+ac.getUri()+"</URI>\n");
			break;
			case AccessControl.TYPE_EMAIL:
				sb.append("\t\t\t\t<EmailAddress>"+ac.getEmail()+"</EmailAddress>\n");
			break;
			}
			
			
			
			
			
			
			sb.append("\t\t\t</Grantee>\n");
			
			// Permission
			sb.append("\t\t\t<Permission>"+ac.getPermission()+"</Permission>\n");

			sb.append("\t\t</Grant>\n");
		}
		sb.append("\t</AccessControlList>\n");
		sb.append("</AccessControlPolicy>");
		
		return sb.toString();
	}
	
	public static void removeDuplicates(List<AccessControl> acl){
		Map<String,AccessControl> map=new LinkedHashMap<String,AccessControl>();
		Iterator<AccessControl> it = acl.iterator();

		while(it.hasNext()){
			AccessControl ac = it.next();
			map.put(ac.hash(),ac);
		}
		
		acl.clear();
		it = map.values().iterator();
		while(it.hasNext()){
			AccessControl ac = it.next();
			acl.add(ac);
		}
		
	}
	
}