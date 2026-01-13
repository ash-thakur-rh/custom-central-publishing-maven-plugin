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
package io.github.agnistack.publishing.model;

import java.util.Objects;

/**
 * Represents Maven artifact coordinates (GAV).
 *
 * @since 0.1.0
 */
public class MavenCoordinates {

  private final String groupId;
  private final String artifactId;
  private final String version;

  public MavenCoordinates(String groupId, String artifactId, String version) {
    this.groupId = Objects.requireNonNull(groupId, "groupId cannot be null");
    this.artifactId = Objects.requireNonNull(artifactId, "artifactId cannot be null");
    this.version = Objects.requireNonNull(version, "version cannot be null");
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  /**
   * Returns the Maven repository path for this artifact.
   * Format: groupId.replace('.', '/') / artifactId / version
   */
  public String getRepositoryPath() {
    return groupId.replace('.', '/') + '/' + artifactId + '/' + version + '/';
  }

  /**
   * Returns the base filename for this artifact (without extension).
   * Format: artifactId-version
   */
  public String getBaseFileName() {
    return artifactId + '-' + version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenCoordinates that = (MavenCoordinates) o;
    return Objects.equals(groupId, that.groupId) &&
        Objects.equals(artifactId, that.artifactId) &&
        Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version);
  }

  @Override
  public String toString() {
    return groupId + ':' + artifactId + ':' + version;
  }
}
