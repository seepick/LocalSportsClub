#!/bin/bash

cd build/compose/binaries/main/app/LocalSportsClub.app/Contents/MacOS
./LocalSportsClub 2>&1 | tee ../../../LSC-stdout_stderr_redirect.log
