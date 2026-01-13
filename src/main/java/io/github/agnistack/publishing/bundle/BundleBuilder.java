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
package io.github.agnistack.publishing.bundle;

import io.github.agnistack.publishing.model.MavenCoordinates;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Interface for building deployment bundles.
 *
 * @since 0.1.0
 */
public interface BundleBuilder extends Closeable {

  /**
   * Adds a file to the bundle.
   *
   * @param file        the file to add
   * @param coordinates the Maven coordinates for the artifact
   * @param fileName    the target file name in the bundle
   * @throws IOException if adding the file fails
   */
  void addFile(File file, MavenCoordinates coordinates, String fileName) throws IOException;

  /**
   * Returns the bundle file.
   *
   * @return the bundle file
   */
  File getBundleFile();
}
