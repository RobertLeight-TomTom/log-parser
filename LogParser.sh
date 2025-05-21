#!/bin/bash
echo "Running log-parser.jar with arguments: '$1' and '$2'"
java -jar log-parser.jar "$1" "$2"
