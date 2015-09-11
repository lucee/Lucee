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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import lucee.commons.color.ColorCaster;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.ListUtil;

public final class Chartseries extends BodyTagImpl {
	
	private ChartSeriesBean series=new ChartSeriesBean();
	private String itemColumn;
	private Query query;
	private String valueColumn;
	
	@Override
	public void release() {
		super.release();
		series=new ChartSeriesBean();
		itemColumn=null;
		valueColumn=null;
		query=null;
	}
	/**
	 * @param colorlist the colorlist to set
	 * @throws ExpressionException 
	 */
	public void setColorlist(String strColorlist) throws ExpressionException {
		String[] arr=ListUtil.listToStringArray(strColorlist.trim(),',');
		Color[] colorlist=new Color[arr.length];
		for(int i=0;i<arr.length;i++) {
			colorlist[i]=ColorCaster.toColor(arr[i]);
		}
		series.setColorlist(colorlist);
	}
	/**
	 * @param dataLabelStyle the dataLabelStyle to set
	 * @throws ExpressionException 
	 */
	public void setDatalabelstyle(String strDataLabelStyle) throws ExpressionException {
		strDataLabelStyle=strDataLabelStyle.trim().toLowerCase();
		
		if("none".equals(strDataLabelStyle))			series.setDataLabelStyle(ChartSeriesBean.DATA_LABEL_STYLE_NONE);
		else if("value".equals(strDataLabelStyle))		series.setDataLabelStyle(ChartSeriesBean.DATA_LABEL_STYLE_VALUE);
		else if("rowlabel".equals(strDataLabelStyle))	series.setDataLabelStyle(ChartSeriesBean.DATA_LABEL_STYLE_ROWLABEL);
		else if("columnlabel".equals(strDataLabelStyle))series.setDataLabelStyle(ChartSeriesBean.DATA_LABEL_STYLE_COLUMNLABEL);
		else if("pattern".equals(strDataLabelStyle))	series.setDataLabelStyle(ChartSeriesBean.DATA_LABEL_STYLE_PATTERN);
		
		else throw new ExpressionException("invalid value ["+strDataLabelStyle+"] for attribute dataLabelStyle, for this attribute only the following values are supported " +
				"[none, value, rowlabel, columnlabel, pattern]");
	}
	/**
	 * @param itemColumn the itemColumn to set
	 */
	public void setItemcolumn(String itemColumn) {
		this.itemColumn=itemColumn;
	}
	/**
	 * @param markerStyle the markerStyle to set
	 * @throws ExpressionException 
	 */
	public void setMarkerstyle(String strMarkerStyle) throws ExpressionException {
		strMarkerStyle=strMarkerStyle.trim().toLowerCase();
		
		if("circle".equals(strMarkerStyle))			series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_CIRCLE);
		else if("diamond".equals(strMarkerStyle))	series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_DIAMOND);
		else if("letter".equals(strMarkerStyle))	series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_LETTER);
		else if("mcross".equals(strMarkerStyle))	series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_MCROSS);
		else if("rcross".equals(strMarkerStyle))	series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_RCROSS);
		else if("rectangle".equals(strMarkerStyle))	series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_RECTANGLE);
		else if("snow".equals(strMarkerStyle))		series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_SNOW);
		else if("triangle".equals(strMarkerStyle))	series.setMarkerStyle(ChartSeriesBean.MARKER_STYLE_TRIANGLE);
		
		else throw new ExpressionException("invalid value ["+strMarkerStyle+"] for attribute markerStyle, for this attribute only the following values are supported " +
				"[circle, diamond, letter, mcross, rcross, rectangle, snow, triangle]");
	}
	/**
	 * @param paintStyle the paintStyle to set
	 * @throws ExpressionException 
	 */
	public void setPaintstyle(String strPaintStyle) throws ExpressionException {
		strPaintStyle=strPaintStyle.trim().toLowerCase();
		
		if("light".equals(strPaintStyle))		series.setPaintStyle(ChartSeriesBean.PAINT_STYLE_LIGHT);
		else if("plain".equals(strPaintStyle))	series.setPaintStyle(ChartSeriesBean.PAINT_STYLE_PLAIN);
		else if("raise".equals(strPaintStyle))	series.setPaintStyle(ChartSeriesBean.PAINT_STYLE_RAISE);
		else if("shade".equals(strPaintStyle))	series.setPaintStyle(ChartSeriesBean.PAINT_STYLE_SHADE);
		
		else throw new ExpressionException("invalid value ["+strPaintStyle+"] for attribute paintStyle, for this attribute only the following values are supported " +
				"[light, plain, raise, shade]");
	}
	/**
	 * @param query the query to set
	 * @throws PageException 
	 */
	public void setQuery(Object oQuery) throws PageException {
		if(oQuery instanceof Query) this.query=(Query)oQuery;
		else if(oQuery instanceof String) this.query=pageContext.getQuery((String)oQuery);
		else query=Caster.toQuery(oQuery);
	}
	/**
	 * @param seriesColor the seriesColor to set
	 * @throws ExpressionException 
	 */
	public void setSeriescolor(String strSeriesColor) throws ExpressionException {
		series.setSeriesColor(ColorCaster.toColor(strSeriesColor));
	}
	/**
	 * @param seriesLabel the seriesLabel to set
	 */
	public void setSerieslabel(String seriesLabel) {
		series.setSeriesLabel(seriesLabel);
	}
	/**
	 * @param type the type to set
	 * @throws ExpressionException 
	 */
	public void setType(String strType) throws ExpressionException {
		strType=strType.trim().toLowerCase();
		
		if("area".equals(strType))					series.setType(ChartSeriesBean.TYPE_AREA);
		else if("bar".equals(strType))				series.setType(ChartSeriesBean.TYPE_BAR);
		else if("cone".equals(strType))				series.setType(ChartSeriesBean.TYPE_CONE);
		else if("curve".equals(strType))			series.setType(ChartSeriesBean.TYPE_CURVE);
		else if("cylinder".equals(strType))			series.setType(ChartSeriesBean.TYPE_CYLINDER);
		else if("horizontalbar".equals(strType))	series.setType(ChartSeriesBean.TYPE_HORIZONTALBAR);
		else if("line".equals(strType))				series.setType(ChartSeriesBean.TYPE_LINE);
		else if("timeline".equals(strType))				series.setType(ChartSeriesBean.TYPE_TIME);
		else if("time".equals(strType))				series.setType(ChartSeriesBean.TYPE_TIME);
		else if("pie".equals(strType))				series.setType(ChartSeriesBean.TYPE_PIE);
		else if("pyramid".equals(strType))			series.setType(ChartSeriesBean.TYPE_PYRAMID);
		else if("scatter".equals(strType))			series.setType(ChartSeriesBean.TYPE_SCATTER);
		else if("scatte".equals(strType))			series.setType(ChartSeriesBean.TYPE_SCATTER);
		else if("step".equals(strType))				series.setType(ChartSeriesBean.TYPE_STEP);
		
		else throw new ExpressionException("invalid value ["+strType+"] for attribute type, for this attribute only the following values are supported " +
				"[area, bar, cone, curve, cylinder, horizontalbar, line,pie,pyramid,scatter,step,timeline]");
	}
	/**
	 * @param valueColumn the valueColumn to set
	 */
	public void setValuecolumn(String valueColumn) {
		this.valueColumn=valueColumn;
	}

	public void addChartData(ChartDataBean data) {
		series.addChartData(data);
	}
	

	@Override
	public int doStartTag()	{
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {

		ChartDataBean data;
		
		if(query!=null) {
			if(StringUtil.isEmpty(itemColumn)) throw new ApplicationException("attribute itemColumn is required for tag cfchartseries when attribute query is defined");
			if(StringUtil.isEmpty(valueColumn)) throw new ApplicationException("attribute valueColumn is required for tag cfchartseries when attribute query is defined");
			
			int rowCount = query.getRecordcount();
			for(int i=1;i<=rowCount;i++) {
				data=new ChartDataBean();
				data.setValue(Caster.toDoubleValue(query.getAt(valueColumn, i, new Double(0))));
				data.setItem(pageContext,query.getAt(itemColumn, i, ""));
				//data.setItem(itemToString(query.getAt(itemColumn, i, "")));
				addChartData(data);
			}
		}
		// get parent chart
		Tag parent=this;
		do{
			parent = parent.getParent();
			if(parent instanceof Chart) {
				((Chart)parent).addChartSeries(series);
				break;
			}
		}
		while(parent!=null);
		return EVAL_PAGE;
	}
}