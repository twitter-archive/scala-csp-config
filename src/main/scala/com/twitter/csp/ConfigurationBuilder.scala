package com.twitter.csp

trait ConfigurationBuilder {
  def withSrcForDirective(directive: String, src: String): ConfigurationBuilder
  def withSourcesForDirective(directive: String, sources: Seq[String]): ConfigurationBuilder
  def withoutSrcForDirective(directive: String, src: String): ConfigurationBuilder
  def withoutDirective(directive: String): ConfigurationBuilder
  def withoutScriptNonce: ConfigurationBuilder
  def withoutStyleNonce: ConfigurationBuilder
  def withUnsafeInlineScript: ConfigurationBuilder
  def withUnsafeInlineStyle: ConfigurationBuilder
  def withUnsafeEval: ConfigurationBuilder
  def withSelfForDirective(directive: String): ConfigurationBuilder
  def withNoSourcesAllowedForDirective(directive: String): ConfigurationBuilder
  def withScriptNonce: ConfigurationBuilder
  def withStyleNonce: ConfigurationBuilder
  /** Sets the configuration to tag a uri with parameters. */
  def withTag: ConfigurationBuilder
  /** Sets the configuration to not tag a uri with parameters. */
  def withoutTag: ConfigurationBuilder
  def copy: ConfigurationBuilder
  def generateHeaderForRequest(
    userAgentString: Option[String],
    scriptNonce: Option[String] = None,
    styleNonce: Option[String] = None,
    appName: String): Option[(String, String)]
}
