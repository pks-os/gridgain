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

package org.apache.ignite.internal.processors.cache.persistence.db.wal;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.processors.cache.persistence.wal.FileDescriptor;
import org.apache.ignite.internal.processors.cache.persistence.wal.FileWALPointer;
import org.apache.ignite.internal.processors.cache.persistence.wal.FileWriteAheadLogManager;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

import static org.apache.ignite.testframework.GridTestUtils.assertThrows;
import static org.apache.ignite.testframework.GridTestUtils.runAsync;

/**
 * Class for testing WAL manager.
 */
public class WriteAheadLogManagerSelfTest extends GridCommonAbstractTest {
    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        stopAllGrids();
        cleanPersistenceDir();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();
        cleanPersistenceDir();
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setCacheConfiguration(new CacheConfiguration<>(DEFAULT_CACHE_NAME))
            .setDataStorageConfiguration(
                new DataStorageConfiguration()
                    .setDefaultDataRegionConfiguration(new DataRegionConfiguration().setPersistenceEnabled(true))
            );
    }

    /** {@inheritDoc} */
    @Override protected IgniteEx startGrids(int cnt) throws Exception {
        IgniteEx n = super.startGrids(cnt);

        n.cluster().state(ClusterState.ACTIVE);
        awaitPartitionMapExchange();

        return n;
    }

    /**
     * Checking the correctness of WAL segment reservation.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testReservation() throws Exception {
        IgniteEx n = startGrids(1);

        for (int i = 0; walMgr(n).lastArchivedSegment() < 2; i++)
            n.cache(DEFAULT_CACHE_NAME).put(i, new byte[(int)(10 * U.KB)]);

        forceCheckpoint();

        assertTrue(walMgr(n).lastArchivedSegment() >= 2);
        assertTrue(walMgr(n).lastTruncatedSegment() == -1);

        FileWALPointer segment0WalPtr = new FileWALPointer(0, 0, 0);
        assertTrue(walMgr(n).reserve(segment0WalPtr));
        assertTrue(walMgr(n).reserved(segment0WalPtr));

        FileWALPointer segment1WalPtr = new FileWALPointer(1, 0, 0);

        // Delete segment manually.
        FileDescriptor segment1 = Arrays.stream(walMgr(n).walArchiveFiles())
            .filter(fd -> fd.idx() == segment1WalPtr.index()).findAny().orElseThrow(AssertionError::new);

        assertTrue(segment1.file().delete());

        assertFalse(walMgr(n).reserve(segment1WalPtr));
        assertTrue(walMgr(n).reserved(segment1WalPtr));

        walMgr(n).release(segment0WalPtr);
        assertFalse(walMgr(n).reserved(segment0WalPtr));
        assertFalse(walMgr(n).reserved(segment1WalPtr));

        assertEquals(1, walMgr(n).truncate(segment1WalPtr));
        assertFalse(walMgr(n).reserve(segment0WalPtr));
        assertFalse(walMgr(n).reserve(segment1WalPtr));
        assertFalse(walMgr(n).reserved(segment0WalPtr));
        assertFalse(walMgr(n).reserved(segment1WalPtr));

        FileWALPointer segmentMaxWalPtr = new FileWALPointer(Long.MAX_VALUE, 0, 0);
        assertFalse(walMgr(n).reserve(segmentMaxWalPtr));
        assertFalse(walMgr(n).reserved(segmentMaxWalPtr));
    }

    /**
     * Checking the correctness of the method {@link FileWriteAheadLogManager#getWalFilesFromArchive}.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testGetWalFilesFromArchive() throws Exception {
        IgniteEx n = startGrids(1);

        FileWALPointer segment0WalPtr = new FileWALPointer(0, 0, 0);
        FileWALPointer segment1WalPtr = new FileWALPointer(1, 0, 0);
        FileWALPointer segment2WalPtr = new FileWALPointer(2, 0, 0);

        CountDownLatch startLatch = new CountDownLatch(1);
        IgniteInternalFuture<Collection<File>> fut = runAsync(() -> {
            startLatch.countDown();

            return walMgr(n).getWalFilesFromArchive(segment0WalPtr, segment2WalPtr);
        });

        startLatch.await();

        // Check that the expected archiving segment 1.
        assertThrows(log, () -> fut.get(1_000), IgniteCheckedException.class, null);

        for (int i = 0; walMgr(n).lastArchivedSegment() < 2; i++)
            n.cache(DEFAULT_CACHE_NAME).put(i, new byte[(int)(10 * U.KB)]);

        assertEquals(2, fut.get(getTestTimeout()).size());

        forceCheckpoint();

        assertEquals(1, walMgr(n).truncate(segment1WalPtr));
        assertEquals(0, walMgr(n).getWalFilesFromArchive(segment0WalPtr, segment2WalPtr).size());
        assertEquals(1, walMgr(n).getWalFilesFromArchive(segment1WalPtr, segment2WalPtr).size());
    }

    /**
     * Getting WAL manager.
     *
     * @param n Node.
     * @return WAL manager.
     */
    private FileWriteAheadLogManager walMgr(IgniteEx n) {
        return (FileWriteAheadLogManager)n.context().cache().context().wal();
    }
}
