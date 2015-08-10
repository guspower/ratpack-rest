export REPO="$(pwd | sed s,^/home/travis/build/,,g)"
echo -e "Current Repo:$REPO --- Travis Branch:$TRAVIS_BRANCH"

git status

git config -l

git config --global user.email "gus@energizedwork.com"
git config --global user.name "travis"

if [ "$TRAVIS_BRANCH" == "master" ]; then
    git checkout gh-pages
    git add -A -f build/reports
    git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed to gh-pages"
    git push https://${GH_TOKEN}@github.com/${REPO} gh-pages > /dev/null
fi