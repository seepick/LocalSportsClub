#!/bin/bash

if [[ `pwd` == */bin ]]
then
    cd ..
fi

echo ">> gradlew clean createDistributable"
# package: creates DMG as well... not needed
if ./gradlew clean createDistributable
then
  open build/compose/binaries/main/app
fi
