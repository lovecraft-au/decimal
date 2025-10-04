# Maintainers Guide: GitHub setup for CI/CD

This repository includes a GitHub Actions workflow at:

- .github/workflows/ci.yml

It does two things:
- Build and test on every push/PR to master (macOS runner).
- Publish artifacts to Maven Central when a version tag vX.Y.Z is pushed.

Follow the steps below to configure your GitHub repository to support this workflow end‑to‑end.


## 1) Ensure GitHub Actions is enabled

- In GitHub: Settings → Actions → General
  - Actions permissions: Allow GitHub Actions to run for this repository.
  - Workflow permissions: Read repository contents (default is fine for this workflow).

macOS runners are used (macos-latest) for iOS/Android/Wasm builds; nothing else required here.


## 2) Confirm the default branch name

This workflow is configured to run on pushes and pull requests to master:

```
on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]
```

If your default branch is main, either:
- Rename your default branch to master, or
- Update .github/workflows/ci.yml to use main instead of master in both places.


## 3) Protect the master branch and require the build check

To keep master green and enforce CI before merging:

- Settings → Branches → Branch protection rules → Add rule
  - Branch name pattern: master
  - Enable “Require status checks to pass before merging”.
  - Add the required check: Build (Kotlin Multiplatform)
    - This is the job name as shown in the workflow. GitHub will list it after the first successful run.
  - Optionally enable:
    - Require pull request reviews before merging.
    - Require linear history.
    - Include administrators (for stronger enforcement).


## 4) Add repository secrets for publishing to Maven Central

Publishing runs when a tag starting with v is pushed (e.g. v1.2.3). The workflow maps repository secrets to conventional Gradle properties:

- OSSRH_USERNAME → ORG_GRADLE_PROJECT_mavenCentralUsername
- OSSRH_PASSWORD → ORG_GRADLE_PROJECT_mavenCentralPassword
- SIGNING_KEY → ORG_GRADLE_PROJECT_signingInMemoryKey
- SIGNING_PASSWORD → ORG_GRADLE_PROJECT_signingInMemoryKeyPassword

Add these as “Repository secrets”:

1) OSSRH (Sonatype) credentials
- Create/request an account at https://s01.oss.sonatype.org/ (or https://oss.sonatype.org/ for legacy hosts).
- Ensure your groupId is approved and tied to the account.
- Add repository secrets:
  - OSSRH_USERNAME: your Sonatype username
  - OSSRH_PASSWORD: your Sonatype password or token

2) PGP signing key (ASCII‑armored)
- Generate a key (if you don’t have one):
  - gpg --full-generate-key
  - Choose RSA and RSA, key size 4096, set a passphrase.
- Find your key ID: gpg --list-secret-keys --keyid-format=long
- Export the private key in ASCII armor:
  - gpg --armor --export-secret-keys <KEY_ID>
- Copy the entire block, including the BEGIN/END lines, into the SIGNING_KEY secret value.
- Add SIGNING_PASSWORD with the passphrase you set when creating the key.

Notes:
- The workflow uses in‑memory signing via Gradle properties, so no files need to be written to disk.
- Keep these secrets scoped to the repository and rotate them if compromised.


## 5) Confirm Gradle publishing configuration in the build (one‑time project setup)

This workflow assumes the project is configured to publish to Sonatype. Typical setup (in build.gradle.kts or convention plugins):

- Plugins: maven-publish, signing, and optionally io.github.gradle-nexus.publish-plugin (recommended).
- Configure publications for the Kotlin Multiplatform targets.
- Configure signing to use the in‑memory key properties used above.
- If using the Nexus Publish plugin, ensure it’s configured with the same mavenCentralUsername/mavenCentralPassword properties.

The release job runs:
- publishToSonatype closeAndReleaseSonatypeStagingRepository (preferred path)
- Falls back to publish if the Nexus plugin tasks are not present.


## 6) Release process (tag‑based)

- Bump the version in gradle.properties (or wherever you define version) to a non‑SNAPSHOT version like 1.2.3.
- Commit the change to master.
- Create and push an annotated tag that matches the version with a leading v:
  - git tag -a v1.2.3 -m "Release 1.2.3"
  - git push origin v1.2.3
- The CI release job will build and publish artifacts to Sonatype. If using the Nexus plugin, it will also close and release the staging repository.

Manual runs:
- You can manually trigger the build job via the Actions tab (workflow_dispatch is enabled). The release job only runs on v* tag pushes.


## 7) Optional: Dry‑run and sanity checks

- Local dry‑run of publications:
  - ./gradlew publishToMavenLocal
- Verify CI check name for branch protection:
  - After one successful CI run on master, copy the exact check name “Build (Kotlin Multiplatform)”.
- Ensure Android SDK setup doesn’t block the build:
  - The workflow accepts licenses and installs API 36 (do 36 or die).


## 8) Troubleshooting

- Release job fails with 401/403 from Sonatype:
  - Check OSSRH_USERNAME/OSSRH_PASSWORD secrets and that the account has rights for your groupId.
- Signing failed: No keys available / wrong passphrase:
  - Ensure SIGNING_KEY contains the full ASCII‑armored private key block and SIGNING_PASSWORD matches.
- Release job never triggers:
  - Confirm the tag starts with v (e.g., v1.2.3) and was pushed to the repository.
- Required check not available in branch protection settings:
  - Let the workflow run once on master so GitHub learns the check name, then add it.
- Workflow not running on PRs:
  - Ensure PR targets master and Actions are enabled for forks (Settings → Actions → General → Fork pull request workflows). Secrets are not exposed to PRs from forks (expected); release will not run for PRs.


## 9) Security considerations

- Keep repository secrets limited to maintainers.
- Rotate OSSRH and signing credentials periodically.
- Consider enabling “Require approval for all outside collaborators” in Actions settings if you accept PRs from forks.


## 10) Where to edit the workflow

- The workflow file lives at .github/workflows/ci.yml.
- Changes to triggers (e.g., switching to main) should be done in that file and reviewed in a pull request.
