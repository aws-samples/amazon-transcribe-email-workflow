#!/bin/bash
set -eo pipefail
gradle -q packageLibs
mv build/distributions/TranscribeEmailDemo.zip build/s3-java-lib.zip