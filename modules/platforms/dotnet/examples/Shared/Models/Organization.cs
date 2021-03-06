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

namespace Apache.Ignite.Examples.Shared.Models
{
    using System;
    using Apache.Ignite.Core.Cache.Configuration;

    /// <summary>
    /// Organization.
    /// </summary>
    public class Organization
    {
        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="name">Name.</param>
        /// <param name="address">Address.</param>
        /// <param name="type">Type.</param>
        /// <param name="lastUpdated">Last update time.</param>
        public Organization(string name, Address address, OrganizationType type, DateTime lastUpdated)
        {
            Name = name;
            Address = address;
            Type = type;
            LastUpdated = lastUpdated;
        }

        /// <summary>
        /// Name.
        /// </summary>
        [QuerySqlField(IsIndexed = true)]
        public string Name { get; set; }

        /// <summary>
        /// Address.
        /// </summary>
        public Address Address { get; set; }

        /// <summary>
        /// Type.
        /// </summary>
        public OrganizationType Type { get; set; }

        /// <summary>
        /// Last update time.
        /// </summary>
        public DateTime LastUpdated { get; set; }

        /// <summary>
        /// Returns a string that represents the current object.
        /// </summary>
        /// <returns>
        /// A string that represents the current object.
        /// </returns>
        /// <filterpriority>2</filterpriority>
        public override string ToString()
        {
            return string.Format("{0} [name={1}, address={2}, type={3}, lastUpdated={4}]", typeof(Organization).Name,
                Name, Address, Type, LastUpdated);
        }
    }
}
