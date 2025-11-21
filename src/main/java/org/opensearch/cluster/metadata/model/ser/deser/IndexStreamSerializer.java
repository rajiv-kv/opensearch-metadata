/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.cluster.metadata.model.ser.deser;

import org.opensearch.Version;
import org.opensearch.action.support.ShardCount;
import org.opensearch.cluster.metadata.IngestionStatus;
import org.opensearch.cluster.metadata.model.IndexModel;
import org.opensearch.cluster.metadata.server.IndexMetadata;
import org.opensearch.cluster.metadata.core.StreamSerializer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.index.Index;

import java.io.IOException;
import java.util.HashMap;

/**
 * Stream serialization for IndexModel
 */
public final class IndexStreamSerializer implements StreamSerializer<IndexModel> {

    private static final IndexStreamSerializer INSTANCE = new IndexStreamSerializer();

    public static IndexStreamSerializer getInstance() {
        return INSTANCE;
    }

    private IndexStreamSerializer() {}

    @Override
    public void writeTo(IndexModel model, StreamOutput out) throws IOException {
        out.writeString(model.getIndex().getName());
        out.writeLong(model.getVersion());
        out.writeVLong(model.getMappingVersion());
        out.writeVLong(model.getSettingsVersion());
        out.writeVLong(model.getAliasesVersion());
        out.writeInt(model.getRoutingNumShards());
        out.writeByte(model.getIndexState().id());
        
        Settings.writeSettingsToStream(model.getSettings(), out);
        out.writeVLongArray(model.getPrimaryTerms());
        
        // Simplified - would need full implementation for production
        out.writeVInt(0); // mappings size
        out.writeVInt(0); // aliases size
        out.writeVInt(0); // custom data size
        out.writeVInt(0); // in sync allocation ids size
        out.writeVInt(0); // rollover infos size
        out.writeBoolean(model.isSystem());
    }

    @Override
    public IndexModel readFrom(StreamInput in) throws IOException {
        String indexName = in.readString();
        long version = in.readLong();
        long mappingVersion = in.readVLong();
        long settingsVersion = in.readVLong();
        long aliasesVersion = in.readVLong();
        int routingNumShards = in.readInt();
        IndexMetadata.State state = IndexMetadata.State.fromId(in.readByte());
        
        Settings settings = Settings.readSettingsFromStream(in);
        long[] primaryTerms = in.readVLongArray();
        
        // Skip complex objects for now
        in.readVInt(); // mappings
        in.readVInt(); // aliases  
        in.readVInt(); // custom data
        in.readVInt(); // in sync allocation ids
        in.readVInt(); // rollover infos
        boolean isSystem = in.readBoolean();
        
        String uuid = settings.get(IndexMetadata.SETTING_INDEX_UUID, IndexMetadata.INDEX_UUID_NA_VALUE);
        Version indexCreatedVersion = IndexMetadata.SETTING_INDEX_VERSION_CREATED.get(settings);
        
        return new IndexModel(
            new Index(indexName, uuid),
            version, mappingVersion, settingsVersion, aliasesVersion, primaryTerms, state,
            IndexMetadata.INDEX_NUMBER_OF_SHARDS_SETTING.get(settings),
            IndexMetadata.INDEX_NUMBER_OF_REPLICAS_SETTING.get(settings),
            IndexMetadata.INDEX_NUMBER_OF_SEARCH_REPLICAS_SETTING.get(settings),
            settings,
            new HashMap<>(), // mappings
            new HashMap<>(), // aliases
            new HashMap<>(), // customData
            new HashMap<>(), // inSyncAllocationIds
            null, null, null, null, // filters
            indexCreatedVersion,
            settings.getAsVersion(IndexMetadata.SETTING_VERSION_UPGRADED, indexCreatedVersion),
            routingNumShards,
            IndexMetadata.INDEX_ROUTING_PARTITION_SIZE_SETTING.get(settings),
            ShardCount.ONE, // Default wait for active shards
            new HashMap<>(), // rolloverInfos
            isSystem,
            IndexMetadata.INDEX_TOTAL_SHARDS_PER_NODE_SETTING.get(settings),
            IndexMetadata.INDEX_TOTAL_PRIMARY_SHARDS_PER_NODE_SETTING.get(settings),
            IndexMetadata.INDEX_APPEND_ONLY_ENABLED_SETTING.get(settings),
            null, // context
            IngestionStatus.getDefaultValue()
        );
    }
}