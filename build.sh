#!/usr/bin/env bash
./mvnw install -Dgit.commit.hash=$(git rev-parse HEAD) -DskipTests=true