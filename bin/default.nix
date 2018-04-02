let
  pkgs = import <nixpkgs> {};
in rec {
  clangEnv = pkgs.stdenv.mkDerivation rec {
    name = "clang-env";
    shellHook = ''
    alias cls=clear
    '';
    ESPEAK_LIB_PATH = pkgs.espeak + "/lib";
    buildInputs = with pkgs; [
      espeak
      openjdk
      stdenv
    ];
  };
} 