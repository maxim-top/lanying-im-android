
package top.maxim.im.message.itemholder;

import android.content.Context;
import androidx.annotation.NonNull;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import io.noties.markwon.Markwon;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import top.maxim.im.R;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.utils.ChatAttachmentManager;
import top.maxim.im.message.utils.MessageEvent;

/**
 * Description : 消息文本类型 Created by Mango on 2018/11/18.
 */
public class MessageItemText extends MessageItemBaseView {

    private TextView mChatText;

    private char[] mText;
    private Handler mHandler = new Handler();
    private int mIndex = 0;
    private int mDelay = 40; // 延迟的时间间隔（毫秒）
    private int mStep = 1; //打字机步长，每次增加字符数
    private boolean mIsTypeWriter = false; //是否开启打字机效果

    private Runnable mTypingRunnable = new Runnable() {
        @Override
        public void run() {
            // 获取要显示的字符
            if (mIndex < mText.length) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(mText, 0, mIndex);
                stringBuffer.append("⚫");
                showAsMarkdown(stringBuffer.toString());
                // 增加索引以显示下一个字符
                mIndex+=mStep;
                EventBus.getDefault().post(new MessageEvent("scroll-to-bottom"));
                // 在延迟的时间间隔后再次调用该方法
                mHandler.postDelayed(this, mDelay);
            }else{
                showAsMarkdown(String.valueOf(mText));
                mMaxMessage.setExtension("{\"typeWriter\":0}");
                EventBus.getDefault().post(new MessageEvent("scroll-to-bottom"));
            }
        }
    };

    public void startTypingAnimation(String text) {
        mText = text.toCharArray();
        int duration = mText.length*mDelay;
        if (duration > 20000){//耗时超过20秒时，保证在20秒内完成动画
            mStep *= duration/20000;
        }
        mIndex = 0;
        mHandler.removeCallbacks(mTypingRunnable);
        mHandler.postDelayed(mTypingRunnable, mDelay);
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        if (mMaxMessage != null) {
            Log.d("TYPEWRITER", "msgId:"+mMaxMessage.msgId() + mMaxMessage.content());
            mHandler.removeCallbacks(mTypingRunnable);
        }
    }

    public MessageItemText(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (mItemPos == ITEM_LEFT) {
            view = View.inflate(mContext, R.layout.item_chat_text_left, parent);
        } else {
            view = View.inflate(mContext, R.layout.item_chat_text_right, parent);
        }
        mChatText = ((TextView)view.findViewById(R.id.txt_message));
        return view;
    }

    @Override
    protected void bindData() {
        fillView();
    }

    /**
     * 填充数据
     */
    private void fillView() {
        setItemViewListener(mChatText);
        showText();
    }

    private void showAsMarkdown(String content){
        Markwon markwon = Markwon.builder(mContext)
                .usePlugin(GlideImagesPlugin.create(mContext))
                .usePlugin(GlideImagesPlugin.create(Glide.with(mContext)))
                .usePlugin(GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                    @NonNull
                    @Override
                    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
                        return Glide.with(mContext).load(drawable.getDestination());
                    }

                    @Override
                    public void cancel(@NonNull Target<?> target) {
                        Glide.with(mContext).clear(target);
                    }
                })).build();
        markwon.setMarkdown(mChatText, content);
    }
    /**
     * 展示文本数据
     */
    private void showText() {
        if (mMaxMessage == null) {
            mChatText.setText("");
            return;
        }
        if (mMaxMessage.contentType() != BMXMessage.ContentType.Text && mMaxMessage.contentType() != BMXMessage.ContentType.RTC) {
            // 非文本转为无法识别
            mChatText.setText(mContext.getString(R.string.unknown_message));
            return;
        }
        mIsTypeWriter = false;

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mMaxMessage.extension());
            if(jsonObject.has("typeWriter") && jsonObject.getInt("typeWriter") == 1){
                mIsTypeWriter = true;
                mMaxMessage.setExtension("{\"typeWriter\":2}");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String content = TextUtils.isEmpty(mMaxMessage.content()) ? "" : mMaxMessage.content();
        BMXMessageConfig config = mMaxMessage.config();
        if (config != null){
            String action = config.getRTCAction();
            if (action != null){
                if (action.equals("hangup")){
                    if (content.equals("rejected")){
                        if (mMaxMessage.isReceiveMsg()){
                            content = getResources().getString(R.string.call_be_declined);
                        } else {
                            content = getResources().getString(R.string.call_declined);
                        }
                    } else if (content.equals("canceled")){
                        if (mMaxMessage.isReceiveMsg()){
                            content = getResources().getString(R.string.call_be_canceled);
                        } else {
                            content = getResources().getString(R.string.call_canceled);
                        }
                    } else if (content.equals("timeout")){
                        if (mMaxMessage.isReceiveMsg()){
                            content = getResources().getString(R.string.call_not_responding);
                        } else {
                            content = getResources().getString(R.string.callee_not_responding);
                        }
                    } else if (content.equals("busy")){
                        if (mMaxMessage.isReceiveMsg()){
                            content = getResources().getString(R.string.callee_busy);
                        } else {
                            content = getResources().getString(R.string.call_busy);
                        }
                    } else {
                        try {
                            long sec = Long.valueOf(content)/1000;
                            content = String.format("通话时长：%02d:%02d",sec/60, sec%60);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                mIsTypeWriter = false;
            }
        }
        if (mIsTypeWriter){
            startTypingAnimation(content);
        }else{
            showAsMarkdown(content);
        }
    }
}
