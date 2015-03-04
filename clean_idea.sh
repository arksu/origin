#!/bin/sh
rm -fr .idea
find . -type f -name "*.iml" -exec rm -f {} \;