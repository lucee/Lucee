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

public class VideoOutputImpl implements VideoOutput {

	private Resource resource;
	private double offset = 0;
	private String comment;
	private String title;
	private String author;
	private String copyright;
	private int fileLimitation;
	private long maxFrames = 0;
	private String format;
	private int frameRate;

	public VideoOutputImpl(Resource resource) {
		this.resource = resource;
	}

	/**
	 * set time offset of the output file based on input file in seconds
	 * 
	 * @param offset
	 */
	@Override
	public void setOffset(double offset) {
		this.offset = offset;
	}

	/**
	 * sets a comment to the output video
	 * 
	 * @param comment
	 */
	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * sets a title to the output video
	 * 
	 * @param title
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * sets an author to the output video
	 * 
	 * @param author
	 */
	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * sets a copyright to the output video
	 * 
	 * @param copyright
	 */
	@Override
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @return the res
	 */
	@Override
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return the offset
	 */
	@Override
	public double getOffset() {
		return offset;
	}

	/**
	 * @return the comment
	 */
	@Override
	public String getComment() {
		return comment;
	}

	/**
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @return the author
	 */
	@Override
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the copyright
	 */
	@Override
	public String getCopyright() {
		return copyright;
	}

	/**
	 * @return the fileLimitation
	 */
	@Override
	public int getFileLimitation() {
		return fileLimitation;
	}

	/**
	 * limit size of the output file
	 * 
	 * @param size the size to set
	 */
	@Override
	public void limitFileSizeTo(int size) {
		this.fileLimitation = size;
	}

	/**
	 * @return the maxFrames
	 */
	@Override
	public long getMaxFrames() {
		return maxFrames;
	}

	/**
	 * @param maxFrames the maxFrames to set
	 */
	@Override
	public void setMaxFrames(long maxFrames) {
		this.maxFrames = maxFrames;
	}
	/*
	 * /**
	 * 
	 * @return the aspectRatio / public int getAspectRatio() { return aspectRatio; }
	 * 
	 * /** sets the aspectRatio (VideoOutput.ASPECT_RATIO_xxx)
	 * 
	 * @param aspectRatio the aspectRatio to set / public void setAspectRatio(int aspectRatio) {
	 * this.aspectRatio = aspectRatio; }
	 * 
	 * /**
	 * 
	 * @return the bitrateMin / public int getVideoBitrateMin() { return videoBitrateMin; }
	 * 
	 * /** set min video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateMin the bitrateMin to set / public void setVideoBitrateMin(int bitrateMin) {
	 * this.videoBitrateMin = bitrateMin; }
	 * 
	 * /**
	 * 
	 * @return the bitrateMax / public int getVideoBitrateMax() { return videoBitrateMax; }
	 * 
	 * /** set max video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateMax the bitrateMax to set / public void setVideoBitrateMax(int bitrateMax) {
	 * this.videoBitrateMax = bitrateMax; }
	 * 
	 * /**
	 * 
	 * @return the bitrateTolerance / public int getVideoBitrateTolerance() { return
	 * videoBitrateTolerance; }
	 * 
	 * /** set video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateTolerance the bitrateTolerance to set / public void setVideoBitrateTolerance(int
	 * bitrateTolerance) { this.videoBitrateTolerance = bitrateTolerance; }
	 * 
	 * /**
	 * 
	 * @return the sameQualityAsSource / public boolean doSameQualityAsSource() { return
	 * sameQualityAsSource; }
	 * 
	 * /**
	 * 
	 * @param sameQualityAsSource the sameQualityAsSource to set / public void
	 * setSameQualityAsSource(boolean sameQualityAsSource) { this.sameQualityAsSource =
	 * sameQualityAsSource; }
	 * 
	 * /**
	 * 
	 * @return the audioBitrate / public int getAudioBitrate() { return audioBitrate; }
	 * 
	 * /**
	 * 
	 * @param audioBitrate the audioBitrate to set / public void setAudioBitrate(int audioBitrate) {
	 * this.audioBitrate = audioBitrate; }
	 * 
	 * 
	 * /** set the type of the output format (see constants "TYPE_xxx" of this class)
	 * 
	 * @param type / public void setType(String type){ this.type=type; }
	 * 
	 * 
	 * /**
	 * 
	 * @return the type / public String getType() { return type; }
	 * 
	 * /**
	 * 
	 * @return the dimension / public String getDimension() { return dimension; }
	 * 
	 * public void setDimension(int width, int height) throws VideoException {
	 * checkDimension(width,"width"); checkDimension(height,"height"); this.dimension=width+"X"+height;
	 * }
	 * 
	 * /**
	 * 
	 * @return the bitrate / public int getVideoBitrate() { return videoBitrate; }
	 * 
	 * /** set video bitrate in kbit/s (default 200)
	 * 
	 * @param bitrate the bitrate to set / public void setVideoBitrate(int bitrate) { this.videoBitrate
	 * = bitrate; }
	 * 
	 * /**
	 * 
	 * @return the framerate / public int getFramerate() { return framerate; }
	 * 
	 * /** sets the framerate (default 25)
	 * 
	 * @param framerate the framerate to set / public void setFramerate(int framerate) { this.framerate
	 * = framerate; }
	 */

	/**
	 * @param resource the resource to set
	 */
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the format
	 */
	@Override
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @param fileLimitation the fileLimitation to set
	 */
	@Override
	public void setFileLimitation(int fileLimitation) {
		this.fileLimitation = fileLimitation;
	}

	/**
	 * @see lucee.runtime.video.VideoOutput#getFrameRate()
	 */
	@Override
	public int getFrameRate() {
		return frameRate;
	}

	/**
	 * @see lucee.runtime.video.VideoOutput#setFrameRate(int)
	 */
	@Override
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	/*
	 * public void setVideoCodec(String videoCodec) { this.videoCodec=videoCodec; }
	 * 
	 * public String getVideoCodec() { return videoCodec; }
	 * 
	 * public String getAudioCodec() { return audioCodec; }
	 * 
	 * public void setAudioCodec(String audioCodec) { this.audioCodec = audioCodec; }
	 */
}