export REPO="$(pwd | sed s,^/home/travis/build/,,g)"
echo -e "Current Repo:$REPO --- Travis Branch:$TRAVIS_BRANCH"

git status

git config -l

git config --global user.email "gus@energizedwork.com"
git config --global user.name "travis"

git remote add upstream https://${GH_TOKEN}@github.com/${REPO}.git > /dev/null
git fetch -qn upstream > /dev/null

find .

if [ "$TRAVIS_BRANCH" == "master" ]; then
    mkdir -p /tmp/gh-pages
    cp -R build/reports /tmp/gh-pages
    git checkout gh-pages
    cp -R /tmp/gh-pages/reports build
    find build/reports -name *json | perl -e 'use JSON; @in=grep(s/\n$//, <>); print encode_json(\@in)."\n";' > build/reports/benchmark.json
    git add -A -f build/reports
    git status
    git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed to gh-pages"
    git push https://${GH_TOKEN}@github.com/${REPO} gh-pages > /dev/null
fi