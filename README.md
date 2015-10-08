# Finagle-CSP

A Scala library for configuring Content Security Policy headers for HTTP responses.

The Github source repository is [here](https://github.com/twitter/finagle-csp). Patches and contributions are welcome.

## Building

Use sbt (simple-build-tool) to build:

  % sbt clean update compile package

The finished jar will be in `target/scala-2.10/`.

## Usage

To read how and why CSP is used, please see the specification [here](http://www.w3.org/TR/CSP2/).

### Basic Usage

Create a new configuration:

    val config = new CspConfiguration()

Create a report-only configuration:

    val reportOnlyConfig = config.inReportOnlyMode

Add a single source to a directive:

    val newConfig = config.withSrcForDirective("script-src", "https://example.com")

Add a set of sources to a directive:

    val newConfig = config.withSourcesForDirective("default-src", Set("https://an.example.com", "https://another.example.com"))

Remove a source from a directive:

    val newConfig = config.withoutSrcForDirective("script-src", "https://example.com")

Remove an entire directive from the policy:

    val newConfig = config.withoutDirective("object-src")

Add 'self' source to a directive:

    val newConfig = config.withSelfForDirective("style-src")

Add 'none' source to a directive:

    val newConfig = config.withNoSourcesAllowedForDirective("media-src")

Add a report-uri:

    val newConfig = config.withReportUri("https://example.com/csp_report")

Generate the header in string format (suitable for including in an HTTP response):

    val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    config.generateHeaderForRequest(Some(userAgent))

### Advanced Usage

To add a nonce to the script-src (or style-src directive), there are two steps:

1) Tell the config to use a nonce for script-src (or style-src) 
    val scriptNonceConfig = config.withScriptNonce
    val styleNonceConfig = config.withStyleNonce

2) Supply a nonce when generating a header:

    val nonce = <a random nonce>
    config.generateHeaderForRequest(Some(userAgent), Some(nonce))

To "tag" the report-uri with an application name and a boolean indicating whether the configuration is report-only (vs. enforcing):

    val taggedConfig = config.withApplicationName("app-name").withTag

The parameters that are used for tagging are `ApplicationNameParameterName` and `ReportOnlyParameterName`.

### Example Configuration

    val config = new CspConfiguration
      .withSelfForDirective("default-src")
      .withSrcForDirective("img-src", "*")
      .withSourcesForDirective("media-src", Set("media1.example.com", "media2.example.com")
      .withSrcForDirective("script-src", "scripts.example.com")
      .withReportUri("https://example.com/csp_report")
      .withScriptNonce
      .withApplicationName("example")
      .withTag

    val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"

    val nonce = <some random number>
    
    val header = config.generateHeaderForRequest(Some(userAgent), Some(nonce))

The code above generates the following header.

    Content-Security-Policy: default-src 'self'; script-src scripts.example.com 'nonce-<some random number>'; img-src *; media-src media1.example.com media2.example.com; report-uri https://example.com/csp_report?a=ZXhhbXBsZQ%3D%3D&ro=false;

## Contributors

* Annie Edmundson
* Matt Finifter

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
