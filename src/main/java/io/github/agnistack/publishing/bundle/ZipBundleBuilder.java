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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP-based implementation of BundleBuilder.
 *
 * @since 0.1.0
 */
public class ZipBundleBuilder implements BundleBuilder {

  private final File bundleFile;
  private final ZipOutputStream zos;

  public ZipBundleBuilder(File bundleFile) throws IOException {
    this.bundleFile = bundleFile;

    // Ensure parent directory exists
    File parentDir = bundleFile.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new IOException("Failed to create directory: " + parentDir);
      }
    }

    this.zos = new ZipOutputStream(new FileOutputStream(bundleFile));
  }

  @Override
  public void addFile(File file, MavenCoordinates coordinates, String fileName) throws IOException {
    String entryPath = coordinates.getRepositoryPath() + fileName;

    ZipEntry zipEntry = new ZipEntry(entryPath);
    zos.putNextEntry(zipEntry);

    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      int length;
      while ((length = fis.read(buffer)) > 0) {
        zos.write(buffer, 0, length);
      }
    }

    zos.closeEntry();
  }

  @Override
  public File getBundleFile() {
    return bundleFile;
  }

  @Override
  public void close() throws IOException {
    if (zos != null) {
      zos.close();
    }
  }
}
