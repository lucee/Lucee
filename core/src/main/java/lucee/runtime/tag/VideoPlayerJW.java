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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.commons.color.ColorCaster;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagSupport;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.functions.string.JSStringFormat;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.video.VideoInput;
import lucee.runtime.video.VideoInputImpl;
import lucee.runtime.video.VideoUtilImpl;

/**
 * implementation of the tag Compiler
 */
public class VideoPlayerJW extends BodyTagSupport {

	private static final int TYPE_NONE = 0;
	private static final int TYPE_PLAYLIST = 1;
	private static final int TYPE_CHAPTERS = 2;

	private static final int PLAYLIST_NONE = 0;
	private static final int PLAYLIST_RIGHT = 1;
	private static final int PLAYLIST_BOTTOM = 2;

	private static Color BG_COLOR = new Color(51, 51, 51);
	private static Color FG_COLOR = new Color(198, 198, 198);

	private String video = null;
	private boolean autostart = false;

	private lucee.runtime.video.Range showPlay = lucee.runtime.video.Range.TRUE;
	private lucee.runtime.video.Range showPause = lucee.runtime.video.Range.TRUE;
	private lucee.runtime.video.Range showTimeline = lucee.runtime.video.Range.TRUE;
	private List params = new ArrayList();
	private java.awt.Color bgcolor = BG_COLOR;
	private java.awt.Color fgcolor = FG_COLOR;
	private java.awt.Color screencolor = null;
	private java.awt.Color lightcolor = null;

	private int width = -1;
	private int height = -1;
	private boolean debug;
	private boolean allowfullscreen;
	private String strWidth;
	private String strHeight;
	// private static Map sizes=new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);

	// JW
	private Struct passthrough = null;
	private String preview;

	private boolean group = false;
	private boolean playlistThumbnails = true;
	private int playlistSize = -1;
	private int playlist = PLAYLIST_NONE;
	private String target = "_self";
	private boolean linkfromdisplay;
	private String overstretch;
	private boolean download;
	private String id;
	private String align;

	public VideoPlayerJW() {

	}

	@Override
	public void release() {
		super.release();
		video = null;
		autostart = false;

		showPlay = lucee.runtime.video.Range.TRUE;
		showPause = lucee.runtime.video.Range.TRUE;
		showTimeline = lucee.runtime.video.Range.TRUE;
		params.clear();
		debug = false;

		id = null;
		group = false;
		playlist = PLAYLIST_NONE;
		playlistSize = -1;
		playlistThumbnails = true;
		target = "_self";
		linkfromdisplay = false;
		overstretch = null;
		/*
		 * group="yes" playlist="right,bottom,none" playlistSize="300" playlistThumbnails="300"
		 * 
		 */
		align = null;

		bgcolor = BG_COLOR;
		fgcolor = FG_COLOR;
		screencolor = null;
		lightcolor = null;
		width = -1;
		height = -1;

		strWidth = null;
		strHeight = null;

		// JW
		passthrough = null;
		preview = null;
		allowfullscreen = false;
		download = false;
	}

	protected void setParam(VideoPlayerParamBean param) {
		params.add(param);
	}

	/**
	 * @param video the video to set
	 */
	public void setVideo(String video) {
		this.video = video;
	}

	/**
	 * @param autostart the autostart to set
	 */
	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}

	/**
	 * @param showPlay the showPlay to set
	 * @throws PageException
	 */
	public void setShowplay(String showPlay) throws PageException {
		this.showPlay = lucee.runtime.video.Range.toRange(showPlay);
	}

	public void setId(String id) throws PageException {
		this.id = Caster.toVariableName(id);
	}

	/**
	 * @param showPause the showPause to set
	 * @throws PageException
	 */
	public void setShowpause(String showPause) throws PageException {
		this.showPause = lucee.runtime.video.Range.toRange(showPause);
	}

	/**
	 * @param showTimeline the showTimeline to set
	 * @throws PageException
	 */
	public void setShowtimeline(String showTimeline) throws PageException {
		this.showTimeline = lucee.runtime.video.Range.toRange(showTimeline);
	}

	/**
	 * @param color the background color to set
	 * @throws PageException
	 */
	public void setBgcolor(String color) throws PageException {
		this.bgcolor = ColorCaster.toColor(color);
	}

	public void setBackgroundcolor(String color) throws PageException {
		setBgcolor(color);
	}

	public void setBackground(String color) throws PageException {
		setBgcolor(color);
	}

	public void setScreencolor(String color) throws PageException {
		this.screencolor = ColorCaster.toColor(color);
	}

	public void setLightcolor(String color) throws PageException {
		this.lightcolor = ColorCaster.toColor(color);
	}

	/**
	 * @param color the background color to set
	 * @throws PageException
	 */
	public void setFgcolor(String color) throws PageException {
		this.fgcolor = ColorCaster.toColor(color);
	}

	public void setForeground(String color) throws PageException {
		setFgcolor(color);
	}

	public void setForegroundcolor(String color) throws PageException {
		setFgcolor(color);
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(String strWidth) {
		this.strWidth = strWidth;
		this.width = Caster.toIntValue(strWidth, -1);
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String strHeight) {
		this.strHeight = strHeight;
		this.height = Caster.toIntValue(strHeight, -1);
	}

	@Override
	public int doStartTag() throws PageException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {

		// fill top video to params
		if (video != null) {
			VideoPlayerParamBean vppb = new VideoPlayerParamBean();
			vppb.setVideo(pageContext, video);
			if (!StringUtil.isEmpty(preview)) vppb.setImage(pageContext, preview);
			params.add(vppb);
		}
		else {
			if (!StringUtil.isEmpty(preview)) throw new ApplicationException("attribute [preview] is only allowed when attribute [video] is used");
		}

		if (params.size() == 0) throw new ApplicationException("you have to define at least one video source");

		// calculate dimension
		int[] dim = calculateDimension(pageContext, params, width, strWidth, height, strHeight);

		// print.out(width+":"+height);
		// print.out(strWidth+":"+strHeight);
		width = dim[0];
		height = dim[1];

		// print.out(width+":"+height);

		// playlist
		int dspHeight = -1, dspWidth = -1;
		if (playlist != PLAYLIST_NONE) {
			if (playlistSize < 20) playlistSize = playlist == PLAYLIST_BOTTOM ? 100 : 200;
			if (playlist == PLAYLIST_BOTTOM) {
				dspHeight = height;
				height += playlistSize;
			}
			else {
				dspWidth = width;
				width += playlistSize;
			}
		}
		else playlistThumbnails = false;

		VideoPlayerParamBean param;

		String id = getId();//
		String placeholderId = "ph_" + id;
		String flashId = "swf_" + id;

		StringBuffer sb = new StringBuffer();

		write(sb, "<script type=\"text/javascript\" src=\"/lucee/swfobject.js.cfm\"></script>");
		write(sb, "<div ");

		if (passthrough != null) {
			Iterator<Entry<Key, Object>> it = passthrough.entryIterator();
			Entry<Key, Object> e;
			String key;
			while (it.hasNext()) {
				e = it.next();
				key = e.getKey().getString();
				if (StringUtil.startsWithIgnoreCase(key, "div.")) write(sb, key.substring(4) + "=\"" + Caster.toString(e.getValue()) + "\" ");
			}
		}
		write(sb, (align != null ? "align=\"" + align + "\"" : "") + " id=\"" + placeholderId
				+ "\"><a href=\"http://www.macromedia.com/go/getflashplayer\">Get the Flash Player</a> to see this player.</a></div>");

		write(sb, "<script type=\"text/javascript\">\n");
		write(sb,
				"var so = new SWFObject(\"/lucee/mediaplayer.swf.cfm\", \"" + flashId + "\", \"" + width + "\", \"" + (height) + "\", \"8\", \"" + format("#", bgcolor) + "\");\n");

		// script
		addParam(sb, "allowscriptaccess", "always");
		addVariable(sb, "enablejs", "true");
		addVariable(sb, "javascriptid", flashId);

		addVariable(sb, "shuffle", "false");
		addVariable(sb, "linktarget", target);
		addVariable(sb, "linkfromdisplay", Caster.toString(linkfromdisplay));
		addVariable(sb, "abouttxt", Constants.NAME + " Video Player");
		addVariable(sb, "aboutlnk", "http://lucee.org");

		// control
		addParam(sb, "allowfullscreen", Caster.toString(allowfullscreen));
		addParam(sb, "usefullscreen", Caster.toString(allowfullscreen));
		addVariable(sb, "autostart", Caster.toString(autostart));
		if (!StringUtil.isEmpty(overstretch)) addVariable(sb, "overstretch", overstretch);
		addVariable(sb, "showdownload", Caster.toString(download));

		// color
		if (lightcolor == null) lightcolor = fgcolor.brighter();
		if (screencolor == null) screencolor = Color.BLACK;// fgcolor.brighter();
		addVariable(sb, "backcolor", format("0x", bgcolor));
		addVariable(sb, "frontcolor", format("0x", fgcolor));
		addVariable(sb, "lightcolor", format("0x", lightcolor));
		addVariable(sb, "screencolor", format("0x", screencolor));

		if (passthrough != null) {
			Iterator<Entry<Key, Object>> it = passthrough.entryIterator();
			Entry<Key, Object> e;
			String key;
			while (it.hasNext()) {
				e = it.next();
				key = e.getKey().getString();
				if (StringUtil.startsWithIgnoreCase(key, "param.")) addParam(sb, key.substring(6), Caster.toString(e.getValue()));
				else if (StringUtil.startsWithIgnoreCase(key, "variable.")) addVariable(sb, key.substring(9), Caster.toString(e.getValue()));
				else if (StringUtil.startsWithIgnoreCase(key, "div.")) {
				}
				else addVariable(sb, key, Caster.toString(e.getValue()));
			}
		}

		if (params.size() > 1 && group) addVariable(sb, "repeat", "true");

		/*
		 * if(playlist!=PLAYLIST_NONE) { if(playlistSize<20)playlistSize=playlist==PLAYLIST_BOTTOM?300:200;
		 * if(playlist==PLAYLIST_BOTTOM) { addVariable(sb,"displayheight",Caster.toString(height));
		 * height+=playlistSize; } else { addVariable(sb,"displaywidth",Caster.toString(width));
		 * width+=playlistSize; } if(playlistThumbnails &&
		 * hasImages())addVariable(sb,"thumbsinplaylist","true"); }
		 */

		// dimension
		if (dspWidth > 0) addVariable(sb, "displaywidth", Caster.toString(dspWidth));
		if (dspHeight > 0) addVariable(sb, "displayheight", Caster.toString(dspHeight));
		addVariable(sb, "width", Caster.toString(width));
		addVariable(sb, "height", Caster.toString(height));
		if (playlistThumbnails && hasImages()) addVariable(sb, "thumbsinplaylist", "true");

		write(sb, "so.write(\"" + placeholderId + "\");\n");
		// if(params.size()>1) {
		Iterator it = params.iterator();
		while (it.hasNext()) {
			param = (VideoPlayerParamBean) it.next();
			addItem(sb, flashId, param);
		}
		// }
		write(sb, "</script>");
		try {
			if (debug) {
				pageContext.forceWrite("<pre>" + StringUtil.replace(sb.toString(), "<", "&lt;", false) + "</pre>");
			}
			pageContext.forceWrite(sb.toString());

		}
		catch (IOException e) {

		}
		return EVAL_PAGE;
	}

	private synchronized String getId() {
		return CreateUniqueId.invoke();
	}

	private boolean hasImages() {
		Iterator it = params.iterator();
		while (it.hasNext()) {
			if (((VideoPlayerParamBean) it.next()).getImage() != null) return true;
		}
		return false;
	}

	private void addItem(StringBuffer sb, String id, VideoPlayerParamBean param) {
		// sb.append("setTimeout('thisMovie(\""+id+"\").addItem({file:\""+JSStringFormat.invoke(path)+"\"},null);',1000);\n");

		// file
		String file = "file:'" + JSStringFormat.invoke(toPath(param.getResource())) + "'";

		// image
		String image = "";
		if (param.getImage() != null) {
			image = ",image:'" + JSStringFormat.invoke(toPath(param.getImage())) + "'";
		}

		// title
		String title = "";
		if (!StringUtil.isEmpty(param.getTitle())) {
			title = ",title:'" + JSStringFormat.invoke(param.getTitle()) + "'";
		}

		// link
		String link = "";
		if (!StringUtil.isEmpty(param.getLink())) {
			link = ",link:'" + JSStringFormat.invoke(param.getLink()) + "'";
		}

		// author
		String author = "";
		if (!StringUtil.isEmpty(param.getAuthor())) {
			author = ",author:'" + JSStringFormat.invoke(param.getAuthor()) + "'";
		}

		sb.append("addItem('" + id + "',{" + file + title + image + link + author + "});\n");
	}

	private void addVariable(StringBuffer sb, String name, String value) {
		value = JSStringFormat.invoke(value);
		if (!(value.equals("false") || value.equals("true"))) value = "'" + value + "'";
		sb.append("so.addVariable('" + JSStringFormat.invoke(name) + "'," + value + ");\n");
	}

	private void addParam(StringBuffer sb, String name, String value) {
		sb.append("so.addParam('" + name + "','" + value + "');\n");
	}

	private static int[] calculateDimension(PageContext pc, List params, int width, String strWidth, int height, String strHeight) throws PageException {
		Iterator it = params.iterator();
		ArrayList<VideoInputImpl> sources = new ArrayList<VideoInputImpl>();
		// Resource[] sources=new Resource[params.size()];
		VideoPlayerParamBean param;

		while (it.hasNext()) {
			param = (VideoPlayerParamBean) it.next();
			if (param.getVideo() != null) sources.add(new VideoInputImpl(param.getVideo()));
		}
		return VideoUtilImpl.getInstance().calculateDimension(pc, sources.toArray(new VideoInput[sources.size()]), width, strWidth, height, strHeight);

	}

	private String toPath(Resource res) {
		if (!(res instanceof FileResource)) return res.getAbsolutePath();

		// Config config=pageContext.getConfig();
		PageSource ps = pageContext.toPageSource(res, null);
		if (ps == null) return res.getAbsolutePath();

		String realPath = ps.getRealpath();
		realPath = realPath.replace('\\', '/');
		if (realPath.endsWith("/")) realPath = realPath.substring(0, realPath.length() - 1);

		// print.out("real:"+realPath);
		String mapping = ps.getMapping().getVirtual();
		mapping = mapping.replace('\\', '/');
		if (mapping.endsWith("/")) mapping = mapping.substring(0, mapping.length() - 1);

		return mapping + realPath;

	}

	private void write(StringBuffer sb, String string) {
		sb.append(string);
	}

	private static String format(String prefix, Color color) {
		return prefix + toHex(color.getRed()) + toHex(color.getGreen()) + toHex(color.getBlue());
	}

	private static String toHex(int value) {
		String str = Integer.toHexString(value);
		if (str.length() == 1) return "0".concat(str);
		return str;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @param passthrough the passthrough to set
	 */
	public void setPassthrough(Struct passthrough) {
		this.passthrough = passthrough;
	}

	/**
	 * @param preview the preview to set
	 * @throws ExpressionException
	 */
	public void setPreview(String preview) {
		this.preview = preview;// ResourceUtil.toResourceExisting(pageContext, preview);
	}

	/**
	 * @param allowfullscreen the allowfullscreen to set
	 */
	public void setAllowfullscreen(boolean allowfullscreen) {
		this.allowfullscreen = allowfullscreen;
	}

	public void setAlign(String strAlign) throws ApplicationException {
		if (StringUtil.isEmpty(strAlign)) return;
		strAlign = strAlign.trim().toLowerCase();
		if ("right".equals(strAlign)) this.align = "right";
		else if ("center".equals(strAlign)) this.align = "center";
		else if ("left".equals(strAlign)) this.align = "left";
		else throw new ApplicationException("invalid value for attribute align [" + strAlign + "], valid values are [left,center,right]");
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(boolean group) {
		this.group = group;
	}

	public void setLinktarget(String target) {
		this.target = target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setLinkfromdisplay(boolean linkfromdisplay) {
		this.linkfromdisplay = linkfromdisplay;
	}

	/**
	 * @param playlistThumbnails the playlistThumbnails to set
	 */
	public void setPlaylistthumbnails(boolean playlistThumbnails) {
		this.playlistThumbnails = playlistThumbnails;
	}

	public void setThumbnails(boolean playlistThumbnails) {
		setPlaylistthumbnails(playlistThumbnails);
	}

	public void setThumbs(boolean playlistThumbnails) {
		setPlaylistthumbnails(playlistThumbnails);
	}

	/**
	 * @param playlistSize the playlistSize to set
	 */
	public void setPlaylistsize(double playlistSize) throws ApplicationException {
		if (playlistSize <= 40) throw new ApplicationException("playlist size has to be a positive number, at least 41px");
		this.playlistSize = (int) playlistSize;
	}

	/**
	 * @param playlist the playlist to set
	 */
	public void setPlaylist(String strPlaylist) throws PageException {
		strPlaylist = strPlaylist.trim().toLowerCase();
		if ("right".equals(strPlaylist)) playlist = PLAYLIST_RIGHT;
		else if ("bottom".equals(strPlaylist)) playlist = PLAYLIST_BOTTOM;
		else if ("none".equals(strPlaylist)) playlist = PLAYLIST_NONE;
		else if (Decision.isBoolean(strPlaylist)) {
			playlist = Caster.toBooleanValue(strPlaylist) ? PLAYLIST_BOTTOM : PLAYLIST_NONE;
		}
		else throw new ApplicationException("invalid playlist definition [" + strPlaylist + "], valid values are [right,bottom,none]");
	}

	/**
	 * @param overstretch the overstretch to set
	 */
	public void setOverstretch(String overstretch) throws PageException {
		overstretch = overstretch.trim().toLowerCase();
		if ("fit".equals(overstretch)) overstretch = "fit";
		else if ("none".equals(overstretch)) overstretch = "none";
		else if ("proportion".equals(overstretch)) overstretch = "true";
		else if (Decision.isBoolean(overstretch)) {
			overstretch = Caster.toString(Caster.toBooleanValue(overstretch));
		}
		else throw new ApplicationException("invalid overstretch definition [" + overstretch + "], valid values are [fit,none,true,false]");

		this.overstretch = overstretch;
	}

	/**
	 * @param download the download to set
	 */
	public void setDownload(boolean download) {
		this.download = download;
	}

}