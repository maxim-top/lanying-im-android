
package top.maxim.im.common.utils;

import android.util.LruCache;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXUserProfile;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;

public class RosterFetcher {

    private static RosterFetcher sFetcher = new RosterFetcher();

    private LruCache<Long, BMXRosterItem> mRosterCache = new LruCache<>(30);

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
        BMXRosterItem item = mRosterCache.get(rosterId);
        if (item != null) {
            return item;
        }
        final BMXRosterItem finalItem = new BMXRosterItem();
        Observable.just(finalItem).map(new Func1<BMXRosterItem, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXRosterItem item) {
                return RosterManager.getInstance().search(rosterId, true, item);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        putRoster(finalItem);
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
        return mGroupCache.get(groupId);
    }

    public BMXUserProfile getProfile() {
        BMXUserProfile profile = mProfileCache.get(SharePreferenceUtils.getInstance().getUserId());
        if (profile != null) {
            return profile;
        }
        final BMXUserProfile finalProfile = new BMXUserProfile();
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().getProfile(finalProfile, true);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        putProfile(finalProfile);
                    }
                });
        return null;
    }

}
