package com.twitter.appsec.csp

import org.apache.commons.codec.binary.Base32
import org.jboss.netty.handler.codec.http.QueryStringEncoder
import scala.collection.mutable
import scala.util.matching.Regex

trait ConfigBuilder {
  def withSrcForDirective(directive: String, src: String): ConfigBuilder
  def withSourcesForDirective(directive: String, sources: Seq[String]): ConfigBuilder
  def withoutSrcForDirective(directive: String, src: String): ConfigBuilder
  def withoutDirective(directive: String): ConfigBuilder
  def withoutScriptNonce(): ConfigBuilder
  def withoutStyleNonce(): ConfigBuilder
  def withUnsafeInlineScript(): ConfigBuilder
  def withUnsafeInlineStyle(): ConfigBuilder
  def withUnsafeEval(): ConfigBuilder
  def withSelfForDirective(directive: String): ConfigBuilder
  def withNoSourcesAllowedForDirective(directive: String): ConfigBuilder
  def withScriptNonce(): ConfigBuilder
  def withStyleNonce(): ConfigBuilder
  /** Sets the configuration to tag a uri with parameters. */
  def withTag(): ConfigBuilder
  /** Sets the configuration to not tag a uri with parameters. */
  def withoutTag(): ConfigBuilder
  def copy: ConfigBuilder
  def generateHeaderForRequest(
    userAgentString: Option[String],
    scriptNonce: Option[String] = None,
    styleNonce: Option[String] = None,
    appName: String
  ): Option[(String, String)]
}

trait CspConfiguration extends ConfigBuilder {

  import CspConfiguration._

  val headersMap: mutable.Map[String, mutable.Set[String]] = mutable.Map()
  val reportOnly: Boolean = false

  var addTag: Boolean = true

  var allowsScriptNonce = false
  var allowsStyleNonce = false

  override def withSrcForDirective(directive: String, src: String): CspConfiguration = {
    if (this.headersMap.contains(directive)) {
      this.headersMap(directive) = this.headersMap(directive) + src
    } else {
      this.headersMap(directive) = mutable.Set(src)
    }
    this
  }

  override def withSourcesForDirective(directive: String, sources: Seq[String]): CspConfiguration = {
    sources.foreach { s =>
      this.withSrcForDirective(directive, s)
    }
    this
  }

  override def withoutSrcForDirective(directive: String, src: String): CspConfiguration = {
    if (this.headersMap.contains(directive) && this.headersMap(directive).contains(src)) {
      this.headersMap(directive) = this.headersMap(directive) -= src
    }
    this
  }

  override def withoutDirective(directive: String): CspConfiguration = {
    if (this.headersMap.contains(directive)){
      this.headersMap -= directive
    }
    this
  }

  override def withoutScriptNonce(): CspConfiguration = {
    this.allowsScriptNonce = false
    this
  }

  override def withoutStyleNonce(): CspConfiguration = {
    this.allowsStyleNonce = false
    this
  }

  override def withTag(): CspConfiguration = {
    this.addTag = true
    this
  }

  override def withoutTag(): CspConfiguration = {
    this.addTag = false
    this
  }

  override def withUnsafeInlineScript(): CspConfiguration = {
    this.withSrcForDirective("script-src", "'unsafe-inline'")
  }

  override def withUnsafeInlineStyle(): CspConfiguration = {
    this.withSrcForDirective("style-src", "'unsafe-inline'")
  }

  override def withUnsafeEval(): CspConfiguration = {
    this.withSrcForDirective("script-src", "'unsafe-eval'")
  }

  override def withSelfForDirective(directive: String): CspConfiguration = {
    this.withSrcForDirective(directive, "'self'")
  }

  override def withNoSourcesAllowedForDirective(directive: String): CspConfiguration = {
    this.withSrcForDirective(directive, "'none'")
  }

  override def withScriptNonce(): CspConfiguration = {
    this.allowsScriptNonce = true
    this
  }

  override def withStyleNonce(): CspConfiguration = {
    this.allowsStyleNonce = true
    this
  }

  def tagReportUri(appName: String): String = {
    val name = new Base32().encodeAsString(appName.getBytes("UTF-8"))
    val encoder = new QueryStringEncoder(this.headersMap("report-uri").addString(new StringBuilder("")).toString)
    encoder.addParam(ApplicationNameParameterName, name)
    encoder.addParam(ReportOnlyParameterName, this.reportOnly.toString)
    encoder.toString
  }

  private def removeFrameAncestors(s: StringBuilder) = {
    val pattern = FrameAncestorsRegex
    val frameAncestors = pattern findFirstIn(s)
    val index = s.indexOf(frameAncestors.get)
    s.delete(index, index + frameAncestors.get.length())
  }

  private def addScriptNonce(s: StringBuilder, n: String) = {
    val index = s.indexOf("script-src")
    s.insert(index + ("script-src".length()), " 'nonce-" + n + "'")
  }

  private def addStyleNonce(s: StringBuilder, n: String) = {
    val index = s.indexOf("style-src")
    s.insert(index + ("style-src".length()), " nonce-" + n + "'")
  }

  private def addUnsafeInline(s: StringBuilder) = {
    val index = s.indexOf("script-src")
    s.insert(index + ("script-src".length()), " 'unsafe-inline'")
  }

  private def tagUri(s: StringBuilder, appName: String) = {
    val pattern = this.headersMap("report-uri").mkString.r
    val uri = pattern findFirstIn(s)
    uri match {
      case Some(u) =>
        val index = s.indexOf(u)
        s.delete(index, index + u.length())
        s.insert(index, tagReportUri(appName))
      case _ =>
    }
  }

  override def generateHeaderForRequest(
    userAgentString: Option[String],
    scriptNonce: Option[String] = None,
    styleNonce: Option[String] = None,
    appName: String
  ): Option[(String, String)] = {

    val headerName: String = if (this.reportOnly) {
      StandardReportOnlyHeaderName
    } else {
      StandardEnforcingHeaderName
    }

    val userAgentCompatibility = UserAgentCompatibilityFlags(userAgentString)

    if (userAgentCompatibility.supportsCSP) {
      val s = new StringBuilder("")
      this.headersMap.keys.foreach { directive =>
        s.append(" " + directive)
        this.headersMap(directive).foreach { source =>
          s.append(" " + source)
        }
        s.append(";")
      }
      
      (styleNonce, this.allowsStyleNonce) match {
        case (Some(x), true) => addStyleNonce(s, styleNonce.get)
        case _ =>
      }
      userAgentCompatibility.supportsFrameAncestors match {
        case true =>
        case false => removeFrameAncestors(s)
      }
      (userAgentCompatibility.supportsScriptNonce, scriptNonce, this.allowsScriptNonce) match {
        case (true, Some(x), true) => addScriptNonce(s, scriptNonce.get)
        case _ =>
      }
      (userAgentCompatibility.supportsUnsafeInlineWithNonce, 
        userAgentCompatibility.supportsScriptNonce, 
        scriptNonce, 
        this.allowsScriptNonce
      ) match {
        case (true, true, Some(x), true) => addUnsafeInline(s)
        case _ =>
      }
      addTag match {
        case true => tagUri(s, appName)
        case false =>
      }
      Some((headerName, s.toString))
    }
    else {
      None
    }
  }

  override def copy: CspConfiguration = {
    if (this.reportOnly) {
      val newConfig = new ReportOnlyCspConfiguration()
      this.headersMap.keys.foreach { directive =>
        newConfig.headersMap(directive) = this.headersMap(directive)
      }
      newConfig
    } else {
      val newConfig = new EnforcingCspConfiguration()
      this.headersMap.keys.foreach { directive =>
        newConfig.headersMap(directive) = this.headersMap(directive)
      }
      newConfig
    }
  }
}

class NoNonceProvidedException extends
  IllegalArgumentException("The configuration specifies to use a nonce, but no nonce was given.")

class EnforcingCspConfiguration extends CspConfiguration {

  override val reportOnly: Boolean = false
}

class ReportOnlyCspConfiguration extends CspConfiguration {

  override val reportOnly: Boolean = true
}

object CspConfiguration {
  val StandardReportOnlyHeaderName = "Content-Security-Policy-Report-Only"
  val StandardEnforcingHeaderName = "Content-Security-Policy"
  val ApplicationNameParameterName = "a"
  val ReportOnlyParameterName = "ro"
  val FrameAncestorsRegex = "frame-ancestors[^;]+;".r
}
