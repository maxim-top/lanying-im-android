
package top.maxim.im.bmxmanager;

import im.floo.floolib.BMXNoticeService;

/**
 * Description : notice Created by Mango on 2018/12/2.
 */
public class NoticeManager extends BaseManager {

    private static final String TAG = NoticeManager.class.getSimpleName();

    private static final NoticeManager sInstance = new NoticeManager();

    private BMXNoticeService mService;

    public static NoticeManager getInstance() {
        return sInstance;
    }

    private NoticeManager() {
        mService = bmxClient.getNoticeService();
    }
}
