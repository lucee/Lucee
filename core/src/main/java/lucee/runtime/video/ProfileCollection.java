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
package lucee.runtime.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;

public class ProfileCollection {

	private static VideoUtil util = VideoUtilImpl.getInstance();

	private Map<String, VideoProfile> profiles;

	public ProfileCollection(Config config) throws ApplicationException {
		init(config, true);
	}

	private void init(Config config, boolean initProfiles) throws ApplicationException {
		// get the video directory
		Resource dir = ((ConfigImpl) config).getVideoDirectory();

		// get the video.xml
		Resource xml = dir.getRealResource("video.xml");

		// create (if not exist) and return video xml as dom
		Element video;
		try {
			video = getVideoXML(xml);
		}
		catch (Exception e) {
			throw new ApplicationException("can not load video xml file [" + xml + "]", Caster.toClassName(e) + ":" + e.getMessage());
		}

		// translate form DOM to a List of VideoProfile
		if (initProfiles) {
			try {
				profiles = translateVideoXML(video);
			}
			catch (PageException e) {
				throw new ApplicationException("can not load profiles from video xml file [" + xml + "] a type is invalid", e.getMessage());
			}
		}
	}

	/**
	 * @return the qualities
	 */
	public Map<String, VideoProfile> getProfiles() {
		return profiles;
	}

	/**
	 * translate form DOM to a List of VideoProfile
	 * 
	 * @param video
	 * @return
	 * @throws PageException
	 */
	private static Map<String, VideoProfile> translateVideoXML(Element video) throws PageException {
		Map<String, VideoProfile> profiles = new LinkedHashMap<String, VideoProfile>();
		// quality
		Element qd = getChildByName(video, "profiles", false);
		Element[] items = getChildren(qd, "profile");
		Element item;
		VideoProfile vq;
		String value;
		for (int i = 0; i < items.length; i++) {
			item = items[i];
			vq = new VideoProfileImpl();
			// aspect-ratio
			value = item.getAttribute("aspect-ratio");
			if (!Util.isEmpty(value)) vq.setAspectRatio(value);

			// aspect-ratio
			value = item.getAttribute("audio-bitrate");
			if (!Util.isEmpty(value)) vq.setAudioBitrate(util.toBytes(value));

			// audio-samplerate
			value = item.getAttribute("audio-samplerate");
			if (!Util.isEmpty(value)) vq.setAudioSamplerate(util.toHerz(value));

			// dimension
			String w = item.getAttribute("width");
			String h = item.getAttribute("height");
			if (!Util.isEmpty(w) && !Util.isEmpty(h)) {
				vq.setDimension(Caster.toIntValue(w), Caster.toIntValue(h));
			}

			// framerate
			value = item.getAttribute("framerate");
			String value2 = item.getAttribute("fps");
			if (!Util.isEmpty(value)) vq.setFramerate(Caster.toDoubleValue(value));
			else if (!Util.isEmpty(value2)) vq.setFramerate(Caster.toDoubleValue(value2));

			// video-bitrate
			value = item.getAttribute("video-bitrate");
			if (!Util.isEmpty(value)) vq.setVideoBitrate(util.toBytes(value));

			// video-bitrate-max
			value = item.getAttribute("video-bitrate-max");
			if (!Util.isEmpty(value)) vq.setVideoBitrateMax(util.toBytes(value));

			// video-bitrate-min
			value = item.getAttribute("video-bitrate-min");
			if (!Util.isEmpty(value)) vq.setVideoBitrateMin(util.toBytes(value));

			// video-bitrate-tolerance
			value = item.getAttribute("video-bitrate-tolerance");
			if (!Util.isEmpty(value)) vq.setVideoBitrateTolerance(util.toBytes(value));

			// video-codec
			value = item.getAttribute("video-codec");
			// print.out("video-codec:"+value);
			if (!Util.isEmpty(value)) vq.setVideoCodec(value);

			// audio-codec
			value = item.getAttribute("audio-codec");
			if (!Util.isEmpty(value)) vq.setAudioCodec(value);

			//
			value = item.getAttribute("label");
			// print.out("label:"+value);
			if (!Util.isEmpty(value)) {
				String[] arr = toArray(value);
				for (int y = 0; y < arr.length; y++) {
					profiles.put(arr[y].trim().toLowerCase(), vq);
				}
			}
		}
		return profiles;
	}

	private static Element getChildByName(Node parent, String nodeName, boolean insertBefore) {
		if (parent == null) return null;
		NodeList list = parent.getChildNodes();
		int len = list.getLength();

		for (int i = 0; i < len; i++) {
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
				return (Element) node;
			}
		}
		Element newEl = parent.getOwnerDocument().createElement(nodeName);
		if (insertBefore) parent.insertBefore(newEl, parent.getFirstChild());
		else parent.appendChild(newEl);
		return newEl;
	}

	private static Element[] getChildren(Node parent, String nodeName) {
		if (parent == null) return new Element[0];
		NodeList list = parent.getChildNodes();
		int len = list.getLength();
		ArrayList rtn = new ArrayList();

		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
				rtn.add(node);
			}
		}
		return (Element[]) rtn.toArray(new Element[rtn.size()]);
	}

	private static String[] toArray(String str) {
		StringTokenizer st = new StringTokenizer(str, ",");
		ArrayList list = new ArrayList();
		while (st.hasMoreTokens()) {
			list.add(str = st.nextToken());
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * create (if not exist) and return video xml as dom
	 * 
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	private static Element getVideoXML(Resource xml) throws IOException, SAXException {
		if (!xml.exists()) {
			createFileFromResource("/resource/video/video.xml", xml);
		}
		Document doc = loadDocument(xml);
		return doc.getDocumentElement();
	}

	public static void createFileFromResource(String path, Resource bin) throws IOException {
		InputStream is = null;
		OutputStream os = null;

		if (bin.exists()) return;

		IOUtil.copy(is = new VideoInputImpl(null).getClass().getResourceAsStream(path), os = bin.getOutputStream(), true, true);

	}

	private static Document loadDocument(Resource xmlFile) throws SAXException, IOException {
		InputStream is = null;
		try {
			return loadDocument(is = xmlFile.getInputStream());
		}
		finally {
			IOUtil.close(is);
		}
	}

	private static Document loadDocument(InputStream is) throws SAXException, IOException {
		try {
			InputSource source = new InputSource(is);
			return XMLUtil.parse(source, null, false);
		}
		finally {
			IOUtil.close(is);
		}
	}
}