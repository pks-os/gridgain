/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.tracing.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.processors.metastorage.DistributedMetaStorage;
import org.apache.ignite.internal.processors.metastorage.persistence.DistributedMetaStorageImpl;
import org.apache.ignite.internal.processors.tracing.Scope;
import org.apache.ignite.internal.util.tostring.GridToStringExclude;
import org.apache.ignite.internal.util.typedef.internal.LT;
import org.jetbrains.annotations.NotNull;

/**
 * Tracing configuration implementation that uses distributed meta storage in order to store tracing configuration.
 */
public class GridTracingConfiguration implements TracingConfiguration {
    /** */
    private static final String TRACING_CONFIGURATION_DISTRIBUTED_METASTORE_KEY_PREFIX =
        DistributedMetaStorageImpl.IGNITE_INTERNAL_KEY_PREFIX + "tr.config.";

    /** Map with default configurations. */
    private static final Map<TracingConfigurationCoordinates, TracingConfigurationParameters> DEFAULT_CONFIGURATION_MAP;

    static {
        Map<TracingConfigurationCoordinates, TracingConfigurationParameters> tmpDfltConfigurationMap = new HashMap<>();

        tmpDfltConfigurationMap.put(
            new TracingConfigurationCoordinates.Builder(Scope.TX).build(),
            TracingConfiguration.DEFAULT_TX_CONFIGURATION);

        tmpDfltConfigurationMap.put(
            new TracingConfigurationCoordinates.Builder(Scope.COMMUNICATION).build(),
            TracingConfiguration.DEFAULT_COMMUNICATION_CONFIGURATION);

        tmpDfltConfigurationMap.put(
            new TracingConfigurationCoordinates.Builder(Scope.EXCHANGE).build(),
            TracingConfiguration.DEFAULT_EXCHANGE_CONFIGURATION);

        tmpDfltConfigurationMap.put(
            new TracingConfigurationCoordinates.Builder(Scope.DISCOVERY).build(),
            TracingConfiguration.DEFAULT_DISCOVERY_CONFIGURATION);

        DEFAULT_CONFIGURATION_MAP = Collections.unmodifiableMap(tmpDfltConfigurationMap);
    }

    /** Kernal context. */
    @GridToStringExclude
    protected final GridKernalContext ctx;

    /** Grid logger. */
    @GridToStringExclude
    protected final IgniteLogger log;

    /**
     * Constructor.
     *
     * @param ctx Context.
     */
    public GridTracingConfiguration(@NotNull GridKernalContext ctx) {
        this.ctx = ctx;

        log = ctx.log(getClass());
    }

    /** {@inheritDoc} */
    @Override public boolean addConfiguration(
        @NotNull TracingConfigurationCoordinates coordinates,
        @NotNull TracingConfigurationParameters parameters)
    {
        DistributedMetaStorage metaStore;

        try {
            metaStore = ctx.distributedMetastorage();
        }
        catch (Exception e) {
            log.warning("Failed to save tracing configuration to meta storage. Meta storage is not available");

            return false;
        }

        if (metaStore == null) {
            log.warning("Failed to save tracing configuration to meta storage. Meta storage is not available");

            return false;
        }

        String scopeSpecificKey = TRACING_CONFIGURATION_DISTRIBUTED_METASTORE_KEY_PREFIX + coordinates.scope().name();

        boolean configurationSuccessfullyUpdated = false;

        try {
            while (!configurationSuccessfullyUpdated) {
                HashMap<String, TracingConfigurationParameters> existingScopeSpecificTracingConfiguration =
                    ctx.distributedMetastorage().read(scopeSpecificKey);

                HashMap<String, TracingConfigurationParameters> updatedScopeSpecificTracingConfiguration =
                    existingScopeSpecificTracingConfiguration != null ?
                        new HashMap<>(existingScopeSpecificTracingConfiguration) : new
                        HashMap<>();

                updatedScopeSpecificTracingConfiguration.put(coordinates.label(), parameters);

                configurationSuccessfullyUpdated = ctx.distributedMetastorage().compareAndSet(
                    scopeSpecificKey,
                    existingScopeSpecificTracingConfiguration,
                    updatedScopeSpecificTracingConfiguration);
            }
        }
        catch (IgniteCheckedException e) {
            log.warning("Failed to save tracing configuration to meta storage.", e);

            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public @NotNull TracingConfigurationParameters retrieveConfiguration(
        @NotNull TracingConfigurationCoordinates coordinates) {
        DistributedMetaStorage metaStore;

        try {
            metaStore = ctx.distributedMetastorage();
        }
        catch (Exception e) {
            LT.warn(log, "Failed to retrieve tracing configuration — meta storage is not available." +
                " Default value will be used.");

            // If metastorage in not available — use scope specific default tracing configuration.
            return TracingConfiguration.super.retrieveConfiguration(coordinates);
        }

        if (metaStore == null) {
            LT.warn(log, "Failed to retrieve tracing configuration — meta storage is not available." +
                " Default value will be used.");

            // If metastorage in not available — use scope specific default tracing configuration.
            return TracingConfiguration.super.retrieveConfiguration(coordinates);
        }

        String scopeSpecificKey = TRACING_CONFIGURATION_DISTRIBUTED_METASTORE_KEY_PREFIX + coordinates.scope().name();

        HashMap<String, TracingConfigurationParameters> scopeSpecificTracingConfiguration;

        try {
            scopeSpecificTracingConfiguration = ctx.distributedMetastorage().read(scopeSpecificKey);
        }
        catch (IgniteCheckedException e) {
            LT.warn(
                log,
                e,
                "Failed to retrieve tracing configuration. Default value will be used.",
                false,
                true);

            // In case of exception during retrieving configuration from metastorage — use scope specific default one.
            return TracingConfiguration.super.retrieveConfiguration(coordinates);
        }

        // If the configuration was not found — use scope specific default one.
        if (scopeSpecificTracingConfiguration == null)
            return TracingConfiguration.super.retrieveConfiguration(coordinates);

        // Retrieving scope + label specific tracing configuration.
        TracingConfigurationParameters lbBasedTracingConfiguration =
            scopeSpecificTracingConfiguration.get(coordinates.label());

        // If scope + label specific was found — use it.
        if (lbBasedTracingConfiguration != null)
            return lbBasedTracingConfiguration;

        // Retrieving scope specific tracing configuration.
        TracingConfigurationParameters rawScopedTracingConfiguration = scopeSpecificTracingConfiguration.get(null);

        // If scope specific was found — use it.
        if (rawScopedTracingConfiguration != null)
            return rawScopedTracingConfiguration;

        // If neither scope + label specific nor just scope specific configuration was found —
        // use scope specific default one.
        return TracingConfiguration.super.retrieveConfiguration(coordinates);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") @Override
    public @NotNull Map<TracingConfigurationCoordinates, TracingConfigurationParameters> retrieveConfigurations() {
        DistributedMetaStorage metaStore;

        try {
            metaStore = ctx.distributedMetastorage();
        }
        catch (Exception e) {
            log.warning("Failed to retrieve tracing configuration — meta storage is not available.");

            return DEFAULT_CONFIGURATION_MAP;
        }

        if (metaStore == null) {
            log.warning("Failed to retrieve tracing configuration — meta storage is not available.");

            return DEFAULT_CONFIGURATION_MAP;
        }

        Map<TracingConfigurationCoordinates, TracingConfigurationParameters> res = new HashMap<>();

        for (Scope scope : Scope.values()) {
            String scopeSpecificKey = TRACING_CONFIGURATION_DISTRIBUTED_METASTORE_KEY_PREFIX + scope.name();

            try {
                for (Map.Entry<String, TracingConfigurationParameters> entry :
                    ((Map<String, TracingConfigurationParameters>)metaStore.read(scopeSpecificKey)).entrySet()) {
                    res.put(
                        new TracingConfigurationCoordinates.Builder(scope).withLabel(entry.getKey()).build(),
                        entry.getValue());
                }
            }
            catch (IgniteCheckedException e) {
                LT.warn(log, "Failed to retrieve tracing configuration");
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override public boolean restoreDefaultConfiguration(@NotNull TracingConfigurationCoordinates coordinates) {
        DistributedMetaStorage metaStore;

        try {
            metaStore = ctx.distributedMetastorage();
        }
        catch (Exception e) {
            log.warning("Failed to restore tracing configuration for coordinates=[" + coordinates +
                "] to default  — meta storage is not available.");

            return false;
        }

        if (metaStore == null) {
            log.warning("Failed to restore tracing configuration for coordinates=[" + coordinates +
                "] to default  — meta storage is not available.");

            return false;
        }

        String scopeSpecificKey = TRACING_CONFIGURATION_DISTRIBUTED_METASTORE_KEY_PREFIX + coordinates.scope().name();

        boolean configurationSuccessfullyUpdated = false;

        try {
            while (!configurationSuccessfullyUpdated) {
                HashMap<String, TracingConfigurationParameters> existingScopeSpecificTracingConfiguration =
                    ctx.distributedMetastorage().read(scopeSpecificKey);

                if (existingScopeSpecificTracingConfiguration == null) {
                    // Nothing to do.
                    return true;
                }

                HashMap<String, TracingConfigurationParameters> updatedScopeSpecificTracingConfiguration =
                        new HashMap<>(existingScopeSpecificTracingConfiguration);

                if (coordinates.label() != null)
                    updatedScopeSpecificTracingConfiguration.remove(coordinates.label());
                else
                    updatedScopeSpecificTracingConfiguration.remove(null);

                configurationSuccessfullyUpdated = ctx.distributedMetastorage().compareAndSet(
                    scopeSpecificKey,
                    existingScopeSpecificTracingConfiguration,
                    updatedScopeSpecificTracingConfiguration);
            }
        }
        catch (IgniteCheckedException e) {
            log.warning("Failed to restore tracing configuration for coordinates=[" + coordinates +
                "] to default  — meta storage is not available.");

            return false;
        }

        return true;
    }
}