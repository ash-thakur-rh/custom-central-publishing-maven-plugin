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
package io.github.agnistack.publishing.processor;

import java.io.File;
import java.io.IOException;

/**
 * Interface for processing artifact files (signing, checksum generation, etc.).
 *
 * @since 0.1.0
 */
public interface FileProcessor {

  /**
   * Signs a file using GPG.
   *
   * @param file the file to sign
   * @return the signature file
   * @throws IOException if signing fails
   */
  File signFile(File file) throws IOException;

  /**
   * Generates a checksum for a file.
   *
   * @param file      the file to generate checksum for
   * @param algorithm the checksum algorithm (e.g., "MD5", "SHA-1")
   * @return the checksum as a hex string
   * @throws IOException if checksum generation fails
   */
  String generateChecksum(File file, String algorithm) throws IOException;

  /**
   * Checks if GPG signing is enabled.
   *
   * @return true if signing is enabled
   */
  boolean isSigningEnabled();
}
