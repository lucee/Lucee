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
import java.util.ArrayList;
import java.util.List;

import lucee.runtime.exp.PageException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Die Klasse TagLibFactory liest die XML Repraesentation einer TLD ein 
 * und laedt diese in eine Objektstruktur. 
 * Sie tut dieses mithilfe eines Sax Parser.
 * Die Klasse kann sowohl einzelne Files oder gar ganze Verzeichnisse von TLD laden.
 */
public final class BucketFactory extends S3Factory {
	
	private boolean insideBuckets=false;
	private boolean insideBucket=false;
	private final S3 s3;
	
	private Bucket bucket; 
	private final List buckets=new ArrayList();
	private boolean insideOwners;
	private String ownerIdKey;
	private String ownerDisplayName; 


	/**
	 * Privater Konstruktor, der als Eingabe die TLD als File Objekt erhaelt.
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param file File Objekt auf die TLD.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public BucketFactory(InputStream in, S3 s3) throws IOException, SAXException {
		super();
		this.s3=s3;
		init(in);
	}

	@Override
	public void doStartElement(String uri, String name, String qName, Attributes atts) {
		if(qName.equals("Owner")) insideOwners=true;
		if(qName.equals("Buckets")) insideBuckets=true;
		if(qName.equals("Bucket")) startBucket();
		
	}
    
	@Override
	public void doEndElement(String uri, String name, String qName) throws SAXException {
		if(qName.equals("Owner")) insideOwners=false;
		if(qName.equals("Buckets")) insideBuckets=false;
		if(qName.equals("Bucket")) endBucket();
	}
	
	
	@Override
	protected void setContent(String value) throws SAXException 	{
		if(insideOwners){
			if(inside.equals("ID")) 					ownerIdKey=value;
			else if(inside.equals("DisplayName")) 		ownerDisplayName=value;
			
		}
		if(insideBuckets && insideBucket)	{
			// Name
			if(inside.equals("Name")) bucket.setName(value);
			// CreationDate
			else if(inside.equals("CreationDate")) {
				try {
					bucket.setCreation(S3.toDate(value,s3.getTimeZone()));
				} 
				catch (PageException e) {
					throw new SAXException(e.getMessage());
				}	
			}
    	}
    }	
	
	
	
	/**
	 * Wird jedesmal wenn das Tag attribute beginnt aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void startBucket()	{
    	insideBucket=true;
    	bucket=new Bucket(s3); 
    }
	
	
	/**
	 * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void endBucket()	{
		bucket.setOwnerDisplayName(ownerDisplayName);
		bucket.setOwnerIdKey(ownerIdKey);
		buckets.add(bucket);
    	insideBucket=false;
    }

	public Bucket[] getBuckets() {
		return (Bucket[]) buckets.toArray(new Bucket[buckets.size()]);
	}

}