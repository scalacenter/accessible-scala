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
    CLANG_PATH = pkgs.clang + "/bin/clang";
    CLANGPP_PATH = pkgs.clang + "/bin/clang++";

    buildInputs = with pkgs; [
      boehmgc
      clang
      espeak
      libunwind
      openjdk
      openjdk
      stdenv
      re2
      sbt
      stdenv
      zlib
    ];
  };
}