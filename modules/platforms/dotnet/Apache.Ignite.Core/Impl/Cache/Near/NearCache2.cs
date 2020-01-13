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

namespace Apache.Ignite.Core.Impl.Cache.Near
{
    using System;
    using System.Collections.Concurrent;
    using System.Diagnostics;
    using Apache.Ignite.Core.Impl.Binary;
    using Apache.Ignite.Core.Impl.Binary.IO;
    using Apache.Ignite.Core.Impl.Memory;

    /// <summary>
    /// Holds near cache data for a given cache, serves one or more <see cref="CacheImpl{TK,TV}"/> instances.
    /// </summary>
    internal class NearCache2<TK, TV>
    {
        // TODO: Init capacity from settings
        // TODO: Eviction
        private ConcurrentDictionary<TK, NearCacheEntry<TV>> _map = new ConcurrentDictionary<TK, NearCacheEntry<TV>>();

        private ConcurrentDictionary<object, NearCacheEntry<object>> _fallbackMap;

        public bool TryGetValue<TKey, TVal>(TKey key, out TVal val)
        {
            if (_fallbackMap != null)
            {
                NearCacheEntry<object> fallbackEntry;
                if (_fallbackMap.TryGetValue(key, out fallbackEntry) && fallbackEntry.HasValue)
                {
                    val = (TVal) fallbackEntry.Value;
                    return true;
                }
            }
            
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                NearCacheEntry<TVal> entry;
                if (map.TryGetValue(key, out entry) && entry.HasValue)
                {
                    val = entry.Value;
                    return true;
                }
            }

            val = default(TVal);
            return false;
        }

        public void Put<TKey, TVal>(TKey key, TVal val)
        {
            // TODO: Eviction according to limits.
            // Eviction callbacks from Java work for 2 out of 3 cases:
            // + Client node (all keys)
            // + Server node (non-primary keys)
            // - Server node (primary keys) - because there is no need to store primary keys in near cache
            // We can just ignore the third case and never evict primary keys - after all, we are on a server node,
            // and it is fine to keep primary keys in memory.

            if (_fallbackMap != null)
            {
                _fallbackMap[key] = new NearCacheEntry<object>(true, val);
                return;
            }
            
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                map[key] = new NearCacheEntry<TVal>(true, val);
                return;
            }

            // Generic downgrade: switch to fallback map.
            EnsureFallbackMap();
            _fallbackMap[key] = new NearCacheEntry<object>(true, val);
        }

        public INearCacheEntry<TVal> GetOrCreateEntry<TKey, TVal>(TKey key)
        {
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            
            
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                return map.GetOrAdd(key, _ => new NearCacheEntry<TVal>());
            }
        }

        public void Update(IBinaryStream stream, Marshaller marshaller)
        {
            Debug.Assert(stream != null);
            Debug.Assert(marshaller != null);

            var reader = marshaller.StartUnmarshal(stream);
            
            // TODO: This throws when new type parameters come into play
            var key = reader.Deserialize<TK>();

            if (reader.ReadBoolean())
            {
                // TODO: This throws when new type parameters come into play
                // Catch exception?
                var val = reader.Deserialize<TV>();
                _map[key] = new NearCacheEntry<TV>(true, val);
            }
            else
            {
                NearCacheEntry<TV> unused;
                _map.TryRemove(key, out unused);
            }
        }

        public void Evict(PlatformMemoryStream stream, Marshaller marshaller)
        {
            Debug.Assert(stream != null);
            Debug.Assert(marshaller != null);

            var key = marshaller.Unmarshal<TK>(stream);
            
            Console.WriteLine("Evict: " + key);
            NearCacheEntry<TV> unused;
            _map.TryRemove(key, out unused);
        }

        public void Clear()
        {
            _map.Clear();
        }

        public void Remove(TK key)
        {
            NearCacheEntry<TV> unused;
            _map.TryRemove(key, out unused);
        }
        
        private void EnsureFallbackMap()
        {
            _map = null;
            _fallbackMap = _fallbackMap ?? new ConcurrentDictionary<object, NearCacheEntry<object>>();
        }
    }
}
