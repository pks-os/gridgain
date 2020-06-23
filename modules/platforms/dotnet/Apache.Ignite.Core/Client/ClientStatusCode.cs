﻿/*
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

namespace Apache.Ignite.Core.Client
{
    using Apache.Ignite.Core.Configuration;

    /// <summary>
    /// Client status codes (see <see cref="IgniteClientException.StatusCode"/>).
    /// </summary>
    public enum ClientStatusCode
    {
        /// <summary>
        /// Operation succeeded.
        /// </summary>
        Success = 0,

        /// <summary>
        /// Operation failed (general-purpose code).
        /// </summary>
        Fail = 1,

        /// <summary>
        /// Invalid request operation code.
        /// </summary>
        InvalidOpCode = 2,

        /// <summary>
        /// Specified cache does not exist.
        /// </summary>
        CacheDoesNotExist = 1000,

        /// <summary>
        /// Cache already exists.
        /// </summary>
        CacheExists = 1001,

        /// <summary>
        /// Too many cursors (see <see cref="ClientConnectorConfiguration.MaxOpenCursorsPerConnection"/>).
        /// </summary>
        TooManyCursors = 1010,

        /// <summary>
        /// Resource does not exist.
        /// </summary>
        ResourceDoesNotExist = 1011,

        /// <summary>
        /// Authorization failure.
        /// </summary>
        SecurityViolation = 1012,

        /// <summary>
        /// Too many compute tasks (see <see cref="ThinClientConfiguration.MaxActiveComputeTasksPerConnection"/>).
        /// </summary>
        TooManyComputeTasks = 1030,

        /// <summary>
        /// Authentication failed.
        /// </summary>
        AuthenticationFailed = 2000
    }
}
