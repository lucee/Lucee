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
package lucee.runtime.net.mail;

import java.io.Serializable;
import java.nio.charset.Charset;

import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;


/**
 * 
 */
public final class MailPart implements Serializable {
    @Override
    public String toString() {
        return "lucee.runtime.mail.MailPart(wraptext:"+wraptext+";type:"+type+";charset:"+charset+";body:"+body+";)";
    }
	/** IThe MIME media type of the part */
    private boolean isHTML;
	
	/** Specifies the maximum line length, in characters of the mail text */
	private int wraptext=-1;

	/** The character encoding in which the part text is encoded */
	private Charset charset;

    private String body;
	private String type;

    /**
     * 
     */
    public void clear() {
    	isHTML=false;
    	type=null;
        wraptext=-1;
        charset=null;
        body="null";
    }	
    
    

    /**
     * 
     */
    public MailPart() {
    }

    /**
     * @param charset
     */
    public MailPart(Charset charset) {
        this.charset = charset;
    }
    /**
     * @return Returns the body.
     */
    public String getBody() {
        return wrap(body);
    }
    /**
     * @param body The body to set.
     */
    public void setBody(String body) {
        this.body = body;
    }
    /**
     * @return Returns the charset.
     */
    public Charset getCharset() {
        return charset;
    }
    /**
     * @param charset The charset to set.
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }
    /**
     * @return Returns the isHTML.
     */
    public boolean isHTML() {
        return isHTML;
    }
    /**
     * @param isHTML The type to set.
     */
    public void isHTML(boolean isHTML) {
        this.isHTML = isHTML;
    }
    /**
     * @return Returns the wraptext.
     */
    public int getWraptext() {
        return wraptext;
    }
    
    
    /**
	 * @return the type
	 */
	public String getType() {
		return type;
	}



	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}



	/**
     * @param wraptext The wraptext to set.
     */
    public void setWraptext(int wraptext) {
        this.wraptext = wraptext;
    }


	/**
	 * wraps a String to specified length
	 * @param str string to erap
	 * @return wraped String
	 */
	private String wrap(String str) {
		if(body==null || wraptext<=0)return str;
		
		StringBuffer rtn=new StringBuffer();
		String ls=System.getProperty("line.separator");
		Array arr = ListUtil.listToArray(str,ls);
		int len=arr.size();
		
		for(int i=1;i<=len;i++) {
			rtn.append(wrapLine(Caster.toString(arr.get(i,""),"")));
			if(i+1<len)rtn.append(ls);
		}
		return rtn.toString();
	}

	/**
	 * wrap a single line
	 * @param str
	 * @return wraped Line
	 */
	private String wrapLine(String str) {
		int wtl=wraptext;
		
		if(str.length()<=wtl) return str;
		
		String sub=str.substring(0,wtl);
		String rest=str.substring(wtl);
		char firstR=rest.charAt(0);
		String ls=System.getProperty("line.separator");
		
		if(firstR==' ' || firstR=='\t') return sub+ls+wrapLine(rest.length()>1?rest.substring(1):"");
		
		
		int indexSpace = sub.lastIndexOf(' ');
		int indexTab = sub.lastIndexOf('\t');
		int index=indexSpace<=indexTab?indexTab:indexSpace;
		
		if(index==-1) return sub+ls+wrapLine(rest);
		return sub.substring(0,index) + ls + wrapLine(sub.substring(index+1)+rest);
		
	}
}