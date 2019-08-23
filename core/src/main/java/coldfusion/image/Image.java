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
package coldfusion.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;

import javax.servlet.jsp.PageContext;

import lucee.runtime.type.Struct;

public interface Image {

	public void addBorder(int arg0, String arg1, String arg2);

	public void blur(int blurRadius);

	public void brighten();

	public void clearRect(int x, int y, int width, int height);

	public Image copyArea(int srcX, int srcY, int width, int height, int destX, int destY);

	public Image copyArea(int srcX, int srcY, int width, int height);

	public void crop(float x, float y, float width, float height);

	public void draw3DRect(int x, int y, int width, int height, boolean raised, boolean filled);

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, boolean filled);

	public void drawCubicCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2);

	public void drawLine(int x1, int y1, int x2, int y2);

	public void drawLines(int[] xcoords, int[] ycoords, boolean isPolygon, boolean filled);

	public void drawOval(int x, int y, int width, int height, boolean filled);

	public void drawPoint(int x, int y);

	public void drawQuadraticCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2);

	public void drawRect(int x, int y, int width, int height, boolean filled);

	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight, boolean filled);

	public void drawString(String arg0, int arg1, int arg2, Struct arg3);

	public void flip(String transpose);

	public String getBase64String(String formatName);

	public Color getColor(String strColor);

	public Graphics2D getCurrentGraphics();

	public BufferedImage getCurrentImage();

	public Struct getExifMetadata(PageContext arg0);

	public String getExifTag(String tagname, PageContext pageContext);

	public int getHeight();

	public byte[] getImageBytes(String arg0);

	public Struct getIptcMetadata(PageContext arg0);

	public String getIptcTag(String tagname, PageContext pageContext);

	public String getSource();

	public int getWidth();

	public void grayscale();

	public Struct info();

	public void initializeMetadata(PageContext pc);

	public void invert();

	public void overlay(Image img);

	public void paste(Image img2, int x, int y);

	public void readBase64(String arg0);

	public void resize(String arg0, String arg1, String arg2, double arg3);

	public void resize(String width, String height, String interpolation);

	public void rotate(float arg0, float arg1, float arg2, String arg3);

	public void rotateAxis(double theta, double x, double y);

	public void rotateAxis(double theta);

	public void scaleToFit(int fitSize);

	public void scaleToFit(String arg0, String arg1, String arg2, double arg3);

	public void scaleToFit(String fitWidth, String fitHeight, String interpolation);

	public void setAntiAliasing(String value);

	public void setBackground(String color);

	public void setColor(String color);

	public void setDrawingStroke(float width, int cap, int joins, float miterlimit, float[] dash, float dash_phase);

	public void setDrawingStroke(Struct arg0);

	public void setRenderingHint(Key hintKey, Object hintValue);

	public void setTranparency(double percent);

	public void setXorMode(String color);

	public void sharpen(float gain);

	public void sharpenEdge();

	public void shear(float arg0, String arg1, String arg2);

	public void shearAxis(double shx, double shy);

	public void translate(int arg0, int arg1, String arg2);

	public void translateAxis(int x, int y);

	public void write(String arg0, float arg1);

	public void writeBase64(String arg0, String arg1, boolean arg2);

}