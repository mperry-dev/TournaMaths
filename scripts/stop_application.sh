#!/bin/bash

# Stop Spring Boot application. Give exit code of 0 even if nothing killed.
pkill -f tournamaths.jar || true
