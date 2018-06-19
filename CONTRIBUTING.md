# Integrations

# Web Demo

install node and yarn

```scala
web/fastOptJS::startWebpackDevServer
~web/fastOptJS
open http://localhost:8080
```

## Visual Studio Code

install node and yarn

```scala
~vscode/open
```

## Sublime Text

install espeak or espeak-ng

sbt cli/nativeLink

```bash
pushd sublime-text
./install.sh
popd
```

## Cli

### General Options

```
-f, --file <arg>      read code from file path
--stdin               read code from stdin (piped)
--output [voice|text] output text to pipe to text-to-speech or voice to useespeak
[summary|describe|breadcrumbs|navigate]
```

### Commands Options:

```
breadcrumbs:
  --offset <arg>

summary:
  [--offset <arg>]

describe:
  [--offset <arg>]

navigate
  -d, --direction (up|down|left|right) where to navigate in the code ast
  --start <arg>   selection start in offset
  --end <arg>     selection end in offset
```