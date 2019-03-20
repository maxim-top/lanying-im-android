
package top.maxim.im.message.customviews.panel;

/**
 * Description : 输入面板 Created by Mango on 2018/11/06.
 */
public interface OnPanelItemListener {

    /**
     * 输入面板点击
     * 
     * @param type 当前点击来自于功能展板
     * @param item 所点击的item
     */
    void onPanelItemClick(int type, Object item);
}
