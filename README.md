# Custom Central Publishing Maven Plugin

A Maven plugin for publishing custom artifacts (POM-only or full artifacts with JAR, sources, javadoc) generated in non-standard locations to Maven Central. It combines multiple artifacts into a single deployment bundle with automatic GPG signing and checksum generation.

## Overview

This plugin is designed to deploy artifacts that are generated in custom locations (like `target/classes`) to Maven Central. It's particularly useful for:
- Projects that generate multiple POM files dynamically in custom directories
- Projects that need to publish artifacts from non-standard output directories
- Combining multiple artifacts into a single Maven Central deployment bundle

## Features

- **Flexible Artifact Support**: Deploy POM-only artifacts (BOMs, parent POMs) or full artifacts (JAR + sources + javadoc)
- **Single Deployment Bundle**: Combines multiple artifacts into a single deployment
- **Automatic GPG Signing**: Signs all artifacts (POM, JAR, sources, javadoc) with GPG
- **Checksum Generation**: Generates MD5 and SHA-1 checksums for all files
- **Proper Repository Structure**: Creates correct Maven repository directory structure
- **Auto-Publishing Support**: Optional automatic publishing to Maven Central
- **Configurable Deployment States**: Wait for UPLOADED, VALIDATED, PUBLISHING, or PUBLISHED state
- **Official API**: Uses the Sonatype Central Publishing API
- **Smart Skipping**: Auto-skips execution on generated artifacts and child modules

## Requirements

- Maven 3.9.0 or higher
- Java 11 or higher
- GPG (for artifact signing)
- Maven Central account with credentials

## Installation

Add the plugin to your project's `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>io.github.agnistack</groupId>
      <artifactId>custom-central-publishing-maven-plugin</artifactId>
      <version>0.1.1</version>
    </plugin>
  </plugins>
</build>
```

## Configuration

### Maven Settings (settings.xml)

Configure your Maven Central credentials in `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>your-token-username</username>
      <password>your-token-password</password>
    </server>
  </servers>
</settings>
```

### Basic Plugin Configuration

```xml
<plugin>
  <groupId>io.github.agnistack</groupId>
  <artifactId>custom-central-publishing-maven-plugin</artifactId>
  <version>0.1.1</version>
  <configuration>
    <bomProjectsDirectory>${project.build.directory}/classes</bomProjectsDirectory>
    <bomProjects>
      <bomProject>my-project-bom</bomProject>
      <bomProject>my-project-bom-with-deps</bomProject>
    </bomProjects>
    <autoPublish>false</autoPublish>
    <waitUntil>VALIDATED</waitUntil>
    <skipGpgSign>false</skipGpgSign>
  </configuration>
</plugin>
```

### Advanced Configuration with All Options

```xml
<plugin>
  <groupId>io.github.agnistack</groupId>
  <artifactId>custom-central-publishing-maven-plugin</artifactId>
  <version>0.1.1</version>
  <inherited>false</inherited>  <!-- Only run on parent POM -->
  <executions>
    <execution>
      <id>publish-custom-artifacts</id>
      <phase>deploy</phase>
      <goals>
        <goal>publish-custom</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <!-- Deployment Settings -->
    <deploymentName>my-project-boms</deploymentName>
    <publishingServerId>central</publishingServerId>
    <centralBaseUrl>https://central.sonatype.com</centralBaseUrl>
    <autoPublish>false</autoPublish>
    <waitUntil>VALIDATED</waitUntil>
    <waitMaxTime>600</waitMaxTime>
    <waitPollingInterval>10</waitPollingInterval>

    <!-- Artifact Locations -->
    <bomProjectsDirectory>${project.build.directory}/classes</bomProjectsDirectory>
    <bomProjects>
      <bomProject>my-project-bom</bomProject>
      <bomProject>my-project-bom-with-deps</bomProject>
    </bomProjects>

    <!-- Optional: Include full artifacts (JAR, sources, javadoc) -->
    <includeJar>false</includeJar>
    <includeSources>false</includeSources>
    <includeJavadoc>false</includeJavadoc>

    <!-- GPG Signing -->
    <skipGpgSign>false</skipGpgSign>
    <gpgExecutable>gpg</gpgExecutable>
  </configuration>
</plugin>
```

## Usage

### Publishing Custom POM Artifacts

#### Command Line Execution

```bash
mvn io.github.agnistack:custom-central-publishing-maven-plugin:0.1.1:publish-custom \
  -DbomProjectsDirectory=${project.build.directory}/classes \
  -DbomProjects=my-project-bom,my-project-bom-with-deps
```

#### Lifecycle Integration

Add to your `pom.xml`:

```xml
<plugin>
  <groupId>io.github.agnistack</groupId>
  <artifactId>custom-central-publishing-maven-plugin</artifactId>
  <version>0.1.1</version>
  <inherited>false</inherited>
  <executions>
    <execution>
      <id>publish-custom-artifacts</id>
      <phase>deploy</phase>
      <goals>
        <goal>publish-custom</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <bomProjectsDirectory>${project.build.directory}/classes</bomProjectsDirectory>
    <bomProjects>
      <bomProject>my-project-bom</bomProject>
    </bomProjects>
  </configuration>
</plugin>
```

Then run:

```bash
mvn deploy
```

### Publishing Full Artifacts (JAR + sources + javadoc)

The plugin can also publish full artifacts with JAR, sources, and javadoc files. Just enable the flags:

```xml
<plugin>
  <groupId>io.github.agnistack</groupId>
  <artifactId>custom-central-publishing-maven-plugin</artifactId>
  <version>0.1.0</version>
  <configuration>
    <bomProjectsDirectory>${project.build.directory}/custom-artifacts</bomProjectsDirectory>
    <bomProjects>
      <bomProject>my-library</bomProject>
    </bomProjects>
    <!-- Enable full artifact publishing -->
    <includeJar>true</includeJar>
    <includeSources>true</includeSources>
    <includeJavadoc>true</includeJavadoc>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>publish-custom</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

**Directory Structure Required:**
```
target/custom-artifacts/
  └── my-library/
      ├── pom.xml
      ├── my-library-1.0.0.jar
      ├── my-library-1.0.0-sources.jar
      └── my-library-1.0.0-javadoc.jar
```

The plugin will:
- Sign all files (POM, JAR, sources, javadoc) with GPG
- Generate MD5 and SHA-1 checksums for each file and signature
- Create the proper Maven repository directory structure
- Combine everything into a single deployment bundle

## Configuration Parameters

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `bomProjectsDirectory` | Yes | - | Directory containing custom artifacts (e.g., `${project.build.directory}/classes`) |
| `bomProjects` | Yes | - | List of artifact directory names to deploy |
| `deploymentName` | No | `${project.groupId}:${project.artifactId}:${project.version}` | Human-readable deployment identifier |
| `includeJar` | No | `false` | Include JAR files in the deployment bundle |
| `includeSources` | No | `false` | Include sources JAR files in the deployment bundle |
| `includeJavadoc` | No | `false` | Include javadoc JAR files in the deployment bundle |
| `publishingServerId` | No | `central` | Server ID in settings.xml |
| `centralBaseUrl` | No | `https://central.sonatype.com` | Maven Central base URL |
| `autoPublish` | No | `true` | Automatically publish after validation |
| `deploymentName` | No | `${project.groupId}:${project.artifactId}:${project.version}` | Deployment identifier |
| `tokenAuth` | No | `true` | Use token authentication (vs basic auth) |
| `waitUntil` | No | `VALIDATED` | Wait until this state: `UPLOADED`, `VALIDATED`, `PUBLISHING`, `PUBLISHED` |
| `waitMaxTime` | No | `600` | Maximum wait time in seconds |
| `waitPollingInterval` | No | `10` | Polling interval in seconds |
| `gpgExecutable` | No | `gpg` | Path to GPG executable |
| `gpg.passphrase` | No | - | GPG passphrase for signing |
| `skipGpgSign` | No | `false` | Skip GPG signing (not recommended for production) |

## Complete Release Profile Example

Configure a release profile in your `pom.xml` to deploy custom artifacts alongside regular modules:

```xml
<distributionManagement>
  <snapshotRepository>
    <id>central</id>
    <name>Central Portal</name>
    <url>https://central.sonatype.com/</url>
  </snapshotRepository>
</distributionManagement>

<profiles>
  <profile>
    <id>release</id>
    <build>
      <plugins>
        <!-- Custom Central Publishing Plugin for custom artifacts -->
        <plugin>
          <groupId>io.github.agnistack</groupId>
          <artifactId>custom-central-publishing-maven-plugin</artifactId>
          <version>0.1.1</version>
          <inherited>false</inherited>
          <executions>
            <execution>
              <id>publish-custom-artifacts</id>
              <phase>deploy</phase>
              <goals>
                <goal>publish-custom</goal>
              </goals>
            </execution>
          </executions>
          <configuration>
            <deploymentName>my-project-custom-artifacts</deploymentName>
            <publishingServerId>central</publishingServerId>
            <bomProjectsDirectory>${project.build.directory}/classes</bomProjectsDirectory>
            <bomProjects>
              <bomProject>my-project-bom</bomProject>
              <bomProject>my-project-bom-with-deps</bomProject>
            </bomProjects>
            <autoPublish>false</autoPublish>
            <waitUntil>VALIDATED</waitUntil>
            <skipGpgSign>false</skipGpgSign>
          </configuration>
        </plugin>

        <!-- GPG Signing for all artifacts -->
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-gpg-plugin</artifactId>
          <version>3.2.8</version>
          <executions>
            <execution>
              <id>sign-artifacts</id>
              <phase>verify</phase>
              <goals>
                <goal>sign</goal>
              </goals>
              <configuration>
                <gpgArguments>
                  <argument>--batch</argument>
                  <arg>--pinentry-mode</arg>
                  <arg>loopback</arg>
                </gpgArguments>
              </configuration>
            </execution>
          </executions>
        </plugin>

        <!-- Standard Maven Central Plugin (for regular modules) -->
        <plugin>
          <groupId>org.sonatype.central</groupId>
          <artifactId>central-publishing-maven-plugin</artifactId>
          <version>0.10.0</version>
          <extensions>true</extensions>
          <configuration>
            <publishingServerId>central</publishingServerId>
            <autoPublish>false</autoPublish>
          </configuration>
        </plugin>

        <!-- Attach sources and javadocs (for regular modules) -->
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-source-plugin</artifactId>
          <version>3.3.1</version>
          <executions>
            <execution>
              <id>attach-sources</id>
              <goals>
                <goal>jar-no-fork</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-javadoc-plugin</artifactId>
          <version>3.12.0</version>
          <executions>
            <execution>
              <id>attach-javadocs</id>
              <goals>
                <goal>jar</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

### Deploy to Maven Central

```bash
mvn deploy -Prelease
```

This will:
1. Sign all artifacts with GPG
2. Deploy custom artifacts from `${project.build.directory}/classes` to Maven Central
3. Deploy regular modules (JAR, sources, javadoc) to Maven Central
4. Wait for validation before completing

## How It Works

1. **Validation**: Validates that the artifacts directory exists and contains the specified projects
2. **Bundle Creation**: For each POM artifact:
   - Reads the `pom.xml` to extract coordinates (groupId, artifactId, version)
   - Signs it with GPG (creates `.asc` signature file)
   - Generates MD5 and SHA-1 checksums for both POM and signature files
   - Creates proper Maven repository directory structure
   - Combines all artifacts into a single deployment bundle ZIP file
3. **Upload**: Uploads the combined bundle to Maven Central using the Central Publishing API
4. **Publish**: Optionally publishes the deployment (if `autoPublish=true`)
5. **Wait**: Optionally waits for the deployment to reach the desired state

## Troubleshooting

### GPG Signing Failures

If GPG signing fails, ensure:
- GPG is installed and in your PATH
- Your GPG key is configured
- The passphrase is correct (if using `gpg.passphrase`)

You can test GPG manually:
```bash
gpg --detach-sign --armor pom.xml
```

### Authentication Failures

Ensure your credentials in `settings.xml` are correct:
- Username should be your Maven Central token username
- Password should be your Maven Central token password
- Generate tokens at: https://central.sonatype.com/account

### Directory Not Found

Verify the `bomProjectsDirectory` path:
```bash
ls -la target/classes/my-project-bom/
```

Expected structure:
```
target/classes/
├── my-project-bom/
│   └── pom.xml
└── my-project-bom-with-deps/
    └── pom.xml
```

## Publishing the Plugin to Maven Central

This plugin is configured to publish itself to Maven Central using the `central-publishing-maven-plugin`. Follow these steps to publish a new version:

### Prerequisites

1. **Maven Central Account**: Sign up at https://central.sonatype.com
2. **GPG Key**: Generate and publish a GPG key for signing artifacts
3. **Credentials**: Configure your credentials in `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>your-central-token-username</username>
      <password>your-central-token-password</password>
    </server>
  </servers>
</settings>
```

### Publishing Steps

1. **Update Version**: Update the version in `pom.xml` (e.g., from `0.1.0` to `0.2.0`)

2. **Build and Deploy**:

```bash
# Clean build with all artifacts
mvn clean verify

# Deploy to Maven Central (will prompt for GPG passphrase)
mvn deploy
```

3. **Manual Publish** (if autoPublish is false):
   - Visit https://central.sonatype.com
   - Navigate to "Deployments"
   - Find your deployment and click "Publish"

### What Gets Published

The build creates and publishes:
- Main plugin JAR (`custom-central-publishing-maven-plugin-0.1.0.jar`)
- Sources JAR (`custom-central-publishing-maven-plugin-0.1.0-sources.jar`)
- Javadoc JAR (`custom-central-publishing-maven-plugin-0.1.0-javadoc.jar`)
- GPG signatures (`.asc` files) for all JARs
- POM file with all metadata

### Automated Publishing Configuration

The plugin is configured with:
- **maven-source-plugin**: Generates source JAR
- **maven-javadoc-plugin**: Generates javadoc JAR
- **maven-gpg-plugin**: Signs all artifacts with GPG
- **central-publishing-maven-plugin**: Handles upload and publishing

See pom.xml:176-188 for the central-publishing-maven-plugin configuration.

## GitHub Actions CI/CD

This project includes automated GitHub Actions workflows:

- **CI Build**: Runs on every push and pull request to validate builds and tests
- **License Check**: Ensures all Java files have proper Apache License v2 headers
- **Release**: Automated release workflow to publish to Maven Central

See [.github/workflows/README.md](.github/workflows/README.md) for detailed documentation on:
- How to trigger releases
- Required GitHub secrets configuration
- Troubleshooting common issues

### Quick Release Guide

1. Go to **Actions** tab → **Release to Maven Central**
2. Click **Run workflow**
3. Enter release version (e.g., `0.1.0`) and next version (e.g., `0.1.1-SNAPSHOT`)
4. The workflow will build, sign, deploy to Maven Central, and create a GitHub release

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

**Before submitting a PR:**
- Ensure all tests pass: `mvn clean verify`
- Add license headers: `mvn license:format`
- Follow the existing code style

## License

Apache License, Version 2.0 - see the LICENSE section in pom.xml

## References

- [Maven Central Publishing Documentation](https://central.sonatype.org/publish/publish-portal-maven/)
- [Central Publishing Maven Plugin](https://github.com/sonatype/central-publishing-maven-plugin)
- [Portal Publisher API](https://central.sonatype.org/publish/publish-portal-api/)
