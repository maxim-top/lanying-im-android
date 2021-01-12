
package top.maxim.im.common.utils;

import android.util.LruCache;

import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXUserProfile;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;

public class RosterFetcher {

    private static RosterFetcher sFetcher = new RosterFetcher();

    private LruCache<Long, BMXRosterItem> mRosterCache = new LruCache<>(50);

    private LruCache<Long, BMXGroup> mGroupCache = new LruCache(50);
    
    private LruCache<Long, BMXUserProfile> mProfileCache = new LruCache(1);

    private RosterFetcher() {
    }

    public static RosterFetcher getFetcher() {
        return sFetcher;
    }

    public void putProfile(BMXUserProfile profile) {
        if (profile != null)
            mProfileCache.put(SharePreferenceUtils.getInstance().getUserId(), profile);
    }

    public void putRoster(BMXRosterItem item) {
        if (item != null)
            mRosterCache.put(item.rosterId(), item);
    }

    public void putRosters(BMXRosterItemList item) {
        if (item != null && !item.isEmpty()) {
            for (int i = 0; i < item.size(); i++) {
                putRoster(item.get(i));
            }
        }
    }

    public BMXRosterItem getRoster(final long rosterId) {
        if (rosterId <= 0) {
            return null;
        }
        //优先从缓存获取
        BMXRosterItem item = mRosterCache.get(rosterId);
        if (item != null) {
            return item;
        }
        //从DB获取
        item = RosterManager.getInstance().getRosterListByDB(rosterId);
        if (item != null) {
            putRoster(item);
            return item;
        }
        //从service获取
        RosterManager.getInstance().getRosterList(rosterId, true, (bmxErrorCode, bmxRosterItem) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                putRoster(bmxRosterItem);
            }
        });
        return null;
    }

    public void putGroup(BMXGroup item) {
        if (item != null)
            mGroupCache.put(item.groupId(), item);
    }

    public void putGroups(BMXGroupList item) {
        if (item != null && !item.isEmpty()) {
            for (int i = 0; i < item.size(); i++) {
                putGroup(item.get(i));
            }
        }
    }

    public BMXGroup getGroup(long groupId) {
        if (groupId <= 0) {
            return null;
        }
        BMXGroup item = mGroupCache.get(groupId);
        if (item != null) {
            return item;
        }
        GroupManager.getInstance().getGroupList(groupId, true, (bmxErrorCode, bmxGroup) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                putGroup(bmxGroup);
            }
        });
        return null;
    }

    public BMXUserProfile getProfile() {
        BMXUserProfile profile = mProfileCache.get(SharePreferenceUtils.getInstance().getUserId());
        if (profile != null) {
            return profile;
        }
        UserManager.getInstance().getProfile(false, (bmxErrorCode, bmxUserProfile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode) && bmxUserProfile != null) {
                putProfile(bmxUserProfile);
            }
        });
        return null;
    }

}
