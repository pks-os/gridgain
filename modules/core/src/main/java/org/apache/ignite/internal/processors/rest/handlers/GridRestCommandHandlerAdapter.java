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

package org.apache.ignite.internal.processors.rest.handlers;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.internal.util.typedef.internal.SB;

/**
 * Abstract command handler.
 */
public abstract class GridRestCommandHandlerAdapter implements GridRestCommandHandler {

    /** Kernal context. */
    protected final GridKernalContext ctx;

    /** Log. */
    protected final IgniteLogger log;

    /**
     * @param ctx Context.
     */
    protected GridRestCommandHandlerAdapter(GridKernalContext ctx) {
        this.ctx = ctx;

        log = ctx.log(getClass());
    }

    /**
     * Return missing parameter error message.
     *
     * @param param Parameter name.
     * @return Missing parameter error message.
     */
    protected static String missingParameter(String param) {
        return "Failed to find mandatory parameter in request: " + param;
    }

    /**
     * Converts exception to string representation for error in response.
     *
     * @param e Exception.
     * @return String representation of exception for error in response.
     */
    protected static String errorMessage(Exception e) {
        SB sb = new SB();

        sb.a(e.getMessage()).a("\n").a("suppressed: \n");

        for (Throwable t : X.getSuppressedList(e))
            sb.a(t.getMessage()).a("\n");

        return sb.toString();
    }
}
