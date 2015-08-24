# Finagle-CSP

A Scala library for configuring Content Security Policy headers for HTTP Requests

The Github source repository is [here](https://github.com/twitter/finagle-csp). Patches and contributions are welcome.

## Building

Use sbt (simple-build-tool) to build:

  % sbt clean update compile package

The finished jar will be in `target/scala-2.10/`.

## Usage

### Basic Usage

Create a configuration: 

  val config = new CSPConfiguration()

Add a single source to a directive:

  config.withSrcForDirective()

Add a set of sources to a directive:

  config.withSourcesForDirective()

Remove a source from a directive:

  config.withoutSrcForDirective()

Remove an entire directive from the policy:

  config.withoutDirective()

Add 'self' source to a directive:

  config.withSelfForDirective()

Add 'none' source to a directive:

  config.withNoSourcesAllowedForDirective()

Generate the header in string format (suitable for including in a HTTP response):

  config.generateHeaderForRequest()

### Advanced Usage

To add a nonce to the script-src (or style-src directive), there are two steps:

1) Tell the config to use a nonce for script-src (or style-src) 
  config.withScriptNonce (or config.withStyleNonce)

2) Supply a nonce when generating a header:
  config.generateHeaderForRequest()

To tag a report uri:

1) Tell the config to tag the uri:
  config.withTag

The parameters that are used for tagging are `ApplicationNameParameterName` and `ReportOnlyParameterName`.

## Contributors

* Annie Edmundson
* Matt Finifter

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
