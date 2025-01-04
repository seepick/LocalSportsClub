#!/bin/bash

cd build/compose/binaries/main/app/LocalSportsClub.app/Contents/MacOS
./LocalSportsClub 2>&1 | tee build/compose/binaries/main/app//stdout_stderr_redirect.log

