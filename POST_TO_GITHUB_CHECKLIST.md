# 📋 Post to GitHub: Quick Checklist

Use this checklist when posting the SafeHaven completion task to Claude Code via GitHub.

---

## ✅ Step-by-Step Instructions

### **Step 1: Create GitHub Issue**

1. [ ] Go to https://github.com/abbyluggery/SafeHaven-Build/issues
2. [ ] Click green "New Issue" button
3. [ ] Title: `Complete SafeHaven Build - Salesforce Backend + Android UI`
4. [ ] Copy entire contents of `GITHUB_ISSUE_TEMPLATE.md`
5. [ ] Paste into issue description
6. [ ] Click "Submit new issue"

### **Step 2: Set Issue Metadata**

1. [ ] Add labels:
   - `critical`
   - `feature`
   - `salesforce`
   - `android-ui`
   - `testing`
2. [ ] Set milestone: `SafeHaven v1.0 MVP`
3. [ ] Assign to: Claude Code (or appropriate GitHub user)
4. [ ] Set priority: **CRITICAL**

### **Step 3: Add Documentation Comment**

Add this comment to the issue:

```markdown
## 📚 Documentation Package

All instructions and code examples are ready in the documentation repository.

**Read in this order**:

1. 📖 **START_HERE_CLAUDE_CODE.md** (quick start guide)
2. 📖 **CLAUDE_CODE_COMPLETION_INSTRUCTIONS.md** (detailed tasks)
3. 📖 **CLAUDE_CODE_BUILD_REVIEW.md** (analysis of previous work)

**Code Examples**:
- ✅ **ADDITIONAL_COMPOSE_SCREENS.md** (6 complete Jetpack Compose screens)
- ✅ **VIEWMODEL_EXAMPLES.md** (8 complete ViewModels)
- ✅ **data/legal_resources_sample.csv** (100 DV resources ready to import)

**Previous Work**:
- Branch: `claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97`
- Status: Excellent Android backend complete (Grade: A+)
- What's missing: Salesforce integration (explicitly requested by user)

**Estimated Time**: 24-36 hours with provided examples

All files are in: `Safehaven-documentation/` directory
```

### **Step 4: Commit Documentation to Repo** (Optional but Recommended)

1. [ ] Create new branch: `docs/completion-instructions`
2. [ ] Commit all 7 documentation files:
   ```bash
   git checkout -b docs/completion-instructions
   git add START_HERE_CLAUDE_CODE.md
   git add CLAUDE_CODE_COMPLETION_INSTRUCTIONS.md
   git add CLAUDE_CODE_BUILD_REVIEW.md
   git add GITHUB_ISSUE_TEMPLATE.md
   git add ADDITIONAL_COMPOSE_SCREENS.md
   git add VIEWMODEL_EXAMPLES.md
   git add data/legal_resources_sample.csv
   git commit -m "docs: add completion instructions for Claude Code sprint"
   git push origin docs/completion-instructions
   ```
3. [ ] Add link to branch in GitHub issue

### **Step 5: Tag Claude Code** (If Direct Messaging)

If you're using Claude Code via direct message instead of GitHub issues:

1. [ ] Copy contents of `START_HERE_CLAUDE_CODE.md`
2. [ ] Send as initial message
3. [ ] Follow up with: "All detailed instructions are in CLAUDE_CODE_COMPLETION_INSTRUCTIONS.md"
4. [ ] Provide link to repository

---

## 📦 Files to Include

Make sure these 7 files are accessible to Claude Code:

1. ✅ **START_HERE_CLAUDE_CODE.md** (450 lines)
2. ✅ **CLAUDE_CODE_COMPLETION_INSTRUCTIONS.md** (2,500+ lines)
3. ✅ **CLAUDE_CODE_BUILD_REVIEW.md** (800 lines)
4. ✅ **GITHUB_ISSUE_TEMPLATE.md** (400 lines)
5. ✅ **ADDITIONAL_COMPOSE_SCREENS.md** (1,000+ lines)
6. ✅ **VIEWMODEL_EXAMPLES.md** (900+ lines)
7. ✅ **data/legal_resources_sample.csv** (100 rows)

**Total**: 6,000+ lines of instructions + 100 sample resources

---

## 🎯 Quick Message Templates

### **GitHub Issue Comment**
```markdown
@ClaudeCode Please complete the SafeHaven build.

Previous work (branch: claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97):
✅ Excellent Android backend complete

What's needed:
1. Salesforce backend (explicitly requested, not delivered)
2. 12 Android UI screens
3. 1,000+ legal resources import
4. Test suite

All instructions ready in documentation. START HERE:
📖 START_HERE_CLAUDE_CODE.md
```

### **Direct Message**
```markdown
Hi Claude Code! I need you to complete SafeHaven.

You built an excellent Android backend. Now finish:
1. Salesforce integration (I explicitly requested this)
2. Android UI (12 screens)
3. Resource data (1,000+)
4. Tests

READ FIRST: START_HERE_CLAUDE_CODE.md
Repository: https://github.com/abbyluggery/SafeHaven-Build

Estimated: 24-36 hours with provided examples.
```

### **Slack/Discord**
```markdown
🚨 SafeHaven needs completion sprint

Previous: Excellent backend ✅
Needed: Salesforce + UI + Data + Tests

All instructions ready.
Start: START_HERE_CLAUDE_CODE.md
Time: 24-36 hrs

@ClaudeCode
```

---

## ⚠️ Important Reminders

### **Before Posting**:
- [ ] User explicitly requested "Salesforce integration" - emphasize this
- [ ] Previous backend work was excellent (Grade: A+) - acknowledge this
- [ ] All code examples are copy/paste ready - mention this
- [ ] Expected completion: 24-36 hours - set expectations

### **After Posting**:
- [ ] Monitor for questions from Claude Code
- [ ] Be available to clarify if needed
- [ ] Check branch for commits every 4-8 hours
- [ ] Review pull request when complete

---

## 📊 Expected Timeline

| Day | Task | Hours |
|-----|------|-------|
| **Day 1** | Salesforce backend | 8-12 |
| **Day 2** | Android UI foundation | 8 |
| **Day 3** | Complete UI screens | 8-16 |
| **Day 4** | Data import + tests | 8-12 |
| **TOTAL** | | **32-48** |

**With provided examples**: 24-36 hours

---

## ✅ Success Indicators

You'll know Claude Code is on track if you see:

**Day 1 commits**:
- [ ] `salesforce/objects/` directory created
- [ ] 6 custom object XML files committed
- [ ] 4 Apex class files committed
- [ ] Deployment guide committed

**Day 2 commits**:
- [ ] `ui/theme/` directory created
- [ ] `ui/navigation/` directory created
- [ ] First 6 screens committed
- [ ] First 4 ViewModels committed

**Day 3 commits**:
- [ ] Remaining 6 screens committed
- [ ] Remaining 4 ViewModels committed
- [ ] Navigation graph updated

**Day 4 commits**:
- [ ] `data/legal_resources.csv` (1,000+ rows) committed
- [ ] `ResourceImporter.kt` committed
- [ ] Test files committed
- [ ] README.md updated

---

## 🎉 Final Verification

Before closing the issue, verify:

- [ ] All 6 Salesforce objects deployed
- [ ] All 4 Apex APIs callable
- [ ] All 12 Android screens implemented
- [ ] App builds without errors
- [ ] Navigation works between screens
- [ ] 1,000+ resources imported
- [ ] Resource search returns results
- [ ] Tests pass (50%+ coverage)
- [ ] README.md updated
- [ ] DEPLOYMENT_GUIDE.md created

**When all checked**: Issue can be closed as **COMPLETE** ✅

---

## 📞 Need Help?

If Claude Code asks questions:

**Q: "Where are the existing backend files?"**
A: Branch `claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97` in `safehaven-core/` directory

**Q: "What Salesforce org should I deploy to?"**
A: User will provide org alias. If not specified, ask user.

**Q: "Should I modify existing encryption code?"**
A: NO. SafeHavenCrypto.kt is perfect. Only add UI layer.

**Q: "Where do I get 1,000+ resources?"**
A: Start with 100 sample CSV, then scrape from data sources listed in instructions.

**Q: "Do I need real blockchain deployment?"**
A: No. Mock hashes are acceptable for MVP.

---

**Ready to post?** ✅ Follow the steps above!

**Estimated completion**: 3-5 sprint cycles (24-36 hours)

**Final deliverable**: Production-ready SafeHaven app 🎉

---

**Created**: 2025-11-17
**For**: GitHub Issue Posting
**User**: @abbyluggery
