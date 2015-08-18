package com.twitter.csp

import com.twitter.finagle.http.{Method, Response, Request}
import eu.bitwalker.useragentutils.UserAgent
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CSPHeaderTest
  extends FunSuite {

  test ("a header should have default configurations") {
    val request = Request(Method.Get, "www.twitter.com")
    request.headers().add("user-agent", CspFixtures.chromeUA)
    val config: CspConfiguration = CspFixtures.defaultConfigEnforce
    assertEquals(config.reportOnly, false)
    val cspHeader: Option[(String, String)] = config.generateHeaderForRequest(userAgentString = request.userAgent, appName = CspFixtures.macawName)
    assertTrue(cspHeader != None)
    val cspName: String = cspHeader.get._1
    val cspValue: String = cspHeader.get._2
    assertEquals(cspName, "Content-Security-Policy")
    assertTrue(cspValue.contains("default-src 'self';"))
    assertTrue(cspValue.contains("script-src 'self';"))
    assertTrue(cspValue.contains("style-src 'self';"))
    assertTrue(cspValue.contains("img-src 'self';"))
    assertTrue(cspValue.contains("media-src 'self';"))
    assertTrue(cspValue.contains("font-src 'self';"))
    assertTrue(cspValue.contains("connect-src 'self';"))
    assertTrue(cspValue.contains("object-src 'self';"))
    assertTrue(cspValue.contains("child-src 'self';"))
    assertTrue(cspValue.contains("frame-ancestors 'self';"))
  }

  test ("a header should have the correct name in report only mode") {
    val request = Request(Method.Get, "www.twitter.com")
    request.headers().add("user-agent", CspFixtures.chromeUA)
    val config: CspConfiguration = CspFixtures.defaultConfigReportOnly
    assertEquals(config.reportOnly, true)
    val cspHeader: Option[(String, String)] = config.generateHeaderForRequest(userAgentString = request.userAgent, appName = CspFixtures.macawName)
    assertTrue(cspHeader != None)
    val cspName: String = cspHeader.get._1
    val cspValue: String = cspHeader.get._2
    assertEquals(cspName, "Content-Security-Policy-Report-Only")
  }
}
