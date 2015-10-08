package com.twitter.csp

import org.apache.commons.codec.binary.Base32
import org.jboss.netty.handler.codec.http.QueryStringEncoder

object CspConfiguration {
  val NoneSource = "'none'"
  val SelfSource = "'self'"
  val WildcardSource = "*"
  val UnsafeInlineSource = "'unsafe-inline'"
  val UnsafeEvalSource = "'unsafe-eval'"
  val ReportUriDirectiveName = "report-uri"
  val StandardReportOnlyHeaderName = "Content-Security-Policy-Report-Only"
  val StandardEnforcingHeaderName = "Content-Security-Policy"
  val ApplicationNameParameterName = "a"
  val ReportOnlyParameterName = "ro"
  val FrameAncestorsRegex = "frame-ancestors[^;]+;".r
}

case class CspConfiguration(
  directivesMap: Map[String, Set[String]] = Map(),
  applicationName: Option[String] = None,
  reportOnly: Boolean = false,
  reportUri: Option[String] = None,
  addTagToReportUri: Boolean = true,
  useScriptNonce: Boolean = false,
  useStyleNonce: Boolean = false
) {

  import CspConfiguration._

  def withApplicationName(applicationName: String): CspConfiguration = {
    copy(applicationName = Some(applicationName))
  }

  def withoutApplicationName: CspConfiguration = {
    copy(applicationName = None)
  }

  def inReportOnlyMode: CspConfiguration = {
    copy(reportOnly = true)
  }

  def inEnforcingMode: CspConfiguration = {
    copy(reportOnly = false)
  }

  def withReportUri(reportUri: String): CspConfiguration = {
    copy(reportUri = Some(reportUri))
  }

  def withoutReportUri: CspConfiguration = {
    copy(reportUri = None)
  }

  def withSourcesForDirectives(directives: Set[String], sources: Set[String]): CspConfiguration = {
    copy(directivesMap = directivesMap ++
      (directives map { directive =>
        (directive -> (directivesMap.getOrElse(directive, Set()) ++ sources))
      })
    )
  }

  def withSourceForDirectives(directives: Set[String], source: String): CspConfiguration = {
    withSourcesForDirectives(directives, Set(source))
  }

  def withSourcesForDirective(directive: String, sources: Set[String]): CspConfiguration = {
    withSourcesForDirectives(Set(directive), sources)
  }

  def withSourceForDirective(directive: String, source: String): CspConfiguration = {
    withSourcesForDirectives(Set(directive), Set(source))
  }

  def withoutSourceForDirective(directive: String, source: String): CspConfiguration = {
    if (directivesMap.contains(directive) && directivesMap(directive).contains(source)) {
      if (directivesMap(directive).size == 1) {
        withNoSourcesAllowedForDirective(directive)
      } else {
        copy(directivesMap = directivesMap + (directive -> (directivesMap(directive) - source)))
      }
    } else {
      this
    }
  }

  def withoutDirective(directive: String): CspConfiguration = {
    copy(directivesMap = directivesMap - directive)
  }

  def withScriptNonce: CspConfiguration = {
    copy(useScriptNonce = true)
  }

  def withoutScriptNonce: CspConfiguration = {
    copy(useScriptNonce = false)
  }

  def withStyleNonce: CspConfiguration = {
    copy(useStyleNonce = true)
  }

  def withoutStyleNonce: CspConfiguration = {
    copy(useStyleNonce = false)
  }

  def withReportUriTag: CspConfiguration = {
    copy(addTagToReportUri = true)
  }

  def withoutReportUriTag: CspConfiguration = {
    copy(addTagToReportUri = false)
  }

  def withUnsafeInlineScript: CspConfiguration = {
    withSourceForDirective("script-src", UnsafeInlineSource)
  }

  def withUnsafeInlineStyle: CspConfiguration = {
    withSourceForDirective("style-src", UnsafeInlineSource)
  }

  def withUnsafeEval: CspConfiguration = {
    withSourceForDirective("script-src", UnsafeEvalSource)
  }

  def withSelfForDirective(directive: String): CspConfiguration = {
    withSourceForDirective(directive, SelfSource)
  }

  def withSelfForDirectives(directives: Set[String]): CspConfiguration = {
    withSourceForDirectives(directives, SelfSource)
  }

  def withNoSourcesAllowedForDirective(directive: String): CspConfiguration = {
    copy(directivesMap = directivesMap + (directive -> Set(NoneSource)))
  }

  def generateHeaderForRequest(
    userAgent: Option[String],
    perRequestNonceValue: Option[String] = None
  ): Option[CspHeader] = {
    val compatibilityFlags = UserAgentCompatibilityFlags(userAgent)
    if (!compatibilityFlags.supportsCsp) {
      None
    } else {
      val headerName = if (reportOnly) StandardReportOnlyHeaderName else StandardEnforcingHeaderName
      val headerValue = withPerRequestNonce(perRequestNonceValue, compatibilityFlags.supportsNonces)
        .getCompatibleConfiguration(compatibilityFlags)
        .withTaggedReportUri
        .toString
      Some(CspHeader(headerName, headerValue))
    }
  }

  override def toString: String = {
    val stringBuilder: StringBuilder = new StringBuilder()
    directivesMap foreach {
      case (directiveName, sourcesSet) =>
        stringBuilder.append(directiveName)
        stringBuilder.append(" ")
        stringBuilder.append(sourcesSet.mkString(" "))
        stringBuilder.append("; ")
    }
    reportUri map { uri =>
      stringBuilder.append("report-uri ")
      stringBuilder.append(uri)
      stringBuilder.append(";")
    }
    stringBuilder.toString
  }

  private def getCompatibleConfiguration(compatibility: UserAgentCompatibilityFlags): CspConfiguration = {
    if (!compatibility.supportsFrameAncestors) withoutDirective("frame-ancestors")
    else this
  }

  private def withPerRequestNonce(nonce: Option[String], userAgentSupportsNonce: Boolean): CspConfiguration = {
    withPerRequestNonceForDirective(useScriptNonce, "script-src", nonce, userAgentSupportsNonce)
      .withPerRequestNonceForDirective(useStyleNonce, "style-src", nonce, userAgentSupportsNonce)
  }

  private def withPerRequestNonceForDirective(shouldUseNonce: Boolean, directive: String, nonce: Option[String], userAgentSupportsNonce: Boolean): CspConfiguration = {
    if (shouldUseNonce) {
      if (userAgentSupportsNonce) {
        val nonceValue = nonce.getOrElse {
          throw new IllegalArgumentException("The configuration is configured to use a nonce, but no nonce was provided.")
        }
        val nonceSource = "'nonce-" + nonceValue + "'"
        withSourceForDirective(directive, nonceSource)
      } else {
        withSourceForDirective(directive, UnsafeInlineSource)
      }
    } else this

  }

  private def withTaggedReportUri: CspConfiguration = {
    if (addTagToReportUri) {
      reportUri map { uri =>
        val encoder = new QueryStringEncoder(uri)
        applicationName map { name =>
          encoder.addParam(ApplicationNameParameterName, new Base32().encodeAsString(name.getBytes("UTF-8")))
        }
        encoder.addParam(ReportOnlyParameterName, reportOnly.toString)
        withReportUri(encoder.toString)
      } getOrElse this
    } else this
  }
}
