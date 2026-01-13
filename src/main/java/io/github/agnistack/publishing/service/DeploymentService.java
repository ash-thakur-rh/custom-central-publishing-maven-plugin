/*-
 * #%L
 * custom-central-publishing-maven-plugin
 * %%
 * Copyright (C) 2026 Ashish Thakur
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.agnistack.publishing.service;

import io.github.agnistack.publishing.bundle.BundleBuilder;
import io.github.agnistack.publishing.collector.ArtifactCollector;
import io.github.agnistack.publishing.model.ArtifactFile;
import io.github.agnistack.publishing.model.MavenCoordinates;
import io.github.agnistack.publishing.parser.PomParser;
import io.github.agnistack.publishing.processor.FileProcessor;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for deploying artifacts to Maven Central.
 * Orchestrates POM parsing, artifact collection, file processing, and bundle building.
 *
 * @since 0.1.0
 */
public class DeploymentService {

  private final PomParser pomParser;
  private final ArtifactCollector artifactCollector;
  private final FileProcessor fileProcessor;
  private final Log log;

  public DeploymentService(PomParser pomParser, ArtifactCollector artifactCollector,
                           FileProcessor fileProcessor, Log log) {
    this.pomParser = pomParser;
    this.artifactCollector = artifactCollector;
    this.fileProcessor = fileProcessor;
    this.log = log;
  }

  /**
   * Processes an artifact directory and adds all files to the bundle.
   *
   * @param pomFile       the POM file
   * @param bundleBuilder the bundle builder
   * @throws IOException if processing fails
   */
  public void processArtifact(File pomFile, BundleBuilder bundleBuilder) throws IOException {
    // Parse POM to get coordinates
    MavenCoordinates coordinates = pomParser.parse(pomFile);
    log.info("  Adding " + coordinates);

    File artifactDir = pomFile.getParentFile();

    // Collect all artifact files
    List<ArtifactFile> artifacts = artifactCollector.collect(artifactDir, coordinates);

    // Track signature files for cleanup
    List<File> signatureFiles = new ArrayList<>();

    try {
      // Process each artifact file
      for (ArtifactFile artifact : artifacts) {
        if (!artifact.exists()) {
          if (artifact.getType() == ArtifactFile.ArtifactType.POM) {
            throw new IOException("Required POM file not found: " + artifact.getFile());
          }
          log.warn("    " + artifact.getType() + " file not found: " + artifact.getFile().getAbsolutePath());
          continue;
        }

        log.info("    Adding " + artifact.getType() + ": " + artifact.getFileName());
        processFile(artifact.getFile(), artifact.getFileName(), coordinates, bundleBuilder, signatureFiles);
      }
    } finally {
      // Clean up signature files
      for (File sigFile : signatureFiles) {
        if (sigFile != null && sigFile.exists()) {
          try {
            Files.delete(sigFile.toPath());
          } catch (IOException e) {
            log.warn("Failed to delete signature file: " + sigFile, e);
          }
        }
      }
    }
  }

  /**
   * Processes a single file: signs it, generates checksums, and adds to bundle.
   */
  private void processFile(File file, String fileName, MavenCoordinates coordinates,
                           BundleBuilder bundleBuilder, List<File> signatureFiles) throws IOException {
    // Add the main file
    bundleBuilder.addFile(file, coordinates, fileName);

    // Generate and add checksums
    addChecksum(file, fileName + ".md5", "MD5", coordinates, bundleBuilder);
    addChecksum(file, fileName + ".sha1", "SHA-1", coordinates, bundleBuilder);

    // Sign the file if enabled
    if (fileProcessor.isSigningEnabled()) {
      File signatureFile = fileProcessor.signFile(file);
      if (signatureFile != null && signatureFile.exists()) {
        signatureFiles.add(signatureFile);

        // Add signature and its checksums
        bundleBuilder.addFile(signatureFile, coordinates, fileName + ".asc");
        addChecksum(signatureFile, fileName + ".asc.md5", "MD5", coordinates, bundleBuilder);
        addChecksum(signatureFile, fileName + ".asc.sha1", "SHA-1", coordinates, bundleBuilder);
      }
    }
  }

  /**
   * Generates a checksum and adds it to the bundle.
   */
  private void addChecksum(File file, String fileName, String algorithm,
                           MavenCoordinates coordinates, BundleBuilder bundleBuilder) throws IOException {
    String checksum = fileProcessor.generateChecksum(file, algorithm);

    // Create a temporary file for the checksum
    File checksumFile = File.createTempFile("checksum-", ".tmp");
    try {
      Files.writeString(checksumFile.toPath(), checksum);
      bundleBuilder.addFile(checksumFile, coordinates, fileName);
    } finally {
      Files.deleteIfExists(checksumFile.toPath());
    }
  }
}
