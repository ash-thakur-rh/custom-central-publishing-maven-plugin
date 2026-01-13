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
package io.github.agnistack.publishing;

import io.github.agnistack.publishing.bundle.BundleBuilder;
import io.github.agnistack.publishing.bundle.ZipBundleBuilder;
import io.github.agnistack.publishing.collector.ArtifactCollector;
import io.github.agnistack.publishing.parser.DefaultPomParser;
import io.github.agnistack.publishing.parser.PomParser;
import io.github.agnistack.publishing.processor.DefaultFileProcessor;
import io.github.agnistack.publishing.processor.FileProcessor;
import io.github.agnistack.publishing.service.DeploymentService;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.sonatype.central.publisher.client.PublisherClient;
import org.sonatype.central.publisher.client.model.PublishingType;
import org.sonatype.central.publisher.plugin.config.PlexusContextConfig;
import org.sonatype.central.publisher.plugin.model.UploadArtifactRequest;
import org.sonatype.central.publisher.plugin.model.WaitForDeploymentStateRequest;
import org.sonatype.central.publisher.plugin.model.WaitUntilRequest;
import org.sonatype.central.publisher.plugin.uploader.ArtifactUploader;
import org.sonatype.central.publisher.plugin.watcher.DeploymentPublishedWatcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.sonatype.central.publisher.client.PublisherConstants.DEFAULT_ORGANIZATION_ID;
import static org.sonatype.central.publisher.client.httpclient.auth.AuthProviderType.BASIC;
import static org.sonatype.central.publisher.client.httpclient.auth.AuthProviderType.USERTOKEN;
import static org.sonatype.central.publisher.plugin.Constants.*;

/**
 * Mojo to publish custom artifacts to Maven Central.
 *
 * <p>This plugin uses a modular architecture with separate concerns for:
 * <ul>
 *   <li>POM parsing ({@link PomParser})</li>
 *   <li>Artifact collection ({@link ArtifactCollector})</li>
 *   <li>File processing - signing and checksums ({@link FileProcessor})</li>
 *   <li>Bundle building ({@link BundleBuilder})</li>
 *   <li>Deployment orchestration ({@link DeploymentService})</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Mojo(name = "publish-custom", defaultPhase = LifecyclePhase.DEPLOY)
public class PublishCustomArtifactMojo extends AbstractMojo {

  @Parameter(defaultValue = PUBLISHING_SERVER_ID_DEFAULT_VALUE)
  private String publishingServerId;

  @Parameter(defaultValue = CENTRAL_BASE_URL_DEFAULT_VALUE)
  private String centralBaseUrl;

  @Parameter(defaultValue = AUTO_PUBLISH_DEFAULT_VALUE)
  private boolean autoPublish;

  @Parameter(defaultValue = "${project.groupId}:${project.artifactId}:${project.version}")
  private String deploymentName;

  @Parameter(defaultValue = "true")
  private boolean tokenAuth;

  @Parameter(defaultValue = "VALIDATED")
  private String waitUntil;

  @Parameter(defaultValue = WAIT_MAX_TIME_DEFAULT_VALUE)
  private int waitMaxTime;

  @Parameter(defaultValue = WAIT_POLLING_INTERVAL_DEFAULT_VALUE)
  private int waitPollingInterval;

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession mavenSession;

  @Parameter(defaultValue = "${project.artifactId}", readonly = true)
  private String currentArtifactId;

  @Component
  private PlexusContextConfig plexusContextConfig;

  @Component
  private ArtifactUploader artifactUploader;

  @Component
  private DeploymentPublishedWatcher deploymentPublishedWatcher;

  @Component
  private PublisherClient publisherClient;

  @Parameter(property = "bomProjectsDirectory", required = true)
  private File bomProjectsDirectory;

  @Parameter(property = "bomProjects", required = true)
  private List<String> bomProjects;

  @Parameter(property = "gpgExecutable", defaultValue = "gpg")
  private String gpgExecutable;

  @Parameter(property = "gpg.passphrase")
  private String gpgPassphrase;

  @Parameter(property = "skipGpgSign", defaultValue = "false")
  private boolean skipGpgSign;

  @Parameter(property = "skipBomPublishing", defaultValue = "false")
  private boolean skip;

  @Parameter(property = "includeJar", defaultValue = "false")
  private boolean includeJar;

  @Parameter(property = "includeSources", defaultValue = "false")
  private boolean includeSources;

  @Parameter(property = "includeJavadoc", defaultValue = "false")
  private boolean includeJavadoc;

  private WaitUntilRequest waitUntilRequest;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // Skip checks
    if (skip) {
      getLog().info("Skipping custom artifact publishing (skip=true)");
      return;
    }

    if (bomProjects != null && bomProjects.contains(currentArtifactId)) {
      getLog().debug("Skipping publishing - running on generated artifact: " + currentArtifactId);
      return;
    }

    if (!bomProjectsDirectory.exists() || !bomProjectsDirectory.isDirectory()) {
      getLog().debug("Skipping publishing - directory does not exist: " + bomProjectsDirectory);
      return;
    }

    // Validate inputs
    if (bomProjects == null || bomProjects.isEmpty()) {
      throw new MojoExecutionException("No artifacts specified. Please provide bomProjects list.");
    }

    getLog().info("Starting custom artifact deployment to Maven Central");

    // Initialize publisher client
    initializePublisherClient();

    // Create and upload bundle
    try {
      File bundleFile = createDeploymentBundle();
      uploadBundle(bundleFile);
      getLog().info("Deployment bundle created at: " + bundleFile.getAbsolutePath());
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to create deployment bundle", e);
    }

    getLog().info("Custom artifact deployment completed successfully");
  }

  private void initializePublisherClient() throws MojoExecutionException {
    publisherClient.setCentralBaseUrl(centralBaseUrl);

    Server server = mavenSession.getSettings().getServer(publishingServerId);
    if (server == null) {
      throw new MojoExecutionException("Server configuration not found for id: " + publishingServerId);
    }

    if (tokenAuth) {
      publisherClient.setAuthProvider(USERTOKEN, DEFAULT_ORGANIZATION_ID, server.getUsername(), server.getPassword());
    } else {
      publisherClient.setAuthProvider(BASIC, DEFAULT_ORGANIZATION_ID, server.getUsername(), server.getPassword());
    }
  }

  private File createDeploymentBundle() throws IOException, MojoExecutionException {
    getLog().info("Creating combined deployment bundle for " + bomProjects.size() + " project(s)");

    // Create bundle directory
    File buildDir = new File(mavenSession.getCurrentProject().getBuild().getDirectory());
    File customPublishingDir = new File(buildDir, "custom-publishing");
    File bundleFile = new File(customPublishingDir, "custom-deployment-bundle.zip");

    // Initialize components
    PomParser pomParser = new DefaultPomParser();
    ArtifactCollector collector = new ArtifactCollector(includeJar, includeSources, includeJavadoc);
    FileProcessor fileProcessor = new DefaultFileProcessor(gpgExecutable, gpgPassphrase, skipGpgSign, getLog());
    DeploymentService deploymentService = new DeploymentService(pomParser, collector, fileProcessor, getLog());

    // Create bundle
    try (BundleBuilder bundleBuilder = new ZipBundleBuilder(bundleFile)) {
      for (String bomProject : bomProjects) {
        getLog().info("Processing artifact: " + bomProject);

        File artifactDir = new File(bomProjectsDirectory, bomProject);
        if (!artifactDir.exists() || !artifactDir.isDirectory()) {
          throw new MojoExecutionException("Artifact directory not found: " + artifactDir);
        }

        File pomFile = new File(artifactDir, "pom.xml");
        if (!pomFile.exists()) {
          throw new MojoExecutionException("pom.xml not found in: " + artifactDir);
        }

        deploymentService.processArtifact(pomFile, bundleBuilder);
      }
    }

    getLog().info("Created combined bundle: " + bundleFile.getAbsolutePath());
    return bundleFile;
  }

  private void uploadBundle(File bundleFile) {
    getLog().info("Uploading combined bundle to Maven Central: " + bundleFile.getName());

    PublishingType publishingType = autoPublish ? PublishingType.AUTOMATIC : PublishingType.USER_MANAGED;

    UploadArtifactRequest uploadRequest = new UploadArtifactRequest(
        deploymentName,
        bundleFile.toPath(),
        publishingType
    );

    String deploymentId = artifactUploader.upload(uploadRequest);

    getLog().info("Deployed to Central with deployment ID: " + deploymentId);
    getLog().info("All " + bomProjects.size() + " artifact(s) uploaded in a single deployment");

    if (getWaitUntil() == WaitUntilRequest.UPLOADED) {
      return;
    }

    getLog().info("Waiting for deployment state to be " + getWaitUntil());

    WaitForDeploymentStateRequest waitRequest = new WaitForDeploymentStateRequest(
        centralBaseUrl,
        deploymentId,
        waitUntilRequest,
        waitMaxTime,
        waitPollingInterval
    );

    deploymentPublishedWatcher.waitForDeploymentState(waitRequest);
  }

  private WaitUntilRequest getWaitUntil() {
    return Objects.requireNonNullElseGet(waitUntilRequest, () -> {
      waitUntilRequest = WaitUntilRequest.valueOf(waitUntil.toUpperCase());

      if (waitUntilRequest == WaitUntilRequest.PUBLISHED && !autoPublish) {
        throw new RuntimeException("Cannot wait until PUBLISHED when autoPublish is disabled");
      }

      return waitUntilRequest;
    });
  }
}
