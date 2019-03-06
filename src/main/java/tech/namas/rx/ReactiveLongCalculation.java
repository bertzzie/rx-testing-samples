package tech.namas.rx;

import rx.Observable;
import rx.Scheduler;
import rx.Single;

import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

class ReactiveLongCalculation {

    Single<Long> calculate(Long a, Long b) {
        return Single.create(subscriber -> subscriber.onSuccess(a / b));
    }

    <S> Observable<S> getManyElements(Integer count, IntFunction<S> applicator) {
        return StreamHelper.toObservable(IntStream.range(0, count).mapToObj(applicator));
    }

    Observable<String> timeSensitiveCalculation(Long seed, Scheduler scheduler) {
        return Observable.interval(1, TimeUnit.DAYS, scheduler)
                         .map(i -> seed + i)
                         .map(Object::toString);
    }
}
