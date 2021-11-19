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

public interface VideoProfile {

	public static final String TYPE_4XM = "4xm";
	public static final String TYPE_8BPS = "8bps";
	public static final String TYPE_AAC = "aac";
	public static final String TYPE_AASC = "aasc";
	public static final String TYPE_AC3 = "ac3";
	public static final String TYPE_ADPCM_4XM = "adpcm_4xm";
	public static final String TYPE_ADPCM_ADX = "adpcm_adx";
	public static final String TYPE_ADPCM_CT = "adpcm_ct";
	public static final String TYPE_ADPCM_EA = "adpcm_ea";
	public static final String TYPE_ADPCM_IMA_DK3 = "adpcm_ima_dk3";
	public static final String TYPE_ADPCM_IMA_DK4 = "adpcm_ima_dk4";
	public static final String TYPE_ADPCM_IMA_QT = "adpcm_ima_qt";
	public static final String TYPE_ADPCM_IMA_SMJPEG = "adpcm_ima_smjpeg";
	public static final String TYPE_ADPCM_IMA_WAV = "adpcm_ima_wav";
	public static final String TYPE_ADPCM_IMA_WS = "adpcm_ima_ws";
	public static final String TYPE_ADPCM_MS = "adpcm_ms";
	public static final String TYPE_ADPCM_SBPRO_2 = "adpcm_sbpro_2";
	public static final String TYPE_ADPCM_SBPRO_3 = "adpcm_sbpro_3";
	public static final String TYPE_ADPCM_SBPRO_4 = "adpcm_sbpro_4";
	public static final String TYPE_ADPCM_SWF = "adpcm_swf";
	public static final String TYPE_ADPCM_XA = "adpcm_xa";
	public static final String TYPE_ADPCM_YAMAHA = "adpcm_yamaha";
	public static final String TYPE_ALAC = "alac";
	public static final String TYPE_AMR_NB = "amr_nb";
	public static final String TYPE_AMR_WB = "amr_wb";
	public static final String TYPE_ASV1 = "asv1";
	public static final String TYPE_ASV2 = "asv2";
	public static final String TYPE_AVS = "avs";
	public static final String TYPE_BMP = "bmp";
	public static final String TYPE_CAMSTUDIO = "camstudio";
	public static final String TYPE_CAMTASIA = "camtasia";
	public static final String TYPE_CINEPAK = "cinepak";
	public static final String TYPE_CLJR = "cljr";
	public static final String TYPE_COOK = "cook";
	public static final String TYPE_CYUV = "cyuv";
	public static final String TYPE_DVBSUB = "dvbsub";
	public static final String TYPE_DVDSUB = "dvdsub";
	public static final String TYPE_DVVIDEO = "dvvideo";
	public static final String TYPE_FFV1 = "ffv1";
	public static final String TYPE_FFVHUFF = "ffvhuff";
	public static final String TYPE_FLAC = "flac";
	public static final String TYPE_FLIC = "flic";
	public static final String TYPE_FLV = "flv";
	public static final String TYPE_FRAPS = "fraps";
	public static final String TYPE_G726 = "g726";
	public static final String TYPE_H261 = "h261";
	public static final String TYPE_H263 = "h263";
	public static final String TYPE_H263I = "h263i";
	public static final String TYPE_H263P = "h263p";
	public static final String TYPE_H264 = "h264";
	public static final String TYPE_HUFFYUV = "huffyuv";
	public static final String TYPE_IDCINVIDEO = "idcinvideo";
	public static final String TYPE_INDEO2 = "indeo2";
	public static final String TYPE_INDEO3 = "indeo3";
	public static final String TYPE_INTERPLAY_DPCM = "interplay_dpcm";
	public static final String TYPE_INTERPLAYVIDEO = "interplayvideo";
	public static final String TYPE_JPEGLS = "jpegls";
	public static final String TYPE_KMVC = "kmvc";
	public static final String TYPE_LJPEG = "ljpeg";
	public static final String TYPE_LOCO = "loco";
	public static final String TYPE_MACE3 = "mace3";
	public static final String TYPE_MACE6 = "mace6";
	public static final String TYPE_MDEC = "mdec";
	public static final String TYPE_MJPEG = "mjpeg";
	public static final String TYPE_MJPEGB = "mjpegb";
	public static final String TYPE_MMVIDEO = "mmvideo";
	public static final String TYPE_MP2 = "mp2";
	public static final String TYPE_MP3 = "mp3";
	public static final String TYPE_MP3ADU = "mp3adu";
	public static final String TYPE_MP3ON4 = "mp3on4";
	public static final String TYPE_MPEG1VIDEO = "mpeg1video";
	public static final String TYPE_MPEG2VIDEO = "mpeg2video";
	public static final String TYPE_MPEG4 = "mpeg4";
	public static final String TYPE_MPEG4AAC = "mpeg4aac";
	public static final String TYPE_MPEGVIDEO = "mpegvideo";
	public static final String TYPE_MSMPEG4 = "msmpeg4";
	public static final String TYPE_MSMPEG4V1 = "msmpeg4v1";
	public static final String TYPE_MSMPEG4V2 = "msmpeg4v2";
	public static final String TYPE_MSRLE = "msrle";
	public static final String TYPE_MSVIDEO1 = "msvideo1";
	public static final String TYPE_MSZH = "mszh";
	public static final String TYPE_NUV = "nuv";
	public static final String TYPE_PAM = "pam";
	public static final String TYPE_PBM = "pbm";
	public static final String TYPE_PCM_ALAW = "pcm_alaw";
	public static final String TYPE_PCM_MULAW = "pcm_mulaw";
	public static final String TYPE_PCM_S16BE = "pcm_s16be";
	public static final String TYPE_PCM_S16LE = "pcm_s16le";
	public static final String TYPE_PCM_S24BE = "pcm_s24be";
	public static final String TYPE_PCM_S24DAUD = "pcm_s24daud";
	public static final String TYPE_PCM_S24LE = "pcm_s24le";
	public static final String TYPE_PCM_S32BE = "pcm_s32be";
	public static final String TYPE_PCM_S32LE = "pcm_s32le";
	public static final String TYPE_PCM_S8 = "pcm_s8";
	public static final String TYPE_PCM_U16BE = "pcm_u16be";
	public static final String TYPE_PCM_U16LE = "pcm_u16le";
	public static final String TYPE_PCM_U24BE = "pcm_u24be";
	public static final String TYPE_PCM_U24LE = "pcm_u24le";
	public static final String TYPE_PCM_U32BE = "pcm_u32be";
	public static final String TYPE_PCM_U32LE = "pcm_u32le";
	public static final String TYPE_PCM_U8 = "pcm_u8";
	public static final String TYPE_PGM = "pgm";
	public static final String TYPE_PGMYUV = "pgmyuv";
	public static final String TYPE_PNG = "png";
	public static final String TYPE_PPM = "ppm";
	public static final String TYPE_QDM2 = "qdm2";
	public static final String TYPE_QDRAW = "qdraw";
	public static final String TYPE_QPEG = "qpeg";
	public static final String TYPE_QTRLE = "qtrle";
	public static final String TYPE_RAWVIDEO = "rawvideo";
	public static final String TYPE_REAL_144 = "real_144";
	public static final String TYPE_REAL_288 = "real_288";
	public static final String TYPE_ROQ_DPCM = "roq_dpcm";
	public static final String TYPE_ROQVIDEO = "roqvideo";
	public static final String TYPE_RPZA = "rpza";
	public static final String TYPE_RV10 = "rv10";
	public static final String TYPE_RV20 = "rv20";
	public static final String TYPE_SHORTEN = "shorten";
	public static final String TYPE_SMACKAUD = "smackaud";
	public static final String TYPE_SMACKVID = "smackvid";
	public static final String TYPE_SMC = "smc";
	public static final String TYPE_SNOW = "snow";
	public static final String TYPE_SOL_DPCM = "sol_dpcm";
	public static final String TYPE_SONIC = "sonic";
	public static final String TYPE_SONICLS = "sonicls";
	public static final String TYPE_SP5X = "sp5x";
	public static final String TYPE_SVQ1 = "svq1";
	public static final String TYPE_SVQ3 = "svq3";
	public static final String TYPE_THEORA = "theora";
	public static final String TYPE_TRUEMOTION1 = "truemotion1";
	public static final String TYPE_TRUEMOTION2 = "truemotion2";
	public static final String TYPE_TRUESPEECH = "truespeech";
	public static final String TYPE_TTA = "tta";
	public static final String TYPE_ULTIMOTION = "ultimotion";
	public static final String TYPE_VC9 = "vc9";
	public static final String TYPE_VCR1 = "vcr1";
	public static final String TYPE_VMDAUDIO = "vmdaudio";
	public static final String TYPE_VMDVIDEO = "vmdvideo";
	public static final String TYPE_VORBIS = "vorbis";
	public static final String TYPE_VP3 = "vp3";
	public static final String TYPE_VQAVIDEO = "vqavideo";
	public static final String TYPE_WMAV1 = "wmav1";
	public static final String TYPE_WMAV2 = "wmav2";
	public static final String TYPE_WMV1 = "wmv1";
	public static final String TYPE_WMV2 = "wmv2";
	public static final String TYPE_WNV1 = "wnv1";
	public static final String TYPE_WS_SND1 = "ws_snd1";
	public static final String TYPE_XAN_DPCM = "xan_dpcm";
	public static final String TYPE_XAN_WC3 = "xan_wc3";
	public static final String TYPE_XL = "xl";
	public static final String TYPE_XVID = "xvid";
	public static final String TYPE_ZLIB = "zlib";
	public static final String TYPE_ZMBV = "zmbv";

	public static final int ASPECT_RATIO_16_9 = 1;
	public static final int ASPECT_RATIO_4_3 = 2;
	public static final int ASPECT_RATIO_1_33333 = ASPECT_RATIO_4_3;
	public static final int ASPECT_RATIO_1_77777 = ASPECT_RATIO_16_9;

	public static final int SCAN_MODE_INTERLACED = 1;
	public static final int SCAN_MODE_PROGRESSIV = 2;

	public VideoProfile duplicate();

	/**
	 * set the type of the output format (see constants "TYPE_xxx" of this class)
	 * 
	 * @param type output format type
	 */
	public void setType(String type);

	/**
	 * @return the type
	 */
	public String getType();

	/**
	 * @return the dimension
	 */
	public String getDimension();

	public void setDimension(int width, int height);

	/**
	 * @return the bitrate
	 */
	public double getVideoBitrate();

	/**
	 * set video bitrate in kbit/s (default 200)
	 * 
	 * @param bitrate the bitrate to set
	 */
	public void setVideoBitrate(long bitrate);

	/**
	 * @return the framerate
	 */
	public double getFramerate();

	/**
	 * sets the framerate (default 25)
	 * 
	 * @param framerate the framerate to set
	 */
	public void setFramerate(double framerate);

	/**
	 * @return the aspectRatio
	 */
	public int getAspectRatio();

	/**
	 * sets the aspectRatio (VideoOutput.ASPECT_RATIO_xxx)
	 * 
	 * @param aspectRatio the aspectRatio to set
	 */
	public void setAspectRatio(int aspectRatio);

	public void setAspectRatio(String strAspectRatio);

	/**
	 * @return the bitrateMin
	 */
	public double getVideoBitrateMin();

	/**
	 * set min video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateMin the bitrateMin to set
	 */
	public void setVideoBitrateMin(long bitrateMin);

	/**
	 * @return the bitrateMax
	 */
	public double getVideoBitrateMax();

	/**
	 * set max video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateMax the bitrateMax to set
	 */
	public void setVideoBitrateMax(long bitrateMax);

	/**
	 * @return the bitrateTolerance
	 */
	public double getVideoBitrateTolerance();

	/**
	 * set video bitrate tolerance (in kbit/s)
	 * 
	 * @param bitrateTolerance the bitrateTolerance to set
	 */
	public void setVideoBitrateTolerance(long bitrateTolerance);

	/**
	 * @return the audioBitrate
	 */
	public double getAudioBitrate();

	/**
	 * @return the scanMode
	 */
	public int getScanMode();

	/**
	 * @param scanMode the scanMode to set
	 */
	public void setScanMode(int scanMode);

	/**
	 * @param audioBitrate the audioBitrate to set
	 */
	public void setAudioBitrate(long audioBitrate);

	public void setAudioCodec(String codec);

	public void setVideoCodec(String codec);

	/**
	 * @return the videoCodec
	 */
	public String getVideoCodec();

	/**
	 * @return the audioCodec
	 */
	public String getAudioCodec();

	/**
	 * @return the audioSamplerate
	 */
	public double getAudioSamplerate();

	/**
	 * @param audioSamplerate the audioSamplerate to set
	 */
	public void setAudioSamplerate(double audioSamplerate);

	/**
	 * @return the bufferSize
	 */
	public long getBufferSize();

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(long bufferSize);

	/**
	 * @return the pass
	 */
	public int getPass();

	/**
	 * @param pass the pass to set
	 */
	public void setPass(int pass);
}