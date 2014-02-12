/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
import org.apache.cordova.LOG;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class IceCreamCordovaWebViewClient extends CordovaWebViewClient {

    private static final String TAG = "IceCreamCordovaWebViewClient";

    public IceCreamCordovaWebViewClient(CordovaInterface cordova) {
        super(cordova);
    }
    
    public IceCreamCordovaWebViewClient(CordovaInterface cordova, CordovaWebView view) {
        super(cordova, view);
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url){
        if(url.endsWith(".mp3")){
//http://stackoverflow.com/questions/10966245/android-playing-an-asset-sound-using-webview
//        	 url = url.replace("file:///android_asset/webpages/", "");
//             Log.i("MyWebViewClient", url);
//             try {
//                 AssetFileDescriptor afd = context.getAssets().openFd(url);
//                 mp = new MediaPlayer();
//                 mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                 afd.close();
//                 mp.prepare();
//                 mp.start();
//             } catch (IllegalArgumentException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             } catch (IllegalStateException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             } catch (IOException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             }
             
        	Log.v("DebuggingAudio", " this is an audio file!"+url);
            return super.shouldOverrideUrlLoading(view, url);
        }else{
        	Log.v("DebuggingAudio", " this is NOT audio file!"+url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
    
    
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    	Log.v(TAG, " shouldInterceptRequest!"+url);

        try {
            // Check the against the white-list.
            if ((url.startsWith("http:") || url.startsWith("https:")) && !Config.isUrlWhiteListed(url)) {
                LOG.w(TAG, "URL blocked by whitelist: " + url);
                // Results in a 404.
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }

            CordovaResourceApi resourceApi = appView.getResourceApi();
            Uri origUri = Uri.parse(url);
            // Allow plugins to intercept WebView requests.
            Uri remappedUri = resourceApi.remapUri(origUri);
            
            if (!origUri.equals(remappedUri) || needsSpecialsInAssetUrlFix(origUri)) {
                OpenForReadResult result = resourceApi.openForRead(remappedUri, true);
                return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);
            }
            // If we don't need to special-case the request, let the browser load it.
            return null;
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                LOG.e("IceCreamCordovaWebViewClient", "Error occurred while loading a file (returning a 404).", e);
            }
            // Results in a 404.
            return new WebResourceResponse("text/plain", "UTF-8", null);
        }
    }

    private static boolean needsSpecialsInAssetUrlFix(Uri uri) {
    	Log.v(TAG, " needsSpecialsInAssetUrlFix!"+uri);

        if (CordovaResourceApi.getUriType(uri) != CordovaResourceApi.URI_TYPE_ASSET) {
            return false;
        }
        if (uri.getQuery() != null || uri.getFragment() != null) {
            return true;
        }
        
        if (!uri.toString().contains("%")) {
            return false;
        }

        switch(android.os.Build.VERSION.SDK_INT){
            case android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH:
            case android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1:
                return true;
        }
        return false;
    }
}
