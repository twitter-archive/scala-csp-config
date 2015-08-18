package com.twitter.csp

import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class CSPConfigTest
  extends FunSuite {

  test ("a default config should have the correct default values") {
    val config: CspConfiguration = CspFixtures.defaultConfigEnforce
    assertEquals(config.reportOnly, false)
    assertEquals(config.headersMap, mutable.Map(
      "script-src" -> mutable.Set("'self'"),
      "style-src" -> mutable.Set("'self'"),
      "default-src" -> mutable.Set("'self'"),
      "img-src" -> mutable.Set("'self'"),
      "media-src" -> mutable.Set("'self'"),
      "font-src" -> mutable.Set("'self'"),
      "connect-src" -> mutable.Set("'self'"),
      "object-src" -> mutable.Set("'self'"),
      "child-src" -> mutable.Set("'self'"),
      "frame-ancestors" -> mutable.Set("'self'"),
      "report-uri" -> mutable.Set("https://twitter.com/i/csp_report"))
    )
  }

  test ("a custom config should have the correct specified fields") {
    val config: CspConfiguration = CspFixtures.defaultConfigReportOnly
      .withSrcForDirective("script-src", "https:")
      .withSrcForDirective("style-src", "'self'")
      .withoutSrcForDirective("script-src", "'self'")
    assertEquals(config.reportOnly, true)
    assertEquals(config.headersMap, mutable.Map(
      "script-src" -> mutable.Set("https:"),
      "style-src" -> mutable.Set("'self'"),
      "default-src" -> mutable.Set("'self'"),
      "img-src" -> mutable.Set("'self'"),
      "media-src" -> mutable.Set("'self'"),
      "font-src" -> mutable.Set("'self'"),
      "connect-src" -> mutable.Set("'self'"),
      "object-src" -> mutable.Set("'self'"),
      "child-src" -> mutable.Set("'self'"),
      "frame-ancestors" -> mutable.Set("'self'"),
      "report-uri" -> mutable.Set("https://twitter.com/i/csp_report"))
    )
  }
}
