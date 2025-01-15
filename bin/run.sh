#!/bin/bash

if [[ `pwd` == */bin ]]
then
    cd ..
fi

cd build/compose/binaries/main/app/LocalSportsClub.app/Contents/MacOS
./LocalSportsClub 2>&1 | tee ../../../LSC-stdout_stderr_redirect.log
