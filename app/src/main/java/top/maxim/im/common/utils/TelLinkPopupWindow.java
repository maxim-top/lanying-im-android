
package top.maxim.im.common.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import top.maxim.im.R;

/**
 * Description : 电话超链接点击弹出框 Created by mango
 */
public class TelLinkPopupWindow extends BaseSlidingPopWindow implements View.OnClickListener {

    private TextView mCall;

    private String mTelURL;

    public TelLinkPopupWindow(Context context) {
        super(context);
        // setAnimationStyle(R.style.PopupAnimation);
    }

    @Override
    public View getPopWindowContentView() {
        View view = View.inflate(getContext(), R.layout.tel_link_popwindow, null);
        mCall = view.findViewById(R.id.btn_call);
        TextView cancel = view.findViewById(R.id.btn_cancel);

        mCall.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return view;
    }

    @Override
    protected boolean haveAnim() {
        return false;
    }

    @Override
    protected Drawable getBackgroundDrawable() {
        return new ColorDrawable(Color.parseColor("#00000000"));
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        if (v.getId() == R.id.btn_call) {
            Uri uri = Uri.parse("tel:" + mTelURL);
            Intent i = new Intent(Intent.ACTION_DIAL, uri);
            context.startActivity(i);
        }
        dismiss();
    }

    public void showAsDropDown(String url, View anchor) {
        mTelURL = url;
        String tel = url.replace("tel:", "");
        String desc = getContext().getString(R.string.call_colon) + tel;
        mCall.setText(desc);
        showAtLocation(anchor, Gravity.BOTTOM | Gravity.CLIP_HORIZONTAL, 0, 0);
    }
}
