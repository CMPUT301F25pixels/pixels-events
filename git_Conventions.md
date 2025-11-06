# Git Conventions

## 1. Introduction

Git is a tool for version control, and GitHub is a platform for managing and sharing code in the cloud.

## 2. Getting Started

### 2.1 Choose a Ticket

1. Ensure the acceptance criteria are updated.
2. If a ticket doesn't exist, create one with appropriate naming so it can be tracked as explained in the branch naming conventions section.

### 2.2 Move Tickets

- Move the ticket to 'In Progress' when you start working on it.
- Once complete, move it to 'Review' and then 'Done' after approval.

## 3. Branching and Workflow

### 3.1 Branch Naming

- Format: `(type)/(ticket-no)-(short-task)`
- Example: `style/02-adds-colors-for-buttons`

### 3.2 Creating and Managing Branches

1. Start with the main branch:
   ```
   git pull origin main
   ```
2. Create a feature branch:
   ```
   git checkout -b feature/[FeatureName]
   ```
3. Push your changes:
   ```
   git push origin feature/[FeatureName]
   ```
4. Open a PR to main.

### 3.3 Branching Strategy

- `main`: Production-ready code only.
- **CAUTION:** All changes to `main` must be made via a Pull Request and require at least 2 approving reviews before merging. Do not push direct commits to `main`.

## 4. Commit Guidelines

### 4.1 Commit Message Format

- Format: `[type]: [Short Summary]`
- Example:
  ```
  feat: Implement user authentication

  - Added JWT-based authentication
  - Implemented login/logout functionality
  ```

### 4.2 Commit Types

| Type      | Description |
|-----------|-------------|
| feat      | A new feature |
| fix       | A bug fix |
| docs      | Documentation changes |
| style     | Code formatting, no logic changes |
| refactor  | Code refactoring without changing functionality |
| test      | Adding or modifying tests |
| chore     | Minor changes, dependencies, scripts |

## 5. Pull Requests

### 5.1 Guidelines

- Open a Pull Request (PR) to `main`.
- Request at least two peer reviews/approvals before merging to the target branch.
- Address any requested changes.

## 6. Common Git Commands

### 6.1 Staging and Pushing Changes

```
git add .
git commit -m "message"
git push
```

### 6.2 Updating Your Branch

```
git checkout main
git pull origin main
```

### 6.3 Pulling Changes into a Feature Branch

```
git checkout feature/[feature-name]
git pull origin main
```

## 7. Troubleshooting

### 7.1 Common Branch Mistake

- If you don't have the latest changes from the remote repository:
  ```
  git pull --rebase origin [branch-name]
  ```

### 7.2 Reverting Changes

- To revert changes:
  ```
  git revert <hash>
  ```

### 7.3 Deleting Branches

- Delete a branch locally:
  ```
  git branch -D [branch-name]
  ```
- Delete a branch remotely:
  ```
  git push origin --delete [branch-name]
  ```