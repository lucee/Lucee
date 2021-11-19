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
package lucee.runtime.tag;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;

public class VideoPlayerParamBean {

	public static final int NONE = 0;
	// public static final int YES = 1;
	// public static final int NO = 2;

	private Resource video = null;
	private Resource flash = null;
	private lucee.runtime.video.Range show = lucee.runtime.video.Range.TRUE;
	private int index = 0;
	private String pathVideo;
	private String pathFlash;
	private String title;
	private String link;
	private String author;
	// private int autostart=NONE;

	private Resource image;

	public void release() {
		video = null;
		flash = null;
		pathVideo = null;
		pathFlash = null;
		show = lucee.runtime.video.Range.TRUE;
		index = 1;
	}

	/**
	 * @return the video
	 */
	public Resource getVideo() {
		return video;
	}

	public Resource getResource() {
		return video != null ? video : flash;
	}

	/**
	 * @param video the video to set
	 * @param pathVideo
	 * @throws PageException
	 */
	public void setVideo(Resource video, String pathVideo) throws PageException {
		if (!"flv".equalsIgnoreCase(getExtension(video))) throw new ApplicationException("only flv movies are supported");
		this.video = video;
		this.pathVideo = pathVideo;
	}

	public void setVideo(PageContext pc, String video) throws PageException {
		setVideo(toResource(pc, video), video);
	}

	/**
	 * @return the flash
	 */
	public Resource getFlash() {
		return flash;
	}

	/**
	 * @param flash the flash to set
	 * @throws PageException
	 */
	public void setFlash(Resource flash, String pathFlash) throws PageException {
		if (!"swf".equalsIgnoreCase(getExtension(flash))) throw new ApplicationException("only swf movies are supported");
		this.flash = flash;
		this.pathFlash = pathFlash;
	}

	/**
	 * @param flash the flash to set
	 * @throws PageException
	 */
	public void setFlash(PageContext pc, String flash) throws PageException {
		setFlash(toResource(pc, flash), flash);
	}

	/**
	 * @return the show
	 */
	public lucee.runtime.video.Range getShow() {
		return show;
	}

	public void setShow(String show) throws PageException {
		this.show = lucee.runtime.video.Range.toRange(show);
	}

	public void setShow(lucee.runtime.video.Range show) {
		this.show = show;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) throws PageException {
		if (index < 0) throw new ApplicationException("index have to be a non-negative integer");
		this.index = index;
	}

	private Resource toResource(PageContext pc, String str) throws PageException {
		Resource res = ResourceUtil.toResourceNotExisting(pc, str);

		// Resource res = Caster.toResource(str,false);
		// print.out(res);
		if (res.getResourceProvider().getScheme().equalsIgnoreCase("file") && !res.exists()) throw new ApplicationException("Resource [" + res + "] does not exist");
		return res;
	}

	private static String getExtension(Resource res) {
		String strFile = res.getName();
		int pos = strFile.lastIndexOf('.');
		if (pos == -1) return null;
		return strFile.substring(pos + 1);
	}

	/**
	 * @return the pathVideo
	 */
	public String getPathVideo() {
		return pathVideo;
	}

	/**
	 * @return the pathFlash
	 */
	public String getPathFlash() {
		return pathFlash;
	}

	public String getPath() {
		return StringUtil.isEmpty(pathVideo) ? pathFlash : pathVideo;
	}

	@Override
	public String toString() {
		return "video:" + pathVideo + ";flash:" + pathFlash + ";index:" + index + ";show:" + show;
	}

	/*
	 * *
	 * 
	 * @return the autostart / public int getAutostart() { return autostart; }
	 * 
	 * /* *
	 * 
	 * @param autostart the autostart to set / public void setAutostart(boolean autostart) {
	 * this.autostart = autostart?YES:NO; }
	 */

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public void setImage(PageContext pc, String image) throws PageException {
		this.image = toResource(pc, image);
	}

	/**
	 * @return the image
	 */
	public Resource getImage() {
		return image;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

}