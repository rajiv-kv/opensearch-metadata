# OpenSearch Metadata Common

This is an extracted standalone version of the `opensearch-metadata-common` library from the OpenSearch project.

## Structure

- `src/main/java/` - Main source code for metadata-common
- `libs/` - Required dependency libraries:
  - `cli/` - Command line interface utilities
  - `common/` - Common utilities and base classes
  - `core/` - Core OpenSearch functionality
  - `x-content/` - Content parsing and serialization

## Building

```bash
./gradlew build
```

## Dependencies

- Lucene 10.2.2
- Jackson 2.18.2
- Log4j 2.21.0
- Joda Time 2.12.7
- JOpt Simple 5.0.4
- SnakeYAML 2.1

## License

Apache License 2.0# opensearch-metadata
