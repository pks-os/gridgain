/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
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

package org.apache.ignite.internal.processors.cache.persistence.diagnostic.pagelocktracker.dumpprocessors;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.pagelocktracker.PageLockDump;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.pagelocktracker.SharedPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.pagelocktracker.SharedPageLockTrackerDump;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.pagelocktracker.ThreadPageLockState;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 * Unit tests for {@link ToStringDumpHelper}.
 */
public class ToStringDumpHelperTest extends GridCommonAbstractTest {

    /** */
    @Test
    public void toStringSharedPageLockTrackerTest() throws Exception {
        SharedPageLockTracker pageLockTracker = new SharedPageLockTracker();

        pageLockTracker.onReadLock(1, 2, 3, 4);

        pageLockTracker.onReadUnlock(1, 2, 3, 4);

        Thread asyncLockUnlock = new Thread(() -> {
            pageLockTracker.onReadLock(4, 32, 1, 64);
        }, "async-lock-unlock");
        asyncLockUnlock.start();
        asyncLockUnlock.join();
        long threadIdInLog = asyncLockUnlock.getId();

        SharedPageLockTrackerDump pageLockDump = pageLockTracker.dump();

        // Hack to have same timestamp in test.
        for (ThreadPageLockState state : pageLockDump.threadPageLockStates)
            GridTestUtils.setFieldValue(state.pageLockDump, PageLockDump.class, "time", 1596173397167L);

        assertNotNull(pageLockDump);

        String dumpStr = ToStringDumpHelper.toStringDump(pageLockDump);

        String expectedLog = "Page locks dump:" +
            U.nl() +
            U.nl() +
            "Thread=[name=async-lock-unlock, id=" + threadIdInLog + "], state=TERMINATED" + U.nl() +
            "Locked pages = [32[0000000000000020](r=1|w=0)]\n" +
            "Locked pages log: name=async-lock-unlock time=(1596173397167, 2020-07-31 08:29:57.167)" + U.nl() +
            "L=1 -> Read lock pageId=32, structureId=null [pageIdHex=0000000000000020, partId=0, pageIdx=32, flags=00000000]" + U.nl() +
            U.nl() +
            U.nl() +
            U.nl();

        assertEquals(expectedLog, dumpStr);
    }
}
