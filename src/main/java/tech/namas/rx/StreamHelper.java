package tech.namas.rx;

import rx.Observable;
import rx.Subscriber;

import java.util.Iterator;
import java.util.stream.Stream;

class StreamHelper<T> extends Observable<T> {

    static <T> StreamHelper<T> toObservable(Stream<T> stream) {
        return new StreamHelper<>(new StreamSubscriber<>(stream));
    }

    private StreamHelper(OnSubscribe<T> f) {
        super(f);
    }

    protected static class StreamSubscriber<T> implements OnSubscribe<T> {

        private final Stream<T> stream;

        StreamSubscriber(Stream<T> stream) {
            this.stream = stream;
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            Iterator<T> i = stream.iterator();
            while(i.hasNext()) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                subscriber.onNext(i.next());
            }

            subscriber.onCompleted();
        }
    }
}
