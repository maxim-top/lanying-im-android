
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.view.Header;

public class ProtocolActivity extends BaseTitleActivity {

    private WebView mWebView;

    public int mProtocolType;

    public static final String PROTOCOL_TYPE = "protocolType";

    public static void openProtocol(Context context, int type) {
        Intent intent = new Intent(context, ProtocolActivity.class);
        intent.putExtra(PROTOCOL_TYPE, type);
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
        mHeader.setTitle(mProtocolType == 0 ? R.string.register_protocol4
                : R.string.register_protocol2);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mProtocolType = intent.getIntExtra(PROTOCOL_TYPE, 0);
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

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                String js = "var meta = document.createElement('meta'); meta.setAttribute('name', 'viewport'); meta.setAttribute('content', 'width=device-width'); document.getElementsByTagName('head')[0].appendChild(meta)";
                mWebView.loadUrl("javascript:" + js);
                super.onPageFinished(view, url);
                dismissLoadingDialog();
            }
        });

        showLoadingDialog(true);
        String protocol_privacy_url = getString(R.string.protocol_privacy);
        String protocol_privacy_terms = getString(R.string.protocol_terms);
        mWebView.loadUrl(mProtocolType == 0 ? protocol_privacy_url
                : protocol_privacy_terms);
    }
}
