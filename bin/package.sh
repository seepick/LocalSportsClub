#!/bin/bash

echo "gradlew createDistributable"
./gradlew createDistributable
# package: creates DMG as well... not needed

open build/compose/binaries/main/app
