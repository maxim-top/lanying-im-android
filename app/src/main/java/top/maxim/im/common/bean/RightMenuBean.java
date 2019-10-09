
package top.maxim.im.common.bean;

import android.graphics.drawable.Drawable;

/**
 * 右键菜单实体类
 */
public class RightMenuBean {

    public static final int TYPE_TEXT = 0;// 文本

    private String title;

    private String icon;

    private Drawable drawable;

    /**
     * 唯一标识
     */
    private int flag;

    private int type = TYPE_TEXT;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
