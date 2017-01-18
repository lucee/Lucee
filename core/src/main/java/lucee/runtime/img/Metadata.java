/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lucee.runtime.img;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.IImageMetadata.IImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata.Item;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;



public class Metadata {
	
	public static void main(String[] args) {
		Resource res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/test/functions/images/BigBen.jpg");
		Struct info=new StructImpl(); 
		addInfo("jpg", res, info);
	}
	
	public static void addExifInfo(String format, final Resource res, Struct info) {
    	InputStream is=null;
    	try {
    		is=res.getInputStream();
    		fillExif(format, is,info);
    	}
    	catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
    	finally {
    		IOUtil.closeEL(is);
    	}
    }
	
	private static void fillExif(String format, InputStream is, Struct info) throws ImageReadException, IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
         IImageMetadata metadata = Imaging.getMetadata(is,"test."+format);
         if(metadata==null) return;
        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            
            
            // EXIF
            if(jpegMetadata!=null) {
            	try{set(jpegMetadata.getExif().getItems(),info,null);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
            }
            // GPS
            try{gps(jpegMetadata,info);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
        }
    }
	
    public static void addInfo(String format, final Resource res, Struct info) {
    	InputStream is=null;
    	try {
    		is=res.getInputStream();
    		fill(format, is,info);
    	}
    	catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
    	finally {
    		IOUtil.closeEL(is);
    	}
    }

    private static void fill(String format, InputStream is, Struct info) throws ImageReadException, IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
         IImageMetadata metadata = Imaging.getMetadata(is,"test."+format);
         if(metadata==null) return;

        // System.out.println(metadata);

        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            
            try{set(jpegMetadata.getItems(),info,null);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
            try{set(metadata.getItems(),info,null);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
            
            // Photoshop
            if(metadata instanceof JpegImageMetadata) {
            	JpegPhotoshopMetadata photoshop = ((JpegImageMetadata) metadata).getPhotoshop();
            	if(photoshop!=null) {
	            	try{
	            		
		            	List<? extends IImageMetadataItem> list = photoshop.getItems();
		            	if(list!=null && !list.isEmpty()) {
		            		Struct ps=new StructImpl();
		            		info.setEL("photoshop", ps);
		            		try{set(list,ps,null);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		            	}
	            	}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
            	}
            }
            
            // EXIF
            if(jpegMetadata!=null) {
            	Struct exif=new StructImpl();
            	info.setEL("exif", exif);
            	try{set(jpegMetadata.getExif().getItems(),exif,null);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
            }
            // GPS
            try{gps(jpegMetadata,info);}catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}

        }
    }

	private static void gps(JpegImageMetadata jpegMetadata, Struct info) throws ImageReadException {
		Struct gps=new StructImpl();
		info.setEL("gps", gps);
		info=gps;
		final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
        Double longitude = null;
        Double latitude = null;
        if (null != exifMetadata) {
            final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
            if (null != gpsInfo) {
                //final String gpsDescription = gpsInfo.toString();
                longitude = gpsInfo.getLongitudeAsDegreesEast();
                latitude = gpsInfo.getLatitudeAsDegreesNorth();

            }
        }

        // more specific example of how to manually access GPS values
        final TiffField gpsLatitudeRefField = jpegMetadata.findEXIFValueWithExactMatch(
                GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
        final TiffField gpsLatitudeField = jpegMetadata.findEXIFValueWithExactMatch(
                GpsTagConstants.GPS_TAG_GPS_LATITUDE);
        final TiffField gpsLongitudeRefField = jpegMetadata.findEXIFValueWithExactMatch(
                GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
        final TiffField gpsLongitudeField = jpegMetadata.findEXIFValueWithExactMatch(
                GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
        if (gpsLatitudeRefField != null && gpsLatitudeField != null && gpsLongitudeRefField != null && gpsLongitudeField != null) {
            // all of these values are strings.
            final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
            final RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField.getValue());
            final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
            final RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField.getValue();

            info.setEL("GPS Latitude",
            		gpsLatitude[0].toDisplayString() + "\""
                    + gpsLatitude[1].toDisplayString() + "'"
                    + gpsLatitude[2].toDisplayString());
            
            info.setEL("GPS Latitude Ref", gpsLatitudeRef);
            Struct sct=new StructImpl();
            gps.setEL("latitude", sct);
            sct.setEL("degrees",gpsLatitude[0].doubleValue());
            sct.setEL("minutes",gpsLatitude[1].doubleValue());
            sct.setEL("seconds",gpsLatitude[2].doubleValue());
            sct.setEL("ref",gpsLatitudeRef);
            sct.setEL("decimal",latitude);

            info.setEL("GPS Longitude",
            		gpsLongitude[0].toDisplayString() + "\""
                    + gpsLongitude[1].toDisplayString() + "'"
                    + gpsLongitude[2].toDisplayString());
            info.setEL("GPS Longitude Ref", gpsLongitudeRef);
            sct=new StructImpl();
            gps.setEL("longitude", sct);
            sct.setEL("degrees",gpsLongitude[0].doubleValue());
            sct.setEL("minutes",gpsLongitude[1].doubleValue());
            sct.setEL("seconds",gpsLongitude[2].doubleValue());
            sct.setEL("ref",gpsLongitudeRef);
            sct.setEL("decimal",longitude);
        }
	}

	private static void set(Struct sct1, Struct sct2, String name1, String name2, Object value) {
		sct1.setEL(name1, value);
		sct2.setEL(name2, value);
	}

	private static Object val(Object value) {
    	if(value==null) return null;
    	if(value instanceof CharSequence) return value.toString();
    	if(value instanceof Number) return ((Number)value).doubleValue();
    	if(Decision.isNativeArray(value) && !(value instanceof Object[])) return value;
    	if(value instanceof Object[]) {
    		Array trg=new ArrayImpl();
    		Object[] arr=(Object[]) value;
    		for(Object obj : arr) {
    			trg.appendEL(val(obj));
    		}
    		return trg;
    	}
    	if(value instanceof RationalNumber) {
    		RationalNumber rn=(RationalNumber) value;
    		return rn.toDisplayString();
    	}
    	return value;
	}

	private static void set(List<? extends IImageMetadataItem> items, Struct data1, Struct data2) {
    	Iterator<? extends IImageMetadataItem> it = items.iterator();
        Item item;
        while(it.hasNext()) {
        	item=(Item) it.next();
        	
        	data1.setEL(item.getKeyword(),item.getText());
        	if(data2!=null)data2.setEL(item.getKeyword(),item.getText());
        }
	}

	private static void set(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo, Struct info) throws ImageReadException {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field != null) {
            if(!info.containsKey(tagInfo.name)){
            	Object val = val(field.getValue());
            	if(val!=null)info.setEL(tagInfo.name, val);
            }
        }
    }

}
