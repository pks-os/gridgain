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

namespace Apache.Ignite.Core.Client.Cache.Query.Continuous
{
    using System;

    /// <summary>
    /// Provides data for the <see cref="IContinuousQueryHandleClient.Disconnected"/> event.
    /// </summary>
    public class ContinuousQueryClientDisconnectedEventArgs : EventArgs
    {
        /** */
        private readonly Exception _exception;

        /// <summary>
        /// Initializes a new instance of <see cref="ContinuousQueryClientDisconnectedEventArgs"/> class.
        /// </summary>
        /// <param name="exception">Exception that caused the disconnect.</param>
        public ContinuousQueryClientDisconnectedEventArgs(Exception exception)
        {
            _exception = exception;
        }

        /// <summary>
        /// Gets the exception that caused the disconnect.
        /// </summary>
        public Exception Exception
        {
            get { return _exception; }
        }
    }
}
