package rxdata;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.util.EndConsumerHelper;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxData<T> {

    private Subject<DataWrap<T>> triggers;

    private T data;

    private final Object dataLock = new Object();

    public RxData() {
        triggers = PublishSubject.create();
    }

    public RxData(T data) {
        this();
        this.data = data;
    }

    public T get() {
        return data;
    }

    public void set(T data) {
        synchronized (dataLock) {
            this.data = data;
            triggers.onNext(new DataWrap<>(data));
        }
    }

    public DataObservable<DataWrap<T>> observable() {
        return observable(null);
    }

    public DataObservable<DataWrap<T>> observable(LifecycleOwner owner) {
        DataCheck<DataWrap<T>> dataCheck = new DataCheck<>(owner);
        return triggers
                .startWith(new DataWrap<>(data))
                .filter(dataCheck.getPredicateCheck())
                .to(DataObservable.<DataWrap<T>>toFunction(dataCheck));
    }

    public static final class DataWrap<T> {
        final T data;

        public DataWrap(T data) {
            this.data = data;
        }

        public boolean isNull() {
            return data == null;
        }

        public T get() {
            return data;
        }
    }

    private static final class DataCheck<T> {
        private LifecycleOwner owner;

        DataCheck(LifecycleOwner owner) {
            this.owner = owner;
        }

        private final Predicate<T> predicateCheck = new Predicate<T>() {
            @Override
            public boolean test(T data) {
                // 当生命周期已经结束时，不必再进行数据发送
                if (isLifecycleState(owner, Lifecycle.State.DESTROYED)) {
                    return false;
                }
                return true;
            }
        };

        Predicate<T> getPredicateCheck() {
            return predicateCheck;
        }
    }

    public static class DataObservable<T> extends Observable<T> implements LifecycleObserver {

        static <T> Function<Observable<T>, DataObservable<T>> toFunction(final DataCheck dataCheck) {
            return new Function<Observable<T>, DataObservable<T>>() {
                @Override
                public DataObservable<T> apply(Observable<T> tObservable) {
                    return new DataObservable<>(tObservable, dataCheck);
                }
            };
        }

        private final Observable<T> observable;
        private final DataCheck dataCheck;
        private DataObserver<T> dataObserver;

        DataObservable(Observable<T> observable, DataCheck dataCheck) {
            this.observable = observable;
            this.dataCheck = dataCheck;
            if (dataCheck.owner != null) {
                if (!isLifecycleState(dataCheck.owner, Lifecycle.State.DESTROYED)) {
                    dataCheck.owner.getLifecycle().addObserver(this);
                }
            }
        }

        @Override
        protected void subscribeActual(Observer<? super T> observer) {
            dataObserver = new DataObserver<>(observer);
            observable.subscribe(dataObserver);
            if (isLifecycleState(dataCheck.owner, Lifecycle.State.DESTROYED)) {
                if (dataObserver != null && !dataObserver.isDisposed()) {
                    dataObserver.dispose();
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onLifecycleDestroy() {
            if (dataObserver != null && !dataObserver.isDisposed()) {
                dataObserver.dispose();
            }
            if (dataCheck.owner != null) {
                dataCheck.owner.getLifecycle().removeObserver(this);
            }
        }
    }

    public static class DataObserver<T> implements Observer<T>, Disposable {

        final AtomicReference<Disposable> s = new AtomicReference<>();

        final Observer<? super T> observer;

        DataObserver(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public final void onSubscribe(@NonNull Disposable s) {
            if (EndConsumerHelper.setOnce(this.s, s, getClass())) {
            }
            observer.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            observer.onNext(t);
        }

        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public void onComplete() {
            observer.onComplete();
        }

        @Override
        public final boolean isDisposed() {
            return s.get() == DisposableHelper.DISPOSED;
        }

        @Override
        public final void dispose() {
            DisposableHelper.dispose(s);
        }
    }

    private static boolean isLifecycleState(LifecycleOwner owner, @android.support.annotation.NonNull Lifecycle.State state) {
        return owner != null && owner.getLifecycle().getCurrentState() == state;
    }
}
