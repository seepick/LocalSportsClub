#!/bin/bash

if [[ `pwd` == */bin ]]
then
    cd ..
fi

VERSION_FILE=./version.txt
OLD_APP_VERSION=$(cat $VERSION_FILE)
echo "Current version is: $OLD_APP_VERSION"
read -p "Enter release version: " APP_VERSION

git_status_verify () {
  git status
  echo
  echo "Verify no GIT changes are uncommitted and hit ENTER to continue."
  read
}

git_tag_and_push () {
  echo "Incrementing and tagging version number via GIT ..."
  echo "$APP_VERSION" > $VERSION_FILE
  git add . || exit 1
  git commit -m "Increment version number to $APP_VERSION" || exit 1
  git tag -a $APP_VERSION -m "Tag new release version $APP_VERSION" || exit 1
  git push || exit 1
  git push origin $APP_VERSION || exit 1
}

git_status_verify
./gradlew check -Plsc.version="$APP_VERSION" || exit 1
git_tag_and_push
echo "Successfully created release $APP_VERSION ✅"

exit 0
