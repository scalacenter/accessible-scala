#!/usr/bin/env bash

HERE="$(dirname $(readlink -f $0))"
PKGS="$HOME/.config/sublime-text-3/Packages"

PLUGIN_DIR="accessible-scala"

if [ ! -d "$PKGS/$PLUGIN_DIR" ]; then
  pushd $PKGS
  ln -s "$HERE/$PLUGIN_DIR" "$PLUGIN_DIR"
  popd
fi

