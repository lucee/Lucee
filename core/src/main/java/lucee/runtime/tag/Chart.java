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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import lucee.commons.color.ColorCaster;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.StringUtil;
import lucee.runtime.chart.BarRenderer3DWrap;
import lucee.runtime.chart.CategoryToolTipGeneratorImpl;
import lucee.runtime.chart.LabelFormatUtil;
import lucee.runtime.chart.PieSectionLabelGeneratorImpl;
import lucee.runtime.chart.PieSectionLegendLabelGeneratorImpl;
import lucee.runtime.chart.PieToolTipGeneratorImpl;
import lucee.runtime.chart.TickUnitsImpl;
import lucee.runtime.converter.JavaConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.functions.dateTime.DateAdd;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.urls.URLUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;


public final class Chart extends BodyTagImpl implements Serializable {

	
	
	public static final Color COLOR_999999=new Color(0x99,0x99,0x99);
	public static final Color COLOR_666666=new Color(0x66,0x66,0x66);
	public static final Color COLOR_333333=new Color(0x33,0x33,0x33);
	
	public static final String FONT_ARIAL = "arial";
	public static final String FONT_TIMES = "times";
	public static final String FONT_COURIER = "courier";
	public static final String FONT_ARIAL_UNICODE_MS = "arialunicodems";

	public static final int FORMAT_GIF = 0;
	public static final int FORMAT_JPG = 1;
	public static final int FORMAT_PNG = 2;
	public static final int FORMAT_FLASH=3;


	public static final int PIE_SLICE_STYLE_SOLID = 0;
	public static final int PIE_SLICE_STYLE_SLICED = 1;
	
	public static final int SERIES_PLACEMENT_DEFAULT = 0;
	public static final int SERIES_PLACEMENT_CLUSTER = 1;
	public static final int SERIES_PLACEMENT_STACKED = 2;
	public static final int SERIES_PLACEMENT_PERCENT = 3;

	public static final int TIP_STYLE_NONE = 0;
	public static final int TIP_STYLE_FORMATS = 1;
	public static final int TIP_STYLE_MOUSEDOWN = 2;
	public static final int TIP_STYLE_MOUSEOVER = 3;
	
	public static final CategoryLabelPositions LABEL_HORIZONTAL = CategoryLabelPositions.STANDARD;
	public static final CategoryLabelPositions LABEL_VERTICAL = CategoryLabelPositions.DOWN_90;
	public static final CategoryLabelPositions LABEL_DOWN_90 = CategoryLabelPositions.DOWN_90;
	public static final CategoryLabelPositions LABEL_DOWN_45 = CategoryLabelPositions.DOWN_45;
	public static final CategoryLabelPositions LABEL_UP_45 = CategoryLabelPositions.UP_45;
	public static final CategoryLabelPositions LABEL_UP_90 = CategoryLabelPositions.UP_90;

	private static final int NONE = 0;
	private static final int YES = 1;
	private static final int NO = 2;
	

	private static int chartIndex=0;
	
	private Color backgroundcolor=Color.WHITE;
	private Color databackgroundcolor=Color.WHITE;
	private Color foregroundcolor=Color.BLACK;
	private Color tipbgcolor=Color.WHITE;
	private String xaxistitle=null;
	private String yaxistitle=null;
	

	private int chartheight=240;
	private int chartwidth=320;
	
	private String font=FONT_ARIAL;
	private int fontstyle=0;
	private int fontsize=11;
	
	private int format=FORMAT_PNG;
	private int gridlines=10;
	
	private int labelFormat=LabelFormatUtil.LABEL_FORMAT_NUMBER;
	private CategoryLabelPositions labelPosition=LABEL_HORIZONTAL;
	private int markersize=-1;
	
	private String name=null;
	
	private int pieslicestyle=PIE_SLICE_STYLE_SLICED;

	private double scalefrom=Double.NaN;
	private double scaleto=Double.NaN;
	private boolean legendMultiLine=false;
	
	private int seriesplacement=SERIES_PLACEMENT_DEFAULT;

	private boolean show3d=false;
	private boolean showtooltip=true;
	private boolean showborder=false;
	private boolean showlegend=true;
	private boolean showmarkers=true;
	private int showxgridlines=NONE;
	private boolean showygridlines=false;
	private boolean sortxaxis=false;

	private String style=null;
	private String title="";
	
	private int tipstyle=TIP_STYLE_MOUSEOVER;
	private List<ChartSeriesBean> _series=new ArrayList<ChartSeriesBean>();

	private String url;
	private double xoffset=0.1;
	private double yoffset=0.1;
	private String xaxistype="category";
	private String yaxistype="category";
	private double smallest;
	private double biggest;
	private boolean showXLabel=true;
	private String source;
	private List<String> _plotItemLables = new ArrayList<String>();
	
	public void release() {
		_series.clear();

		url=null;
		xoffset=0.1;
		yoffset=0.1;
		xaxistype="category";
		yaxistype="category";
				
		xaxistitle="";
		yaxistitle="";
		legendMultiLine=false;
		// TODO super.release();
		backgroundcolor=Color.WHITE;
		databackgroundcolor=Color.WHITE;
		foregroundcolor=Color.BLACK;
		tipbgcolor=Color.WHITE;
		
		chartheight=240;
		chartwidth=320;
		
		font=FONT_ARIAL;
		fontstyle=0;
		fontsize=11;
		
		format=FORMAT_PNG;
		gridlines=10;
		
		labelFormat=LabelFormatUtil.LABEL_FORMAT_NUMBER;
		labelPosition=LABEL_HORIZONTAL;

		markersize=-1;
		name=null;
		
		pieslicestyle=PIE_SLICE_STYLE_SLICED;
		
		scalefrom=Double.NaN;
		scaleto=Double.NaN;
		seriesplacement=SERIES_PLACEMENT_DEFAULT;
		
		show3d=false;
		showborder=false;
		showlegend=true;
		showmarkers=true;
		showxgridlines=NONE;
		showygridlines=false;
		sortxaxis=false;
		showXLabel=true;
		showtooltip=true;
		style=null;
		title="";
		source=null;
		tipstyle=TIP_STYLE_MOUSEOVER;
		_plotItemLables = new ArrayList<String>();
	}
	
	

	public void setShowxlabel(boolean showXLabel) {
		this.showXLabel = showXLabel;
	}
	public void setCategorylabelpositions(String strOrientation) {
		strOrientation=strOrientation.trim().toLowerCase();
		if("vertical".equals(strOrientation))labelPosition=LABEL_VERTICAL;
		else if("up_45".equals(strOrientation))labelPosition=LABEL_UP_45;
		else if("up_90".equals(strOrientation))labelPosition=LABEL_UP_90;
		else if("down_45".equals(strOrientation))labelPosition=LABEL_DOWN_45;
		else if("down_90".equals(strOrientation))labelPosition=LABEL_DOWN_90;
		else if("standard".equals(strOrientation))labelPosition=LABEL_HORIZONTAL;
		else labelPosition=LABEL_HORIZONTAL;
		//else throw new ExpressionException("invalid value ["+strOrientation+"] for attribute CategoryLabelPositions, for this attribute only the following values are supported [horizontal,vertical,up_90,up_45,down_90,down_45]");
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setShowtooltip(boolean showtooltip) {
		this.showtooltip = showtooltip;
	}
	public void setBackgroundcolor(String strBackgroundColor) throws ExpressionException {
		this.backgroundcolor = ColorCaster.toColor(strBackgroundColor);
	}
	
	public void setDatabackgroundcolor(String strDatabackgroundcolor) throws ExpressionException {
		this.databackgroundcolor = ColorCaster.toColor(strDatabackgroundcolor);
	}

	public void setForegroundcolor(String strForegroundcolor) throws ExpressionException {
		this.foregroundcolor = ColorCaster.toColor(strForegroundcolor);
	}

	public void setTipbgcolor(String strTipbgcolor) throws ExpressionException {
		this.tipbgcolor = ColorCaster.toColor(strTipbgcolor);
	}
	
	public void setChartheight(double chartheight) {
		this.chartheight = (int) chartheight;
	}

	public void setChartwidth(double chartwidth) {
		this.chartwidth = (int) chartwidth;
	}

	public void setFont(String strFont) {
		strFont=strFont.trim().toLowerCase();
		if("arial".equals(strFont))font=FONT_ARIAL;
		else if("times".equals(strFont))font=FONT_TIMES;
		else if("courier".equals(strFont))font=FONT_COURIER;
		else if("arialunicodems".equals(strFont))font=FONT_ARIAL_UNICODE_MS;
		else font=strFont;
		//else throw new ExpressionException("invalid value ["+strFont+"] for attribute font, for this attribute only the following values are supported [arial,times,courier,arialunicodeMS]");
	}

	public void setFontbold(boolean fontbold) {
		if(fontbold)fontstyle+=Font.BOLD;
	}

	public void setFontitalic(boolean fontitalic) {
		if(fontitalic)fontstyle+=Font.ITALIC;
	}

	public void setFontsize(double fontsize) {
		this.fontsize = (int) fontsize;
	}

	public void setFormat(String strFormat) throws ExpressionException {
		strFormat=strFormat.trim().toLowerCase();
		if("gif".equals(strFormat))			format=FORMAT_GIF;
		else if("jpg".equals(strFormat))	format=FORMAT_JPG;
		else if("jpeg".equals(strFormat))	format=FORMAT_JPG;
		else if("jpe".equals(strFormat))	format=FORMAT_JPG;
		else if("png".equals(strFormat))	format=FORMAT_PNG;
		//else if("flash".equals(strFormat))	format=FORMAT_FLASH;
		//else if("swf".equals(strFormat))	format=FORMAT_FLASH;
		
		else throw new ExpressionException("invalid value ["+strFormat+"] for attribute format, for this attribute only the following values are supported [gif,jpg,png]");
	}

	public void setGridlines(double gridlines) {
		this.gridlines = (int) gridlines;
	}

	public void setLabelformat(String strLabelFormat) throws ExpressionException {
		strLabelFormat=strLabelFormat.trim().toLowerCase();
		if("number".equals(strLabelFormat))			labelFormat=LabelFormatUtil.LABEL_FORMAT_NUMBER;
		else if("numeric".equals(strLabelFormat))	labelFormat=LabelFormatUtil.LABEL_FORMAT_NUMBER;
		else if("currency".equals(strLabelFormat))	labelFormat=LabelFormatUtil.LABEL_FORMAT_CURRENCY;
		else if("date".equals(strLabelFormat))		labelFormat=LabelFormatUtil.LABEL_FORMAT_DATE;
		else if("percent".equals(strLabelFormat))	labelFormat=LabelFormatUtil.LABEL_FORMAT_PERCENT;
		//else if("integer".equals(strLabelFormat))	labelFormat=LabelFormatUtil.LABEL_FORMAT_INTEGER;
		
		else throw new ExpressionException("invalid value ["+strLabelFormat+"] for attribute labelFormat, for this attribute only the following values are supported [date,percent,currency,number]");
	}

	public void setMarkersize(double markersize) throws ExpressionException {
		if(markersize<1) throw new ExpressionException("invalid value ["+markersize+"] for attribute markersize, value must be a positive integer greater than 0");
		this.markersize=(int) markersize;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPieslicestyle(String strPieslicestyle) throws ExpressionException {
		strPieslicestyle=strPieslicestyle.trim().toLowerCase();
		if("sliced".equals(strPieslicestyle))		pieslicestyle=PIE_SLICE_STYLE_SLICED;
		else if("slice".equals(strPieslicestyle))	pieslicestyle=PIE_SLICE_STYLE_SLICED;
		else if("solid".equals(strPieslicestyle))	pieslicestyle=PIE_SLICE_STYLE_SOLID;
		
		else throw new ExpressionException("invalid value ["+strPieslicestyle+"] for attribute pieSliceStyle, for this attribute only the following values are supported [sliced,solid]");
	}

	public void setScaleto(double scaleto) {
		//if(scaleto<0) throw new ExpressionException("invalid value ["+scaleto+"] for attribute scaleto, value must be a positive integer greater or equal than 0");
		this.scaleto =  scaleto;
	}

	public void setScalefrom(double scaletrom)  {
		//if(scaletrom<0) throw new ExpressionException("invalid value ["+scaletrom+"] for attribute scaletrom, value must be a positive integer greater or equal than 0");
		this.scalefrom =  scaletrom;
	}

	public void setSeriesplacement(String strSeriesplacement) throws ExpressionException {
		strSeriesplacement=strSeriesplacement.trim().toLowerCase();
		if("default".equals(strSeriesplacement))	seriesplacement=SERIES_PLACEMENT_DEFAULT;
		else if("cluster".equals(strSeriesplacement))seriesplacement=SERIES_PLACEMENT_CLUSTER;
		else if("stacked".equals(strSeriesplacement))seriesplacement=SERIES_PLACEMENT_STACKED;
		else if("percent".equals(strSeriesplacement))seriesplacement=SERIES_PLACEMENT_PERCENT;
		
		else throw new ExpressionException("invalid value ["+strSeriesplacement+"] for attribute seriesplacement, for this attribute only the following values are supported [default,cluster,percent,stacked]");
	}

	public void setShow3d(boolean show3d) {
		this.show3d = show3d;
	}

	public void setShowborder(boolean showborder) {
		this.showborder = showborder;
	}

	public void setShowlegend(boolean showlegend) {
		this.showlegend = showlegend;
	}

	public void setShowmarkers(boolean showmarkers) {
		this.showmarkers = showmarkers;
	}

	public void setShowxgridlines(boolean showxgridlines) {
		this.showxgridlines = showxgridlines?YES:NO;
	}

	public void setShowygridlines(boolean showygridlines) {
		this.showygridlines = showygridlines;
	}

	public void setSortxaxis(boolean sortxaxis) {
		this.sortxaxis = sortxaxis;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTipstyle(String strTipstyle) throws ExpressionException {
		strTipstyle=strTipstyle.trim().toLowerCase();
		if("mousedown".equals(strTipstyle))		tipstyle=TIP_STYLE_MOUSEDOWN;
		else if("mouseover".equals(strTipstyle))tipstyle=TIP_STYLE_MOUSEOVER;
		else if("none".equals(strTipstyle))	   	tipstyle=TIP_STYLE_NONE;
		else if("formats".equals(strTipstyle))	tipstyle=TIP_STYLE_FORMATS;
		
		else throw new ExpressionException("invalid value ["+strTipstyle+"] for attribute Tipstyle, for this attribute only the following values are supported [mouseover,mousedown,one,formats]");
	}
	

	
	/**
	 * @param xaxistitle the xaxistitle to set
	 */
	public void setXaxistitle(String xaxistitle) {
		this.xaxistitle = xaxistitle;
	}

	/**
	 * @param yaxistitle the yaxistitle to set
	 */
	public void setYaxistitle(String yaxistitle) {
		this.yaxistitle = yaxistitle;
	}

	public void addChartSeries(ChartSeriesBean series) {
		_series.add(series);
	}
	

	public int doStartTag()	{
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		if(_series.size()==0) throw new ApplicationException("at least one cfchartseries tag required inside cfchart"); 
		//if(_series.size()>1) throw new ApplicationException("only one cfchartseries tag allowed inside cfchart"); 
		//doSingleSeries((ChartSeriesBean) _series.get(0));
		ChartSeriesBean first= _series.get(0);
		
		try {
				
			if(first.getType()==ChartSeriesBean.TYPE_BAR)
				//throw new ApplicationException("type bar is not supported");
				chartBar();
			else if(first.getType()==ChartSeriesBean.TYPE_TIME)
				chartTimeLine();
			else if(first.getType()==ChartSeriesBean.TYPE_AREA)
				chartArea();
			else if(first.getType()==ChartSeriesBean.TYPE_CONE)
				throw new ApplicationException("type cone is not supported");
			else if(first.getType()==ChartSeriesBean.TYPE_CURVE)
				chartLine();
				//throw new ApplicationException("type curve is not supported");
			else if(first.getType()==ChartSeriesBean.TYPE_CYLINDER)
				throw new ApplicationException("type cylinder is not supported");
			else if(first.getType()==ChartSeriesBean.TYPE_HORIZONTALBAR)
				chartHorizontalBar();
			else if(first.getType()==ChartSeriesBean.TYPE_LINE)
				chartLine();
				//throw new ApplicationException("type line is not supported");
			else if(first.getType()==ChartSeriesBean.TYPE_PIE)
				chartPie();
			else if(first.getType()==ChartSeriesBean.TYPE_PYRAMID)
				throw new ApplicationException("type pyramid is not supported");
			else if(first.getType()==ChartSeriesBean.TYPE_SCATTER)
				chartScatter();
			else if(first.getType()==ChartSeriesBean.TYPE_STEP)
				chartStep();
		}
		catch(IOException ioe) {
			throw Caster.toPageException(ioe);
		}
		
		return EVAL_PAGE;
	}

	private void chartPie() throws PageException, IOException {
		// do dataset
		DefaultPieDataset dataset = new DefaultPieDataset();
		ChartSeriesBean csb =  _series.get(0);
        
		ChartDataBean cdb;
        
		List datas=csb.getDatas();
		if(sortxaxis)Collections.sort(datas);
        Iterator itt = datas.iterator();
    	while(itt.hasNext()) {
    		cdb=(ChartDataBean) itt.next();
    		dataset.setValue(cdb.getItemAsString(), cdb.getValue());
    	}	
		
    	
        JFreeChart chart = show3d?
        		ChartFactory.createPieChart3D	(title, dataset, false, true, true):
        		ChartFactory.createPieChart		(title, dataset, false, true, true);
        
        Plot p = chart.getPlot();
		PiePlot pp = (PiePlot)p;
        
		Font _font = getFont();
        pp.setLegendLabelGenerator(new PieSectionLegendLabelGeneratorImpl(_font,chartwidth));
        pp.setBaseSectionOutlinePaint(Color.GRAY); // rand der st_cke
        pp.setLegendItemShape(new Rectangle(7,7));
        pp.setLabelFont(new Font(font,0,11));
        pp.setLabelLinkPaint(COLOR_333333);
        pp.setLabelLinkMargin(-0.05);
        pp.setInteriorGap(0.123);
        pp.setLabelGenerator(new PieSectionLabelGeneratorImpl(labelFormat));
        
        
        
        
        databackgroundcolor=backgroundcolor;
        
        setBackground(chart,p);
		setBorder(chart,p);
		setLegend(chart,p,_font);
		set3d(p);
		setFont(chart, _font);
        setTooltip(chart);
        setScale(chart);
        
        // Slice Type and colors
        boolean doSclice=pieslicestyle==PIE_SLICE_STYLE_SLICED;
        Color[] colors = csb.getColorlist();
        Iterator it = csb.getDatas().iterator();
        int count=0;
    	while(it.hasNext()) {
    		cdb=(ChartDataBean) it.next();
            if(doSclice)pp.setExplodePercent(cdb.getItemAsString(), 0.13);
            
            if(count<colors.length){
            	pp.setSectionPaint(cdb.getItemAsString(), colors[count]);
            }
            count++;
    	}
        
        writeOut(chart);
	}
	

	private void set3d(Plot plot) {
        if(!show3d) return;
        
        plot.setForegroundAlpha(0.6f);
        
        if(plot instanceof CategoryPlot) {
            plot.setForegroundAlpha(0.8f);
        	CategoryPlot cp=(CategoryPlot) plot;
        	CategoryItemRenderer renderer = cp.getRenderer();
        	if(renderer instanceof BarRenderer3D) {
        		BarRenderer3D br3d=(BarRenderer3D) renderer;
        		cp.setRenderer(new BarRenderer3DWrap(br3d,xoffset,yoffset));
        	}
        	
        }
        else if(plot instanceof PiePlot3D) {
        	PiePlot3D pp3d=(PiePlot3D) plot;
            pp3d.setDepthFactor(0.10);    
        }
        
        

        //CategoryItemRenderer renderer = plot.getRenderer();
        
	}

	private void setFont(JFreeChart chart, Font font) {
		// title
		TextTitle title=chart.getTitle();
		if(title!=null) {
			title.setFont(font);
			title.setPaint(foregroundcolor);
			chart.setTitle(title);
		}
		
		// axis fonts
		Plot plot = chart.getPlot();
		if(plot instanceof CategoryPlot) {
			CategoryPlot cp = (CategoryPlot)plot;
			setAxis(cp.getRangeAxis(),font);
			setAxis(cp.getDomainAxis(),font);
		}
		if(plot instanceof XYPlot) {
			XYPlot cp = (XYPlot)plot;
			setAxis(cp.getRangeAxis(),font);
			setAxis(cp.getDomainAxis(),font);
		}
		
		
	}
	
	
	private void setAxis(Axis axis, Font font) {
		if(axis!=null) {
			axis.setLabelFont(font);
			axis.setLabelPaint(foregroundcolor);
			
			axis.setTickLabelFont(font);
	        axis.setTickLabelPaint(foregroundcolor);
	        axis.setTickLabelsVisible(true);
		}
	}



	private void setLegend(JFreeChart chart, Plot plot, Font font) {
		if(!showlegend) return;
			
			
		Color bg = backgroundcolor==null?databackgroundcolor:backgroundcolor;
		if(font==null)font=getFont();
		
		
		
		LegendTitle legend = legendMultiLine?
        		new LegendTitle(plot,new ColumnArrangement(), new ColumnArrangement()):
        		new LegendTitle(plot);
        legend.setBackgroundPaint(bg);
        legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        legend.setFrame(new LineBorder());
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
        
        legend.setWidth(chartwidth-20);// geht nicht
        legend.setItemFont(font);
		legend.setItemPaint(foregroundcolor);

		//RectangleInsets labelPadding;
		legend.setItemLabelPadding(new RectangleInsets(2,2,2,2));
		legend.setBorder(0,0,0,0); 
		legend.setLegendItemGraphicLocation(RectangleAnchor.TOP_LEFT);
		legend.setLegendItemGraphicPadding(new RectangleInsets(8,10,0,0));
		chart.addLegend(legend);
		
	}



	private void setBorder(JFreeChart chart, Plot plot) {
		chart.setBorderVisible(false);
		chart.setBorderPaint(foregroundcolor);
		plot.setOutlinePaint(foregroundcolor);
	}



	private void setBackground(JFreeChart chart, Plot plot) {
		//Color bg = backgroundcolor==null?databackgroundcolor:backgroundcolor;

		chart.setBackgroundPaint(backgroundcolor);
		plot.setBackgroundPaint(databackgroundcolor);
		chart.setBorderPaint(databackgroundcolor);
		
		
        plot.setOutlineVisible(false);
		
		// Pie
		if(plot instanceof PiePlot) {
			PiePlot pp=(PiePlot) plot;
			pp.setLabelOutlinePaint(backgroundcolor); 
	        pp.setLabelBackgroundPaint(backgroundcolor);
	        pp.setLabelShadowPaint(backgroundcolor);
	        pp.setShadowPaint(backgroundcolor);
		}
		// Bar
		/*if(plot instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) plot;
			
		}*/
	}



	
 
	private Font getFont() {
		return new Font(font,fontstyle,fontsize);
	}

	private void writeOut(JFreeChart jfc) throws PageException, IOException {
		final ChartRenderingInfo info=new ChartRenderingInfo();
        
		// map name
		chartIndex++;
		if(chartIndex<0)chartIndex=0;
		String mapName="chart_"+chartIndex;
		setUrl(jfc);
		
		// write out to variable
		if(!StringUtil.isEmpty(name)){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copy(baos, jfc,info);
			pageContext.setVariable(name, baos.toByteArray());
			return;
		}
		
		// write out as link
		String id=Md5.getDigestAsString(JavaConverter.serialize(this));
		Resource graph = pageContext.getConfig().getTempDirectory().getRealResource("graph");
		Resource res = graph.getRealResource(id);
		if(!res.exists()) {
			clean(graph);
			copy(res.getOutputStream(),jfc,info);
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copy(baos, jfc,info);			
		}
		
		String contextPath = pageContext.getHttpServletRequest().getContextPath();
		contextPath = StringUtil.isEmpty(contextPath) ? "/" : contextPath+"/";
		String src=contextPath+"lucee/graph.cfm?img="+id+"&type="+formatToString(format);
		
		if(!StringUtil.isEmpty(source)) {
			pageContext.setVariable(source, src);
			return;
		}
		try {
			if(showtooltip || !StringUtil.isEmpty(url)) {
				String map=ChartUtilities.getImageMap(mapName,info).trim();
				pageContext.write(map);
			}
			pageContext.write("<img border=\"0\" usemap=\"#"+mapName+"\" src=\""+src+"\">");
		} 
		catch (IOException e) {
			throw Caster.toPageException(e);
		}		
	}

	private void copy(OutputStream os, JFreeChart jfc, ChartRenderingInfo info) throws ApplicationException, IOException, ExpressionException {
		//OutputStream os = null;
		try {
			//os = res.getOutputStream();
			
			BufferedImage bi;
			if (format==FORMAT_JPG) {
				bi = jfc.createBufferedImage(chartwidth,chartheight,BufferedImage.TYPE_INT_RGB,info);
			} else {
				bi = jfc.createBufferedImage(chartwidth,chartheight,info);
			}
			Image img;
			
			// add border
			if(showborder) {
				try {
					img = new Image(bi);
					img.addBorder(1,Color.BLACK,Image.BORDER_TYPE_CONSTANT);
					bi=img.getBufferedImage();
				}
				catch (PageException e) {}
			}
			if(format==FORMAT_PNG)		ChartUtilities.writeBufferedImageAsPNG(os, bi);
			else if(format==FORMAT_JPG)	ChartUtilities.writeBufferedImageAsJPEG(os, bi);
			else if(format==FORMAT_GIF)	{
				img = new lucee.runtime.img.Image(bi);
				img.writeOut(os, "gif",1,true);
				
				//throw new ApplicationException("format gif not supported");
			}
			else if(format==FORMAT_FLASH)throw new ApplicationException("format flash not supported");
		}
		finally {
			IOUtil.flushEL(os);
			IOUtil.closeEL(os);
		}
	}

	private String formatToString(int format) {
		if(format==FORMAT_GIF) return "gif";
		if(format==FORMAT_JPG) return "jpeg";
		if(format==FORMAT_PNG) return "png";
		return "swf";
	}

	private void clean(Resource graph) throws IOException {
		if(!graph.exists())graph.createDirectory(true);
		else if(graph.isDirectory() && ResourceUtil.getRealSize(graph)>(1024*1024)) {
			
			Resource[] children = graph.listResources();
			long maxAge=System.currentTimeMillis()-(1000*60);
			for(int i=0;i<children.length;i++) {
				if(children[i].lastModified()<maxAge)
					children[i].delete();
			}
		}
	}

	private void chartBar() throws PageException, IOException {
		// create the chart...
        final JFreeChart chart = show3d?
        	ChartFactory.createBarChart3D(title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.VERTICAL,false,true,false):
        	ChartFactory.createBarChart  (title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.VERTICAL,false,true,false);
        Plot p = chart.getPlot();
        Font _font = getFont();
        // settings
        
        
        setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		setLegend(chart, p, _font);
        setTooltip(chart);
		setScale(chart);
        setAxis(chart);
        setColor(chart);
        
        writeOut(chart);
	}
	




	private void chartLine() throws PageException, IOException {
		// create the chart...
        final JFreeChart chart = show3d?
        	ChartFactory.createLineChart3D(title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.VERTICAL,false,true,false):
        	ChartFactory.createLineChart(title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.VERTICAL,false,true,false);
        Plot p = chart.getPlot();
        Font _font = getFont();
        
        // settings
        setMarker(chart,p);
        setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		setLegend(chart, p, _font);
        setTooltip(chart);
		setScale(chart);
        setAxis(chart);
        setColor(chart);
        
        writeOut(chart);
	}
	
	private void chartArea() throws PageException, IOException {
		// create the chart...
        final JFreeChart chart = ChartFactory.createAreaChart(title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.VERTICAL,false,true,false);
        Plot p = chart.getPlot();
        Font _font = getFont();
        
        // settings
        setMarker(chart,p);
        setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		setLegend(chart, p, _font);
        setTooltip(chart);
		setScale(chart);
        setAxis(chart);
        setColor(chart);
        
        writeOut(chart);
	}
	
	private void chartTimeLine() throws PageException, IOException {
		// create the chart...
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(title,xaxistitle,yaxistitle,createTimeSeriesCollection(),false,true,false);
        Plot p = chart.getPlot();
        Font _font = getFont();
        
        // settings
        setMarker(chart,p);
        setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		setLegend(chart, p, _font);
        setTooltip(chart);
		setScale(chart);
        setAxis(chart);
        setColor(chart);
        
        writeOut(chart);
	}

	private void chartHorizontalBar() throws PageException, IOException {
		// create the chart...
        final JFreeChart chart = show3d?
        	ChartFactory.createBarChart3D(title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.HORIZONTAL,false,true,false):
        	ChartFactory.createBarChart  (title,xaxistitle,yaxistitle,createDatasetCategory(),PlotOrientation.HORIZONTAL,false,true,false);
    	final CategoryPlot p = chart.getCategoryPlot();
    	p.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        Font _font = getFont();
        // settings            
        
        setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		setLegend(chart, p, _font);
        setTooltip(chart);
		setScale(chart);
        setAxis(chart);
        setColor(chart);
        
        writeOut(chart);
	}
	
	private void chartScatter() throws PageException, IOException {
		// create the chart...
		final JFreeChart chart = ChartFactory.createScatterPlot(title,xaxistitle,yaxistitle,createXYSeriesCollection(),PlotOrientation.VERTICAL,false,true,false);
		final XYPlot p = chart.getXYPlot();
		Font _font = getFont();
		// settings            

		setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		setLegend(chart, p, _font);
		setTooltip(chart);
		setScale(chart);
		setAxis(chart);
		setColor(chart);
		
		writeOut(chart);
	}
	
	private void chartStep() throws PageException, IOException {
		// create the chart...
		final JFreeChart chart = ChartFactory.createXYStepChart(title,xaxistitle,yaxistitle,createXYSeriesCollection(),PlotOrientation.VERTICAL,false,true,false);
		final XYPlot p = chart.getXYPlot();
		Font _font = getFont();
		// settings            
		
		setBackground(chart,p);
		setBorder(chart,p);
		set3d(p);
		setFont(chart,_font);
		setLabelFormat(chart);
		p.getDomainAxis().setRange(Range.expandToInclude(p.getDomainAxis().getRange(), p.getDomainAxis().getUpperBound()+0.25));
		p.getDomainAxis().setRange(Range.expandToInclude(p.getDomainAxis().getRange(), p.getDomainAxis().getLowerBound()-0.25));
		setLegend(chart, p, _font);
		setTooltip(chart);
		setScale(chart);
		setAxis(chart);
		setColor(chart);
		
		writeOut(chart);
	}
	


	private void setMarker(JFreeChart chart, Plot p) {
		if(!showmarkers) return;
		
		if(markersize<1 || markersize>100) markersize=4;
		
		
		
		if(p instanceof XYPlot) {
			XYPlot xyp=(XYPlot) p;
			XYItemRenderer r = xyp.getRenderer();
			if (r instanceof XYLineAndShapeRenderer) {
				XYLineAndShapeRenderer xyr = (XYLineAndShapeRenderer) r;
				xyr.setBaseShapesVisible(true);
				xyr.setBaseShapesFilled(true);
				
				int seriesCount=_series.size();
				for(int i=0;i<seriesCount;i++){
					xyr.setSeriesShapesVisible(i, true);
					xyr.setSeriesItemLabelsVisible(i, true);
					xyr.setSeriesShape(i, ShapeUtilities.createDiamond(markersize));
					xyr.setUseFillPaint(true);
					xyr.setBaseFillPaint(databackgroundcolor);
				}
			}
		}
		else if(p instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) p;
			CategoryItemRenderer r = cp.getRenderer();
			if (r instanceof LineAndShapeRenderer) {
				LineAndShapeRenderer lsr = (LineAndShapeRenderer)r;
			
				int seriesCount=_series.size();
				for(int i=0;i<seriesCount;i++){
					lsr.setSeriesShapesVisible(i, true);
					lsr.setSeriesItemLabelsVisible(i, true);
					lsr.setSeriesShape(i, ShapeUtilities.createDiamond(markersize));
			        lsr.setUseFillPaint(true);
			        lsr.setBaseFillPaint(databackgroundcolor);
				}
			}
		}
	}



	private void setAxis(JFreeChart chart) {
		Plot plot = chart.getPlot();
		if(plot instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot)plot;
			
			// Y
			cp.setDomainGridlinesVisible(showygridlines);
			if(showygridlines) cp.setDomainGridlinePaint(foregroundcolor);
			
			cp.setRangeGridlinesVisible(showxgridlines!=NO);
			if(showxgridlines==NONE)cp.setRangeGridlinePaint(Color.GRAY);
			else if(showxgridlines==YES)cp.setRangeGridlinePaint(foregroundcolor);
		}
		else if(plot instanceof XYPlot) {
			XYPlot cp=(XYPlot)plot;
			
			// Y
			cp.setDomainGridlinesVisible(showygridlines);
			if(showygridlines) cp.setDomainGridlinePaint(foregroundcolor);
			
			cp.setRangeGridlinesVisible(showxgridlines!=NO);
			if(showxgridlines==NONE)cp.setRangeGridlinePaint(Color.GRAY);
			else if(showxgridlines==YES)cp.setRangeGridlinePaint(foregroundcolor);
		}
	}



	private void setTooltip(JFreeChart chart) {
		Plot plot = chart.getPlot();
		if(plot instanceof PiePlot) {
			PiePlot pp = (PiePlot)plot;		
			
			pp.setToolTipGenerator(new PieToolTipGeneratorImpl(labelFormat));
			
		}
		else if(plot instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) plot;
			CategoryItemRenderer renderer = cp.getRenderer();
			renderer.setBaseToolTipGenerator(new CategoryToolTipGeneratorImpl(labelFormat));
		}
		/*else if(plot instanceof XYPlot) {
			XYPlot cp=(XYPlot) plot;
			XYItemRenderer renderer = cp.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGeneratorImpl(labelFormat));
		}*/
		
	}

	private void setUrl(JFreeChart chart) {
		if(StringUtil.isEmpty(url)) return;
		Plot plot = chart.getPlot();
		if(plot instanceof PiePlot) {
			PiePlot pp = (PiePlot)plot;		
			pp.setURLGenerator(new PieURLGenerator() {
			    public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
			    	if(!StringUtil.contains(url, "?")) url += "?series=$SERIESLABEL$&category=$ITEMLABEL$&value=$VALUE$";
			    	String retUrl=StringUtil.replace(url, "$ITEMLABEL$", URLUtilities.encode(key.toString(),"UTF-8"),false,true);
			    	retUrl = StringUtil.replace(retUrl,"$SERIESLABEL$",Integer.toString(pieIndex),false,true);
			    	retUrl = StringUtil.replace(retUrl,"$VALUE$",URLUtilities.encode(dataset.getValue(key).toString(),"UTF-8"),false,true);
			    	return retUrl;
			    }
			});
		}
		else if(plot instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) plot;
			CategoryItemRenderer renderer = cp.getRenderer();
			renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator() {
			    public String generateURL(CategoryDataset dataset, int series,int category) {
			    	if(!StringUtil.contains(url, "?")) url += "?series=$SERIESLABEL$&category=$ITEMLABEL$&value=$VALUE$";
			    	String retUrl=StringUtil.replace(url, "$ITEMLABEL$", URLUtilities.encode(dataset.getColumnKey(category).toString(),"UTF-8"),false,true);
			    	retUrl = StringUtil.replace(retUrl,"$SERIESLABEL$",URLUtilities.encode(dataset.getRowKey(series).toString(),"UTF-8"),false,true);
			    	retUrl = StringUtil.replace(retUrl,"$VALUE$",URLUtilities.encode(dataset.getValue(series, category).toString(),"UTF-8"),false,true);
			    	return retUrl;
			    }
			});
		}
		else if(plot instanceof XYPlot) {
			XYPlot cp=(XYPlot) plot;
			XYItemRenderer renderer = cp.getRenderer();
			renderer.setURLGenerator(new StandardXYURLGenerator() {
			    public String generateURL(XYDataset dataset, int series,int category) {
			    	if(!StringUtil.contains(url, "?")) url += "?series=$SERIESLABEL$&category=$ITEMLABEL$&value=$VALUE$";
			    	String itemLabel = _plotItemLables.get(category+1) != null ? _plotItemLables.get(category+1) : dataset.getX(series, category).toString();
			    	String retUrl=StringUtil.replace(url, "$ITEMLABEL$", URLUtilities.encode(itemLabel,"UTF-8"),false,true);
			    	retUrl = StringUtil.replace(retUrl,"$SERIESLABEL$",URLUtilities.encode(dataset.getSeriesKey(series).toString(),"UTF-8"),false,true);
			    	retUrl = StringUtil.replace(retUrl,"$VALUE$",URLUtilities.encode(dataset.getY(series, category).toString(),"UTF-8"),false,true);
			    	return retUrl;
			    }
			});
		}
		
	}
	


	private void setScale(JFreeChart chart) {
		Plot plot = chart.getPlot();
		if(plot instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) plot;
			ValueAxis rangeAxis = cp.getRangeAxis();
			Range r=rangeAxis.getRange();
			double lower=r.getLowerBound();
			double upper=r.getUpperBound();
			
			if(labelFormat==LabelFormatUtil.LABEL_FORMAT_DATE && rangeAxis.getRange().getLowerBound()==0) {
				lower = smallest;
				upper=biggest;
				try	{
					DateTime d = Caster.toDate(Caster.toDouble(lower),true,null,null);
					lower = DateAdd.call(pageContext,"yyyy", -1, d).castToDoubleValue(lower);	
				}
				catch (PageException e) {}
			}
			if(!Double.isNaN(scalefrom))lower=scalefrom;
			if(!Double.isNaN(scaleto))upper=scaleto;
			rangeAxis.setRange(new Range(lower,upper),true,true);
		}
		else if(plot instanceof XYPlot) {
			XYPlot cp=(XYPlot) plot;
			ValueAxis rangeAxis = cp.getRangeAxis();
			Range r=rangeAxis.getRange();
			double lower=r.getLowerBound();
			double upper=r.getUpperBound();
			
			if(labelFormat==LabelFormatUtil.LABEL_FORMAT_DATE && rangeAxis.getRange().getLowerBound()==0) {
				lower = smallest;
				upper=biggest;
				try	{
					DateTime d = Caster.toDate(Caster.toDouble(lower),true,null,null);
					lower = DateAdd.call(pageContext,"yyyy", -1, d).castToDoubleValue(lower);	
				}
				catch (PageException e) {}
			}
			if(!Double.isNaN(scalefrom))lower=scalefrom;
			if(!Double.isNaN(scaleto))upper=scaleto;
			rangeAxis.setRange(new Range(lower,upper),true,true);
		}
	}

	private void setLabelFormat(JFreeChart chart) {
		Plot plot = chart.getPlot();
		if(plot instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) plot;
			ValueAxis rangeAxis = cp.getRangeAxis();
			rangeAxis.setAutoTickUnitSelection(true);
			rangeAxis.setStandardTickUnits(new TickUnitsImpl(rangeAxis.getStandardTickUnits(),labelFormat));
			CategoryItemRenderer r = cp.getRenderer();
			r.setBaseItemLabelsVisible(false);
			
			CategoryAxis da = cp.getDomainAxis();
			if(!showXLabel)da.setTickLabelsVisible(false);
			da.setCategoryLabelPositions(labelPosition);
			da.setMaximumCategoryLabelWidthRatio(100);
			//da.setVisible(false);
		}
		if(plot instanceof XYPlot) {
			XYPlot cp=(XYPlot) plot;
			ValueAxis rangeAxis = cp.getRangeAxis();
			rangeAxis.setAutoTickUnitSelection(true);
			rangeAxis.setStandardTickUnits(new TickUnitsImpl(rangeAxis.getStandardTickUnits(),labelFormat));
			XYItemRenderer r = cp.getRenderer();
			r.setBaseItemLabelsVisible(false);
			ValueAxis da = cp.getDomainAxis();
			if(!_plotItemLables.isEmpty()){
				_plotItemLables.add(0, "");
				String[] cols = _plotItemLables.toArray(new String[_plotItemLables.size()]);
				SymbolAxis sa = new SymbolAxis(da.getLabel(), cols);
				sa.setRange(da.getRange());
				if(labelPosition == LABEL_VERTICAL) {
					sa.setVerticalTickLabels(true);
				}
				cp.setDomainAxis(sa);
			}
			if(!showXLabel)cp.getDomainAxis().setTickLabelsVisible(false);
			//da.setVisible(false);
		}
	}



	// set individual colors for series
	private void setColor(JFreeChart chart) {
		Plot p = chart.getPlot();
		if(p instanceof CategoryPlot) {
			CategoryPlot cp=(CategoryPlot) p;
			
			CategoryItemRenderer renderer = cp.getRenderer();
	        
			
			
			Iterator<ChartSeriesBean> cs = _series.iterator();
			//int seriesCount=_series.size();
			ChartSeriesBean csb;
			GradientPaint gp;
			Color c=null;
			Color[] ac;
			
			int index=0;
			while(cs.hasNext()) {
				csb= cs.next();
				// more than 1 series
				//if(seriesCount>1) {
					c=csb.getSeriesColor();
					if(c==null) {
						ac=csb.getColorlist();
						if(ac!=null && ac.length>0)c=ac[0];
					}
					
				//}
				if(c==null) continue;
				gp = new GradientPaint(0.0f, 0.0f, c, 0.0f, 0.0f,c);
				renderer.setSeriesPaint(index++, gp);
			}
		}
		else if(p instanceof XYPlot) {
			XYPlot cp=(XYPlot) p;
			
			XYItemRenderer renderer = cp.getRenderer();
	        
			
			
			Iterator<ChartSeriesBean> cs = _series.iterator();
			//int seriesCount=_series.size();
			ChartSeriesBean csb;
			GradientPaint gp;
			Color c=null;
			Color[] ac;
			
			int index=0;
			while(cs.hasNext()) {
				csb= cs.next();
				// more than 1 series
				//if(seriesCount>1) {
					c=csb.getSeriesColor();
					if(c==null) {
						ac=csb.getColorlist();
						if(ac!=null && ac.length>0)c=ac[0];
					}
					
				//}
				if(c==null) continue;
				gp = new GradientPaint(0.0f, 0.0f, c, 0.0f, 0.0f,c);
				renderer.setSeriesPaint(index++, gp);
			}
		}
	}



	private DefaultPieDataset createDatasetPie() {
		DefaultPieDataset dataset = new DefaultPieDataset();
		ChartSeriesBean csb =  _series.get(0);
        
		ChartDataBean cdb;
        // write data set
        Iterator itt = csb.getDatas().iterator();
    	while(itt.hasNext()) {
    		cdb=(ChartDataBean) itt.next();
    		dataset.setValue(cdb.getItemAsString(), cdb.getValue());
    	}	
    	return dataset;
    }
	

	
	

	private CategoryDataset createDatasetCategory() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Iterator<ChartSeriesBean> it = _series.iterator();
        //int seriesCount=_series.size();
        Iterator itt;
        List datas;
        ChartSeriesBean csb;
        ChartDataBean cdb;
        int count=0;
        smallest=Double.MAX_VALUE;
        biggest = Double.MIN_VALUE;
        String label;
        boolean hasLabels=false;
        while(it.hasNext()) {
        	count++;
        	csb= it.next();
        	label=csb.getSeriesLabel();
        	if(StringUtil.isEmpty(label))label=""+count;
        	else hasLabels=true;
        	datas = csb.getDatas();
        	if(sortxaxis)Collections.sort(datas);
            itt=datas.iterator();
            while(itt.hasNext()) {
        		cdb=(ChartDataBean) itt.next();
        		if(smallest>cdb.getValue())smallest=cdb.getValue();
        		if(biggest<cdb.getValue())biggest=cdb.getValue();
        		//if(seriesCount>1)
        		
        		dataset.addValue(cdb.getValue(), label,cdb.getItemAsString());
        		
        		//else dataset.addValue(cdb.getValue(), cdb.getItem(),"");
        		
            	
        	}
        }
        if(!hasLabels)showlegend=false;
        return dataset;
    }
	private XYDataset createTimeSeriesCollection() {
		TimeZone tz = ThreadLocalPageContext.getTimeZone();
		final TimeSeriesCollection coll=new TimeSeriesCollection(tz);
		
        //final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Iterator<ChartSeriesBean> it = _series.iterator();
        //int seriesCount=_series.size();
        Iterator itt;
        List datas;
        ChartSeriesBean csb;
        ChartDataBean cdb;
        int count=0;
        smallest=Double.MAX_VALUE;
        biggest = Double.MIN_VALUE;
        String label;
        boolean hasLabels=false;
        while(it.hasNext()) {
        	count++;
        	csb=it.next();
        	label=csb.getSeriesLabel();
        	if(StringUtil.isEmpty(label))label=""+count;
        	else hasLabels=true;
        	datas = csb.getDatas();
        	if(sortxaxis)Collections.sort(datas);
            itt=datas.iterator();
            TimeSeries ts=new TimeSeries(label,Second.class);
            while(itt.hasNext()) {
        		cdb=(ChartDataBean) itt.next();
        		if(smallest>cdb.getValue())smallest=cdb.getValue();
        		if(biggest<cdb.getValue())biggest=cdb.getValue();
        		//if(seriesCount>1)
        		ts.addOrUpdate(new Second(DateCaster.toDateSimple(cdb.getItem(),DateCaster.CONVERTING_TYPE_NONE,false, tz,null)), cdb.getValue());
        		
        		//else dataset.addValue(cdb.getValue(), cdb.getItem(),"");
        		
            	
        	}
            coll.addSeries(ts);
        }
        if(!hasLabels)showlegend=false;
        return coll;
    }
	private XYDataset createXYSeriesCollection() {
		final XYSeriesCollection coll=new XYSeriesCollection();
		Iterator<ChartSeriesBean> it = _series.iterator();
		Iterator itt;
		List datas;
		ChartSeriesBean csb;
		ChartDataBean cdb;
		int count=0;
		String label;
		boolean hasLabels=false;
		while(it.hasNext()) {
			count++;
			csb=it.next();
			label=csb.getSeriesLabel();
			if(StringUtil.isEmpty(label))label=""+count;
			else hasLabels=true;
			datas = csb.getDatas();
			if(sortxaxis)Collections.sort(datas);
			itt=datas.iterator();
			XYSeries xySeries=new XYSeries(label,false,true);
			int stepNum = 0;
			while(itt.hasNext()) {
				cdb=(ChartDataBean) itt.next();
//				if(cdb.getItem().toString().matches("-?\\d+(\\.\\d+)?")){
//					xySeries.add(Double.parseDouble(cdb.getItem().toString()),cdb.getValue());
//				} else {
					stepNum++;
					xySeries.add(stepNum,cdb.getValue());
//				}
				if(!_plotItemLables.contains(cdb.getItem().toString()))_plotItemLables.add(cdb.getItem().toString());
			}
			coll.addSeries(xySeries);
		}
		if(!hasLabels)showlegend=false;
		return coll;
	}
	
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param xoffset the xoffset to set
	 */
	public void setXoffset(double xoffset) {
		this.xoffset = xoffset;
	}

	/**
	 * @param yoffset the yoffset to set
	 */
	public void setYoffset(double yoffset) {
		this.yoffset = yoffset;
	}

	/**
	 * @param yaxistype the yaxistype to set
	 */
	public void setYaxistype(String yaxistype) {
		this.yaxistype = yaxistype;
	}
	/**
	 * @param yaxistype the yaxistype to set
	 */
	public void setXaxistype(String xaxistype) {
		this.xaxistype = xaxistype;
	}
	
}