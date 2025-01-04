#!/bin/bash

echo "gradlew clean createDistributable"
./gradlew clean createDistributable
# package: creates DMG as well... not needed

open build/compose/binaries/main/app
