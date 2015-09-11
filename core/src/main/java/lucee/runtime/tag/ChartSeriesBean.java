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
package lucee.runtime.tag;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

public class ChartSeriesBean implements Serializable {

	public static final int MARKER_STYLE_RECTANGLE = 0;
	public static final int MARKER_STYLE_TRIANGLE = 1;
	public static final int MARKER_STYLE_DIAMOND = 2;
	public static final int MARKER_STYLE_CIRCLE = 3;
	public static final int MARKER_STYLE_LETTER = 4;
	public static final int MARKER_STYLE_MCROSS = 5;
	public static final int MARKER_STYLE_SNOW = 6;
	public static final int MARKER_STYLE_RCROSS = 7;

	public static final int PAINT_STYLE_PLAIN = 0;
	public static final int PAINT_STYLE_RAISE = 1;
	public static final int PAINT_STYLE_SHADE = 2;
	public static final int PAINT_STYLE_LIGHT = 3;
	
	public static final int TYPE_BAR = 0;
	public static final int TYPE_LINE = 1;
	public static final int TYPE_PYRAMID = 2;
	public static final int TYPE_AREA = 3;
	public static final int TYPE_HORIZONTALBAR = 4;
	public static final int TYPE_CONE = 5;
	public static final int TYPE_CURVE = 6;
	public static final int TYPE_CYLINDER = 7;
	public static final int TYPE_STEP = 8;
	public static final int TYPE_SCATTER = 9;
	public static final int TYPE_PIE = 10;
	public static final int TYPE_TIME = 11;

	public static final int DATA_LABEL_STYLE_NONE = 0;
	public static final int DATA_LABEL_STYLE_VALUE = 1;
	public static final int DATA_LABEL_STYLE_ROWLABEL = 2;
	public static final int DATA_LABEL_STYLE_COLUMNLABEL = 3;
	public static final int DATA_LABEL_STYLE_PATTERN = 4;

	private Color[] colorlist=null;
	private int markerStyle=MARKER_STYLE_RECTANGLE;
	private int paintStyle=PAINT_STYLE_PLAIN;
	private Color seriesColor;
	private String seriesLabel;
	private int type=TYPE_BAR;
	private int dataLabelStyle=DATA_LABEL_STYLE_NONE;
	private java.util.List datas=new ArrayList();
	/**
	 * @return the colorlist
	 */
	public Color[] getColorlist() {
		if(colorlist==null) return new Color[0];
		return colorlist;
	}
	/**
	 * @param colorlist the colorlist to set
	 */
	public void setColorlist(Color[] colorlist) {
		this.colorlist = colorlist;
	}
	/**
	 * @return the dataLabelStyle
	 */
	public int getDataLabelStyle() {
		return dataLabelStyle;
	}
	/**
	 * @param dataLabelStyle the dataLabelStyle to set
	 */
	public void setDataLabelStyle(int dataLabelStyle) {
		this.dataLabelStyle = dataLabelStyle;
	}
	

	/**
	 * @return the markerStyle
	 */
	public int getMarkerStyle() {
		return markerStyle;
	}
	/**
	 * @param markerStyle the markerStyle to set
	 */
	public void setMarkerStyle(int markerStyle) {
		this.markerStyle = markerStyle;
	}
	/**
	 * @return the paintStyle
	 */
	public int getPaintStyle() {
		return paintStyle;
	}
	/**
	 * @param paintStyle the paintStyle to set
	 */
	public void setPaintStyle(int paintStyle) {
		this.paintStyle = paintStyle;
	}
	/**
	 * @return the seriesColor
	 */
	public Color getSeriesColor() {
		return seriesColor;
	}
	/**
	 * @param seriesColor the seriesColor to set
	 */
	public void setSeriesColor(Color seriesColor) {
		this.seriesColor = seriesColor;
	}
	/**
	 * @return the seriesLabel
	 */
	public String getSeriesLabel() {
		return seriesLabel;
	}
	/**
	 * @param seriesLabel the seriesLabel to set
	 */
	public void setSeriesLabel(String seriesLabel) {
		this.seriesLabel = seriesLabel;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	public void addChartData(ChartDataBean data) {
		datas.add(data);
	}
	/**
	 * @return the datas
	 */
	public java.util.List getDatas() {
		return datas;
	}
	
}