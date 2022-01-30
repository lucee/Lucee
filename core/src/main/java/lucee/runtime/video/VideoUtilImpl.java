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

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.type.http.HTTPResource;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.string.Hash;
import lucee.runtime.op.Caster;

public class VideoUtilImpl implements VideoUtil {

	private static Map<String, SoftReference<int[]>> sizes = new ConcurrentHashMap<String, SoftReference<int[]>>();
	private static VideoUtilImpl instance = new VideoUtilImpl();

	private VideoUtilImpl() {
	}

	public static VideoUtilImpl getInstance() {
		return instance;
	}

	/**
	 * @see lucee.runtime.video.VideoUtil#createVideoInput(lucee.commons.io.res.Resource)
	 */
	@Override
	public VideoInput createVideoInput(Resource input) {
		return new VideoInputImpl(input);
	}

	/**
	 * @see lucee.runtime.video.VideoUtil#createVideoOutput(lucee.commons.io.res.Resource)
	 */
	@Override
	public VideoOutput createVideoOutput(Resource output) {
		return new VideoOutputImpl(output);
	}

	/**
	 * @see lucee.runtime.video.VideoUtil#createVideoProfile()
	 */
	@Override
	public VideoProfile createVideoProfile() {
		return new VideoProfileImpl();
	}

	@Override
	public long toBytes(String byt) throws PageException {
		byt = byt.trim().toLowerCase();
		if (byt.endsWith("kb/s") || byt.endsWith("kbps")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 4).trim()) * 1024);
		}
		if (byt.endsWith("mb/s") || byt.endsWith("mbps")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 4).trim()) * 1024 * 1024);
		}
		if (byt.endsWith("gb/s") || byt.endsWith("gbps")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 4).trim()) * 1024 * 1024 * 1024);
		}
		if (byt.endsWith("b/s") || byt.endsWith("bps")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 3).trim()));
		}

		if (byt.endsWith("kbit/s")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 6).trim()) * 1024);
		}
		if (byt.endsWith("mbit/s")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 6).trim()) * 1024 * 1024);
		}
		if (byt.endsWith("gbit/s")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 6).trim()) * 1024 * 1024 * 1024);
		}
		if (byt.endsWith("bit/s")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 5).trim()));
		}

		if (byt.endsWith("kb")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()) * 1024);
		}
		if (byt.endsWith("mb")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()) * 1024 * 1024);
		}
		if (byt.endsWith("gb")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()) * 1024 * 1024 * 1024);
		}

		if (byt.endsWith("g")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()) * 1024 * 1024 * 1024);
		}
		if (byt.endsWith("m")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()) * 1024 * 1024);
		}
		if (byt.endsWith("k")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()) * 1024);
		}
		if (byt.endsWith("b")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()));
		}
		return Caster.toLongValue(byt);
	}

	@Override
	public long toHerz(String byt) throws PageException {
		byt = byt.trim().toLowerCase();
		if (byt.endsWith("mhz")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 3).trim()) * 1000 * 1000);
		}
		if (byt.endsWith("khz")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 3).trim()) * 1000);
		}
		if (byt.endsWith("hz")) {
			return (long) (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()));
		}
		return Caster.toLongValue(byt);
	}

	@Override
	public long toMillis(String time) throws PageException {
		int last = 0, index = time.indexOf(':');
		long hour = Caster.toIntValue(time.substring(last, index).trim());
		last = index + 1;

		index = time.indexOf(':', last);
		long minute = Caster.toIntValue(time.substring(last, index).trim());

		double seconds = Caster.toDoubleValue(time.substring(index + 1).trim());
		return (hour * 60L * 60L * 1000L) + (minute * 60L * 1000L) + ((int) (seconds * 1000F));
	}

	public static VideoExecuter createVideoExecuter(Config config) throws ClassException {
		Class clazz = config.getVideoExecuterClass();
		return (VideoExecuter) ClassUtil.loadInstance(clazz);
	}

	@Override
	public int[] calculateDimension(PageContext pc, VideoInput[] sources, int width, String strWidth, int height, String strHeight) throws PageException {
		int[] rtn;

		if (width != -1 && height != -1) {
			return new int[] { width, height };
		}

		// video component not installed
		try {
			if (VideoUtilImpl.createVideoExecuter(pc.getConfig()) instanceof VideoExecuterNotSupported) {
				throw new ApplicationException("attributes width/height are required when no video analyser is installed");
			}
		}
		catch (ClassException e) {

		}

		VideoInput source;

		// hash
		StringBuffer sb = new StringBuffer(strHeight + "-" + strWidth);
		for (int i = 0; i < sources.length; i++) {
			sb.append(sources[i].getResource().toString());
		}

		// get from casche
		String key = Hash.call(pc, sb.toString());

		SoftReference<int[]> tmp = sizes.get(key);
		int[] ci = tmp == null ? null : tmp.get();
		if (ci != null) {
			return ci;
		}
		// getSize
		int w = 0, h = 0;
		try {
			for (int i = 0; i < sources.length; i++) {
				source = sources[i];
				checkResource(source.getResource());

				VideoInfo info = VideoUtilImpl.createVideoExecuter(pc.getConfig()).info(pc.getConfig(), source);

				if (w < info.getWidth()) {
					h = info.getHeight();
					w = info.getWidth();
				}

			}
		}
		catch (Exception ve) {
			throw Caster.toPageException(ve);
		}

		// calculate only height
		if (width != -1) {
			height = calculateSingle(w, width, strHeight, h);
		}
		// calculate only height
		else if (height != -1) {
			width = calculateSingle(h, height, strWidth, w);
		}
		else {
			width = procent2pixel(strWidth, w);
			height = procent2pixel(strHeight, h);
			if (width != -1 && height != -1) {
			}
			else if (width == -1 && height == -1) {
				width = w;
				height = h;
			}
			else if (width != -1) height = calucalteFromOther(h, w, width);
			else width = calucalteFromOther(w, h, height);

		}
		rtn = new int[] { width, height };
		sizes.put(key, new SoftReference<int[]>(rtn));
		return rtn;
	}

	private static int procent2pixel(String str, int source) throws ExpressionException {
		if (!StringUtil.isEmpty(str)) {
			if (StringUtil.endsWith(str, '%')) {
				str = str.substring(0, str.length() - 1).trim();
				double procent = Caster.toDoubleValue(str);
				if (procent < 0) throw new ExpressionException("procent has to be positive number (now " + str + ")");
				return (int) (source * (procent / 100D));
			}
			return Caster.toIntValue(str);
		}
		return -1;
	}

	private static int calculateSingle(int srcOther, int destOther, String strDim, int srcDim) throws ExpressionException {
		int res = procent2pixel(strDim, srcDim);
		if (res != -1) return res;
		return calucalteFromOther(srcDim, srcOther, destOther);// (int)(Caster.toDoubleValue(srcDim)*Caster.toDoubleValue(destOther)/Caster.toDoubleValue(srcOther));
	}

	private static int calucalteFromOther(int srcDim, int srcOther, int destOther) {
		return (int) (Caster.toDoubleValue(srcDim) * Caster.toDoubleValue(destOther) / Caster.toDoubleValue(srcOther));
	}

	private static void checkResource(Resource resource) throws ApplicationException {
		if (resource instanceof FileResource) return;
		if (resource instanceof HTTPResource) throw new ApplicationException("attribute width and height are required when external sources are invoked");

		throw new ApplicationException("the resource type [" + resource.getResourceProvider().getScheme() + "] is not supported");
	}
}