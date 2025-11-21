/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.cluster.metadata.model.ser.deser;

import org.opensearch.Version;
import org.opensearch.action.support.ShardCount;
import org.opensearch.cluster.metadata.IngestionStatus;
import org.opensearch.cluster.metadata.model.IndexModel;
import org.opensearch.cluster.metadata.server.IndexMetadata;
import org.opensearch.cluster.metadata.core.XContentSerializer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;

import java.io.IOException;
import java.util.HashMap;

/**
 * XContent serialization for IndexModel
 */
public final class IndexXContentSerializer implements XContentSerializer<IndexModel> {

    private static final IndexXContentSerializer INSTANCE = new IndexXContentSerializer();

    public static IndexXContentSerializer getInstance() {
        return INSTANCE;
    }

    private IndexXContentSerializer() {}

    public void toXContent(IndexModel model, XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field("version", model.getVersion());
        builder.field("mapping_version", model.getMappingVersion());
        builder.field("settings_version", model.getSettingsVersion());
        builder.field("aliases_version", model.getAliasesVersion());
        builder.field("routing_num_shards", model.getRoutingNumShards());
        builder.field("state", model.getIndexState().name().toLowerCase());
        
        builder.startObject("settings");
        model.getSettings().toXContent(builder, params);
        builder.endObject();
        
        builder.array("primary_terms", model.getPrimaryTerms());
        builder.field("system", model.isSystem());
    }

    @Override
    public IndexModel fromXContent(XContentParser parser) throws IOException {
        String indexName = null;
        long version = -1;
        long mappingVersion = 1;
        long settingsVersion = 1;
        long aliasesVersion = 1;
        int routingNumShards = -1;
        IndexMetadata.State state = null;
        Settings settings = Settings.EMPTY;
        long[] primaryTerms = null;
        boolean isSystem = false;
        
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                String fieldName = parser.currentName();
                parser.nextToken();
                
                switch (fieldName) {
                    case "version":
                        version = parser.longValue();
                        break;
                    case "mapping_version":
                        mappingVersion = parser.longValue();
                        break;
                    case "settings_version":
                        settingsVersion = parser.longValue();
                        break;
                    case "aliases_version":
                        aliasesVersion = parser.longValue();
                        break;
                    case "routing_num_shards":
                        routingNumShards = parser.intValue();
                        break;
                    case "state":
                        state = IndexMetadata.State.fromString(parser.text());
                        break;
                    case "settings":
                        settings = Settings.fromXContent(parser);
                        break;
                    case "primary_terms":
                        primaryTerms = parser.list().stream().mapToLong(o -> ((Number) o).longValue()).toArray();
                        break;
                    case "system":
                        isSystem = parser.booleanValue();
                        break;
                }
            }
        }
        
        String uuid = settings.get(IndexMetadata.SETTING_INDEX_UUID, IndexMetadata.INDEX_UUID_NA_VALUE);
        Version indexCreatedVersion = IndexMetadata.SETTING_INDEX_VERSION_CREATED.get(settings);
        
        return new IndexModel(
            new Index(indexName != null ? indexName : "unknown", uuid),
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