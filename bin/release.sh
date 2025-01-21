#!/bin/bash

if [[ `pwd` == */bin ]]
then
    cd ..
fi

VERSION_FILE=./version.txt
APP_VERSION=$(($(cat $VERSION_FILE) + 1))

git_status_verify () {
  git status
  echo
  echo "Verify no GIT changes are uncommitted and hit ENTER to continue."
  read
}

git_tag_and_push () {
  echo "Incrementing and tagging version number via GIT ..."
  echo $APP_VERSION > $VERSION_FILE
  git add .
  TAG_NAME="v${APP_VERSION}"
  git commit -m "Increment version number to $APP_VERSION"
  git tag -a $TAG_NAME -m "Tag new release version $APP_VERSION"
  git push
  git push origin $TAG_NAME
}

git_status_verify
./gradlew package -Plsc.version="$VERSION"
git_tag_and_push
echo "Successfully created release $APP_VERSION ✅"

exit 0
