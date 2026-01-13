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
package io.github.agnistack.publishing.parser;

import io.github.agnistack.publishing.model.MavenCoordinates;

import java.io.File;
import java.io.IOException;

/**
 * Interface for parsing POM files to extract Maven coordinates.
 *
 * @since 0.1.0
 */
public interface PomParser {

  /**
   * Parses a POM file and extracts Maven coordinates.
   *
   * @param pomFile the POM file to parse
   * @return the Maven coordinates
   * @throws IOException if parsing fails
   */
  MavenCoordinates parse(File pomFile) throws IOException;
}
