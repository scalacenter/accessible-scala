[![Travis Build Status](https://travis-ci.org/scalacenter/accessible-scala.svg?branch=master)](https://travis-ci.org/scalacenter/accessible-scala)
<!-- [![AppVeyor Build status](https://ci.appveyor.com/api/projects/status/u7o2296k904lnwyc/branch/master?svg=true)](https://ci.appveyor.com/project/scalacenter/accessible-scala/branch/master) -->
[![codecov.io](http://codecov.io/github/scalacenter/accessible-scala/coverage.svg?branch=master)](http://codecov.io/github/scalacenter/accessible-scala?branch=master)
[![Join the chat at https://gitter.im/scalacenter/accessible-scala](https://badges.gitter.im/scalacenter/accessible-scala.svg)](https://gitter.im/scalacenter/accessible-scala)
========

# Accessible Scala

Scala is proudly a welcoming environment for all. One way to maintain and demonstrate this would be to provide industry-leading support for blind and partially-sighted developers.

[SCP-016: Accessible Scala](https://github.com/scalacenter/advisoryboard/blob/master/proposals/016-verbal-descriptions.md)

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

This plugin is not up-to-date compared to the web-demo or vscode.

```bash
# setup path to espeak lib
export ESPEAK_LIB_PATH=/usr/lib/espeak

pushd sublime-text
./install.sh
popd
```

## Emacspeak

There is no integration for emacspeak yet. We would love to have your contribution!

# Roadmap

Describe:

* Terms

Cursor:

* Term.Select chain is recursive (a.b.c => a.b => a)
* Comments (licenses, commented out code)
* Priority (def name over modifiers)

Integrations:

* emacspeak