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

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Displays a graphical representation of data.
 *
 *
 *
 **/
public final class Graph extends TagImpl {

	/** The font used for the item labels. */
	private String itemlabelfont;

	/** The placement of the legend that identifies colors with the data labels. */
	private String showlegend;

	/**
	 * Title to display centered above the chart, or below the chart if the legend is above the chart.
	 */
	private String title;

	/** The size the value text, in points. */
	private double valuelabelsize;

	/** The size of the item labels, in points. */
	private double itemlabelsize;

	/** Width of the graph line, in pixels. */
	private double fill;

	/** Border color. */
	private String bordercolor;

	/**
	 * Name of the query containing the data to graph. Required if you do not use cfgraphdata tags in
	 * the cfgraph tag body to specify the data values.
	 */
	private String query;

	/** The font used to display data values. */
	private String valuelabelfont;

	/** The font used to display the title. */
	private String titlefont;

	/**
	 * An integer that specifies the number of grid lines to display on the chart between the top and
	 * bottom lines.
	 */
	private double gridlines;

	/** A URL to load when the user clicks any data point on the chart. */
	private String url;

	/**
	 * Query column that contains the data values. Required if you do not use cfgraphdata tags in the
	 * cfgraph tag body to specify the data values.
	 */
	private String valuecolumn;

	/** Spacing between bars in the chart, in pixels. */
	private double barspacing;

	/**
	 * Specifies whether to fill the area below the line with the line color to create an area graph.
	 */
	private double linewidth;

	/** Border thickness, in pixels. */
	private String borderwidth;

	/** Specifies whether values are displayed for the data points. */
	private boolean showvaluelabel;

	/**
	 * The minimum value of the graph value axis (the vertical axis for Bar charts, the horizontal axis
	 * for HorizontalBar charts).
	 */
	private double scalefrom;

	/**
	 * Specifies whether to put item labels on the horizontal axis of bar charts and the vertical axis
	 * of HorizontalBar charts.
	 */
	private boolean showitemlabel;

	/** Type of chart to display. */
	private String type;

	/** Depth of 3D chart appearance, in pixels. */
	private double depth;

	/**
	 * Query column containing URL information to load when the user clicks the corresponding data
	 * point.
	 */
	private String urlcolumn;

	/** The font used to display the legend. */
	private String legendfont;

	/** Color of the chart background. */
	private String backgroundcolor;

	/** Comma delimited list of colors to use for each data point. */
	private String colorlist;

	/** The maximum value of the graph value axis. */
	private double scaleto;

	/** Width of the graph, in pixels. Default is 320. */
	private double graphwidth;

	/** Where value labels are placed. */
	private String valuelocation;

	/**
	 * Query column that contains the item label for the corresponding data point. The item labels
	 * appear in the chart legend.
	 */
	private String itemcolumn;

	/** Orientation of item labels. */
	private String itemlabelorientation;

	/** The color used to draw the data line. */
	private String linecolor;

	/** Height of the graph, in pixels. Default is 240. */
	private double graphheight;

	/** File type to be used for the output displayed in the browser. */
	private String fileformat;

	/**
	 * constructor for the tag class
	 **/
	public Graph() throws ExpressionException {
		throw new ExpressionException("tag cfgraph is deprecated");
	}

	/**
	 * set the value itemlabelfont The font used for the item labels.
	 * 
	 * @param itemlabelfont value to set
	 **/
	public void setItemlabelfont(String itemlabelfont) {
		this.itemlabelfont = itemlabelfont;
	}

	/**
	 * set the value showlegend The placement of the legend that identifies colors with the data labels.
	 * 
	 * @param showlegend value to set
	 **/
	public void setShowlegend(String showlegend) {
		this.showlegend = showlegend;
	}

	/**
	 * set the value title Title to display centered above the chart, or below the chart if the legend
	 * is above the chart.
	 * 
	 * @param title value to set
	 **/
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * set the value valuelabelsize The size the value text, in points.
	 * 
	 * @param valuelabelsize value to set
	 **/
	public void setValuelabelsize(double valuelabelsize) {
		this.valuelabelsize = valuelabelsize;
	}

	/**
	 * set the value itemlabelsize The size of the item labels, in points.
	 * 
	 * @param itemlabelsize value to set
	 **/
	public void setItemlabelsize(double itemlabelsize) {
		this.itemlabelsize = itemlabelsize;
	}

	/**
	 * set the value fill Width of the graph line, in pixels.
	 * 
	 * @param fill value to set
	 **/
	public void setFill(double fill) {
		this.fill = fill;
	}

	/**
	 * set the value bordercolor Border color.
	 * 
	 * @param bordercolor value to set
	 **/
	public void setBordercolor(String bordercolor) {
		this.bordercolor = bordercolor;
	}

	/**
	 * set the value query Name of the query containing the data to graph. Required if you do not use
	 * cfgraphdata tags in the cfgraph tag body to specify the data values.
	 * 
	 * @param query value to set
	 **/
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * set the value valuelabelfont The font used to display data values.
	 * 
	 * @param valuelabelfont value to set
	 **/
	public void setValuelabelfont(String valuelabelfont) {
		this.valuelabelfont = valuelabelfont;
	}

	/**
	 * set the value titlefont The font used to display the title.
	 * 
	 * @param titlefont value to set
	 **/
	public void setTitlefont(String titlefont) {
		this.titlefont = titlefont;
	}

	/**
	 * set the value gridlines An integer that specifies the number of grid lines to display on the
	 * chart between the top and bottom lines.
	 * 
	 * @param gridlines value to set
	 **/
	public void setGridlines(double gridlines) {
		this.gridlines = gridlines;
	}

	/**
	 * set the value url A URL to load when the user clicks any data point on the chart.
	 * 
	 * @param url value to set
	 **/
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * set the value valuecolumn Query column that contains the data values. Required if you do not use
	 * cfgraphdata tags in the cfgraph tag body to specify the data values.
	 * 
	 * @param valuecolumn value to set
	 **/
	public void setValuecolumn(String valuecolumn) {
		this.valuecolumn = valuecolumn;
	}

	/**
	 * set the value barspacing Spacing between bars in the chart, in pixels.
	 * 
	 * @param barspacing value to set
	 **/
	public void setBarspacing(double barspacing) {
		this.barspacing = barspacing;
	}

	/**
	 * set the value linewidth Specifies whether to fill the area below the line with the line color to
	 * create an area graph.
	 * 
	 * @param linewidth value to set
	 **/
	public void setLinewidth(double linewidth) {
		this.linewidth = linewidth;
	}

	/**
	 * set the value borderwidth Border thickness, in pixels.
	 * 
	 * @param borderwidth value to set
	 **/
	public void setBorderwidth(String borderwidth) {
		this.borderwidth = borderwidth;
	}

	/**
	 * set the value showvaluelabel Specifies whether values are displayed for the data points.
	 * 
	 * @param showvaluelabel value to set
	 **/
	public void setShowvaluelabel(boolean showvaluelabel) {
		this.showvaluelabel = showvaluelabel;
	}

	/**
	 * set the value scalefrom The minimum value of the graph value axis (the vertical axis for Bar
	 * charts, the horizontal axis for HorizontalBar charts).
	 * 
	 * @param scalefrom value to set
	 **/
	public void setScalefrom(double scalefrom) {
		this.scalefrom = scalefrom;
	}

	/**
	 * set the value showitemlabel Specifies whether to put item labels on the horizontal axis of bar
	 * charts and the vertical axis of HorizontalBar charts.
	 * 
	 * @param showitemlabel value to set
	 **/
	public void setShowitemlabel(boolean showitemlabel) {
		this.showitemlabel = showitemlabel;
	}

	/**
	 * set the value type Type of chart to display.
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * set the value depth Depth of 3D chart appearance, in pixels.
	 * 
	 * @param depth value to set
	 **/
	public void setDepth(double depth) {
		this.depth = depth;
	}

	/**
	 * set the value urlcolumn Query column containing URL information to load when the user clicks the
	 * corresponding data point.
	 * 
	 * @param urlcolumn value to set
	 **/
	public void setUrlcolumn(String urlcolumn) {
		this.urlcolumn = urlcolumn;
	}

	/**
	 * set the value legendfont The font used to display the legend.
	 * 
	 * @param legendfont value to set
	 **/
	public void setLegendfont(String legendfont) {
		this.legendfont = legendfont;
	}

	/**
	 * set the value backgroundcolor Color of the chart background.
	 * 
	 * @param backgroundcolor value to set
	 **/
	public void setBackgroundcolor(String backgroundcolor) {
		this.backgroundcolor = backgroundcolor;
	}

	/**
	 * set the value colorlist Comma delimited list of colors to use for each data point.
	 * 
	 * @param colorlist value to set
	 **/
	public void setColorlist(String colorlist) {
		this.colorlist = colorlist;
	}

	/**
	 * set the value scaleto The maximum value of the graph value axis.
	 * 
	 * @param scaleto value to set
	 **/
	public void setScaleto(double scaleto) {
		this.scaleto = scaleto;
	}

	/**
	 * set the value graphwidth Width of the graph, in pixels. Default is 320.
	 * 
	 * @param graphwidth value to set
	 **/
	public void setGraphwidth(double graphwidth) {
		this.graphwidth = graphwidth;
	}

	/**
	 * set the value valuelocation Where value labels are placed.
	 * 
	 * @param valuelocation value to set
	 **/
	public void setValuelocation(String valuelocation) {
		this.valuelocation = valuelocation;
	}

	/**
	 * set the value itemcolumn Query column that contains the item label for the corresponding data
	 * point. The item labels appear in the chart legend.
	 * 
	 * @param itemcolumn value to set
	 **/
	public void setItemcolumn(String itemcolumn) {
		this.itemcolumn = itemcolumn;
	}

	/**
	 * set the value itemlabelorientation Orientation of item labels.
	 * 
	 * @param itemlabelorientation value to set
	 **/
	public void setItemlabelorientation(String itemlabelorientation) {
		this.itemlabelorientation = itemlabelorientation;
	}

	/**
	 * set the value linecolor The color used to draw the data line.
	 * 
	 * @param linecolor value to set
	 **/
	public void setLinecolor(String linecolor) {
		this.linecolor = linecolor;
	}

	/**
	 * set the value graphheight Height of the graph, in pixels. Default is 240.
	 * 
	 * @param graphheight value to set
	 **/
	public void setGraphheight(double graphheight) {
		this.graphheight = graphheight;
	}

	/**
	 * set the value fileformat File type to be used for the output displayed in the browser.
	 * 
	 * @param fileformat value to set
	 **/
	public void setFileformat(String fileformat) {
		this.fileformat = fileformat;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		itemlabelfont = "";
		showlegend = "";
		title = "";
		valuelabelsize = 0d;
		itemlabelsize = 0d;
		fill = 0d;
		bordercolor = "";
		query = "";
		valuelabelfont = "";
		titlefont = "";
		gridlines = 0d;
		url = "";
		valuecolumn = "";
		barspacing = 0d;
		linewidth = 0d;
		borderwidth = "";
		showvaluelabel = false;
		scalefrom = 0d;
		showitemlabel = false;
		type = "";
		depth = 0d;
		urlcolumn = "";
		legendfont = "";
		backgroundcolor = "";
		colorlist = "";
		scaleto = 0d;
		graphwidth = 0d;
		valuelocation = "";
		itemcolumn = "";
		itemlabelorientation = "";
		linecolor = "";
		graphheight = 0d;
		fileformat = "";
	}
}