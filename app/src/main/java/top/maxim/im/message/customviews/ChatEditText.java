
package top.maxim.im.message.customviews;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Description : 聊天输入框 Created by Mango on 2018/11/06.
 */
public class ChatEditText extends AppCompatEditText {

    public ChatEditText(Context context) {
        super(context);
    }

    public ChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 删除时如果有@ 则单独处理
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            int select = getSelectionStart();
            if (select > 0) {
                Editable editable = getEditableText();
                // 输入框不为空 删除时回调
                if (editable != null && editable.length() > 0
                        && editable.toString().contains("@")) {
                    // 光标前面要删除的字符串
                    int lastAtPos = editable.subSequence(0, select).toString().lastIndexOf("@");
                    if (lastAtPos > -1 && select > lastAtPos) {
                        CharSequence handleEdit = editable.subSequence(lastAtPos, select);
                        if (!TextUtils.isEmpty(handleEdit) && handleEdit instanceof Spannable) {
                            // @的内容会用ForegroundColorSpan代替 如果包含@ 触发删除操作
                            ForegroundColorSpan spans[] = ((Spannable)handleEdit).getSpans(0,
                                    handleEdit.length(), ForegroundColorSpan.class);
                            if (spans != null && spans.length == 1) {
                                ForegroundColorSpan span = spans[0];
                                // 光标的位置和当前@对象的末尾位置一样 则直接删除整个@对象
                                if (editable.getSpanEnd(span) == getSelectionStart()) {
                                    editable.delete(editable.getSpanStart(span),
                                            editable.getSpanEnd(span));
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
