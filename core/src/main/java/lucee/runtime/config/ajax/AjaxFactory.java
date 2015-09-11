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
package lucee.runtime.config.ajax;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.XMLConfigFactory;

public class AjaxFactory {

	private static final String TEMPLATE_EXTENSION = "cfm";
	private static final String COMPONENT_EXTENSION = "cfc";


/**
* this method deploy all ajax functions to the Lucee enviroment and the helper files
* @param dir tag directory
* @param doNew redeploy even the file exist, this is set to true when a new version is started
*/
public static void deployFunctions(Resource dir, boolean doNew) {
Resource f = dir.getRealResource("ajaxOnLoad."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/function/ajaxOnLoad."+TEMPLATE_EXTENSION,f);
        
}

/**
* this functions deploy all ajax tags to the Lucee enviroment and the helper files
* @param dir tag directory
* @param doNew redeploy even the file exist, this is set to true when a new version is started
*/
public static void deployTags(Resource dir, boolean doNew) {
// tags
        Resource f = dir.getRealResource("AjaxImport."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/AjaxImport."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("AjaxProxy."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/AjaxProxy."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("Div."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/Div."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("Map."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/Map."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("MapItem."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/MapItem."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("Layout."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/Layout."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("LayoutArea."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/LayoutArea."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("Window."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew){
        	//String md5 = ConfigWebUtil.createMD5FromResource(f);
        	XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/Window."+COMPONENT_EXTENSION,f);
        }
        
        
        
        
        // helper files
        dir=dir.getRealResource("lucee/core/ajax/");
        if(!dir.isDirectory())dir.mkdirs();
        f = dir.getRealResource("AjaxBase."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/AjaxBase."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("AjaxBinder."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/AjaxBinder."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("AjaxProxyHelper."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/AjaxProxyHelper."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("JSLoader."+COMPONENT_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/JSLoader."+COMPONENT_EXTENSION,f);
        f = dir.getRealResource("LuceeJs."+COMPONENT_EXTENSION);
        if(f.exists())f.delete();
        
        //js
        Resource jsDir = dir.getRealResource("js");
        if(!jsDir.isDirectory())jsDir.mkdirs();
        f = jsDir.getRealResource("LuceeAjax.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/LuceeAjax.js",f);
        f = jsDir.getRealResource("LuceeMap.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/LuceeMap.js",f);
        f = jsDir.getRealResource("LuceeWindow.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/LuceeWindow.js",f);
        f = jsDir.getRealResource("LuceeLayout.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/LuceeLayout.js",f);
        
        // delete wrong directory comes with 3.1.2.015
        Resource gDir = dir.getRealResource("google");
        if(gDir.isDirectory())ResourceUtil.removeEL(gDir, true);
        
        // create google/... again
        gDir = jsDir.getRealResource("google");
        if(!gDir.isDirectory())gDir.mkdirs();
        f = gDir.getRealResource("google-map.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/google/google-map.js",f);
        
        
        //jquery resources
        Resource jqDir = jsDir.getRealResource("jquery");
        if(!jqDir.isDirectory())jqDir.mkdirs();
        f = jqDir.getRealResource("jquery-1.4.2.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/jquery/jquery-1.4.2.js",f);
        f = jqDir.getRealResource("jquery-ui-1.8.2.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/jquery/jquery-ui-1.8.2.js",f);
        f = jqDir.getRealResource("jquery.layout.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/jquery/jquery.layout.js",f);
        f = jqDir.getRealResource("jquery.window.js");
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/js/jquery/jquery.window.js",f);
  
        //css Skin
        Resource cssDir = dir.getRealResource("css/jquery");
        if(!cssDir.isDirectory())cssDir.mkdirs();
        f = cssDir.getRealResource("LuceeSkin.css."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/LuceeSkin.css."+TEMPLATE_EXTENSION,f);
        
        //css images
        Resource imgDir = cssDir.getRealResource("images");
        if(!imgDir.isDirectory())imgDir.mkdirs();
        f = imgDir.getRealResource("ui-anim_basic_16x16.gif."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-anim_basic_16x16.gif."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_flat_0_aaaaaa_40x100.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_flat_0_aaaaaa_40x100.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_flat_75_ffffff_40x100.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_flat_75_ffffff_40x100.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_glass_55_fbf9ee_1x400.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_glass_55_fbf9ee_1x400.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_glass_65_ffffff_1x400.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_glass_65_ffffff_1x400.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_glass_75_dadada_1x400.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_glass_75_dadada_1x400.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_glass_75_e6e6e6_1x400.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_glass_75_e6e6e6_1x400.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_glass_95_fef1ec_1x400.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_glass_95_fef1ec_1x400.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-bg_highlight-soft_75_cccccc_1x100.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-bg_highlight-soft_75_cccccc_1x100.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-icons_222222_256x240.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-icons_222222_256x240.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-icons_2e83ff_256x240.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-icons_2e83ff_256x240.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-icons_454545_256x240.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-icons_454545_256x240.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-icons_888888_256x240.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-icons_888888_256x240.png."+TEMPLATE_EXTENSION,f);
        f = imgDir.getRealResource("ui-icons_cd0a0a_256x240.png."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/css/jquery/images/ui-icons_cd0a0a_256x240.png."+TEMPLATE_EXTENSION,f);
       
        
        //image loader
        dir = dir.getRealResource("loader");
        if(!dir.isDirectory())dir.mkdirs();
        f = dir.getRealResource("loading.gif."+TEMPLATE_EXTENSION);
        if(!f.exists() || doNew)XMLConfigFactory.createFileFromResourceEL("/resource/library/tag/lucee/core/ajax/loader/loading.gif."+TEMPLATE_EXTENSION,f);
}

}