package com.itheima.webviewdemo;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

  public  EditText etPath;
  public  ProgressBar pbProgress;
  public  WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        etPath = (EditText) findViewById(R.id.et_path);
        pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
        webview = (WebView) findViewById(R.id.webview);
        //设置默认的文本编码方式为utf-8
        webview.getSettings().setDefaultTextEncodingName("utf-8");
        //允许执行js代码
        webview.getSettings().setJavaScriptEnabled(true);
//        设置缩放级别
//        这个方法过时了
        webview.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        //
        webview.getSettings().setLoadWithOverviewMode(true);


//        避免加载本地html和本地html加载网页图片同时进行
//        if(Build.VERSION.SDK_INT >= 19) {
//            webview.getSettings().setLoadsImagesAutomatically(true);
//        } else {
//            webview.getSettings().setLoadsImagesAutomatically(false);
//        }
        /*
        WebViewClient就是帮助WebView处理各种通知、请求事件的
         */
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //页面跳转是否在此webview中进行
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //这个方法是页面开始加载的时候调用
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //页面加载完成后调用的方法

//                避免加载本地html和本地html加载网页图片同时进行
//                if (!view.getSettings().getLoadsImagesAutomatically()) {
//                    view.getSettings().setLoadsImagesAutomatically(true);
//                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //通过这里我们可以得到错误码，然后自定义错误的显示界面
                super.onReceivedError(view, request, error);
            }
        });

        /**
         * WebChromeClient主要处理解析，渲染网页等浏览器做的事情
         */
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                //收到标题
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //网页的加载进度
                //显示progre 控件
                pbProgress.setVisibility(View.VISIBLE);
                //设置最大进度
                pbProgress.setMax(100);
                //设置当前的进度
                pbProgress.setProgress(newProgress);
                if (newProgress == 100) {
                    //隐藏进度条
                    pbProgress.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                //监听js的弹框
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                //监听js的日志信息
                //js里面有个console对象，里面有输出日志的方法，相当于android里面的log，console.log("some log");
                return super.onConsoleMessage(consoleMessage);
            }
        });

        //js和android交互
        webview.addJavascriptInterface(new TestJavaScriptInterface(),"Android");

    }

    /**
     * 首次进入页面的时候webview加载网页能成功，但是以后每次进入该页面的时候，webview加载的进度总是卡在９９
     */
    public void setLayerType() {
        if (Build.VERSION.SDK_INT >= 11) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * 跨域问题
     * 本地html,在通过ajax做网络请求的时候获取不到数据报错提示跨域问题的解决方案
     * setAllowUniversalAccessFromFileURLs这个方法没有对外暴露
     */
    public void setCrossDomain() {
        try {
            if (Build.VERSION.SDK_INT >= 16) {
                Class<?> clazz = webview.getSettings().getClass();
                Method method = clazz.getMethod(
                        "setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(webview.getSettings(), true);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启允许本地存储
     */
    public void setLocalStorage() {
        webview.getSettings().setDomStorageEnabled(true);
    }

    /**
     * 移除android系统本身的风险接口
     * 系统本身提供的部分接口存在漏洞，可能会被黑客编写的恶意程序利用
     */
    public void setWebviewSecurityConfig() {
        webview.removeJavascriptInterface("searchBoxJavaBridge_");
        webview.removeJavascriptInterface("accessibility");
        webview.removeJavascriptInterface("accessibilityTraversal");
    }

    @OnClick({R.id.btn_AndroidLoadJSMethod,R.id.btn_JSLoadAndroidMethod, R.id.btn_loadurl})
    public void onClick(View v) {
        switch (v.getId()) {
            //android调用js的方法
            case R.id.btn_AndroidLoadJSMethod:
                webview.loadUrl("javascript:changeInputValue('哈哈 js 您好')");
                break;
            //js调用android这边的方法
            case R.id.btn_JSLoadAndroidMethod:
                webview.loadUrl("file:///android_asset/test.html");
                break;
            case R.id.btn_loadurl:
                //加载网页
                String path = etPath.getText().toString().trim();
                webview.loadUrl(path);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // 这里是后退，还有前进forward
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 测试js和android交互的类
     */
    class TestJavaScriptInterface{
        /**
         * @JavascriptInterface 这个注解在4.2及以后要加在方法上
         *　在ｊｓ里面点击一个按钮，android这边将js传过来的内容以土司的形式弹出
         */
        @JavascriptInterface
        public void showToast(String content){
            Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
        }
    }
}
