package com.twitter.csp

object CspFixtures {

  val macawName = "macaw-test"

  val chromeUA = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"

  val safariUA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A"

  val firefoxUA = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0"

  val ieUA = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko"

  def defaultConfigEnforce: CspConfiguration = { new EnforcingCspConfiguration()
    .withSrcForDirective("default-src", "'self'")
    .withSrcForDirective("script-src", "'self'")
    .withSrcForDirective("style-src", "'self'")
    .withSrcForDirective("img-src", "'self'")
    .withSrcForDirective("media-src", "'self'")
    .withSrcForDirective("font-src", "'self'")
    .withSrcForDirective("connect-src", "'self'")
    .withSrcForDirective("object-src", "'self'")
    .withSrcForDirective("child-src", "'self'")
    .withSrcForDirective("frame-ancestors", "'self'")
    .withSrcForDirective("report-uri", "https://twitter.com/i/csp_report")
  }

  def defaultConfigEnforceWithNonce: CspConfiguration = { new EnforcingCspConfiguration()
    .withSrcForDirective("default-src", "'self'")
    .withSrcForDirective("script-src", "'self'")
    .withSrcForDirective("style-src", "'self'")
    .withSrcForDirective("img-src", "'self'")
    .withSrcForDirective("media-src", "'self'")
    .withSrcForDirective("font-src", "'self'")
    .withSrcForDirective("connect-src", "'self'")
    .withSrcForDirective("object-src", "'self'")
    .withSrcForDirective("child-src", "'self'")
    .withSrcForDirective("frame-ancestors", "'self'")
    .withSrcForDirective("report-uri", "https://twitter.com/i/csp_report")
    .withScriptNonce()
  }

  def defaultConfigReportOnly: CspConfiguration = { new ReportOnlyCspConfiguration()
    .withSrcForDirective("default-src", "'self'")
    .withSrcForDirective("script-src", "'self'")
    .withSrcForDirective("style-src", "'self'")
    .withSrcForDirective("img-src", "'self'")
    .withSrcForDirective("media-src", "'self'")
    .withSrcForDirective("font-src", "'self'")
    .withSrcForDirective("connect-src", "'self'")
    .withSrcForDirective("object-src", "'self'")
    .withSrcForDirective("child-src", "'self'")
    .withSrcForDirective("frame-ancestors", "'self'")
    .withSrcForDirective("report-uri", "https://twitter.com/i/csp_report")
  }
}
