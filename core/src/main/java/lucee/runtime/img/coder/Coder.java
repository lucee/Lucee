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
package lucee.runtime.img.coder;

import java.awt.image.BufferedImage;
import java.io.IOException;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SystemOut;
public abstract class Coder {
	
	private static Coder instance;
	
	protected Coder(){}
	
	public static Coder getInstance(){
		
		if(instance==null){
			instance = new JRECoder();
			
			// try to load Sanselan, does not load when lib not exist
			try{
				SanselanCoder sanselan = new SanselanCoder();
				instance=new DoubleCoder(instance,sanselan); // used JRE first because Sanselan has troubles with JPG (inverted colors)
				SystemOut.printDate("use JRE and Sanselan Image Coder ");
			}
			catch(Exception e){
				SystemOut.printDate("use JRE Image Coder ");
			}
		} 
		return instance;
	}
	

	/**
	 * translate a file resource to a buffered image
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public abstract BufferedImage toBufferedImage(Resource res,String format) throws IOException;

	/**
	 * translate a binary array to a buffered image
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	public abstract BufferedImage toBufferedImage(byte[] bytes,String format) throws IOException;

	public abstract String[] getWriterFormatNames();
	
	public abstract String[] getReaderFormatNames();
	
}