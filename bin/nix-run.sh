#!/usr/bin/env bash

HERE="`dirname $0`"

nix-shell $HERE/default.nix -A clangEnv