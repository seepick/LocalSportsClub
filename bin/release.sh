#!/bin/bash

if [[ `pwd` == */bin ]]
then
    cd ..
fi

VERSION_FILE=./version.txt
echo "Current version is $VERSION_FILE"
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
  git add .
  git commit -m "Increment version number to $APP_VERSION"
  git tag -a $APP_VERSION -m "Tag new release version $APP_VERSION"
  git push
  git push origin $APP_VERSION
}

git_status_verify
./gradlew package -Plsc.version="$VERSION"
git_tag_and_push
echo "Successfully created release $APP_VERSION ✅"

exit 0
