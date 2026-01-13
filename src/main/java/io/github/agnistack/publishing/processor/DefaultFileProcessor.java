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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of FileProcessor with GPG signing and checksum generation.
 *
 * @since 0.1.0
 */
public class DefaultFileProcessor implements FileProcessor {

  private final String gpgExecutable;
  private final String gpgPassphrase;
  private final boolean skipGpgSign;
  private final Log log;

  public DefaultFileProcessor(String gpgExecutable, String gpgPassphrase, boolean skipGpgSign, Log log) {
    this.gpgExecutable = gpgExecutable != null ? gpgExecutable : "gpg";
    this.gpgPassphrase = gpgPassphrase;
    this.skipGpgSign = skipGpgSign;
    this.log = log;
  }

  @Override
  public File signFile(File file) throws IOException {
    if (skipGpgSign) {
      return null;
    }

    log.info("Signing file: " + file.getName());

    File signatureFile = new File(file.getAbsolutePath() + ".asc");

    List<String> command = new ArrayList<>();
    command.add(gpgExecutable);
    command.add("--detach-sign");
    command.add("--armor");
    command.add("--output");
    command.add(signatureFile.getAbsolutePath());

    if (gpgPassphrase != null && !gpgPassphrase.isEmpty()) {
      command.add("--passphrase");
      command.add(gpgPassphrase);
      command.add("--batch");
      command.add("--yes");
    }

    command.add(file.getAbsolutePath());

    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectErrorStream(true);
      Process process = pb.start();
      int exitCode = process.waitFor();

      if (exitCode != 0) {
        throw new IOException("GPG signing failed with exit code: " + exitCode);
      }

      log.info("Successfully signed: " + file.getName());
      return signatureFile;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("GPG signing was interrupted", e);
    }
  }

  @Override
  public String generateChecksum(File file, String algorithm) throws IOException {
    try {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      try (FileInputStream fis = new FileInputStream(file)) {
        byte[] buffer = new byte[8192];
        int length;
        while ((length = fis.read(buffer)) > 0) {
          digest.update(buffer, 0, length);
        }
      }

      byte[] hash = digest.digest();
      StringBuilder hexString = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Unsupported checksum algorithm: " + algorithm, e);
    }
  }

  @Override
  public boolean isSigningEnabled() {
    return !skipGpgSign;
  }
}
