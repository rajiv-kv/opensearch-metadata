/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.cluster.metadata.server;

import java.io.IOException;
import org.opensearch.Version;
import org.opensearch.cluster.Diff;
import org.opensearch.cluster.metadata.model.IndexModel;
import org.opensearch.cluster.metadata.model.ser.deser.IndexStreamSerializer;
import org.opensearch.cluster.metadata.model.ser.deser.IndexXContentSerializer;

import org.opensearch.cluster.metadata.core.AbstractMappingMetadata;
import org.opensearch.common.annotation.PublicApi;
import org.opensearch.common.settings.Setting;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;

/**
 * Index metadata information
 *
 * @opensearch.api
 */
@PublicApi(since = "3.2.0")
public final class IndexMetadata {

    // Essential constants moved from AbstractIndexMetadata
    public static final String SETTING_INDEX_UUID = "index.uuid";
    public static final String INDEX_UUID_NA_VALUE = "_na_";
    public static final String SETTING_NUMBER_OF_SHARDS = "index.number_of_shards";
    public static final String SETTING_NUMBER_OF_REPLICAS = "index.number_of_replicas";
    public static final String SETTING_NUMBER_OF_SEARCH_REPLICAS = "index.number_of_search_replicas";
    public static final String SETTING_ROUTING_PARTITION_SIZE = "index.routing_partition_size";
    public static final String SETTING_CREATION_DATE = "index.creation_date";
    public static final String SETTING_VERSION_UPGRADED = "index.version.upgraded";
    public static final String SETTING_WAIT_FOR_ACTIVE_SHARDS_KEY = "index.write.wait_for_active_shards";
    
    public static final Setting<Integer> INDEX_NUMBER_OF_SHARDS_SETTING = Setting.intSetting(SETTING_NUMBER_OF_SHARDS, 1, 1, 1024, Setting.Property.IndexScope);
    public static final Setting<Integer> INDEX_NUMBER_OF_REPLICAS_SETTING = Setting.intSetting(SETTING_NUMBER_OF_REPLICAS, 1, 0, Setting.Property.IndexScope, Setting.Property.Dynamic);
    public static final Setting<Integer> INDEX_NUMBER_OF_SEARCH_REPLICAS_SETTING = Setting.intSetting(SETTING_NUMBER_OF_SEARCH_REPLICAS, 0, 0, Setting.Property.IndexScope, Setting.Property.Dynamic);
    public static final Setting<Integer> INDEX_ROUTING_PARTITION_SIZE_SETTING = Setting.intSetting(SETTING_ROUTING_PARTITION_SIZE, 1, 1, Setting.Property.IndexScope);
    public static final Setting<String> SETTING_WAIT_FOR_ACTIVE_SHARDS = Setting.simpleString(SETTING_WAIT_FOR_ACTIVE_SHARDS_KEY, "1", Setting.Property.IndexScope, Setting.Property.Dynamic);
    
    // Ingestion settings for compatibility
    public static final Setting<Long> INGESTION_SOURCE_MAX_POLL_SIZE = Setting.longSetting("index.ingestion.source.max_poll_size", 1000L, 1L, Setting.Property.IndexScope);
    public static final Setting<Integer> INGESTION_SOURCE_POLL_TIMEOUT = Setting.intSetting("index.ingestion.source.poll_timeout", 30, 1, Setting.Property.IndexScope);
    public static final Setting<Integer> INGESTION_SOURCE_NUM_PROCESSOR_THREADS_SETTING = Setting.intSetting("index.ingestion.source.num_processor_threads", 1, 1, Setting.Property.IndexScope);
    public static final Setting<Integer> INGESTION_SOURCE_INTERNAL_QUEUE_SIZE_SETTING = Setting.intSetting("index.ingestion.source.internal_queue_size", 100, 1, Setting.Property.IndexScope);
    public static final Setting<Version> SETTING_INDEX_VERSION_CREATED = Setting.versionSetting("index.version.created", Version.CURRENT, Setting.Property.IndexScope, Setting.Property.PrivateIndex);
    public static final Setting<Boolean> INDEX_APPEND_ONLY_ENABLED_SETTING = Setting.boolSetting("index.append_only", false, Setting.Property.IndexScope);
    public static final Setting<Integer> INDEX_TOTAL_SHARDS_PER_NODE_SETTING = Setting.intSetting("index.routing.allocation.total_shards_per_node", -1, -1, Setting.Property.IndexScope, Setting.Property.Dynamic);
    public static final Setting<Integer> INDEX_TOTAL_PRIMARY_SHARDS_PER_NODE_SETTING = Setting.intSetting("index.routing.allocation.total_primary_shards_per_node", -1, -1, Setting.Property.IndexScope, Setting.Property.Dynamic);

    /**
     * The state of the index.
     *
     * @opensearch.api
     */
    @PublicApi(since = "3.2.0")
    public enum State {
        OPEN((byte) 0),
        CLOSE((byte) 1);

        private final byte id;

        State(byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }

        public static State fromId(byte id) {
            if (id == 0) {
                return OPEN;
            } else if (id == 1) {
                return CLOSE;
            }
            throw new IllegalStateException("No state match for id [" + id + "]");
        }

        public static State fromString(String state) {
            if ("open".equals(state)) {
                return OPEN;
            } else if ("close".equals(state)) {
                return CLOSE;
            }
            throw new IllegalStateException("No state match for [" + state + "]");
        }
    }

    final IndexModel model;

    IndexMetadata(IndexModel model) {
        this.model = model;
    }

    // Delegate all methods to the model
    public String getIndex() { return model.getIndex().getName(); }
    public long getVersion() { return model.getVersion(); }
    public long getMappingVersion() { return model.getMappingVersion(); }
    public long getSettingsVersion() { return model.getSettingsVersion(); }
    public long getAliasesVersion() { return model.getAliasesVersion(); }
    public State getState() { return model.getIndexState(); }
    public int getNumberOfShards() { return model.getNumberOfShards(); }
    public int getNumberOfReplicas() { return model.getNumberOfReplicas(); }
    public int getNumberOfSearchOnlyReplicas() { return model.getNumberOfSearchOnlyReplicas(); }

    public Diff<IndexMetadata> diff(IndexMetadata previousState) {
        return new IndexModelDiffWrapper(previousState, this);
    }

    public static Diff<IndexMetadata> readDiffFrom(StreamInput in) throws IOException {
        return new IndexModelDiffWrapper(in);
    }

    public static IndexMetadata readFrom(StreamInput in) throws IOException {
        IndexModel model = IndexStreamSerializer.getInstance().readFrom(in);
        return new IndexMetadata(model);
    }

    public void toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        IndexXContentSerializer.getInstance().toXContent(this.model, builder, params);
    }

    public static IndexMetadata fromXContent(XContentParser parser) throws IOException {
        IndexModel model = IndexXContentSerializer.getInstance().fromXContent(parser);
        return new IndexMetadata(model);
    }

    private static class IndexModelDiffWrapper implements Diff<IndexMetadata> {
        // Simplified diff implementation

        IndexModelDiffWrapper(IndexMetadata before, IndexMetadata after) {
            // Simplified diff
        }

        IndexModelDiffWrapper(StreamInput in) throws IOException {
            // Simplified diff
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            // Simplified diff serialization
        }

        @Override
        public IndexMetadata apply(IndexMetadata part) {
            return new IndexMetadata(part.model); // Simplified
        }
    }

    public static Builder builder(String index) {
        return new Builder(index);
    }

    public static Builder builder(IndexMetadata indexMetadata) {
        return new Builder(indexMetadata);
    }

    /**
     * Builder of index metadata.
     *
     * @opensearch.api
     */
    @PublicApi(since = "3.2.0")
    public static final class Builder {
        private String index;
        private State state = State.OPEN;
        private long version = 1;
        private long mappingVersion = 1;
        private long settingsVersion = 1;
        private long aliasesVersion = 1;
        private long[] primaryTerms = new long[]{1};
        private int numberOfShards = 1;
        private int numberOfReplicas = 0;
        private int numberOfSearchReplicas = 0;
        private org.opensearch.common.settings.Settings settings = org.opensearch.common.settings.Settings.EMPTY;
        private java.util.Map<String, AbstractMappingMetadata> mappings = new java.util.HashMap<>();
        private java.util.Map<String, org.opensearch.cluster.metadata.AliasMetadata> aliases = new java.util.HashMap<>();
        private java.util.Map<String, org.opensearch.cluster.metadata.DiffableStringMap> customData = new java.util.HashMap<>();
        private java.util.Map<Integer, java.util.Set<String>> inSyncAllocationIds = new java.util.HashMap<>();
        private int routingNumShards = 1;
        private int routingPartitionSize = 1;
        private boolean isSystem = false;
        private org.opensearch.cluster.metadata.Context context = null;
        private org.opensearch.cluster.metadata.IngestionStatus ingestionStatus = org.opensearch.cluster.metadata.IngestionStatus.getDefaultValue();

        public Builder(String index) {
            this.index = index;
        }

        public Builder(IndexMetadata indexMetadata) {
            IndexModel model = indexMetadata.model;
            this.index = model.getIndex().getName();
            this.state = model.getIndexState();
            this.version = model.getVersion();
            this.mappingVersion = model.getMappingVersion();
            this.settingsVersion = model.getSettingsVersion();
            this.aliasesVersion = model.getAliasesVersion();
            this.primaryTerms = model.getPrimaryTerms();
            this.numberOfShards = model.getNumberOfShards();
            this.numberOfReplicas = model.getNumberOfReplicas();
            this.numberOfSearchReplicas = model.getNumberOfSearchOnlyReplicas();
            this.settings = model.getSettings();
            this.mappings = new java.util.HashMap<>(model.getMappings());
            this.aliases = new java.util.HashMap<>(model.getAliases());
            this.customData = new java.util.HashMap<>(model.getCustomData());
            this.inSyncAllocationIds = new java.util.HashMap<>(model.getInSyncAllocationIds());
            this.routingNumShards = model.getRoutingNumShards();
            this.routingPartitionSize = model.getRoutingPartitionSize();
            this.isSystem = model.isSystem();
            this.context = model.context();
            this.ingestionStatus = model.getIngestionStatus();
        }

        public Builder index(String index) { this.index = index; return this; }
        
        public Builder numberOfShards(int numberOfShards) {
            this.numberOfShards = numberOfShards;
            this.settings = org.opensearch.common.settings.Settings.builder().put(settings).put(SETTING_NUMBER_OF_SHARDS, numberOfShards).build();
            return this;
        }
        
        public Builder setRoutingNumShards(int routingNumShards) { this.routingNumShards = routingNumShards; return this; }
        
        public Builder numberOfReplicas(int numberOfReplicas) {
            this.numberOfReplicas = numberOfReplicas;
            this.settings = org.opensearch.common.settings.Settings.builder().put(settings).put(SETTING_NUMBER_OF_REPLICAS, numberOfReplicas).build();
            return this;
        }
        
        public Builder numberOfSearchReplicas(int numberOfSearchReplicas) { this.numberOfSearchReplicas = numberOfSearchReplicas; return this; }
        
        public Builder routingPartitionSize(int routingPartitionSize) { this.routingPartitionSize = routingPartitionSize; return this; }
        
        public Builder creationDate(long creationDate) {
            this.settings = org.opensearch.common.settings.Settings.builder().put(settings).put(SETTING_CREATION_DATE, creationDate).build();
            return this;
        }
        
        public Builder settings(org.opensearch.common.settings.Settings.Builder settings) { return settings(settings.build()); }
        
        public Builder settings(org.opensearch.common.settings.Settings settings) { this.settings = settings; return this; }
        
        public AbstractMappingMetadata mapping() { return mappings.get("_doc"); }
        public Builder putMapping(String source) throws IOException {
            mappings.put("_doc", new AbstractMappingMetadata("_doc", java.util.Map.of()));
            return this;
        }
        public Builder putMapping(AbstractMappingMetadata mappingMd) {
            if (mappingMd != null) mappings.put(mappingMd.type(), mappingMd);
            return this;
        }
        public Builder state(State state) { this.state = state; return this; }
        public Builder putAlias(org.opensearch.cluster.metadata.AliasMetadata aliasMetadata) {
            aliases.put(aliasMetadata.alias(), aliasMetadata);
            return this;
        }
        public Builder putAlias(org.opensearch.cluster.metadata.AliasMetadata.Builder aliasMetadata) {
            return putAlias(aliasMetadata.build());
        }
        public Builder removeAlias(String alias) {
            aliases.remove(alias);
            return this;
        }
        public Builder removeAllAliases() {
            aliases.clear();
            return this;
        }
        public Builder putCustom(String type, java.util.Map<String, String> customIndexMetadata) {
            customData.put(type, new org.opensearch.cluster.metadata.DiffableStringMap(customIndexMetadata));
            return this;
        }
        public Builder putInSyncAllocationIds(int shardId, java.util.Set<String> allocationIds) {
            inSyncAllocationIds.put(shardId, new java.util.HashSet<>(allocationIds));
            return this;
        }
        public Builder putRolloverInfo(org.opensearch.action.admin.indices.rollover.RolloverInfo rolloverInfo) {
            return this; // Simplified - no rolloverInfos member variable
        }
        public Builder version(long version) { this.version = version; return this; }
        public Builder mappingVersion(final long mappingVersion) { this.mappingVersion = mappingVersion; return this; }
        public Builder settingsVersion(final long settingsVersion) { this.settingsVersion = settingsVersion; return this; }
        public Builder aliasesVersion(final long aliasesVersion) { this.aliasesVersion = aliasesVersion; return this; }
        public Builder primaryTerm(int shardId, long primaryTerm) {
            if (primaryTerms.length <= shardId) {
                long[] newTerms = new long[shardId + 1];
                System.arraycopy(primaryTerms, 0, newTerms, 0, primaryTerms.length);
                primaryTerms = newTerms;
            }
            primaryTerms[shardId] = primaryTerm;
            return this;
        }
        public Builder system(boolean system) { this.isSystem = system; return this; }
        public Builder context(org.opensearch.cluster.metadata.Context context) { this.context = context; return this; }
        public Builder ingestionStatus(org.opensearch.cluster.metadata.IngestionStatus ingestionStatus) { this.ingestionStatus = ingestionStatus; return this; }

        public IndexMetadata build() {
            return new IndexMetadata(new IndexModel(
                new org.opensearch.core.index.Index(index, settings.get(SETTING_INDEX_UUID, INDEX_UUID_NA_VALUE)),
                version, mappingVersion, settingsVersion, aliasesVersion, primaryTerms, state,
                numberOfShards, numberOfReplicas, numberOfSearchReplicas, settings, mappings, aliases,
                customData, inSyncAllocationIds, null, null, null, null,
                org.opensearch.Version.CURRENT, org.opensearch.Version.CURRENT,
                routingNumShards, routingPartitionSize, org.opensearch.action.support.ShardCount.ONE,
                new java.util.HashMap<>(), isSystem, -1, -1, false, context, ingestionStatus
            ));
        }
    }
}