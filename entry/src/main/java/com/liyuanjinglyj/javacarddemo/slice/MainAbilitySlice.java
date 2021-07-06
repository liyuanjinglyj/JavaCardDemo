package com.liyuanjinglyj.javacarddemo.slice;

import com.liyuanjinglyj.javacarddemo.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.webengine.*;
import ohos.agp.utils.TextTool;
import ohos.agp.window.dialog.ToastDialog;
import ohos.global.resource.Resource;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class MainAbilitySlice extends AbilitySlice {
    HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "TAG");
    private WebView webView;
    private Button button;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        this.webView=(WebView)findComponentById(ResourceTable.Id_ability_main_webview);
        this.button=(Button)findComponentById(ResourceTable.Id_ability_main_button);
        this.webView.setWebAgent(new WebAgent(){
            @Override
            public boolean isNeedLoadUrl(WebView webView, ResourceRequest request) {
                return super.isNeedLoadUrl(webView, request);
            }

            @Override
            public ResourceResponse processResourceRequest(WebView webView, ResourceRequest request) {
                final String authority = "example.com";
                final String rawFile = "/rawfile/";
                final String local = "/local/";
                Uri requestUri = request.getRequestUrl();
                if (authority.equals(requestUri.getDecodedAuthority())) {
                    String path = requestUri.getDecodedPath();
                    if (TextTool.isNullOrEmpty(path)) {
                        return super.processResourceRequest(webView, request);
                    }
                    if (path.startsWith(rawFile)) {
                        // 根据自定义规则访问资源文件
                        String rawFilePath = "entry/resources/rawfile/" + path.replace(rawFile, "");
                        String mimeType = URLConnection.guessContentTypeFromName(rawFilePath);
                        try {
                            Resource resource = getResourceManager().getRawFileEntry(rawFilePath).openRawFile();
                            ResourceResponse response = new ResourceResponse(mimeType, resource, null);
                            return response;
                        } catch (IOException e) {
                            HiLog.info(TAG, "open raw file failed");
                        }
                    }
                    if (path.startsWith(local)) {
                        // 根据自定义规则访问本地文件
                        String localFile = getContext().getFilesDir() + path.replace(local, "/");
                        HiLog.info(TAG, "open local file " + localFile);
                        File file = new File(localFile);
                        if (!file.exists()) {
                            HiLog.info(TAG, "file not exists");
                            return super.processResourceRequest(webView, request);
                        }
                        String mimeType = URLConnection.guessContentTypeFromName(localFile);
                        try {
                            InputStream inputStream = new FileInputStream(file);
                            ResourceResponse response = new ResourceResponse(mimeType, inputStream, null);
                            return response;
                        } catch (IOException e) {
                            HiLog.info(TAG, "open local file failed");
                        }
                    }
                }
                return super.processResourceRequest(webView, request);
            }
        });
        this.webView.getWebConfig().setJavaScriptPermit(true);
        this.webView.load("https://example.com/rawfile/example.html");
        this.button.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                webView.executeJs("javascript:aAddb(5,5)", new AsyncCallback<String>() {
                    @Override
                    public void onReceive(String msg) {
                        new ToastDialog(getContext())
                                .setText("a+b="+msg)
                                .show();
                    }
                });
            }
        });
        final String jsName = "JsCallbackToApp";
        webView.addJsCallback(jsName, new JsCallback() {
            @Override
            public String onCallback(String msg) {
                new ToastDialog(getContext())
                        .setText(msg)
                        .show();
                return "jsResult";
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
