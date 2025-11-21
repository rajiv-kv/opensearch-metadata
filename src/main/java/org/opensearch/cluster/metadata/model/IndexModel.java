/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.cluster.metadata.model;

import org.opensearch.Version;
import org.opensearch.action.admin.indices.rollover.RolloverInfo;
import org.opensearch.action.support.ShardCount;
import org.opensearch.cluster.metadata.AliasMetadata;
import org.opensearch.cluster.metadata.Context;
import org.opensearch.cluster.metadata.DiffableStringMap;
import org.opensearch.cluster.metadata.IngestionStatus;
import org.opensearch.cluster.metadata.server.IndexMetadata;
import org.opensearch.cluster.metadata.core.AbstractMappingMetadata;
import org.opensearch.cluster.node.DiscoveryNodeFilters;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;

import java.util.Map;
import java.util.Set;

/**
 * Internal model class for IndexMetadata - pure data holder
 */
public final class IndexModel {

    private final Index index;
    private final long version;
    private final long mappingVersion;
    private final long settingsVersion;
    private final long aliasesVersion;
    private final long[] primaryTerms;
    private final IndexMetadata.State state;
    private final int numberOfShards;
    private final int numberOfReplicas;
    private final int numberOfSearchOnlyReplicas;
    private final Settings settings;
    private final Map<String, AbstractMappingMetadata> mappings;
    private final Map<String, AliasMetadata> aliases;
    private final Map<String, DiffableStringMap> customData;
    private final Map<Integer, Set<String>> inSyncAllocationIds;
    private final DiscoveryNodeFilters requireFilters;
    private final DiscoveryNodeFilters initialRecoveryFilters;
    private final DiscoveryNodeFilters includeFilters;
    private final DiscoveryNodeFilters excludeFilters;
    private final Version indexCreatedVersion;
    private final Version indexUpgradedVersion;
    private final int routingNumShards;
    private final int routingPartitionSize;
    private final ShardCount waitForActiveShards;
    private final Map<String, RolloverInfo> rolloverInfos;
    private final boolean isSystem;
    private final int indexTotalShardsPerNodeLimit;
    private final int indexTotalPrimaryShardsPerNodeLimit;
    private final boolean isAppendOnlyIndex;
    private final Context context;
    private final IngestionStatus ingestionStatus;

    public IndexModel(
        final Index index,
        final long version,
        final long mappingVersion,
        final long settingsVersion,
        final long aliasesVersion,
        final long[] primaryTerms,
        final IndexMetadata.State state,
        final int numberOfShards,
        final int numberOfReplicas,
        final int numberOfSearchOnlyReplicas,
        final Settings settings,
        final Map<String, AbstractMappingMetadata> mappings,
        final Map<String, AliasMetadata> aliases,
        final Map<String, DiffableStringMap> customData,
        final Map<Integer, Set<String>> inSyncAllocationIds,
        final DiscoveryNodeFilters requireFilters,
        final DiscoveryNodeFilters initialRecoveryFilters,
        final DiscoveryNodeFilters includeFilters,
        final DiscoveryNodeFilters excludeFilters,
        final Version indexCreatedVersion,
        final Version indexUpgradedVersion,
        final int routingNumShards,
        final int routingPartitionSize,
        final ShardCount waitForActiveShards,
        final Map<String, RolloverInfo> rolloverInfos,
        final boolean isSystem,
        final int indexTotalShardsPerNodeLimit,
        final int indexTotalPrimaryShardsPerNodeLimit,
        boolean isAppendOnlyIndex,
        final Context context,
        final IngestionStatus ingestionStatus
    ) {
        this.index = index;
        this.version = version;
        this.mappingVersion = mappingVersion;
        this.settingsVersion = settingsVersion;
        this.aliasesVersion = aliasesVersion;
        this.primaryTerms = primaryTerms;
        this.state = state;
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
        this.numberOfSearchOnlyReplicas = numberOfSearchOnlyReplicas;
        this.settings = settings;
        this.mappings = mappings;
        this.aliases = aliases;
        this.customData = customData;
        this.inSyncAllocationIds = inSyncAllocationIds;
        this.requireFilters = requireFilters;
        this.initialRecoveryFilters = initialRecoveryFilters;
        this.includeFilters = includeFilters;
        this.excludeFilters = excludeFilters;
        this.indexCreatedVersion = indexCreatedVersion;
        this.indexUpgradedVersion = indexUpgradedVersion;
        this.routingNumShards = routingNumShards;
        this.routingPartitionSize = routingPartitionSize;
        this.waitForActiveShards = waitForActiveShards;
        this.rolloverInfos = rolloverInfos;
        this.isSystem = isSystem;
        this.indexTotalShardsPerNodeLimit = indexTotalShardsPerNodeLimit;
        this.indexTotalPrimaryShardsPerNodeLimit = indexTotalPrimaryShardsPerNodeLimit;
        this.isAppendOnlyIndex = isAppendOnlyIndex;
        this.context = context;
        this.ingestionStatus = ingestionStatus;
    }

    // Getter methods
    public Index getIndex() { return index; }
    public long getVersion() { return version; }
    public long getMappingVersion() { return mappingVersion; }
    public long getSettingsVersion() { return settingsVersion; }
    public long getAliasesVersion() { return aliasesVersion; }
    public long[] getPrimaryTerms() { return primaryTerms; }
    public IndexMetadata.State getIndexState() { return state; }
    public int getNumberOfShards() { return numberOfShards; }
    public int getNumberOfReplicas() { return numberOfReplicas; }
    public int getNumberOfSearchOnlyReplicas() { return numberOfSearchOnlyReplicas; }
    public Settings getSettings() { return settings; }
    public Map<String, AbstractMappingMetadata> getMappings() { return mappings; }
    public Map<String, AliasMetadata> getAliases() { return aliases; }
    public Map<String, DiffableStringMap> getCustomData() { return customData; }
    public Map<Integer, Set<String>> getInSyncAllocationIds() { return inSyncAllocationIds; }
    public DiscoveryNodeFilters requireFilters() { return requireFilters; }
    public DiscoveryNodeFilters getInitialRecoveryFilters() { return initialRecoveryFilters; }
    public DiscoveryNodeFilters includeFilters() { return includeFilters; }
    public DiscoveryNodeFilters excludeFilters() { return excludeFilters; }
    public Version getCreationVersion() { return indexCreatedVersion; }
    public Version getUpgradedVersion() { return indexUpgradedVersion; }
    public int getRoutingNumShards() { return routingNumShards; }
    public int getRoutingPartitionSize() { return routingPartitionSize; }
    public ShardCount getWaitForActiveShards() { return waitForActiveShards; }
    public Map<String, RolloverInfo> getRolloverInfos() { return rolloverInfos; }
    public boolean isSystem() { return isSystem; }
    public int getIndexTotalShardsPerNodeLimit() { return indexTotalShardsPerNodeLimit; }
    public int getIndexTotalPrimaryShardsPerNodeLimit() { return indexTotalPrimaryShardsPerNodeLimit; }
    public boolean isAppendOnlyIndex() { return isAppendOnlyIndex; }
    public Context context() { return context; }
    public IngestionStatus getIngestionStatus() { return ingestionStatus; }
}