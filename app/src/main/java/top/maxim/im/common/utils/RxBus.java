
package top.maxim.im.common.utils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {
    private static volatile RxBus mRxBusInstance;

    private final Subject<Object, Object> mBus = new SerializedSubject(PublishSubject.create());

    RxBus() {
    }

    public static RxBus getInstance() {
        if (mRxBusInstance == null) {
            Class var0 = RxBus.class;
            synchronized (RxBus.class) {
                if (mRxBusInstance == null) {
                    mRxBusInstance = new RxBus();
                }
            }
        }

        return mRxBusInstance;
    }

    public synchronized void send(Object o) {
        this.mBus.onNext(o);
    }

    public <T> Observable<T> toObservable(Class<T> eventType) {
        return this.mBus.ofType(eventType);
    }
}
