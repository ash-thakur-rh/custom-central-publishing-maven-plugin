# Architecture

This document describes the production-ready architecture of the Custom Central Publishing Maven Plugin.

## Design Principles

The plugin follows these SOLID principles:

1. **Single Responsibility**: Each class has one well-defined responsibility
2. **Open/Closed**: Extension points for customization without modifying existing code
3. **Liskov Substitution**: Interfaces can be swapped with alternative implementations
4. **Interface Segregation**: Small, focused interfaces
5. **Dependency Inversion**: Depends on abstractions, not concrete implementations

## Architecture Overview

```
PublishCustomArtifactMojo (Main Plugin Entry Point)
    ├── DeploymentService (Orchestration)
    │   ├── PomParser (POM Parsing)
    │   ├── ArtifactCollector (Artifact Discovery)
    │   ├── FileProcessor (Signing & Checksums)
    │   └── BundleBuilder (Bundle Creation)
    ├── ArtifactUploader (Maven Central Upload)
    └── DeploymentPublishedWatcher (State Monitoring)
```

## Component Breakdown

### 1. Model Layer (`io.github.agnistack.publishing.model`)

#### `MavenCoordinates`
- Immutable value object representing Maven GAV (GroupId, ArtifactId, Version)
- Provides utility methods for repository paths and file naming
- Thread-safe, equality-based

#### `ArtifactFile`
- Represents a single artifact file with metadata
- Enum for artifact types: POM, JAR, SOURCES, JAVADOC, SIGNATURE, CHECKSUM
- Encapsulates file existence checks

### 2. Parser Layer (`io.github.agnistack.publishing.parser`)

#### `PomParser` (Interface)
- Contract for parsing POM files

#### `DefaultPomParser` (Implementation)
- DOM-based XML parsing
- XXE attack prevention (secure processing enabled)
- Extracts coordinates from POM and parent POM
- Validates required fields

**Security Features:**
- Disables DOCTYPE declarations
- Disables external entity processing
- Prevents XML External Entity (XXE) attacks

### 3. Processor Layer (`io.github.agnistack.publishing.processor`)

#### `FileProcessor` (Interface)
- Contract for file processing operations

#### `DefaultFileProcessor` (Implementation)
- GPG signing using subprocess
- Checksum generation (MD5, SHA-1)
- Configurable GPG executable and passphrase
- Thread interruption handling

**Features:**
- Automatic signature file generation
- Multiple checksum algorithms
- Graceful error handling

### 4. Collector Layer (`io.github.agnistack.publishing.collector`)

#### `ArtifactCollector`
- Discovers artifacts based on configuration flags
- Builds list of `ArtifactFile` objects
- Handles optional artifacts (JAR, sources, javadoc)

**Configuration-driven:**
- `includeJar`: Include JAR files
- `includeSources`: Include sources JARs
- `includeJavadoc`: Include javadoc JARs

### 5. Bundle Layer (`io.github.agnistack.publishing.bundle`)

#### `BundleBuilder` (Interface)
- Contract for creating deployment bundles
- Implements `Closeable` for resource management

#### `ZipBundleBuilder` (Implementation)
- Creates ZIP-based deployment bundles
- Proper Maven repository structure
- Try-with-resources compatible

**Features:**
- Automatic directory structure creation
- Maven repository path formatting
- Efficient streaming to avoid memory issues

### 6. Service Layer (`io.github.agnistack.publishing.service`)

#### `DeploymentService`
- Orchestrates the entire deployment process
- Coordinates between all components
- Manages signature file lifecycle
- Handles temporary file cleanup

**Responsibilities:**
1. Parse POM to get coordinates
2. Collect artifact files
3. Process each file (sign + checksum)
4. Add to bundle with proper structure
5. Clean up temporary files

### 7. Plugin Layer (`io.github.agnistack.publishing`)

#### `PublishCustomArtifactMojo`
- Maven plugin entry point
- Configuration parameter validation
- Maven Central client initialization
- Delegates to `DeploymentService`

**Simplified responsibilities:**
- Parameter validation
- Service initialization
- Error handling and logging

## Data Flow

1. **Initialization**
   ```
   Mojo → Create Services (PomParser, Collector, FileProcessor, DeploymentService)
   ```

2. **For Each Artifact**
   ```
   PomParser → Extract Coordinates
   ArtifactCollector → Find Files (POM, JAR, sources, javadoc)
   DeploymentService → Process Each File:
      ├── Sign with GPG
      ├── Generate Checksums
      └── Add to Bundle
   ```

3. **Finalization**
   ```
   BundleBuilder → Close Bundle
   Mojo → Upload to Maven Central
   Mojo → Wait for Validation/Publishing
   ```

## Extension Points

### Custom POM Parser
Implement `PomParser` interface for alternative parsing strategies:
```java
public class CustomPomParser implements PomParser {
    @Override
    public MavenCoordinates parse(File pomFile) throws IOException {
        // Custom parsing logic
    }
}
```

### Custom File Processor
Implement `FileProcessor` interface for alternative signing/checksum methods:
```java
public class CustomFileProcessor implements FileProcessor {
    // Custom signing and checksum logic
}
```

### Custom Bundle Format
Implement `BundleBuilder` interface for non-ZIP bundles:
```java
public class TarBundleBuilder implements BundleBuilder {
    // TAR.GZ bundle creation
}
```

## Error Handling Strategy

1. **Checked Exceptions**: `IOException` for all I/O operations
2. **Unchecked Exceptions**: `IllegalArgumentException` for invalid inputs
3. **Cleanup**: Try-with-resources and finally blocks ensure cleanup
4. **Logging**: Comprehensive logging at INFO, WARN, and DEBUG levels

## Testing Strategy

### Unit Tests
- Model classes: Equality, hash code, immutability
- Parsers: Valid/invalid POM files, XXE attacks
- Processors: Signing, checksum generation
- Collectors: File discovery with various configurations

### Integration Tests
- End-to-end artifact processing
- Bundle creation and verification
- Maven Central API interaction (mocked)

### Test Doubles
- Mock `FileProcessor` for testing without GPG
- Mock `PomParser` for testing with synthetic coordinates
- Mock `BundleBuilder` for testing without I/O

## Performance Considerations

1. **Streaming**: Files are streamed to avoid loading entire artifacts in memory
2. **Buffer Size**: 8KB buffers for efficient I/O
3. **Parallel Processing**: Can be extended for parallel artifact processing
4. **Resource Management**: Proper use of try-with-resources prevents leaks

## Security Considerations

1. **XXE Prevention**: Secure XML parsing configuration
2. **Path Traversal**: Coordinate validation prevents directory traversal
3. **GPG Security**: Passphrase handling, secure subprocess execution
4. **Input Validation**: All inputs validated before processing

## Future Enhancements

1. **Parallel Processing**: Process multiple artifacts concurrently
2. **Caching**: Cache parsed POMs to avoid re-parsing
3. **Custom Checksum Algorithms**: Support SHA-256, SHA-512
4. **Custom Bundle Formats**: Support TAR.GZ bundles
5. **Artifact Verification**: Verify artifact signatures before upload
6. **Dry Run Mode**: Simulate deployment without uploading
7. **Resume Capability**: Resume interrupted deployments

## Dependencies

### Maven Plugin API
- `maven-plugin-api`: Core plugin functionality
- `maven-plugin-annotations`: Plugin configuration

### Maven Components
- `maven-core`: Access to MavenSession and MavenProject
- `maven-compat`: Compatibility layer

### Central Publishing API
- `central-publishing-maven-plugin`: Upload and state management

### XML Processing
- Java DOM API (built-in): Secure XML parsing

### Security
- Java Security API (built-in): Message digests
- GPG (external): Artifact signing

## Maintainability

1. **Clear Separation**: Each layer has distinct responsibility
2. **Interfaces**: Easy to mock and test
3. **Immutability**: Model objects are immutable
4. **Documentation**: Comprehensive JavaDoc on all public APIs
5. **Error Messages**: Clear, actionable error messages
6. **Logging**: Detailed logging for troubleshooting