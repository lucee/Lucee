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
package lucee.servlet.pic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.commons.io.IOUtil;
import lucee.runtime.net.http.ReqRspUtil;

/**
 * Die Klasse PicServlet wird verwendet um Bilder darzustellen, alle Bilder die innerhalb des
 * Deployer angezeigt werden, werden ueber diese Klasse aus der lucee.jar Datei geladen, das macht
 * die Applikation flexibler und verlangt nicht das die Bilder fuer die Applikation an einem
 * bestimmten Ort abgelegt sein muessen.
 */
public final class PicServlet extends HttpServlet {

	/**
	 * Verzeichnis in welchem die bilder liegen
	 */
	public final static String PIC_SOURCE = "/resource/img/";

	/**
	 * Interpretiert den Script-Name und laedt das entsprechende Bild aus den internen Resourcen.
	 * 
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		// get out Stream

		// pic
		String[] arrPath = (req.getServletPath()).split("\\.");
		String pic = PIC_SOURCE + "404.gif";
		if (arrPath.length >= 3) {
			pic = PIC_SOURCE + ((arrPath[arrPath.length - 3] + "." + arrPath[arrPath.length - 2]).replaceFirst("/", ""));

			// mime type
			String mime = "image/" + arrPath[arrPath.length - 2];
			ReqRspUtil.setContentType(rsp, mime);
		}

		// write data from pic input to response output
		OutputStream os = null;
		InputStream is = null;
		try {
			os = rsp.getOutputStream();
			is = getClass().getResourceAsStream(pic);
			if (is == null) {
				is = getClass().getResourceAsStream(PIC_SOURCE + "404.gif");
			}

			byte[] buf = new byte[4 * 1024];
			int nread = 0;
			while ((nread = is.read(buf)) >= 0) {
				os.write(buf, 0, nread);
			}
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
		}
		finally {
			IOUtil.close(is, os);
		}
	}

}