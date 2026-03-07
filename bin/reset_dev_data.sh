#!/bin/bash

echo "rm -rf ~/.lsc-dev/database/"
rm -rf ~/.lsc-dev/database || exit 1

echo "cp -r  ~/.lsc/database/ ~/.lsc-dev/database/"
cp -r  ~/.lsc/database ~/.lsc-dev/database || exit 1

echo
echo "Deleting dev-DB was successful ✅👍🏻"

exit 0
