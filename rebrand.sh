#!/bin/bash

set -o errexit -o nounset -o pipefail

paths=(
    app/src/main/java
    lib/src/main/java
)

for path in ${paths[@]}; do
    git mv $path/org/calyxos/ $path/org/grapheneos/
    git mv $path/org/ $path/app/
done

find app lib -type f -exec sed -i 's/org.calyxos/app.grapheneos/' {} +
