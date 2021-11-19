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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Random;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagSupport;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.video.ProfileCollection;
import lucee.runtime.video.VideoExecuter;
import lucee.runtime.video.VideoInfo;
import lucee.runtime.video.VideoInput;
import lucee.runtime.video.VideoInputImpl;
import lucee.runtime.video.VideoOutput;
import lucee.runtime.video.VideoOutputImpl;
import lucee.runtime.video.VideoProfile;
import lucee.runtime.video.VideoProfileImpl;
import lucee.runtime.video.VideoUtilImpl;

/**
 * implementation of the tag Compiler
 */
public class Video extends TagSupport {

	private static final int ACTION_CONVERT = 0;
	private static final int ACTION_INFO = 1;
	private static final int ACTION_CONCAT = 2;
	private static final int ACTION_CUT_IMAGE = 4;
	private static final int ACTION_INSTALL = 8;
	private static final int ACTION_UNINSTALL = 16;

	public static final int NAMECONFLICT_UNDEFINED = 0;
	public static final int NAMECONFLICT_ERROR = 1;
	public static final int NAMECONFLICT_SKIP = 2;
	public static final int NAMECONFLICT_OVERWRITE = 3;
	public static final int NAMECONFLICT_MAKEUNIQUE = 4;

	public static final int EXECUTION_QUALITY = 0;
	public static final int EXECUTION_PERFORMANCE = 1;
	private static final Key SOURCE = KeyConstants._source;
	private static final Key SOURCE1 = KeyImpl.getInstance("source1");
	private static final Key SOURCE2 = KeyImpl.getInstance("source2");
	private static final Key AUDIO = KeyImpl.getInstance("audio");
	private static final Key VIDEO = KeyImpl.getInstance("video");

	private static VideoUtilImpl util = VideoUtilImpl.getInstance();

	private String result = "cfvideo";
	private int action;
	private String strAction;
	private VideoInput source;
	private VideoInput source1;
	private VideoInput source2;
	private VideoOutput destination;
	private int nameconflict = NAMECONFLICT_UNDEFINED;
	private String name;
	private int width = -1;
	private int height = -1;
	private String strWidth;
	private String strHeight;
	private Struct data;

	private long audiosamplerate = 0;
	private long audioBitrate = 0;
	private double startTime = 0;
	private long startFrame = 0;
	private double maxTime = 0;
	private long maxFrame = 0;

	private VideoProfile profile = null;

	private int aspectRatio;
	private int framerate;
	private long videoBitrate;
	private long videoBitrateMin;
	private long videoBitrateMax;
	private long videoBitrateTolerance;
	private String debug = null;

	private String author;
	private String title;
	private String comment;
	private String copyright;
	private String videoCodec;
	private String audioCodec;
	private long buffersize;
	private int execution = EXECUTION_PERFORMANCE;
	private static ProfileCollection _profileCollection;

	public Video() {

	}

	@Override
	public void release() {
		super.release();
		result = "cfvideo";
		execution = EXECUTION_PERFORMANCE;
		source = null;
		source1 = null;
		source2 = null;
		destination = null;
		nameconflict = NAMECONFLICT_UNDEFINED;
		name = null;
		width = -1;
		height = -1;
		profile = null;
		audioBitrate = 0;
		videoCodec = null;
		audioCodec = null;

		audiosamplerate = 0;
		aspectRatio = 0;
		framerate = 0;
		videoBitrate = 0;
		videoBitrateMin = 0;
		videoBitrateMax = 0;
		videoBitrateTolerance = 0;
		buffersize = 0;
		author = null;
		title = null;
		comment = null;
		copyright = null;
		debug = null;

		maxTime = 0;
		maxFrame = 0;
		startTime = 0;
		startFrame = 0;
		strWidth = null;
		strHeight = null;
		data = null;
	}

	/**
	 * @param action the action to set
	 * @throws PageException
	 */
	public void setAction(String action) throws PageException {
		strAction = action;
		action = action.toLowerCase().trim();
		if (action.equals("concat")) this.action = ACTION_CONCAT;
		else if (action.equals("merge")) this.action = ACTION_CONCAT;
		else if (action.equals("convert")) this.action = ACTION_CONVERT;
		// else if(action.equals("cut")) this.action=ACTION_CUT;
		else if (action.equals("cut image")) this.action = ACTION_CUT_IMAGE;
		else if (action.equals("cutimage")) this.action = ACTION_CUT_IMAGE;
		else if (action.equals("cut_image")) this.action = ACTION_CUT_IMAGE;
		else if (action.equals("cut-image")) this.action = ACTION_CUT_IMAGE;
		else if (action.equals("info")) this.action = ACTION_INFO;

		else if (action.equals("install")) this.action = ACTION_INSTALL;
		else if (action.equals("uninstall")) this.action = ACTION_UNINSTALL;

		else throw doThrow("invalid value for attribute action for tag video [" + action + "], " + "valid actions are [concat, convert, cutImage, info,install,uninstall]");

	}

	/**
	 * @param source the source to set
	 * @throws PageException
	 */
	public void setSource(String source) throws PageException {
		this.source = new VideoInputImpl(ResourceUtil.toResourceExisting(pageContext, source));
	}

	public void setData(Struct data) {
		this.data = data;
	}

	/**
	 * @param destination the destination to set
	 * @throws PageException
	 */
	public void setDestination(String destination) {
		this.destination = new VideoOutputImpl(ResourceUtil.toResourceNotExisting(pageContext, destination));
	}

	/**
	 * set the value nameconflict Action to take if filename is the same as that of a file in the
	 * directory.
	 * 
	 * @param nameconflict value to set
	 * @throws ApplicationException
	 **/
	public void setNameconflict(String nameconflict) throws PageException {
		nameconflict = nameconflict.toLowerCase().trim();
		if ("error".equals(nameconflict)) this.nameconflict = NAMECONFLICT_ERROR;
		else if ("skip".equals(nameconflict)) this.nameconflict = NAMECONFLICT_SKIP;
		else if ("overwrite".equals(nameconflict)) this.nameconflict = NAMECONFLICT_OVERWRITE;
		else if ("makeunique".equals(nameconflict)) this.nameconflict = NAMECONFLICT_MAKEUNIQUE;
		else throw doThrow("invalid value for attribute nameconflict [" + nameconflict + "]", "valid values are [error,skip,overwrite,makeunique]");
	}

	public void setProfile(String strProfile) throws PageException {
		VideoProfile p = getProfile(strProfile);
		if (p != null) profile = p.duplicate();
		else throw doThrow("invalid profile definition [" + strProfile + "], valid profiles are [" + getProfileKeyList() + "]");
	}

	public void setExecution(String execution) throws PageException {

		execution = execution.toLowerCase().trim();
		if ("quality".equals(execution)) this.execution = EXECUTION_QUALITY;
		else if ("q".equals(execution)) this.execution = EXECUTION_QUALITY;
		else if ("performance".equals(execution)) this.execution = EXECUTION_PERFORMANCE;
		else if ("p".equals(execution)) this.execution = EXECUTION_PERFORMANCE;
		else throw doThrow("invalid value for attribute execution [" + execution + "]", "valid values are [quality,performance]");
	}

	public void setQuality(String strQuality) throws PageException {
		setProfile(strQuality);
	}

	@Override
	public int doStartTag() throws PageException {

		try {
			if (action == ACTION_CONVERT) doActionConvert();
			else if (action == ACTION_CONCAT) doActionConcat();
			else if (action == ACTION_CUT_IMAGE) doActionCutImage();
			else if (action == ACTION_INFO) doActionInfo();
			else if (action == ACTION_INSTALL) doActionInstall();
			else if (action == ACTION_UNINSTALL) doActionUninstall();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		return SKIP_BODY;
	}

	private void doActionInstall() throws ClassException, IOException {
		if (data == null) data = new StructImpl();
		getVideoExecuter().install(pageContext.getConfig(), data);
	}

	private void doActionUninstall() throws ClassException, IOException {
		getVideoExecuter().uninstall(pageContext.getConfig());
	}

	public Struct doActionInfo() throws PageException, IOException {
		return doActionInfo(source);

	}

	private Struct doActionInfo(VideoInput source) throws PageException, IOException {

		// precheck settings
		checkFile(source, "source", true, true, false);

		// VideoConfig vc = getConfig();
		debug(source);
		Struct info = toStruct(getVideoExecuter().info(pageContext.getConfig(), source));
		pageContext.setVariable(result, info);
		return info;

	}

	private VideoExecuter getVideoExecuter() throws ClassException {
		return VideoUtilImpl.createVideoExecuter(pageContext.getConfig());
	}

	private VideoInfo getInfo(VideoInput source) throws PageException, IOException {

		// precheck settings
		checkFile(source, "source", true, true, false);

		// execute
		return getVideoExecuter().info(pageContext.getConfig(), source);
		// print.out(raw);

		// write cfvideo
		// return new Info(raw,true,startTime,maxTime);
	}

	private void doActionCutImage() throws PageException, IOException {

		// precheck settings
		checkFile(source, "source", true, true, false);
		if (!checkDestination(source, destination, name, nameconflict)) return;

		// input
		if (profile == null) profile = new VideoProfileImpl();

		// settings
		settings(destination, profile);

		destination.setMaxFrames(1);
		destination.setFormat("image2");
		// execute
		Struct info = toStruct(getVideoExecuter().convert(pageContext.getConfig(), new VideoInput[] { source }, destination, profile));

		// write cfvideo
		debug(source);
		pageContext.setVariable(result, info);

		// setResult(raw,false);

	}

	private void doActionConcat() throws PageException, IOException {
		// precheck settings
		checkFile(source1, "source1", true, true, false);
		checkFile(source2, "source2", true, true, false);
		if (!checkDestination(source1, destination, name, nameconflict)) return;

		Pair s1 = toMpeg(source1);
		Pair s2 = toMpeg(source2);
		source = new VideoInputImpl(pageContext.getConfig().getTempDirectory().getRealResource("tmp-" + new Random().nextInt() + ".mpg"));
		try {
			merge(s1.res, s2.res, source.getResource());
			Struct sct = doActionConvert();
			sct.setEL(SOURCE1, s1.sct);
			sct.setEL(SOURCE2, s2.sct);
			sct.removeEL(SOURCE);

		}
		finally {
			source.getResource().delete();
			if (!s1.res.equals(source1.getResource())) s1.res.delete();
			if (!s2.res.equals(source2.getResource())) s2.res.delete();
		}

	}

	private void merge(Resource in1, Resource in2, Resource out) throws IOException {
		InputStream is1 = null;
		InputStream is2 = null;
		OutputStream os = null;
		try {
			is1 = in1.getInputStream();
			is2 = in2.getInputStream();
			os = out.getOutputStream();
		}
		catch (IOException ioe) {
			IOUtil.close(is1);
			IOUtil.close(is2);
			IOUtil.close(os);
			throw ioe;
		}

		try {
			copy(is1, os);
			copy(is2, os);
		}
		finally {
			IOUtil.close(is1);
			IOUtil.close(is2);
			IOUtil.close(os);
		}
	}

	public final static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[0xffff];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
	}

	private Pair toMpeg(VideoInput vi) throws PageException, IOException {
		VideoInfo info = getInfo(vi);
		// Struct sct = _doActionInfo(vi);

		if ("mpeg1video".equals(info.getVideoCodec())) return new Pair(vi.getResource(), toStruct(info));

		VideoOutput tmp = new VideoOutputImpl(pageContext.getConfig().getTempDirectory().getRealResource("tmp-" + new Random().nextInt() + ".mpg"));
		try {
			doActionConvert(vi, tmp, null, null, NAMECONFLICT_ERROR);
			return new Pair(tmp.getResource(), toStruct(info));
		}
		catch (PageException pe) {
			tmp.getResource().delete();
			throw pe;
		}
		catch (IOException ioe) {
			tmp.getResource().delete();
			throw ioe;
		}

	}

	private Struct doActionConvert() throws PageException, IOException {
		return doActionConvert(source, destination, profile, name, nameconflict);
	}

	private Struct doActionConvert(VideoInput source, VideoOutput destination, VideoProfile quality, String name, int nameconflict) throws PageException, IOException {

		// precheck settings
		checkFile(source, "source", true, true, false);
		if (!checkDestination(source, destination, name, nameconflict)) return new StructImpl();

		// input
		if (quality == null) quality = new VideoProfileImpl();

		// settings
		settings(destination, quality);

		// execute
		Struct info = toStruct(getVideoExecuter().convert(pageContext.getConfig(), new VideoInput[] { source }, destination, quality));

		// write cfvideo
		debug(source);

		pageContext.setVariable(result, info);
		return info;
		// return setResult(raw,false);
	}

	private void debug(VideoInput vi) throws PageException {
		if (!StringUtil.isEmpty(debug)) pageContext.setVariable(debug, vi.getCommandAsString());
	}

	private void settings(VideoOutput vo, VideoProfile vq) throws PageException, IOException {
		defineSize(vq, new VideoInput[] { source });
		if (audioBitrate > 0) vq.setAudioBitrate(audioBitrate);
		if (aspectRatio > 0) vq.setAspectRatio(aspectRatio);
		if (framerate > 0) vq.setFramerate(framerate);
		if (videoBitrate > 0) vq.setVideoBitrate(videoBitrate);
		if (videoBitrateMin > 0) vq.setVideoBitrateMin(videoBitrateMin);
		if (videoBitrateMax > 0) vq.setVideoBitrateMax(videoBitrateMax);
		if (videoBitrateTolerance > 0) vq.setVideoBitrateTolerance(videoBitrateTolerance);
		if (audiosamplerate > 0) vq.setAudioSamplerate(audiosamplerate);
		if (buffersize > 0) vq.setBufferSize(buffersize);
		if (execution == EXECUTION_QUALITY) vq.setPass(2);

		if (!StringUtil.isEmpty(title)) vo.setTitle(title);
		if (!StringUtil.isEmpty(author)) vo.setAuthor(author);
		if (!StringUtil.isEmpty(comment)) vo.setComment(comment);
		if (!StringUtil.isEmpty(copyright)) vo.setCopyright(copyright);
		if (!StringUtil.isEmpty(videoCodec)) vq.setVideoCodec(videoCodec);
		if (!StringUtil.isEmpty(audioCodec)) vq.setAudioCodec(audioCodec);

		if (framerate == 0 && (startFrame > 0 || maxTime > 0)) {
			framerate = (int) getInfo(source).getFramerate();
		}

		// start
		if (startFrame > 0) {
			if (framerate > 0) startTime = (startFrame / framerate);
		}
		if (startTime > 0) destination.setOffset(startTime);

		// max
		if (maxTime > 0) {
			if (framerate > 0) maxFrame = (long) (maxTime * framerate);
		}
		if (maxTime > 0) vo.setMaxFrames(maxFrame);

		// maxtime (only for cfvideo struct)
		if (maxFrame > 0 && maxTime == 0) {
			if (framerate != -1) maxTime = maxFrame / framerate;
		}
		destination.setFrameRate(framerate);

	}

	private ProfileCollection getProfileCollection() throws PageException {
		return getProfileCollection(pageContext);
	}

	public static ProfileCollection getProfileCollection(PageContext pc) throws PageException {
		if (_profileCollection == null) {
			try {
				_profileCollection = new ProfileCollection(pc.getConfig());
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return _profileCollection;
	}

	private void defineSize(VideoProfile quality, VideoInput[] sources) throws PageException {
		if (strWidth != null || strHeight != null) {
			int[] dim = VideoUtilImpl.getInstance().calculateDimension(pageContext, sources, width, strWidth, height, strHeight);
			quality.setDimension(dim[0], dim[1]);
		}
	}

	private void checkFile(VideoInput vi, String label, boolean exist, boolean canRead, boolean canWrite) throws PageException {
		if (vi == null) {
			throw doThrow("attribute [" + label + "] is required for tag video action [" + strAction + "]");
		}
		checkFile(vi.getResource(), label, exist, canRead, canWrite);
	}

	private void checkFile(VideoOutput vo, String label, boolean exist, boolean canRead, boolean canWrite) throws PageException {
		if (vo == null) {
			throw doThrow("attribute [" + label + "] is required for tag video action [" + strAction + "]");
		}
		checkFile(vo.getResource(), label, exist, canRead, canWrite);
	}

	private void checkFile(Resource res, String label, boolean exist, boolean canRead, boolean canWrite) throws PageException {

		if (res == null) {
			throw doThrow("attribute [" + label + "] is required for tag video action [" + strAction + "]");
		}

		if (!res.exists()) {
			if (exist) throw doThrow("[" + label + "] file does not exist");
		}
		else {
			if (!res.isFile()) throw doThrow(label + " [" + res.toString() + "] is not a file");
			else if (canRead && !res.canRead()) throw doThrow("no read access to " + label + " [" + res.toString() + "]");
			else if (canWrite && !res.canWrite()) throw doThrow("no write access to " + label + " [" + res.toString() + "]");
		}
	}

	private boolean checkDestination(VideoInput vi, VideoOutput destination, String name, int nameconflict) throws PageException {
		checkFile(destination, "destination", false, false, false);

		// destination
		if (!Util.isEmpty(name)) destination.setResource(destination.getResource().getRealResource(name));
		else if (destination.getResource().isDirectory()) {
			destination.setResource(destination.getResource().getRealResource(vi.getResource().getName()));
		}

		// escape %d
		String _name = destination.getResource().getName();
		if (_name.indexOf("%d") != -1) {
			destination.setResource(destination.getResource().getParentResource().getRealResource(_name.replaceAll("%d", "%%d")));
		}

		if (destination.getResource().exists() && nameconflict != NAMECONFLICT_OVERWRITE) {
			// SKIP
			if (nameconflict == NAMECONFLICT_SKIP) return false;
			// MAKEUNIQUE
			else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) destination.setResource(makeUnique(destination.getResource()));
			// ERROR
			else doThrow("destination file [" + destination.toString() + "] already exist");
		}
		return true;
	}

	private Resource makeUnique(Resource res) {

		String ext = getFileExtension(res);
		String name = getFileName(res);
		ext = (ext == null) ? "" : "." + ext;
		int count = 0;
		while (res.exists()) {
			res = res.getParentResource().getRealResource(name + (++count) + ext);
		}
		return res;
	}

	/**
	 * get file extension of a file object
	 * 
	 * @param file file object
	 * @return extension
	 */
	private static String getFileExtension(Resource file) {
		String name = file.getName();
		int index = name.lastIndexOf('.');
		if (index == -1) return null;
		return name.substring(index + 1).trim();
	}

	/**
	 * get file name of a file object without extension
	 * 
	 * @param file file object
	 * @return name of the file
	 */
	private static String getFileName(Resource file) {
		String name = file.getName();
		int pos = name.lastIndexOf(".");

		if (pos == -1) return name;
		return name.substring(0, pos);
	}

	private VideoProfile getProfile(String strProfile) throws PageException {
		strProfile = strProfile.trim().toLowerCase();
		return getProfileCollection().getProfiles().get(strProfile);
	}

	private String getProfileKeyList() throws PageException {
		Iterator<String> it = getProfileCollection().getProfiles().keySet().iterator();
		StringBuffer sb = new StringBuffer();
		boolean doDel = false;
		while (it.hasNext()) {
			if (doDel) sb.append(", ");
			sb.append(it.next());
			doDel = true;
		}

		return sb.toString();
	}

	private PageException doThrow(String message) {
		return doThrow(message, null);
	}

	private PageException doThrow(String message, String detail) {
		if (detail == null) return new ApplicationException(message);
		return new ApplicationException(message, detail);
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(String strWidth) {
		this.width = Caster.toIntValue(strWidth, -1);
		this.strWidth = strWidth;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String strHeight) {
		this.height = Caster.toIntValue(strHeight, -1);
		this.strHeight = strHeight;
	}

	/**
	 * @param audioBitrate the audioBitrate to set
	 * @throws PageException
	 */
	public void setAudiobitrate(String audioBitrate) throws PageException {
		this.audioBitrate = util.toBytes(audioBitrate);
	}

	/**
	 * @param aspectRatio the aspectRatio to set
	 * @throws PageException
	 */
	public void setAspectratio(String strAspectRatio) throws PageException {
		strAspectRatio = strAspectRatio.trim().toLowerCase();
		if ("16:9".equals(strAspectRatio)) aspectRatio = VideoProfile.ASPECT_RATIO_16_9;
		else if ("4:3".equals(strAspectRatio)) aspectRatio = VideoProfile.ASPECT_RATIO_4_3;
		else throw doThrow("invalid aspect ratio definition [" + strAspectRatio + "], valid values are [16:9, 4:3]");
	}

	/**
	 * @param framerate the framerate to set
	 */
	public void setFramerate(double framerate) {
		setFps(framerate);
	}

	public void setFps(double framerate) {
		this.framerate = (int) framerate;
	}

	/**
	 * @param videoBitrate the videoBitrate to set
	 * @throws PageException
	 */
	public void setVideobitrate(String videoBitrate) throws PageException {
		this.videoBitrate = util.toBytes(videoBitrate);
	}

	/**
	 * @param videoBitrateMin the videoBitrateMin to set
	 * @throws PageException
	 */
	public void setVideobitratemin(String videoBitrateMin) throws PageException {
		this.videoBitrateMin = util.toBytes(videoBitrateMin);
	}

	/**
	 * @param videoBitrateMax the videoBitrateMax to set
	 * @throws PageException
	 */
	public void setVideobitratemax(String videoBitrateMax) throws PageException {
		this.videoBitrateMax = util.toBytes(videoBitrateMax);
	}

	/**
	 * @param videoBitrateTolerance the videoBitrateTolerance to set
	 * @throws PageException
	 */
	public void setVideobitratetolerance(String videoBitrateTolerance) throws PageException {
		this.videoBitrateTolerance = util.toBytes(videoBitrateTolerance);
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @param copyright the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @param max the maxFrames to set
	 * @throws PageException
	 */
	public void setMax(String strMax) throws PageException {
		strMax = strMax.trim().toLowerCase();
		if (strMax.endsWith("f")) this.maxFrame = Caster.toLongValue(strMax.substring(0, strMax.length() - 1));
		else if (strMax.endsWith("ms")) this.maxTime = Caster.toDoubleValue(strMax.substring(0, strMax.length() - 2)) / 1000F;
		else if (strMax.endsWith("s")) this.maxTime = Caster.toDoubleValue(strMax.substring(0, strMax.length() - 1));
		else setStart(strMax + " ms");
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param source1 the source1 to set
	 * @throws PageException
	 */
	public void setSource1(String source1) throws PageException {
		this.source1 = new VideoInputImpl(ResourceUtil.toResourceExisting(pageContext, source1));
	}

	/**
	 * @param source2 the source2 to set
	 * @throws PageException
	 */
	public void setSource2(String source2) throws PageException {
		this.source2 = new VideoInputImpl(ResourceUtil.toResourceExisting(pageContext, source2));
	}

	/**
	 * @param strStart the position to set
	 * @throws PageException
	 */
	public void setStart(String strStart) throws PageException {
		strStart = strStart.trim().toLowerCase();
		if (strStart.endsWith("f")) this.startFrame = Caster.toLongValue(strStart.substring(0, strStart.length() - 1));
		else if (strStart.endsWith("ms")) this.startTime = Caster.toDoubleValue(strStart.substring(0, strStart.length() - 2)) / 1000F;
		else if (strStart.endsWith("s")) this.startTime = Caster.toDoubleValue(strStart.substring(0, strStart.length() - 1));
		else setStart(strStart + " ms");
	}

	/**
	 * @param videoCodec the videoCodec to set
	 */
	public void setVideocodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public void setAudiocodec(String audioCodec) {
		this.audioCodec = audioCodec;
	}

	class Pair {
		Resource res;
		Struct sct;

		public Pair(Resource res, Struct sct) {
			this.res = res;
			this.sct = sct;
		}
	}

	/**
	 * @param audiosamplerate the audiosamplerate to set
	 * @throws PageException
	 */
	public void setAudiosamplerate(String sAudiosamplerate) throws PageException {
		this.audiosamplerate = util.toHerz(sAudiosamplerate);
	}

	public static void checkRestriction() {

	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(String debug) {
		this.debug = debug;
	}

	/**
	 * @param buffersize the buffersize to set
	 */
	public void setBuffersize(double buffersize) {
		this.buffersize = (long) buffersize;
	}

	public Struct toStruct(VideoInfo[] infos) {
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._source, toStruct(infos[0]));
		sct.setEL(KeyConstants._destination, toStruct(infos[1]));
		return sct;
	}

	private Struct toStruct(VideoInfo info) {

		Struct sct = info.toStruct();

		// audio
		Struct audio = Caster.toStruct(sct.get(AUDIO, null), null);
		if (audio == null) {
			audio = new StructImpl();
			sct.setEL(AUDIO, audio);
		}

		// video
		Struct video = Caster.toStruct(sct.get(VIDEO, null), null);
		if (video == null) {
			video = new StructImpl();
			sct.setEL(VIDEO, video);
		}

		// Audio
		audio.setEL("channels", info.getAudioChannels());
		audio.setEL(KeyConstants._codec, info.getAudioCodec());
		if (info.getAudioBitrate() != -1) audio.setEL("bitrate", new Double(info.getAudioBitrate()));
		if (info.getAudioSamplerate() != -1) audio.setEL("samplerate", new Double(info.getAudioSamplerate()));

		// Video
		video.setEL(KeyConstants._codec, info.getVideoCodec());
		video.setEL(KeyConstants._format, info.getVideoFormat());
		if (info.getVideoBitrate() != -1) video.setEL("bitrate", new Double(info.getVideoBitrate()));
		if (info.getFramerate() != -1) video.setEL("framerate", new Double(info.getFramerate()));

		// Allgemein
		if (info.getDuration() != -1) sct.setEL("duration", new Double(info.getDuration()));
		if (info.getHeight() != -1) sct.setEL(KeyConstants._height, new Double(info.getHeight()));
		if (info.getWidth() != -1) sct.setEL(KeyConstants._width, new Double(info.getWidth()));

		return sct;
	}
}