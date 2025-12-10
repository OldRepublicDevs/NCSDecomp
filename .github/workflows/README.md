# GitHub Actions Workflows Documentation

This directory contains GitHub Actions workflows for automated CI/CD, security analysis, and release management for the NCSDecomp project.

## Overview

The repository includes the following workflows:

1. **CI Workflow** (`ci.yml`) - Continuous Integration for building and testing
2. **CodeQL Security Analysis** (`codeql.yml`) - Automated security vulnerability scanning
3. **Dependency Review** (`dependency-review.yml`) - Dependency security and license checking
4. **Release Workflow** (`release.yml`) - Automated release creation and publishing

---

## 1. CI Workflow (`ci.yml`)

### Purpose

Automatically builds and tests the project on multiple operating systems and Java versions whenever code is pushed or pull requests are created.

### Triggers

- Push to `main`, `master`, or `develop` branches
- Pull requests targeting `main`, `master`, or `develop` branches

### What It Does

1. **Multi-Platform Testing**: Tests on Ubuntu, Windows, and macOS
2. **Multi-Java Version Testing**: Tests on Java 8, 11, 17, and 21
   - Note: Java 8 is excluded on macOS (not available)
3. **Build Process**:
   - Checks out the code
   - Sets up the specified Java version
   - Caches Maven dependencies for faster builds
   - Compiles the project
   - Runs all tests
   - Packages the application into a JAR file
4. **Artifacts**:
   - Uploads test results for all matrix combinations
   - Uploads JAR artifacts (from Ubuntu + Java 17 build only)

### Matrix Strategy

The workflow uses a matrix strategy to test multiple combinations:

- **Operating Systems**: `ubuntu-latest`, `windows-latest`, `macos-latest`
- **Java Versions**: `8`, `11`, `17`, `21`
- **Exclusions**: Java 8 on macOS (not supported)

### Manual Steps Required

**None** - This workflow runs automatically on push/PR.

### Viewing Results

- Go to the **Actions** tab in your GitHub repository
- Click on the workflow run to see detailed logs
- Download test results and JAR artifacts from the workflow run page

### Customization

To modify Java versions or operating systems, edit the `strategy.matrix` section in `ci.yml`.

---

## 2. CodeQL Security Analysis (`codeql.yml`)

### Purpose

Automatically scans the codebase for security vulnerabilities and code quality issues using GitHub's CodeQL engine.

### Triggers

- Push to `main`, `master`, or `develop` branches
- Pull requests targeting `main`, `master`, or `develop` branches
- Weekly schedule (Mondays at 2:00 UTC)

### What It Does

1. Checks out the code
2. Initializes CodeQL with Java language support
3. Sets up Java 17 for building
4. Automatically builds the project
5. Performs security and quality analysis
6. Uploads results to GitHub Security tab

### Security Queries

The workflow uses `+security-and-quality` which includes:

- Security vulnerability queries
- Code quality queries
- Best practice queries

### Manual Steps Required

**None** - This workflow runs automatically.

### Viewing Results

- Go to the **Security** tab in your GitHub repository
- Click on **Code scanning alerts** to see detected issues
- Results are also shown in pull request checks

### Customization

To add custom CodeQL queries:

1. Create a `.github/codeql/queries` directory
2. Add your custom `.ql` query files
3. Reference them in the `queries` field of the `init` step

---

## 3. Dependency Review (`dependency-review.yml`)

### Purpose

Reviews dependencies in pull requests for known security vulnerabilities and license compliance.

### Triggers

- Pull requests targeting `main`, `master`, or `develop` branches

### What It Does

1. Checks out the code
2. Analyzes `pom.xml` for dependency changes
3. Checks for known security vulnerabilities
4. Validates license compatibility
5. Fails the check if:
   - Vulnerabilities with severity "moderate" or higher are found
   - Denied licenses (GPL-2.0, GPL-3.0) are detected
   - Denied packages are included

### Configuration

Current settings:

- **Fail on severity**: `moderate` (fails on moderate, high, or critical vulnerabilities)
- **Denied licenses**: `GPL-2.0`, `GPL-3.0`
- **Denied packages**: None (example pattern shown)

### Manual Steps Required

**None** - This workflow runs automatically on pull requests.

### Viewing Results

- Results appear as a check on the pull request
- Click "Details" to see the full dependency review report

### Customization

To modify the configuration:

- Edit the `fail-on-severity` field (options: `low`, `moderate`, `high`, `critical`)
- Add/remove licenses in the `deny-licenses` field
- Add package patterns to `deny-packages` (Maven format: `pkg:maven/groupId/artifactId@*`)

---

## 4. Release Workflow (`release.yml`)

### Purpose

Automates the creation of GitHub releases with built artifacts when a new version tag is pushed or manually triggered.

### Triggers

1. **Automatic**: Push of a tag matching pattern `v*` (e.g., `v1.0.0`, `v2.1.3`)
2. **Manual**: Workflow dispatch from the Actions tab

### What It Does

1. Checks out the code
2. Sets up Java 17 for building
3. Determines the version from the tag or manual input
4. Builds the project with Maven
5. Generates a changelog from commits
6. Creates a GitHub release with:
   - Release notes
   - Changelog
   - JAR artifacts attached
7. Uploads release assets as artifacts

### Manual Trigger Steps

1. Go to the **Actions** tab in your GitHub repository
2. Select **Release** workflow from the left sidebar
3. Click **Run workflow**
4. Fill in:
   - **Version**: The version number (e.g., `1.0.0`)
   - **Create tag**: Check if you want to create a new tag
5. Click **Run workflow**

### Automatic Release Steps

1. Create and push a tag:

   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. The workflow will automatically:
   - Detect the tag
   - Build the project
   - Create a release
   - Attach the JAR file

### Viewing Results

- Go to the **Releases** page in your GitHub repository
- The new release will appear with all attached artifacts
- Download the JAR file from the release page

### Customization

To modify release behavior:

- Edit the tag pattern in the `on.push.tags` section
- Modify the release body template in the `Create Release` step
- Add additional build steps or artifacts as needed

### Permissions Required

This workflow requires `contents: write` permission to create releases. This is automatically granted for workflows in the repository.

---

## Workflow Status Badges

You can add status badges to your README.md to show workflow status:

```markdown
![CI](https://github.com/bolabaden/NCSDecomp/workflows/CI/badge.svg)
![CodeQL](https://github.com/bolabaden/NCSDecomp/workflows/CodeQL%20Security%20Analysis/badge.svg)
```

---

## Troubleshooting

### CI Workflow Fails

- **Check Java version compatibility**: Ensure your code compiles on all tested Java versions
- **Check test failures**: Review test results in the uploaded artifacts
- **Platform-specific issues**: Check if any code is platform-specific and needs conditional compilation

### CodeQL Finds Issues

- Review the Security tab for detailed information
- Fix issues or mark false positives
- Update the workflow to exclude certain query categories if needed

### Dependency Review Fails

- Review the PR check for specific dependency issues
- Update vulnerable dependencies
- If a license is acceptable, remove it from the deny list

### Release Workflow Fails

- Ensure you have push permissions to the repository
- Check that the tag format matches `v*` pattern
- Verify Maven build succeeds locally before triggering release

---

## Best Practices

1. **Keep workflows updated**: Regularly update action versions to the latest
2. **Monitor security alerts**: Review CodeQL and dependency review results regularly
3. **Test before release**: Always test locally before creating a release
4. **Use semantic versioning**: Follow semantic versioning (e.g., 1.0.0, 1.1.0, 2.0.0)
5. **Review changelogs**: Verify generated changelogs before publishing releases

---

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [CodeQL Documentation](https://codeql.github.com/docs/)
- [Dependency Review Action](https://github.com/actions/dependency-review-action)
- [Maven Documentation](https://maven.apache.org/guides/)

---

## Support

For issues or questions about these workflows:

1. Check the workflow logs in the Actions tab
2. Review this documentation
3. Open an issue in the repository with the workflow name and error details
