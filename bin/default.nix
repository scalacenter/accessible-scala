let
  pkgs = import <nixpkgs> {};
in rec {
  espeak = pkgs.espeak.overrideDerivation (attrs: {
    configureFlags = [
      "--with-async=yes"
      "--with-pcaudiolib=yes"
    ];
  });

  clangEnv = pkgs.stdenv.mkDerivation rec {
    name = "clang-env";
    shellHook = ''
    alias cls=clear
    '';
    ESPEAK_LIB_PATH = espeak + "/lib";
    buildInputs = [
      espeak
      pkgs.openjdk
      pkgs.stdenv
      pkgs.cmake
    ];
  };
} 