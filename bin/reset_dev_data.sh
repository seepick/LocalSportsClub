#!/bin/bash

echo "rm -rf ~/.lsc-dev/database/"
rm -rf ~/.lsc-dev/database/ || exit 1
echo "cp -r  ~/.lsc-dev/database-prod/ ~/.lsc-dev/database/"
cp -r  ~/.lsc-dev/database-prod/ ~/.lsc-dev/database/ || exit 1

echo
echo "Deleting dev-DB was successful ✅👍🏻"

exit 0
