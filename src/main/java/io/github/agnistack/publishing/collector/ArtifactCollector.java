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
package io.github.agnistack.publishing.collector;

import io.github.agnistack.publishing.model.ArtifactFile;
import io.github.agnistack.publishing.model.MavenCoordinates;

import java.io.File;
import java.util.List;

/**
 * Collects artifact files from a directory based on Maven coordinates.
 *
 * @since 0.1.0
 */
public class ArtifactCollector {

  private final boolean includeJar;
  private final boolean includeSources;
  private final boolean includeJavadoc;

  public ArtifactCollector(boolean includeJar, boolean includeSources, boolean includeJavadoc) {
    this.includeJar = includeJar;
    this.includeSources = includeSources;
    this.includeJavadoc = includeJavadoc;
  }

  /**
   * Collects all artifact files for the given coordinates from the directory.
   *
   * @param artifactDir the directory containing the artifacts
   * @param coordinates the Maven coordinates
   * @return list of artifact files
   */
  public List<ArtifactFile> collect(File artifactDir, MavenCoordinates coordinates) {
    List<ArtifactFile> artifacts = new java.util.ArrayList<>();
    String baseFileName = coordinates.getBaseFileName();

    // Always include POM
    File pomFile = new File(artifactDir, "pom.xml");
    artifacts.add(new ArtifactFile(pomFile, baseFileName + ".pom", ArtifactFile.ArtifactType.POM));

    // Optionally include JAR
    if (includeJar) {
      File jarFile = new File(artifactDir, baseFileName + ".jar");
      artifacts.add(new ArtifactFile(jarFile, baseFileName + ".jar", ArtifactFile.ArtifactType.JAR));
    }

    // Optionally include sources
    if (includeSources) {
      File sourcesFile = new File(artifactDir, baseFileName + "-sources.jar");
      artifacts.add(new ArtifactFile(sourcesFile, baseFileName + "-sources.jar", ArtifactFile.ArtifactType.SOURCES));
    }

    // Optionally include javadoc
    if (includeJavadoc) {
      File javadocFile = new File(artifactDir, baseFileName + "-javadoc.jar");
      artifacts.add(new ArtifactFile(javadocFile, baseFileName + "-javadoc.jar", ArtifactFile.ArtifactType.JAVADOC));
    }

    return artifacts;
  }
}
