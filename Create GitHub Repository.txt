# 1. Initialize Git repo
cd safehaven-documentation
git init

# 2. Create .gitignore
echo "*.DS_Store
node_modules/
.env" > .gitignore

# 3. Add all files
git add .
git commit -m "Initial commit: SafeHaven complete documentation"

# 4. Create GitHub repo (on github.com)
# - Go to github.com/new
# - Name: "safehaven-documentation"
# - Description: "Complete technical and operational documentation for NeuroThrive SafeHaven - a free domestic violence safety planning platform"
# - Public repository
# - Don't initialize with README (you already have one)

# 5. Push to GitHub
git remote add origin https://github.com/YOUR_USERNAME/safehaven-documentation.git
git branch -M main
git push -u origin main