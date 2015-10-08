package com.twitter.csp

object CspFixtures {
  val AppName = "test-application"
  val ReportUri = "https://example.com/csp_report"
  val ChromeUA = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
  val Chrome35UA = "Mozilla/5.0 (X11; CrOS x86_64 5712.49.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.99 Safari/537.36"
  val SafariUA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A"
  val FirefoxUA = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0"
  val IeUA = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko"

  val AllowEverythingConfig: CspConfiguration = {
    new CspConfiguration().withSourceForDirective("default-src", CspConfiguration.WildcardSource)
  }

  val DenyEverythingConfig: CspConfiguration = {
    new CspConfiguration().withSourceForDirective("default-src", CspConfiguration.NoneSource)
  }
}
