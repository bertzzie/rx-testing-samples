package tech.namas.rx;

import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import java.util.concurrent.TimeUnit;

public class ReactiveLongCalculationTests {

    private ReactiveLongCalculation service;

    @Before
    public void setup() {
        this.service = new ReactiveLongCalculation();
    }

    @Test
    public void testCalculate() {
        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        this.service.calculate(100L, 2L).subscribe(subscriber);

        subscriber.assertCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertValue(50L);
    }

    @Test
    public void testCalculateError() {
        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        this.service.calculate(100L, 0L).subscribe(subscriber);

        subscriber.assertError(ArithmeticException.class);
        subscriber.assertNotCompleted();
    }

    @Test
    public void testGetManyElements() {
        int count = 100;
        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        this.service.getManyElements(count, i -> i * 50L).subscribe(subscriber);

        subscriber.assertNoErrors();

        Long[] expectedValues = new Long[count];
        for (int i = 0; i < count; i++) {
            expectedValues[i] = i * 50L;
        }

        subscriber.assertValues(expectedValues);

        subscriber.assertValueCount(100);
        subscriber.assertCompleted();
    }

    @Test
    public void testGetManyElementsError() {
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        this.service.getManyElements(5, i -> {
            if (i == 4) {
                return i / 0;
            }

            return i + 1;
        }).subscribe(subscriber);

        subscriber.assertError(ArithmeticException.class);
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(4);
        subscriber.assertValues(1, 2, 3, 4);
    }

    /*
     * TestScheduler.advanceTimeTo -> Set time to x time unit since starts
     * TestScheduler.advanceTimeBy -> Move time forwards for x time unit
     */
    @Test
    public void testTimeSensitiveCalculation() {
        Long seed = 100L;
        TestScheduler scheduler = new TestScheduler();
        TestSubscriber<String> subscriber = new TestSubscriber<>();

        this.service.timeSensitiveCalculation(seed, scheduler)
                    .subscribe(subscriber);

        // Interval not triggered yet. Not completed and no values.
        subscriber.assertNotCompleted();
        subscriber.assertNoValues();

        scheduler.advanceTimeTo(1, TimeUnit.DAYS);

        subscriber.assertValue(Long.valueOf(seed).toString());

        scheduler.advanceTimeTo(2, TimeUnit.DAYS);

        String[] after2Secs = new String[2];
        after2Secs[0] = seed.toString();
        after2Secs[1] = Long.valueOf(seed + 1).toString();
        subscriber.assertValues(after2Secs);

        scheduler.advanceTimeTo(0, TimeUnit.DAYS);
        scheduler.advanceTimeBy(10, TimeUnit.DAYS);
        String[] after10Secs = new String[10];
        for (int i = 0; i < 10; i++) {
            after10Secs[i] = Long.valueOf(seed + i).toString();
        }
        subscriber.assertValues(after10Secs);

        // complete the observer
        subscriber.onCompleted();

        subscriber.assertCompleted();
        subscriber.assertNoErrors();
    }
}
