
package top.maxim.im.message.itemholder;

import static top.maxim.im.common.utils.AppContextUtils.getApplication;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.LinkResolverDef;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.common.base.MaxIMApplication;
import top.maxim.im.message.interfaces.ChatActionListener;
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
    private int mWaitTimes = 0; //等待后续部分的次数
    public static final int MAX_WAIT_TIMES = 500;
    private void clearTypeWriterMsgId(BMXMessage message){
        if (message.msgId() == ((MaxIMApplication) getApplication()).typeWriterMsgId){
            ((MaxIMApplication) getApplication()).typeWriterMsgId = 0;
        }
    }

    private Runnable mTypingRunnable = new Runnable() {
        @Override
        public void run() {
            // 获取要显示的字符
            if (mIndex <= mText.length) {
                if(mWaitTimes < MAX_WAIT_TIMES){
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
                    clearTypeWriterMsgId(mMaxMessage);
                    EventBus.getDefault().post(new MessageEvent("scroll-to-bottom"));
                }
            }else{
                BMXDataCallBack<BMXMessage> callBack = new BMXDataCallBack<BMXMessage>() {
                    @Override
                    public void onResult(BMXErrorCode code, BMXMessage data) {
                        mMaxMessage = data;
                        mText = mMaxMessage.content().toCharArray();

                        if((mText.length-mIndex)<mStep && mText.length!=mIndex){
                            mIndex = mText.length;
                        }else{
                            mWaitTimes ++;
                        }
                        Boolean finish = false;
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(mMaxMessage.extension());
                            finish = jsonObject.getJSONObject("ai").getBoolean("finish");
                            if(finish){
                                showAsMarkdown(String.valueOf(mText));
                                clearTypeWriterMsgId(mMaxMessage);
                                EventBus.getDefault().post(new MessageEvent("scroll-to-bottom"));
                            }
                        } catch (JSONException e) {
                            finish = true;
                            showAsMarkdown(String.valueOf(mText));
                            clearTypeWriterMsgId(mMaxMessage);
                            EventBus.getDefault().post(new MessageEvent("scroll-to-bottom"));
                        }
                        if(!finish){
                            mHandler.postDelayed(mTypingRunnable, mDelay);
                        }
                    }
                };
                ChatManager.getInstance().getMessage(mMaxMessage.msgId(),callBack);
            }
        }
    };

    public void startTypingAnimation() {
        int duration = mText.length*mDelay;
        if (duration > 20000){//耗时超过20秒时，保证在20秒内完成动画
            mStep *= duration/20000;
        }
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

    private RequestManager requestManagerByGlide(){
        if (mContext instanceof Activity){
            Activity activity = (Activity) mContext;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
                return null;
            }
        }
        return Glide.with(mContext);
    }

    private void showAsMarkdown(String content){
        try{
            GlideImagesPlugin plugin1 = GlideImagesPlugin.create(mContext);
            GlideImagesPlugin plugin2 = GlideImagesPlugin.create(requestManagerByGlide());
            GlideImagesPlugin plugin3 = GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                @NonNull
                @Override
                public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
                    RequestManager requestManager = requestManagerByGlide();
                    if (requestManager!=null){
                        return requestManager.load(drawable.getDestination());
                    }
                    return null;
                }

                @Override
                public void cancel(@NonNull Target<?> target) {
                    RequestManager requestManager = requestManagerByGlide();
                    if (requestManager!=null){
                        requestManager.clear(target);
                    }
                }
            });
            Markwon markwon = Markwon.builder(mContext)
                    .usePlugin(LinkifyPlugin.create())
                    .usePlugin(new AbstractMarkwonPlugin() {
                        @Override
                        public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                            builder.linkResolver(new LinkResolverDef() {
                                @Override
                                public void resolve(View view, @NonNull String link) {
                                    if (link.startsWith("lanying:")) {
                                    } else {
                                        super.resolve(view, link);
                                    }
                                }
                            });
                        }
                    })
                    .usePlugin(plugin1)
                    .usePlugin(plugin2)
                    .usePlugin(plugin3).build();
            Spanned spanned = markwon.toMarkdown(content);
            mChatText.setText(spanned);
            mChatText.setMovementMethod(LinkMovementMethod.getInstance());
        }catch (Exception e){
            e.printStackTrace();
        }
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
        boolean showTypeWriter = ((MaxIMApplication) getApplication()).typeWriterMsgId == mMaxMessage.msgId(); //是否开启打字机效果
        String content = TextUtils.isEmpty(mMaxMessage.content()) ? "" : mMaxMessage.content();
        BMXMessageConfig config = mMaxMessage.config();
        if (config != null){
            String action = config.getRTCAction();
            if (!TextUtils.isEmpty(action)){
                showTypeWriter = false;
                if (action.equals("record")){
                    if (content.equals("rejected")){
                        if (!mMaxMessage.isReceiveMsg()){
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
                        if (!mMaxMessage.isReceiveMsg()){
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
            }
        }
        mText = content.toCharArray();

        if (showTypeWriter){
            startTypingAnimation();
        }else{
            showAsMarkdown(content);
        }
    }
}
