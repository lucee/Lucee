/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.video;

import lucee.commons.io.res.Resource;

public interface VideoOutput {

	/**
	 * limit size of the output file
	 * 
	 * @param size the size to set
	 */
	public void limitFileSizeTo(int size);

	/**
	 * set time offset of the output file based on input file in seconds
	 * 
	 * @param offset offset
	 */
	public void setOffset(double offset);

	/**
	 * sets a comment to the output video
	 * 
	 * @param comment comment
	 */
	public void setComment(String comment);

	/**
	 * sets a title to the output video
	 * 
	 * @param title title
	 */
	public void setTitle(String title);

	/**
	 * sets an author to the output video
	 * 
	 * @param author author
	 */
	public void setAuthor(String author);

	/**
	 * sets a copyright to the output video
	 * 
	 * @param copyright copyright
	 */
	public void setCopyright(String copyright);

	/**
	 * @param maxFrames the maxFrames to set
	 */
	public void setMaxFrames(long maxFrames);

	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource);

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format);

	/**
	 * @param fileLimitation the fileLimitation to set
	 */
	public void setFileLimitation(int fileLimitation);

	/**
	 * @return the res
	 */
	public Resource getResource();

	/**
	 * @return the offset
	 */
	public double getOffset();

	/**
	 * @return the comment
	 */
	public String getComment();

	/**
	 * @return the title
	 */
	public String getTitle();

	/**
	 * @return the author
	 */
	public String getAuthor();

	/**
	 * @return the copyright
	 */
	public String getCopyright();

	/**
	 * @return the fileLimitation
	 */
	public int getFileLimitation();

	/**
	 * @return the maxFrames
	 */
	public long getMaxFrames();

	/**
	 * @return the format
	 */
	public String getFormat();

	public void setFrameRate(int framerate);

	public int getFrameRate();

}