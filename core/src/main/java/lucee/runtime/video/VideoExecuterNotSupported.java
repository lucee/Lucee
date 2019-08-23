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

import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.type.Struct;

public class VideoExecuterNotSupported implements VideoExecuter {

	/**
	 * @see lucee.runtime.video.VideoExecuter#convertRaw(lucee.runtime.config.ConfigWeb,
	 *      lucee.runtime.video.VideoInput[], lucee.runtime.video.VideoOutput,
	 *      lucee.runtime.video.VideoProfile)
	 */
	@Override
	public VideoInfo[] convert(ConfigWeb config, VideoInput[] inputs, VideoOutput output, VideoProfile quality) throws IOException {
		throw notSupported();
	}

	/**
	 * @see lucee.runtime.video.VideoExecuter#infoRaw(lucee.runtime.config.ConfigWeb,
	 *      lucee.runtime.video.VideoInput)
	 */
	@Override
	public VideoInfo info(ConfigWeb config, VideoInput input) throws IOException {
		throw notSupported();
	}

	/**
	 * @see lucee.runtime.video.VideoExecuter#test(lucee.runtime.config.ConfigWeb)
	 */
	@Override
	public void test(ConfigWeb config) throws IOException {
		throw notSupported();
	}

	/**
	 * @see lucee.runtime.video.VideoExecuter#uninstall(lucee.runtime.config.Config)
	 */
	@Override
	public void uninstall(Config config) throws IOException {
		throw notSupported();
	}

	/**
	 * @see lucee.runtime.video.VideoExecuter#install(lucee.runtime.config.ConfigWeb,
	 *      lucee.runtime.type.Struct)
	 */
	@Override
	public void install(ConfigWeb config, Struct data) throws IOException {
		throw notSupported();
	}

	private VideoException notSupported() {
		return new VideoException("The video components are not installed, please go to the Lucee Server Administrator in order to install the video extension");
	}

}