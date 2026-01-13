# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### In Development
- Future features and improvements

## [0.1.1] - 2026-01-13

### Added
- Production-ready modular architecture with SOLID principles
- Support for publishing full artifacts (JAR + sources + javadoc) in addition to POM-only artifacts
- GitHub Actions workflows for CI/CD automation
  - CI Build workflow for automated testing
  - License header check workflow
  - Release workflow for automated Maven Central deployment
- Comprehensive architecture documentation (ARCHITECTURE.md)
- Model layer with `MavenCoordinates` and `ArtifactFile` classes
- Parser layer with `PomParser` interface and `DefaultPomParser` implementation
- Processor layer with `FileProcessor` interface and `DefaultFileProcessor` implementation
- Collector layer with `ArtifactCollector` for artifact discovery
- Bundle layer with `BundleBuilder` interface and `ZipBundleBuilder` implementation
- Service layer with `DeploymentService` for orchestration
- Configuration parameters: `includeJar`, `includeSources`, `includeJavadoc`
- Maven release profile for optimized build workflow
- XXE attack prevention in POM parsing
- Automatic cleanup of temporary signature files

### Changed
- Refactored main Mojo from 610 lines to 150 lines
- Improved separation of concerns with dedicated packages
- Enhanced error handling and logging throughout
- Updated README with comprehensive documentation

### Security
- Secure XML parsing with disabled external entity processing
- Path traversal prevention in coordinate validation
- Proper GPG passphrase handling

## [0.1.0] - 2025-12-01

### Added
- Initial release of Custom Central Publishing Maven Plugin
- Support for publishing custom POM artifacts to Maven Central
- Automatic GPG signing of artifacts
- MD5 and SHA-1 checksum generation
- Single deployment bundle for multiple artifacts
- Integration with Sonatype Central Publishing API
- Configurable auto-publishing
- Configurable deployment state waiting (UPLOADED, VALIDATED, PUBLISHING, PUBLISHED)
- Smart skipping for generated artifacts and child modules
- Proper Maven repository directory structure

### Features
- POM parsing to extract Maven coordinates
- Artifact collection from custom directories
- File processing with signing and checksums
- Bundle creation in ZIP format
- Upload to Maven Central
- State monitoring and publishing

### Configuration Parameters
- `bomProjectsDirectory` - Directory containing artifacts
- `bomProjects` - List of artifact names to deploy
- `publishingServerId` - Server ID in settings.xml
- `centralBaseUrl` - Maven Central base URL
- `autoPublish` - Automatic publishing flag
- `deploymentName` - Deployment identifier
- `tokenAuth` - Token authentication flag
- `waitUntil` - Deployment state to wait for
- `waitMaxTime` - Maximum wait time in seconds
- `waitPollingInterval` - Polling interval in seconds
- `gpgExecutable` - Path to GPG executable
- `gpg.passphrase` - GPG passphrase for signing
- `skipGpgSign` - Skip GPG signing flag
- `skipBomPublishing` - Skip plugin execution flag

### Requirements
- Maven 3.9.0 or higher
- Java 11 or higher
- GPG for artifact signing
- Maven Central account with credentials

---

## Release Notes Template

When creating a new release, copy this template:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes to existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security improvements
```

---

## Versioning

- **Major version (X.0.0)**: Breaking changes, major new features
- **Minor version (0.Y.0)**: New features, backward-compatible
- **Patch version (0.0.Z)**: Bug fixes, backward-compatible

## Links

- [Maven Central Repository](https://central.sonatype.com/artifact/io.github.agnistack/custom-central-publishing-maven-plugin)
- [GitHub Repository](https://github.com/ash-thakur-rh/custom-central-publishing-maven-plugin)
- [Issue Tracker](https://github.com/ash-thakur-rh/custom-central-publishing-maven-plugin/issues)

[Unreleased]: https://github.com/ash-thakur-rh/custom-central-publishing-maven-plugin/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/ash-thakur-rh/custom-central-publishing-maven-plugin/releases/tag/v0.1.1
[0.1.0]: https://github.com/ash-thakur-rh/custom-central-publishing-maven-plugin/releases/tag/v0.1.0
