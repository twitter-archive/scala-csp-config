package com.twitter.csp

import com.twitter.util.Try
import eu.bitwalker.useragentutils.UserAgent

case class UserAgentCompatibilityFlags(
  supportsCsp: Boolean,
  supportsNonces: Boolean,
  supportsFrameAncestors: Boolean
)

object UserAgentCompatibilityFlags {

  private val Chrome: String = "Chrome"
  private val Firefox: String = "Firefox"
  private val Safari: String = "Safari"
  private val ChromeMinimumNonceVersion = 36
  private val FirefoxMinimumNonceVersion = 36
  private val ChromeMinimumFrameAncestorsVersion = 40

  private def getBrowserNameAndVersion(userAgentString: Option[String]): (String, Option[Int]) = {
    val userAgent = new UserAgent(userAgentString.getOrElse(""))
    val browserName = userAgent.getBrowser.getGroup.getName
    val versionNumber = Try(userAgent.getBrowserVersion.getMajorVersion.toInt).toOption
    (browserName, versionNumber)
  }

  private def supportsCsp(browserName: String, browserMajorVersion: Option[Int]): Boolean = {
    browserName == Chrome || browserName == Firefox || browserName == Safari
  }

  private def supportsNonces(browserName: String, browserMajorVersion: Option[Int]): Boolean = {
    (browserMajorVersion.exists { _ >= ChromeMinimumNonceVersion } && browserName == Chrome) ||
      (browserMajorVersion.exists { _ >= FirefoxMinimumNonceVersion } && browserName == Firefox)
  }

  private def supportsFrameAncestors(browserName: String, browserMajorVersion: Option[Int]): Boolean = {
    browserName == Firefox ||
      (browserMajorVersion.exists { _ >= ChromeMinimumFrameAncestorsVersion } && browserName == Chrome)
  }

  def apply(userAgent: Option[String]): UserAgentCompatibilityFlags = {
    val (name, majorVersion) = getBrowserNameAndVersion(userAgent)
    new UserAgentCompatibilityFlags(
      supportsCsp(name, majorVersion),
      supportsNonces(name, majorVersion),
      supportsFrameAncestors(name, majorVersion)
    )
  }
}
