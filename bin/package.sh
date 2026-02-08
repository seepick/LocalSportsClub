#!/bin/bash

if [[ `pwd` == */bin ]]
then
    cd ..
fi

echo ">> gradlew clean createDistributable"
./gradlew clean createDistributable || exit 1
open build/compose/binaries/main/app
