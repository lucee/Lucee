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
package lucee.runtime.img;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
public class PSDReader {

    /**
     * File read status: No errors.
     */
       public static final int STATUS_OK = 0;

    /**
     * File read status: Error decoding file (may be partially decoded)
     */
    public static final int STATUS_FORMAT_ERROR = 1;

    /**
     * File read status: Unable to open source.
     */
    public static final int STATUS_OPEN_ERROR = 2;

    /**
     * File read status: Unsupported format
     */
    public static final int STATUS_UNSUPPORTED = 3;

    public static int ImageType = BufferedImage.TYPE_INT_ARGB;

    protected BufferedInputStream input;
    protected int frameCount;
    protected BufferedImage[] frames;
    protected int status = 0;
    protected int nChan;
    protected int width;
    protected int height;
    protected int nLayers;
    protected int miscLen;
    protected boolean hasLayers;
    protected LayerInfo[] layers;
    protected short[] lineLengths;
    protected int lineIndex;
    protected boolean rleEncoded;
    
    protected class LayerInfo {
        int x, y, w, h;
        int nChan;
        int[] chanID;
        int alpha;
    }

    /**
     * Gets the number of layers read from file.
     * @return frame count
     */
    public int getFrameCount() {
        return frameCount;
    }
    
    protected void setInput(InputStream stream) {
        // open input stream
        init();
        if (stream == null) {
            status = STATUS_OPEN_ERROR;
        } else {
            if (stream instanceof BufferedInputStream)
                input = (BufferedInputStream) stream;
            else
                input = new BufferedInputStream(stream);
        }
    }
    
    protected void setInput(String name) {
        // open input file
        init();
        try {
            name = name.trim();
            if (name.startsWith("file:")) {
                name = name.substring(5);
                while (name.startsWith("/"))
                    name = name.substring(1);
            }
            if (name.indexOf("://") > 0) {
                URL url = new URL(name);
                input = new BufferedInputStream(url.openStream());
            } else {
                input = new BufferedInputStream(new FileInputStream(name));
            }
        } catch (IOException e) {
            status = STATUS_OPEN_ERROR;
        }
    }
    
    /**
     * Gets display duration for specified frame.  Always returns 0.
     *
     */
    public int getDelay(int forFrame) {
        return 0;
    }
    
    /**
     * Gets the image contents of frame n.  Note that this expands the image
     * to the full frame size (if the layer was smaller) and any subsequent
     * use of getLayer() will return the full image.
     *
     * @return BufferedImage representation of frame, or null if n is invalid.
     */
    public BufferedImage getFrame(int n) {
        BufferedImage im = null;
        if ((n >= 0) && (n < nLayers)) {
            im = frames[n];
            LayerInfo info = layers[n];
            if ((info.w != width) || (info.h != height)) {
                BufferedImage temp =
                    new BufferedImage(width, height, ImageType);
                Graphics2D gc = temp.createGraphics();
                gc.drawImage(im, info.x, info.y, null);
                gc.dispose();
                im = temp;
                frames[n] = im;
            }
        }
        return im;
    }
    
    /**
     * Gets maximum image size.  Individual layers may be smaller.
     *
     * @return maximum image dimensions
     */
    public Dimension getFrameSize() {
        return new Dimension(width, height);
    }
    
    /**
     * Gets the first (or only) image read.
     *
     * @return BufferedImage containing first frame, or null if none.
     */
    public BufferedImage getImage() {
        return getFrame(0);
    }
    
    /**
     * Gets the image contents of layer n.  May be smaller than full frame
     * size - use getFrameOffset() to obtain position of subimage within
     * main image area.
     *
     * @return BufferedImage representation of layer, or null if n is invalid.
     */
    public BufferedImage getLayer(int n) {
        BufferedImage im = null;
        if ((n >= 0) && (n < nLayers)) {
            im = frames[n];
        }
        return im;
    }
    
    /**
     * Gets the subimage offset of layer n if it is smaller than the
     * full frame size.
     *
     * @return Point indicating offset from upper left corner of frame.
     */
    public Point getLayerOffset(int n) {
        Point p = null;
        if ((n >= 0) && (n < nLayers)) {
            int x = layers[n].x;
            int y = layers[n].y;
            p = new Point(x, y);
        }
        if (p == null) {
            p = new Point(0, 0);
        }
        return p;
    }
    
    /**
     * Reads PhotoShop layers from stream.
     *
     * @param InputStream in PhotoShop format.
     * @return read status code (0 = no errors)
     */
    public int read(InputStream stream) {
        setInput(stream);
        process();
        return status;
    }
    
    /**
     * Reads PhotoShop file from specified source (file or URL string)
     *
     * @param name String containing source
     * @return read status code (0 = no errors)
     */
    public int read(String name) {
        setInput(name);
        process();
        return status;
    }
    
    /**
     * Closes input stream and discards contents of all frames.
     *
     */
    public void reset() {
        init();
    }
    
    protected void close() {
        if (input != null) {
            try {
                input.close();
            } catch (Exception e) {}
            input = null;
        }
    }
    protected boolean err() {
        return status != STATUS_OK;
    }
    
    protected byte[] fillBytes(int size, int value) {
        // create byte array filled with given value
        byte[] b = new byte[size];
        if (value != 0) {
            byte v = (byte) value;
            for (int i = 0; i < size; i++) {
                b[i] = v;
            }
        }
        return b;
    }
    
    protected void init() {
        close();
        frameCount = 0;
        frames = null;
        layers = null;
        hasLayers = true;
        status = STATUS_OK;
    }
    
    protected void makeDummyLayer() {
        // creat dummy layer for non-layered image
        rleEncoded = readShort() == 1;
        hasLayers = false;
        nLayers = 1;
        layers = new LayerInfo[1];
        LayerInfo layer = new LayerInfo();
        layers[0] = layer;
        layer.h = height;
        layer.w = width;
        int nc = Math.min(nChan, 4);
        if (rleEncoded) {
            // get list of rle encoded line lengths for all channels
            readLineLengths(height * nc);
        }
        layer.nChan = nc;
        layer.chanID = new int[nc];
        for (int i = 0; i < nc; i++) {
            int id = i;
            if (i == 3) id = -1;
            layer.chanID[i] = id;
        }
    }

    protected void readLineLengths(int nLines) {
        // read list of rle encoded line lengths
        lineLengths = new short[nLines];
        for (int i = 0; i < nLines; i++) {
            lineLengths[i] = readShort();
        }
        lineIndex = 0;
    }

    protected BufferedImage makeImage(int w, int h, byte[] r, byte[] g, byte[] b, byte[] a) {
        // create image from given plane data
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] data = ((DataBufferInt) im.getRaster().getDataBuffer()).getData();
        int n = w * h;
        int j = 0;
        while (j < n) {
            try {
                int ac = a[j] & 0xff;
                int rc = r[j] & 0xff;
                int gc = g[j] & 0xff;
                int bc = b[j] & 0xff;
                data[j] = (((((ac << 8) | rc) << 8) | gc) << 8) | bc;
            } catch (Exception e) {}
            j++;
        }
        return im;
    }
    
    protected void process() {
        // decode PSD file
        if (err()) return;
        readHeader();
        if (err()) return;
        readLayerInfo();
        if (err()) return;
        if (nLayers == 0) {
            makeDummyLayer();
            if (err()) return;
        }
        readLayers();
    }
    
    protected int readByte() {
        // read single byte from input
        int curByte = 0;
        try {
            curByte = input.read();
        } catch (IOException e) {
            status = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }

    protected int readBytes(byte[] bytes, int n) {
        // read multiple bytes from input
        if (bytes == null) return 0;
        int r = 0;
        try {
            r = input.read(bytes, 0, n);
        } catch (IOException e) {
            status = STATUS_FORMAT_ERROR;
        }
        if (r < n) {
            status = STATUS_FORMAT_ERROR;
        }
        return r;
    }
    
    protected void readHeader() {
        // read PSD header info
        String sig = readString(4);
        int ver = readShort();
        skipBytes(6);
        nChan = readShort();
        height = readInt();
        width = readInt();
        int depth = readShort();
        int mode = readShort();
        int cmLen = readInt();
        skipBytes(cmLen);
        int imResLen = readInt();
        skipBytes(imResLen);

        // require 8-bit RGB data
        if ((!sig.equals("8BPS")) || (ver != 1)) {
            status = STATUS_FORMAT_ERROR;
        } else if ((depth != 8) || (mode != 3)) {
            status = STATUS_UNSUPPORTED;
        }
    }
    
    protected int readInt() {
        // read big-endian 32-bit integer
        return (((((readByte() << 8) | readByte()) << 8) | readByte()) << 8)
            | readByte();
    }
    
    protected void readLayerInfo() {
        // read layer header info
        miscLen = readInt();
        if (miscLen == 0) {
            return; // no layers, only base image
        }
        readInt();
        nLayers = readShort();
        if (nLayers > 0) {
            layers = new LayerInfo[nLayers];
        }
        for (int i = 0; i < nLayers; i++) {
            LayerInfo info = new LayerInfo();
            layers[i] = info;
            info.y = readInt();
            info.x = readInt();
            info.h = readInt() - info.y;
            info.w = readInt() - info.x;
            info.nChan = readShort();
            info.chanID = new int[info.nChan];
            for (int j = 0; j < info.nChan; j++) {
                int id = readShort();
                readInt();
                info.chanID[j] = id;
            }
            String s = readString(4);
            if (!s.equals("8BIM")) {
                status = STATUS_FORMAT_ERROR;
                return;
            }
            skipBytes(4); // blend mode
            info.alpha = readByte();
            readByte();
            readByte();
            readByte(); // filler
            int extraSize = readInt();
            skipBytes(extraSize);
        }
    }
    
    protected void readLayers() {
        // read and convert each layer to BufferedImage
        frameCount = nLayers;
        frames = new BufferedImage[nLayers];
        for (int i = 0; i < nLayers; i++) {
            LayerInfo info = layers[i];
            byte[] r = null, g = null, b = null, a = null;
            for (int j = 0; j < info.nChan; j++) {
                int id = info.chanID[j];
                switch (id) {
                    case  0 : r = readPlane(info.w, info.h); break;
                    case  1 : g = readPlane(info.w, info.h); break;
                    case  2 : b = readPlane(info.w, info.h); break;
                    case -1 : a = readPlane(info.w, info.h); break;
                    default : readPlane(info.w, info.h);
                }
                if (err()) break;
            }
            if (err()) break;
            int n = info.w * info.h;
            if (r == null) r = fillBytes(n, 0);
            if (g == null) g = fillBytes(n, 0);
            if (b == null) b = fillBytes(n, 0);
            if (a == null) a = fillBytes(n, 255);

            BufferedImage im = makeImage(info.w, info.h, r, g, b, a);
            frames[i] = im;
        }
        lineLengths = null;
        if ((miscLen > 0) && !err()) {
            int n = readInt(); // global layer mask info len
            skipBytes(n);
        }
    }
    
    protected byte[] readPlane(int w, int h) {
        // read a single color plane
        byte[] b = null;
        int size = w * h;
        if (hasLayers) {
            // get RLE compression info for channel
            rleEncoded = readShort() == 1;
            if (rleEncoded) {
                // list of encoded line lengths
                readLineLengths(h);
            }
        }

        if (rleEncoded) {
            b = readPlaneCompressed(w, h);
        } else {
            b = new byte[size];
            readBytes(b, size);
        }

        return b;

    }

    protected byte[] readPlaneCompressed(int w, int h) {
        byte[] b = new byte[w * h];
        byte[] s = new byte[w * 2];
        int pos = 0;
        for (int i = 0; i < h; i++) {
            if (lineIndex >= lineLengths.length) {
                status = STATUS_FORMAT_ERROR;
                return null;
            }
            int len = lineLengths[lineIndex++];
            readBytes(s, len);
            decodeRLE(s, 0, len, b, pos);
            pos += w;
        }
        return b;
    }

    protected void decodeRLE(byte[] src, int sindex, int slen, byte[] dst, int dindex) {
        try {
            int max = sindex + slen;
            while (sindex < max) {
                byte b = src[sindex++];
                int n = b;
                if (n < 0) {
                    // dup next byte 1-n times
                    n = 1 - n;
                    b = src[sindex++];
                    for (int i = 0; i < n; i++) {
                        dst[dindex++] = b;
                    }
                } else {
                    // copy next n+1 bytes
                    n = n + 1;
                    System.arraycopy(src, sindex, dst, dindex, n);
                    dindex += n;
                    sindex += n;
                }
            }
        } catch (Exception e) {
            status = STATUS_FORMAT_ERROR;
        }
    }
    
    protected short readShort() {
        // read big-endian 16-bit integer
        return (short) ((readByte() << 8) | readByte());
    }
    
    protected String readString(int len) {
        // read string of specified length
        String s = "";
        for (int i = 0; i < len; i++) {
            s = s + (char) readByte();
        }
        return s;
    }
    
    protected void skipBytes(int n) {
        // skip over n input bytes
        for (int i = 0; i < n; i++) {
            readByte();
        }
    }
}