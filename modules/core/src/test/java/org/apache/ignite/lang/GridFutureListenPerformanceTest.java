/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.lang;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.util.future.GridFutureAdapter;

/**
 *
 */
public class GridFutureListenPerformanceTest {
    /** */
    private static volatile boolean done;

    /**
     * @param args Args.
     * @throws InterruptedException If failed.
     */
    public static void main(String[] args) throws InterruptedException {
        final LongAdder cnt = new LongAdder();

        final ConcurrentLinkedDeque<GridFutureAdapter<Object>> futs = new ConcurrentLinkedDeque<>();

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Thread statThread = new Thread() {
            @SuppressWarnings("BusyWait")
            @Override public void run() {
                while (!done) {
                    try {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException ignored) {
                        return;
                    }

                    System.out.println(new Date() + " Notifications per sec: " + (cnt.sumThenReset() / 5));
                }
            }
        };

        statThread.setDaemon(true);

        statThread.start();

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            pool.submit(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    Random rnd = new Random();

                    while (!done) {
                        for (int j = 0; j < rnd.nextInt(10); j++) {
                            GridFutureAdapter<Object> fut = new GridFutureAdapter<>();

                            futs.add(fut);

                            for (int k = 1; k < rnd.nextInt(3); k++) {
                                fut.listen(new IgniteInClosure<IgniteInternalFuture<Object>>() {
                                    @Override public void apply(IgniteInternalFuture<Object> t) {
                                        try {
                                            t.get();
                                        }
                                        catch (IgniteCheckedException e) {
                                            e.printStackTrace();
                                        }

                                        cnt.increment();
                                    }
                                });
                            }
                        }

                        GridFutureAdapter<Object> fut;

                        while ((fut = futs.poll()) != null)
                            fut.onDone();
                    }

                    return null;
                }
            });
        }

        Thread.sleep(5 * 60 * 1000);

        done = true;

        pool.shutdownNow();

        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}