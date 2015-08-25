# Finagle-CSP

A Scala library for configuring Content Security Policy headers for HTTP Requests

The Github source repository is [here](https://github.com/twitter/finagle-csp). Patches and contributions are welcome.

## Building

Use sbt (simple-build-tool) to build:

  % sbt clean update compile package

The finished jar will be in `target/scala-2.10/`.

## Usage

To read how and why CSP is used, please see the standard [here](https://www.w3.org/TR/CSP/).

### Basic Usage

Create a configuration and specify if it will be a report-only configuration or if it will be enforced: 

    val config = new CSPConfiguration(reportOnly = false)

Add a single source to a directive:

    config.withSrcForDirective("script-src", "https://www.twitter.com")

Add a set of sources to a directive:

    config.withSourcesForDirective("default-src", Seq("https://www.twitter.com", "https://www.google.com"))

Remove a source from a directive:

    config.withoutSrcForDirective("script-src", "https://www.twitter.com")

Remove an entire directive from the policy:

    config.withoutDirective("object-src")

Add 'self' source to a directive:

    config.withSelfForDirective("style-src")

Add 'none' source to a directive:

    config.withNoSourcesAllowedForDirective("media-src")

Generate the header in string format (suitable for including in a HTTP response):

    val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    config.generateHeaderForRequest(userAgentString = Some(userAgent), appName = "testApp")

### Advanced Usage

To add a nonce to the script-src (or style-src directive), there are two steps:

1) Tell the config to use a nonce for script-src (or style-src) 
    config.withScriptNonce (or config.withStyleNonce)

2) Supply a nonce when generating a header:

    val nonce = <some random number>
    config.generateHeaderForRequest(userAgentString = Some(userAgent), scriptNonce = Some(nonce), appName = "testApp")

To tag a report uri:

    config.withTag

The parameters that are used for tagging are `ApplicationNameParameterName` and `ReportOnlyParameterName`.

### Example Configuration

    val config = new CSPConfiguration(reportOnly = false)
      .withSelfForDirective("default-src")
      .withSrcForDirective("img-src", "*")
      .withSourcesForDirective("media-src", Seq("media1.com", "media2.com", "*.cdn.com"))
      .withSrcForDirective("script-src", "trustedscripts.example.com")
      .withSrcForDirective("report-uri", "mysite.example.com")
      .withTag
      .withScriptNonce

    val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"

    val nonce = <some random number>
    
    val header = config.generateHeaderForRequest(Some(userAgent), scriptNonce = Some(nonce), appName = "example")

The code above generates the following header.

    Content-Security-Policy: default-src 'self'; script-src trustedscripts.example.com 'nonce-<some random number>'; style-src ; img-src *; media-src media1.com media2.com *.cdn.com; report-uri mysite.example.com?a=ZXhhbXBsZQ==ro=false;

## Contributors

* Annie Edmundson
* Matt Finifter

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
