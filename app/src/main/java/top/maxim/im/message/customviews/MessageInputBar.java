
package top.maxim.im.message.customviews;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import top.maxim.im.R;
import top.maxim.im.message.customviews.panel.AutoComputerInputMethodHeightView;
import top.maxim.im.message.customviews.panel.OnPanelItemListener;
import top.maxim.im.message.customviews.panel.PanelContainer;
import top.maxim.im.message.customviews.panel.PanelFactoryImp;

/**
 * Description : 聊天输入面板 Created by Mango on 2018/11/06.
 */
public class MessageInputBar extends AutoComputerInputMethodHeightView
        implements View.OnClickListener, OnPanelItemListener {

    private static final String TAG = MessageInputBar.class.getSimpleName();

    private boolean isShowKeyBoard = false;

    private ChatEditText mChatEditText;

    // private ImageView mEmojiView;

    private OnInputPanelListener mListener;

    private ImageView mMoreView;

    private TextView mTvSend;

    /* 切换语音 文本按钮 */
    private ImageView mKeyBoardView;

    /* 语音按钮 */
    private TextView mVoiceView;

    /* 是否是输入框 */
    private boolean mIsChatEdit = true;

    public static final int VOICE_START = 1;

    public static final int VOICE_FINISH = 2;

    public static final int VOICE_CANCEL = 3;

    public static final int VOICE_TIME_OUT = 4;

    public static final int VOICE_NORMAL = 5;

    public static final int VOICE_OVER = 6;

    /* 语音计时 */
    private int mCurrentTime;

    /* 是否计时中 */
    private boolean mTimeing = false;

    private PanelContainer mPanel;

    public MessageInputBar(Context context) {
        this(context, null);
    }

    public MessageInputBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageInputBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.color_white));
        initView();
    }

    private void initView() {
        findViewById(inflate(getContext(), R.layout.chat_control_bar, this));
        mPanel = new PanelContainer(getContext());
        addView(mPanel);
    }

    private void findViewById(View view) {
        mKeyBoardView = ((ImageView)view.findViewById(R.id.control_voice));
        mMoreView = ((ImageView)view.findViewById(R.id.control_more));
        mTvSend = ((TextView)view.findViewById(R.id.tv_send));
        mTvSend.setVisibility(View.GONE);
        mTvSend.setEnabled(false);
        mChatEditText = ((ChatEditText)view.findViewById(R.id.chat_edit_text));
        mVoiceView = (TextView)view.findViewById(R.id.chat_voice_view);
        setViewListener();
    }

    private void requestEditTextFocus() {
        mChatEditText.setFocusableInTouchMode(true);
        mChatEditText.setFocusable(true);
        boolean isEmpty = TextUtils.isEmpty(mChatEditText.getEditableText().toString().trim());
        mTvSend.setEnabled(!isEmpty);
        mTvSend.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mMoreView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (!mChatEditText.hasFocus()) {
            mChatEditText.requestFocus();
        }
    }

    private void setViewListener() {
        mKeyBoardView.setOnClickListener(this);
        mMoreView.setOnClickListener(this);
        mTvSend.setOnClickListener(this);
        mChatEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                requestEditTextFocus();
                showKeyboard(mPanel);
                if (event.getAction() == 0 && mListener != null) {
                    mListener.onTagChanged(OnInputPanelListener.TAG_EMPTY);
                    mListener.onTagChanged(OnInputPanelListener.TAG_OPEN);
                }
                return false;
            }
        });
        mChatEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hidePanel();
                }
            }
        });
        mChatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 && TextUtils.equals("@", s.subSequence(start, start + 1))
                        && (start <= 0 || !s.subSequence(start - 1, start).toString()
                                .matches("[a-z,A-Z,0-9]"))
                        && mListener != null) {
                    mListener.onChatAtMember();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(s.toString().trim());
                mTvSend.setEnabled(!isEmpty);
                mTvSend.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                mMoreView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
        });

        // 语音按钮
        mVoiceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float height = v.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        actionDown();
                        if (null != mListener) {
                            mListener.onSendVoiceRequest(VOICE_START, mCurrentTime);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < height) {
                            // 超出范围抬起 返回录音取消
                            if (mListener != null) {
                                mListener.onSendVoiceRequest(VOICE_OVER, mCurrentTime);
                            }
                        } else {
                            if (null != mListener) {
                                mListener.onSendVoiceRequest(VOICE_NORMAL, mCurrentTime);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (event.getY() < height) {
                            // 超出范围抬起 返回录音取消
                            if (mListener != null) {
                                mListener.onSendVoiceRequest(VOICE_CANCEL, mCurrentTime);
                            }
                        } else {
                            if (null != mListener) {
                                mListener.onSendVoiceRequest(VOICE_FINISH, mCurrentTime);
                            }
                        }
                        actionUp();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void hideKeyboard() {
        dismissKeyBoard();
        isShowKeyBoard = false;
    }

    public void hidePanel() {
        mChatEditText.clearFocus();
        mPanel.hidePanel();
        hideKeyboard();
        mTvSend.setVisibility(GONE);
        mMoreView.setVisibility(VISIBLE);
        if (mListener != null) {
            mListener.onTagChanged(OnInputPanelListener.TAG_CLOSE);
        }
    }

    public boolean isShowKeyBoard() {
        return isShowKeyBoard;
    }

    public boolean isShowPanel() {
        return mPanel.isShown();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_voice:
                if (mListener != null) {
                    mListener.onTagChanged(OnInputPanelListener.TAG_EMPTY);
                }
                changeEdit(!mIsChatEdit);
                break;
            // case R.id.control_emoji:
            // changeEdit(true);
            // if (mListener != null) {
            // mListener.onTagChanged(OnInputPanelListener.TAG_EMPTY);
            // }
            // hideKeyboard();
            // mPanel.showPanel(PanelFactoryImp.TYPE_EMOJI, this);
            // break;
            case R.id.control_more:
                changeEdit(true);
                if (mListener != null) {
                    mListener.onTagChanged(OnInputPanelListener.TAG_EMPTY);
                }
                hideKeyboard();
                mPanel.showPanel(PanelFactoryImp.TYPE_FUNCTION, this);
                break;
            case R.id.tv_send:
                // 发送文本
                if (mListener != null) {
                    mListener.onSendTextRequest(mChatEditText.getText().toString());
                    mChatEditText.setText("");
                }
                break;
        }
    }

    public void setInputListener(OnInputPanelListener listener) {
        mListener = listener;
    }

    public void showKeyboard(View view) {
        showKeyBoard(view);
        isShowKeyBoard = true;
    }

    /**
     * 切换输入框和语音
     */
    private void changeEdit(boolean isChatEdit) {
        mIsChatEdit = isChatEdit;
        if (isChatEdit) {
            mKeyBoardView.setBackgroundResource(R.drawable.chat_voice_icon_selector);
            mVoiceView.setVisibility(GONE);
            mChatEditText.setVisibility(VISIBLE);
            requestEditTextFocus();
            showKeyboard(mPanel);
        } else {
            mKeyBoardView.setBackgroundResource(R.drawable.chat_keyboard_icon_selector);
            mVoiceView.setVisibility(VISIBLE);
            mChatEditText.setVisibility(GONE);
            mVoiceView.setPressed(false);
            mVoiceView.setText("按住说话");
            hidePanel();
        }
    }

    /**
     * 设置输入框内容
     */
    public void appendString(String text) {
        if (mChatEditText != null && mChatEditText.getEditableText() != null) {
            mChatEditText.getEditableText().append(text);
        }
    }

    /**
     * 获取输入框内容
     * 
     * @return String
     */
    public String getChatEditText() {
        if (mChatEditText != null && mChatEditText.getText() != null) {
            return mChatEditText.getText().toString();
        }
        return "";
    }

    public void insertInAt(List<String> atNames) {
        if (atNames == null || atNames.isEmpty()) {
            return;
        }
        Editable editable = mChatEditText.getText();
        int cursorIndex = mChatEditText.getSelectionStart();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (String name : atNames) {
            ForegroundColorSpan span = new ForegroundColorSpan(
                    getContext().getResources().getColor(R.color.color_black));
            SpannableString spannableString = new SpannableString(name);
            spannableString.setSpan(span, 0, spannableString.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(spannableString);
        }
        editable.replace(cursorIndex - 1, cursorIndex, builder);
        requestEditTextFocus();
        showKeyboard(mPanel);
        final InputMethodManager inputManager = (InputMethodManager)mChatEditText.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(mChatEditText, 0);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    inputManager.showSoftInput(mChatEditText, 0);
                }
            }, 200L);
        }

        if (null != mListener) {
            mListener.onTagChanged(OnInputPanelListener.TAG_OPEN);
        }

    }

    @Override
    public void onPanelItemClick(int type, Object item) {
        if (item == null) {
            return;
        }
        switch (type) {
            case PanelFactoryImp.TYPE_FUNCTION:
                if (mListener != null) {
                    mListener.onFunctionRequest((String)item);
                }
                break;
            case PanelFactoryImp.TYPE_EMOJI:
                // ItemEmoji emoji = (ItemEmoji)item;
                // insertInEditText(emoji);
                break;
            default:
                break;
        }
    }

    /**
     * 语音按下
     */
    private void actionDown() {
        mVoiceView.setPressed(true);
        mVoiceView.setText("上滑取消");
        startRecord();
    }

    /**
     * 语音抬起
     */
    private void actionUp() {
        if (mCurrentTime < 1) {
            mVoiceView.postDelayed(rResetHint, 500);
        } else {
            mVoiceView.setPressed(false);
            mVoiceView.setText("按住说话");
        }
        stopRecord();
    }

    private void startRecord() {
        mTimeing = true;
        mCurrentTime = 0;
        mVoiceView.postDelayed(rTime, 1000);
    }

    private void stopRecord() {
        mTimeing = false;
        mCurrentTime = 0;
        mVoiceView.removeCallbacks(rTime);
    }

    private Runnable rResetHint = new Runnable() {
        @Override
        public void run() {
            mVoiceView.setPressed(false);
            mVoiceView.setText("按住说话");
        }
    };

    private Runnable rTime = new Runnable() {
        @Override
        public void run() {
            mCurrentTime++;
            Log.d(TAG, "time:" + mCurrentTime);
            if (mTimeing) {
                mVoiceView.postDelayed(rTime, 1000);
            }
        }
    };

    public interface OnInputPanelListener {

        int TAG_EMPTY = -1;

        int TAG_OPEN = -2;

        int TAG_CLOSE = -3;

        int TAG_EDIT = 1;

        int TAG_EMOJI = 2;

        int TAG_FUNC = 3;

        int TAG_VOICE = 4;

        /* 功能监听 */
        void onFunctionRequest(String functionType);

        /* 发送文本 */
        void onSendTextRequest(String sendText);

        /* 发送语音 */
        void onSendVoiceRequest(int voiceAction, long time);

        /* 输入@ */
        void onChatAtMember();

        /* 标记位 */
        void onTagChanged(int tag);

    }
}
