#!/bin/bash

CONFIG=~/dkt/config/config.properties
SAMPLE_DIR=~/data/test_i1-i9

INPUT_DIR=$SAMPLE_DIR/input
OUTPUT_DIR=$SAMPLE_DIR/output
LOG_FILE=$SAMPLE_DIR/log.txt

echo "Tento skript spouští DKT workflow nad testovacími daty"
echo "Konfigurační soubor: $CONFIG"
echo "Adresář s testovacími daty: $SAMPLE_DIR"
echo

java -jar ~/dkt/dkt-workflow.jar -c $CONFIG -i $INPUT_DIR -o $OUTPUT_DIR >$LOG_FILE 2>&1
