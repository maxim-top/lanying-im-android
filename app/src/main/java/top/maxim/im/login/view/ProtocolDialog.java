
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.dialog.BaseDialog;

/**
 * Description : 自定义daolog Created by Mango on 2018/11/11.
 */
public class ProtocolDialog extends BaseDialog implements View.OnClickListener {

    /* 弹出框标题 */
    private TextView mTvTitle;

    /* 确定 */
    private TextView mTvConfirm;

    private OnDialogListener mOnDialogListener;

    private WebView webView;

    @Override
    protected View onCreateDialogView() {
        initWebView();
        View view = View.inflate(getActivity(), R.layout.dialog_protocol_view, null);
        FrameLayout container = view.findViewById(R.id.fl_dialog_container);
        if (webView != null) {
            container.addView(webView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.widthPixels));
        }
        mTvTitle = view.findViewById(R.id.tv_dialog_title);
        mTvConfirm = view.findViewById(R.id.tv_dialog_confirm);
        mTvConfirm.setOnClickListener(this);
        return view;
    }

    @Override
    protected void init() {
        super.init();
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void initWebView() {
        webView = new WebView(getActivity());
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        WebSettings settings = webView.getSettings();
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

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                String js = "var meta = document.createElement('meta'); meta.setAttribute('name', 'viewport'); meta.setAttribute('content', 'width=device-width'); document.getElementsByTagName('head')[0].appendChild(meta)";
                webView.loadUrl("javascript:" + js);
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(CommonConfig.PROTOCOL_URL);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindView();
    }

    private void bindView() {
        mTvTitle.setText(getString(R.string.register_protocol2));
        mTvConfirm.setText("同意并继续");
    }

    public void setDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_dialog_confirm:
                dismiss();
                if (mOnDialogListener != null) {
                    mOnDialogListener.onConfirmListener();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 展示dialog
     */
    public void showDialog(Activity activity) {
        show(activity.getFragmentManager(), "CommonDialog");
    }

    /**
     * 确定 取消点击
     */
    public interface OnDialogListener {

        /**
         * 确定
         */
        void onConfirmListener();

        /**
         * 取消
         */
        void onCancelListener();
    }
}
