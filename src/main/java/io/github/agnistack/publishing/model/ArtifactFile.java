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

import java.io.File;
import java.util.Objects;

/**
 * Represents a Maven artifact file with its metadata.
 *
 * @since 0.1.0
 */
public class ArtifactFile {

  private final File file;
  private final String fileName;
  private final ArtifactType type;

  public ArtifactFile(File file, String fileName, ArtifactType type) {
    this.file = Objects.requireNonNull(file, "file cannot be null");
    this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
    this.type = Objects.requireNonNull(type, "type cannot be null");
  }

  public File getFile() {
    return file;
  }

  public String getFileName() {
    return fileName;
  }

  public ArtifactType getType() {
    return type;
  }

  public boolean exists() {
    return file.exists();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ArtifactFile that = (ArtifactFile) o;
    return Objects.equals(file, that.file) &&
        Objects.equals(fileName, that.fileName) &&
        type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(file, fileName, type);
  }

  @Override
  public String toString() {
    return "ArtifactFile{" +
        "fileName='" + fileName + '\'' +
        ", type=" + type +
        ", exists=" + exists() +
        '}';
  }

  /**
   * Types of artifact files
   */
  public enum ArtifactType {
    POM,
    JAR,
    SOURCES,
    JAVADOC,
    SIGNATURE,
    CHECKSUM
  }
}
