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

public class VideoProfileImpl implements VideoProfile {

	private String type;
	private String dimension;
	private long audioBitrate = 0;
	private long videoBitrate = 0;
	private long videoBitrateMin = 0;
	private long videoBitrateMax = 0;
	private long videoBitrateTolerance = 0;
	// private boolean sameQualityAsSource =false;
	private double framerate = 0;
	private double audioSamplerate = 0;
	private int aspectRatio = 0;
	private int scanMode = 0;
	private String videoCodec;
	private String audioCodec;
	// private long bufferSize;
	private long bufferSize;
	private int pass = 0;

	public VideoProfileImpl(String type, String dimension, long audioBitrate, long videoBitrate, long videoBitrateMin, long videoBitrateMax, long videoBitrateTolerance,
			double framerate, int aspectRatio, int scanMode, String audioCodec, String videoCodec, double audioSamplerate) {
		super();
		this.type = type;
		this.dimension = dimension;
		this.audioBitrate = audioBitrate;
		this.videoBitrate = videoBitrate;
		this.videoBitrateMin = videoBitrateMin;
		this.videoBitrateMax = videoBitrateMax;
		this.videoBitrateTolerance = videoBitrateTolerance;
		this.framerate = framerate;
		this.aspectRatio = aspectRatio;
		this.scanMode = scanMode;
		this.audioCodec = audioCodec;
		this.videoCodec = videoCodec;
		this.audioSamplerate = audioSamplerate;
	}

	public VideoProfileImpl() {
	}

	@Override
	public VideoProfile duplicate() {

		return new VideoProfileImpl(type, dimension, audioBitrate, videoBitrate, videoBitrateMin, videoBitrateMax, videoBitrateTolerance, framerate, aspectRatio, scanMode,
				audioCodec, videoCodec, audioSamplerate);
	}

	/**
	 * set the type of the output format (see constants "TYPE_xxx" of this class)
	 * 
	 * @param type
	 */
	@Override
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @return the dimension
	 */
	@Override
	public String getDimension() {
		return dimension;
	}

	@Override
	public void setDimension(int width, int height) {
		checkDimension(width, "width");
		checkDimension(height, "height");
		this.dimension = width + "X" + height;
	}

	private void checkDimension(int value, String label) {
		// if(((value/2)*2)!=value)
		// throw new VideoException("dimension ["+value+"] "+label+" must be the muliple of 2 (2,4,8,16
		// ...)");
	}

	/**
	 * @return the bitrate
	 */
	@Override
	public double getVideoBitrate() {
		return videoBitrate;
	}

	/**
	 * set video bitrate in kbit/s (default 200)
	 * 
	 * @param bitrate the bitrate to set
	 */
	@Override
	public void setVideoBitrate(long bitrate) {
		this.videoBitrate = bitrate;
	}

	/**
	 * @return the framerate
	 */
	@Override
	public double getFramerate() {
		return framerate;
	}

	/**
	 * sets the framerate (default 25)
	 * 
	 * @param framerate the framerate to set
	 */
	@Override
	public void setFramerate(double framerate) {
		this.framerate = framerate;
	}

	/**
	 * @return the aspectRatio
	 */
	@Override
	public int getAspectRatio() {
		return aspectRatio;
	}

	/**
	 * sets the aspectRatio (VideoOutput.ASPECT_RATIO_xxx)
	 * 
	 * @param aspectRatio the aspectRatio to set
	 */
	@Override
	public void setAspectRatio(int aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	@Override
	public void setAspectRatio(String strAspectRatio) {
		strAspectRatio = strAspectRatio.trim().toLowerCase();
		if ("16:9".equals(strAspectRatio)) this.aspectRatio = ASPECT_RATIO_16_9;
		else if ("4:3".equals(strAspectRatio)) this.aspectRatio = ASPECT_RATIO_4_3;
	}

	/**
	 * @return the bitrateMin
	 */
	@Override
	public double getVideoBitrateMin() {
		return videoBitrateMin;
	}

	/**
	 * set min video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateMin the bitrateMin to set
	 */
	@Override
	public void setVideoBitrateMin(long bitrateMin) {
		this.videoBitrateMin = bitrateMin;
	}

	/**
	 * @return the bitrateMax
	 */
	@Override
	public double getVideoBitrateMax() {
		return videoBitrateMax;
	}

	/**
	 * set max video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateMax the bitrateMax to set
	 */
	@Override
	public void setVideoBitrateMax(long bitrateMax) {
		this.videoBitrateMax = bitrateMax;
	}

	/**
	 * @return the bitrateTolerance
	 */
	@Override
	public double getVideoBitrateTolerance() {
		return videoBitrateTolerance;
	}

	/**
	 * set video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateTolerance the bitrateTolerance to set
	 */
	@Override
	public void setVideoBitrateTolerance(long bitrateTolerance) {
		this.videoBitrateTolerance = bitrateTolerance;
	}

	/**
	 * @return the audioBitrate
	 */
	@Override
	public double getAudioBitrate() {
		return audioBitrate;
	}

	/**
	 * @return the scanMode
	 */
	@Override
	public int getScanMode() {
		return scanMode;
	}

	/**
	 * @param scanMode the scanMode to set
	 */
	@Override
	public void setScanMode(int scanMode) {
		this.scanMode = scanMode;
	}

	/**
	 * @param audioBitrate the audioBitrate to set
	 */
	@Override
	public void setAudioBitrate(long audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	@Override
	public void setAudioCodec(String codec) {
		this.audioCodec = codec;
	}

	@Override
	public void setVideoCodec(String codec) {
		this.videoCodec = codec;
	}

	/**
	 * @return the videoCodec
	 */
	@Override
	public String getVideoCodec() {
		return videoCodec;
	}

	/**
	 * @return the audioCodec
	 */
	@Override
	public String getAudioCodec() {
		return audioCodec;
	}

	/**
	 * @return the audioSamplerate
	 */
	@Override
	public double getAudioSamplerate() {
		return audioSamplerate;
	}

	/**
	 * @param audioSamplerate the audioSamplerate to set
	 */
	@Override
	public void setAudioSamplerate(double audioSamplerate) {
		this.audioSamplerate = audioSamplerate;
	}

	/**
	 * @return the bufferSize
	 */
	@Override
	public long getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	@Override
	public void setBufferSize(long bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the pass
	 */
	@Override
	public int getPass() {
		return pass;
	}

	/**
	 * @param pass the pass to set
	 */
	@Override
	public void setPass(int pass) {
		this.pass = pass;
	}
}