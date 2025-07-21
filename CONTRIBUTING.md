# Loadscreens Development Workflow

## Branch Structure

- **`master`** - Production-ready code, stable releases only
- **`develop`** - Main development branch, integration of features
- **`feature/*`** - Individual feature development
- **`bugfix/*`** - Bug fixes
- **`hotfix/*`** - Emergency fixes for production

## Workflow Process

### For New Features

1. **Start from develop branch:**
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes and commit:**
   ```bash
   git add .
   git commit -m "Add: description of your feature"
   ```

4. **Push the feature branch:**
   ```bash
   git push -u origin feature/your-feature-name
   ```

5. **Create a Pull Request:**
   - Go to GitHub repository
   - Click "Compare & pull request"
   - Set base branch to `develop`
   - Add description and request reviewers
   - Wait for code review and approval

6. **After merge, clean up:**
   ```bash
   git checkout develop
   git pull origin develop
   git branch -d feature/your-feature-name
   ```

### For Bug Fixes

1. **Create from develop:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b bugfix/fix-description
   ```

2. **Follow same process as features** (steps 3-6 above)

### For Hotfixes (Emergency Production Fixes)

1. **Create from master:**
   ```bash
   git checkout master
   git pull origin master
   git checkout -b hotfix/critical-fix
   ```

2. **After fix, merge to both master AND develop**

## Commit Message Guidelines

- **Add:** New features
- **Fix:** Bug fixes
- **Update:** Modifications to existing features
- **Remove:** Deletion of features/code
- **Docs:** Documentation changes

Example: `Add: player look direction configuration for loadscreens`

## Code Review Checklist

- [ ] Code follows project conventions
- [ ] No hardcoded values (use config)
- [ ] Proper error handling
- [ ] Comments for complex logic
- [ ] No debug print statements
- [ ] Tested with different configurations

## Release Process

1. **Feature complete on develop**
2. **Create release branch:** `release/v3.1`
3. **Final testing and bug fixes**
4. **Merge to master with version tag**
5. **Merge back to develop**

## Branch Protection Recommendations

For repository admins, consider enabling:
- Require pull request reviews before merging
- Require status checks to pass
- Require branches to be up to date before merging
- Restrict pushes to master branch
