
package top.maxim.im.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.view.Header;

public class WebViewActivity extends BaseTitleActivity {

    private WebView mWebView;

    public String mUrl;

    public static final String URL = "url";

    private Map<String, String> mUrlTitleMap = new HashMap<>();

    public static void openUrl(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(URL, url);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle("");
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_protocol, null);
        mWebView = view.findViewById(R.id.webview);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mUrl = intent.getStringExtra(URL);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        bindHtml();
    }

    private void bindHtml() {
        if (mWebView == null) {
            return;
        }
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setVerticalScrollBarEnabled(false);
        WebSettings settings = mWebView.getSettings();
        if (settings != null) {
            settings.setDefaultTextEncodingName("UTF-8");
            settings.setJavaScriptEnabled(true);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setUseWideViewPort(true);
            settings.setDisplayZoomControls(false);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            settings.setLoadWithOverviewMode(true);
            settings.setDefaultFontSize(18);
        }

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                String currentUrl = view.getUrl();
                if (currentUrl != null && title != null) {
                    // 记录或更新当前URL对应的标题
                    mUrlTitleMap.put(currentUrl, title);
                    // 更新界面标题
                    mHeader.setTitle(title);
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String js = "var meta = document.createElement('meta'); meta.setAttribute('name', 'viewport'); meta.setAttribute('content', 'width=device-width'); document.getElementsByTagName('head')[0].appendChild(meta)";
                mWebView.loadUrl("javascript:" + js);
                // 确保使用最终正确的标题
                if (mUrlTitleMap.containsKey(url)) {
                    mHeader.setTitle(mUrlTitleMap.get(url));
                }
                dismissLoadingDialog();
            }
        });

        showLoadingDialog(true);
        mWebView.loadUrl(mUrl);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
