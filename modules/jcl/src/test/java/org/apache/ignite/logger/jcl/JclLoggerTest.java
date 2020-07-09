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

package org.apache.ignite.logger.jcl;

import org.apache.commons.logging.LogFactory;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.testframework.junits.common.GridCommonTest;
import org.junit.Test;

/**
 * Jcl logger test.
 */
@GridCommonTest(group = "Logger")
public class JclLoggerTest {
    /** */
    @SuppressWarnings({"FieldCanBeLocal"})
    private IgniteLogger log;

    /** */
    @Test
    public void testLogInitialize() {
        log = new JclLogger(LogFactory.getLog(JclLoggerTest.class.getName()));

        assert log.isInfoEnabled();

        log.info("This is 'info' message.");
        log.warning("This is 'warning' message.");
        log.warning("This is 'warning' message.", new Exception("It's a test warning exception"));
        log.error("This is 'error' message.");
        log.error("This is 'error' message.", new Exception("It's a test error exception"));

        assert log.getLogger(JclLoggerTest.class.getName()) instanceof JclLogger;
    }
}
