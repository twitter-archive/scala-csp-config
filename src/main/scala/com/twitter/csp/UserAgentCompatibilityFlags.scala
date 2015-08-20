package com.twitter.csp

import com.twitter.util.Try
import eu.bitwalker.useragentutils.UserAgent

case class UserAgentCompatibilityFlags(
  supportsFrameAncestors: Boolean,
  supportsScriptNonce: Boolean,
  supportsUnsafeInlineWithNonce: Boolean,
  supportsCSP: Boolean)

object UserAgentCompatibilityFlags {

  private def userAgentAttributes(userAgentString: Option[String]) = {
    val userAgent = new UserAgent(userAgentString.getOrElse(""))
    val browser = userAgent.getBrowser
    val browserName = browser.getGroup.getName
    val browserMajorVersion = for {
      userAgent <- userAgentString
      version <- Option(browser.getVersion(userAgent))
      majorVersion <- Try(version.getMajorVersion.toInt).toOption
    } yield majorVersion
    (browserName, browserMajorVersion)
  }

  def supportsFrameAncestor(browserName: String, browserMajorVersion: Option[Int]): Boolean = {
    browserName == "Firefox" ||
    (browserMajorVersion.exists { _ >= 40 } && browserName == "Chrome")
  }

  def supportsScriptNonce(browserName: String, browserMajorVersion: Option[Int]): Boolean = {
    (browserMajorVersion.exists { _ >= 36 } && browserName == "Chrome") ||
    (browserMajorVersion.exists { _ >= 36 } && browserName == "Firefox")
  }

  def supportsUnsafeInlineWithNonce(browserName: String): Boolean = {
    // chrome follows the spec (ignores 'unsafe-inline' when a nonce is present), but firefox and safari don't
    // bug link: https://bugzilla.mozilla.org/show_bug.cgi?id=1004703
    browserName == "Chrome"
  }

  def supportsCSP(browserName: String): Boolean = {
    browserName == "Chrome" ||
    browserName == "Firefox" ||
    browserName == "Safari"
  }

  def apply(userAgent: Option[String]): UserAgentCompatibilityFlags = {
    val browserName = userAgentAttributes(userAgent)._1
    val browserMajorVersion = userAgentAttributes(userAgent)._2
    new UserAgentCompatibilityFlags(
      supportsFrameAncestor(browserName, browserMajorVersion),
      supportsScriptNonce(browserName, browserMajorVersion),
      supportsUnsafeInlineWithNonce(browserName),
      supportsCSP(browserName))
  }
}
