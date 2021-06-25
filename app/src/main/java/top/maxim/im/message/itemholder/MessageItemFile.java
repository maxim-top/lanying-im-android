
package top.maxim.im.message.itemholder;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import im.floo.floolib.BMXFileAttachment;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.message.customviews.FileProgressView;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.interfaces.MsgAttachmentCallback;
import top.maxim.im.message.utils.ChatAttachmentManager;

/**
 * Description : 消息文件类型 Created by Mango on 2018/11/18.
 */
public class MessageItemFile extends MessageItemBaseView {

    private LongSparseArray<Integer> mProgressCache = new LongSparseArray<>();

    private LinearLayout mFileLayout;

    private TextView mFileDesc;

    private RelativeLayout mFileProgress;

    private FileProgressView mProgressView;

    private MsgAttachmentCallback listener = new MsgAttachmentCallback() {
        @Override
        public void onProgress(long msgId, int percent) {
            if (mMaxMessage != null) {
                mProgressCache.put(mMaxMessage.msgId(), percent);
            }
            showFileProgress();
        }

        @Override
        public void onFinish(long msgId) {
            mProgressCache.remove(msgId);
            mFileProgress.setVisibility(View.GONE);
        }

        @Override
        public void onFail(long msgId) {
            mProgressCache.remove(msgId);
            mFileProgress.setVisibility(View.GONE);
        }
    };

    public MessageItemFile(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (mItemPos == ITEM_LEFT) {
            view = View.inflate(mContext, R.layout.item_chat_file_left, parent);
            mFileLayout = view.findViewById(R.id.layout_file_left);
            mFileDesc = view.findViewById(R.id.txt_file_title_left);
            mFileProgress = view.findViewById(R.id.fl_file_progress_left);
            mProgressView = view.findViewById(R.id.file_progress_left);
        } else {
            view = View.inflate(mContext, R.layout.item_chat_file_right, parent);
            mFileLayout = view.findViewById(R.id.layout_file_right);
            mFileDesc = view.findViewById(R.id.txt_file_title_right);
            mFileProgress = view.findViewById(R.id.fl_file_progress_right);
            mProgressView = view.findViewById(R.id.file_progress_right);
        }
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
        setItemViewListener(mFileLayout);
        showFile();
    }

    @Override
    protected void setItemViewListener(View view) {
        // 长按
        view.setOnLongClickListener(new ItemLongClickListener());
        // 点击
        view.setOnClickListener(v -> {
            BMXFileAttachment body = BMXFileAttachment.dynamic_cast(mMaxMessage.attachment());
            boolean register = body != null
                    && (TextUtils.isEmpty(body.path()) || !new File(body.path()).exists());
            if (register) {
                long msgId = mMaxMessage.msgId();
                if (mProgressCache.get(msgId, -1) == -1) {
                    mProgressCache.put(msgId, 0);
                }
                ChatAttachmentManager.getInstance().registerListener(msgId, listener);
            }
            if (mActionListener != null) {
                mActionListener.onItemFunc(mMaxMessage);
            }
        });
    }

    @Override
    public void onViewAttach() {
        super.onViewAttach();
        registerListener();
    }

    @Override
    public void onViewDetach() {
        super.onViewDetach();
        if (mMaxMessage != null) {
            ChatAttachmentManager.getInstance().unRegisterListener(mMaxMessage.msgId());
        }
    }

    /**
     * 展示进度
     */
    private void showFileProgress() {
        if (mMaxMessage == null) {
            mFileProgress.setVisibility(View.GONE);
            return;
        }
        long msgId = mMaxMessage.msgId();
        int percent = mProgressCache.get(msgId, -1);
        if (percent >= 100) {
            mProgressCache.remove(msgId);
            mFileProgress.setVisibility(View.GONE);
            return;
        }
        if (percent == -1) {
            mFileProgress.setVisibility(View.GONE);
        } else {
            mFileProgress.setVisibility(View.VISIBLE);
            mProgressView.setCurrent(percent);
        }
    }

    /**
     * 展示数据
     */
    private void showFile() {
        registerListener();
        showFileProgress();
        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.File) {
            return;
        }
        BMXFileAttachment body = BMXFileAttachment.dynamic_cast(mMaxMessage.attachment());
        if (body == null) {
            return;
        }
        String title = body.displayName();
        mFileDesc.setText(title);
    }

    /**
     * 注册上传下载监听
     */
    private void registerListener() {
        if (mMaxMessage == null) {
            return;
        }
        BMXFileAttachment body = BMXFileAttachment.dynamic_cast(mMaxMessage.attachment());
        boolean register = false;
        if (mItemPos == ITEM_RIGHT) {
            BMXMessage.DeliveryStatus sendStatus = mMaxMessage.deliveryStatus();
            register = sendStatus != null && sendStatus != BMXMessage.DeliveryStatus.Deliveried
                    && sendStatus != BMXMessage.DeliveryStatus.Failed;
        } else if (mItemPos == ITEM_LEFT) {
            // 对方发送文件 需要在点击时候注册
        }
        long msgId = mMaxMessage.msgId();
        if (register) {
            if (mProgressCache.get(msgId, -1) == -1) {
                mProgressCache.put(msgId, 0);
            }
            ChatAttachmentManager.getInstance().registerListener(msgId, listener);
        }
    }
}
