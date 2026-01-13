# GitHub Actions Workflows

This directory contains GitHub Actions workflows for CI/CD automation.

## Workflows

### 1. CI Build (`ci.yml`)

**Triggers:**
- Push to `main` branches
- Pull requests to `main` branches

**Purpose:**
- Validates that the code compiles
- Runs unit tests
- Ensures the build is successful

**What it does:**
- Checks out the code
- Sets up JDK 11
- Runs `mvn clean verify`
- Runs `mvn test`

### 2. License Header Check (`license-check.yml`)

**Triggers:**
- Push to `main` branches
- Pull requests to `main` branches

**Purpose:**
- Ensures all Java source files have proper Apache License v2 headers

**What it does:**
- Checks out the code
- Sets up JDK 11
- Runs `mvn license:check-file-header`
- Fails if any files are missing license headers

**If the workflow fails:**
Run this command locally to add missing license headers:
```bash
mvn license:update-file-header
```

Then commit and push the changes.

### 3. Release (`release.yml`)

**Triggers:**
- Manual workflow dispatch (triggered from GitHub Actions UI)

**Purpose:**
- Builds and releases a new version to Maven Central
- Creates a GitHub release
- Tags the release in Git

**Required Inputs:**
- `releaseVersion`: The version to release (e.g., `0.1.0`)
- `nextVersion`: The next development version (e.g., `0.1.1-SNAPSHOT`)

**Required Secrets:**

Configure these secrets in GitHub Settings → Secrets and variables → Actions:

| Secret Name | Description |
|-------------|-------------|
| `MAVEN_CENTRAL_USERNAME` | Your Maven Central token username |
| `MAVEN_CENTRAL_PASSWORD` | Your Maven Central token password |
| `GPG_PRIVATE_KEY` | Your GPG private key (export with `gpg --armor --export-secret-keys YOUR_KEY_ID`) |
| `GPG_PASSPHRASE` | Your GPG key passphrase |

**What it does:**

1. **Update version**: Sets the version to the release version
2. **Build**: Compiles and verifies the project with `-Prelease` profile
3. **Deploy**: Deploys artifacts to Maven Central
   - Main JAR
   - Sources JAR
   - Javadoc JAR
   - POM file
   - GPG signatures for all files
4. **Tag**: Creates a Git tag (e.g., `v0.1.0`)
5. **GitHub Release**: Creates a GitHub release with release notes
6. **Next version**: Updates the version to the next development version
7. **Push**: Pushes changes and tags to the repository

## How to Release

### Step 1: Prepare

Ensure all changes are committed and pushed to `main` branch.

### Step 2: Trigger Release Workflow

1. Go to **Actions** tab in GitHub
2. Click on **Release to Maven Central** workflow
3. Click **Run workflow** button
4. Fill in the inputs:
   - **Release version**: `0.1.0` (without `v` prefix)
   - **Next development version**: `0.1.1-SNAPSHOT`
5. Click **Run workflow**

### Step 3: Monitor

Watch the workflow execution in the Actions tab. The workflow will:
- ✅ Build the project
- ✅ Deploy to Maven Central
- ✅ Create Git tag
- ✅ Create GitHub Release
- ✅ Update version for next development

### Step 4: Verify

1. Check [Maven Central](https://central.sonatype.com) for the new version
2. Check GitHub Releases for the published release
3. Verify the `main` branch has the next development version

## Setting Up Secrets

### Maven Central Credentials

1. Log in to https://central.sonatype.com
2. Go to **Account** → **Generate User Token**
3. Copy the username and password
4. Add them as `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` secrets

### GPG Key

#### Generate GPG Key (if you don't have one)

```bash
gpg --gen-key
```

Follow the prompts to create a key.

#### Export Private Key

```bash
# List your keys to find the KEY_ID
gpg --list-secret-keys --keyid-format LONG

# Export the private key (replace YOUR_KEY_ID)
gpg --armor --export-secret-keys YOUR_KEY_ID
```

Copy the entire output (including `-----BEGIN PGP PRIVATE KEY BLOCK-----` and `-----END PGP PRIVATE KEY BLOCK-----`).

#### Publish Public Key

```bash
# Upload to key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

#### Add to GitHub Secrets

1. Go to GitHub repository settings
2. Navigate to **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add `GPG_PRIVATE_KEY` with the exported private key
5. Add `GPG_PASSPHRASE` with your GPG key passphrase

## Troubleshooting

### License Check Fails

**Problem**: Some files are missing license headers.

**Solution**:
```bash
mvn license:format
git add .
git commit -m "Add missing license headers"
git push
```

### Release Fails at Deploy

**Problem**: Maven Central authentication fails.

**Solution**:
- Verify `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` secrets are correct
- Check that your Maven Central account is active
- Ensure you've verified your namespace (e.g., `io.github.agnistack`)

### GPG Signing Fails

**Problem**: GPG signing fails during release.

**Solution**:
- Verify `GPG_PRIVATE_KEY` secret contains the full private key
- Verify `GPG_PASSPHRASE` is correct
- Ensure the GPG key hasn't expired

### Release Workflow Can't Push

**Problem**: Workflow fails at git push step.

**Solution**:
- Go to repository **Settings** → **Actions** → **General**
- Under **Workflow permissions**, select **Read and write permissions**
- Check **Allow GitHub Actions to create and approve pull requests**
- Click **Save**

## Local Testing

### Test License Check

```bash
mvn license:check
```

### Test Release Build

```bash
mvn clean verify -Prelease
```

### Add License Headers

```bash
mvn license:format
```

## Additional Resources

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-portal-maven/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GPG Signing Guide](https://central.sonatype.org/publish/requirements/gpg/)
