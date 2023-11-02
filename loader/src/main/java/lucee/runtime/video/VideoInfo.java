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

import lucee.runtime.type.Struct;

public interface VideoInfo {

	public long getAudioBitrate();

	/**
	 * @return the audioChannels
	 */
	public String getAudioChannels();

	/**
	 * @return the audioCodec
	 */
	public String getAudioCodec();

	/**
	 * @return the audioSampleRate
	 */
	public long getAudioSamplerate();

	/**
	 * @return the duration
	 */
	public long getDuration();

	/**
	 * @return the bitrate
	 */
	public long getVideoBitrate();

	/**
	 * @return the framerate
	 */
	public double getFramerate();

	/**
	 * @return the videoCodec
	 */
	public String getVideoCodec();

	/**
	 * @return the videoFormat
	 */
	public String getVideoFormat();

	/**
	 * @return the height
	 */
	public int getHeight();

	/**
	 * @return the width
	 */
	public int getWidth();

	/**
	 * returns the information as Struct
	 * 
	 * @return
	 */
	public Struct toStruct();
}