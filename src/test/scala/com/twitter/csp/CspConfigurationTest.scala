package com.twitter.csp.config

import com.twitter.inject.Test
import org.apache.commons.codec.binary.Base32
import org.jboss.netty.handler.codec.http.QueryStringEncoder

class CspConfigurationTest extends Test {

  "withSourcesForDirective" should {
    "correctly add multiple sources to multiple directives" in {
      val baseConfig = CspFixtures.AllowEverythingConfig
      val newConfig = baseConfig
        .withSourcesForDirectives(Set("img-src", "frame-src"), Set("https://foo.com", "https://bar.com"))
      newConfig.directivesMap should equal(Map(
        "default-src" -> Set("*"),
        "img-src" -> Set("https://foo.com", "https://bar.com"),
        "frame-src" -> Set("https://foo.com", "https://bar.com")
      ))
    }
    "correctly add a single source to multiple directives" in {
      val baseConfig = CspFixtures.AllowEverythingConfig
      val newConfig = baseConfig
        .withSourcesForDirectives(Set("img-src", "frame-src"), Set("https://bar.com"))
      newConfig.directivesMap should equal(Map(
        "default-src" -> Set("*"),
        "img-src" -> Set("https://bar.com"),
        "frame-src" -> Set("https://bar.com")
      ))
    }
    "correctly add zero sources to multiple directives" in {
      val baseConfig = CspFixtures.AllowEverythingConfig
      val newConfig = baseConfig
        .withSourcesForDirectives(Set("img-src", "frame-src"), Set())
      newConfig.directivesMap should equal(Map(
        "default-src" -> Set("*"),
        "img-src" -> Set(),
        "frame-src" -> Set()
        // TODO: may want to change this behavior.  e.g., on output, if directive is present but no sources, use 'none'
      ))
    }
    "correctly add multiple sources to a single directive" in {
      val baseConfig = CspFixtures.AllowEverythingConfig
      val newConfig = baseConfig
        .withSourcesForDirectives(Set("frame-src"), Set("https://foo.com", "https://bar.com"))
      newConfig.directivesMap should equal(Map(
        "default-src" -> Set("*"),
        "frame-src" -> Set("https://foo.com", "https://bar.com")
      ))
    }
    "correctly add multiple sources to zero directives" in {
      val baseConfig = CspFixtures.AllowEverythingConfig
      val newConfig = baseConfig
        .withSourcesForDirectives(Set(), Set("https://foo.com", "https://bar.com"))
      newConfig.directivesMap should equal(Map(
        "default-src" -> Set("*")
      ))
    }
    "correctly add zero sources to zero directives" in {
      val baseConfig = CspFixtures.AllowEverythingConfig
      val newConfig = baseConfig
        .withSourcesForDirectives(Set(), Set())
      newConfig.directivesMap should equal(Map(
        "default-src" -> Set("*")
      ))
    }
  }

  "withoutSourceForDirective" should {
    "remove a single source when there are multiple sources for a given directive" in {
      val baseConfig = CspConfiguration()
        .withSourcesForDirectives(Set("script-src", "img-src"), Set("https://foo.com", "https://bar.com"))
      val newConfig = baseConfig
        .withoutSourceForDirective("img-src", "https://foo.com")
      newConfig.directivesMap should equal(Map(
        "script-src" -> Set("https://foo.com", "https://bar.com"),
        "img-src" -> Set("https://bar.com")
      ))
    }

    "set the sources to 'none' when removing the last source for a given directive" in {
      val baseConfig = CspConfiguration()
        .withSourceForDirectives(Set("script-src", "img-src"), "https://foo.com")
      val newConfig = baseConfig
        .withoutSourceForDirective("img-src", "https://foo.com")
      newConfig.directivesMap should equal(Map(
        "script-src" -> Set("https://foo.com"),
        "img-src" -> Set("'none'")
      ))
    }

    "do nothing when the source isn't specified for the directive" in {
      val baseConfig = CspConfiguration()
        .withSourcesForDirectives(Set("script-src", "img-src"), Set("https://foo.com", "https://bar.com"))
      val newConfig = baseConfig
        .withoutSourceForDirective("img-src", "https://example.com")
      newConfig should equal(baseConfig)
    }
  }

  "generateHeaderForRequest" should {
    "return None when the user agent doesn't support CSP" in {
      val config = CspFixtures.AllowEverythingConfig
      val header = config.generateHeaderForRequest(Some(CspFixtures.IeUA))
      header should equal(None)
    }

    "use the report-only header name when the configuration is report-only" in {
      val config = CspFixtures.AllowEverythingConfig.inReportOnlyMode
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA))
      header should not equal(None)
      header.get.name should equal(CspConfiguration.StandardReportOnlyHeaderName)
    }

    "use the enforcing header name when the configuration is enforcing" in {
      val config = CspFixtures.AllowEverythingConfig
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA))
      header should not equal(None)
      header.get.name should equal(CspConfiguration.StandardEnforcingHeaderName)
    }

    "not add a nonce when configured not to use nonces" in {
      val config = CspFixtures.AllowEverythingConfig
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA), Some("nonce-value"))
      header should not equal(None)
      header.get.value should not include("nonce")
    }

    "add a nonce when configured to do so and the user agent supports it" in {
      val config = CspFixtures.AllowEverythingConfig
        .withScriptNonce
        .withStyleNonce
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA), Some("nonce-value"))
      header should not equal(None)
      header.get.value should include regex("script-src[^;]+'nonce-nonce-value'")
      header.get.value should include regex("style-src[^;]+'nonce-nonce-value'")
    }

    "add unsafe-inline when configured to use nonce and the user agent does not support it" in {
      val config = CspFixtures.AllowEverythingConfig
        .withScriptNonce
        .withStyleNonce
      val header = config.generateHeaderForRequest(Some(CspFixtures.Chrome35UA), Some("nonce-value"))
      header should not equal(None)
      header.get.value should not include regex("script-src[^;]+'nonce-nonce-value'")
      header.get.value should not include regex("style-src[^;]+'nonce-nonce-value'")
      header.get.value should include regex("script-src[^;]+'unsafe-inline'")
      header.get.value should include regex("style-src[^;]+'unsafe-inline'")
    }

    "throw an exception when configured to use nonce but no nonce was provided" in {
      val config = CspFixtures.AllowEverythingConfig
        .withScriptNonce
        .withStyleNonce
      val thrown = the [IllegalArgumentException] thrownBy config.generateHeaderForRequest(Some(CspFixtures.ChromeUA))
      thrown.getMessage should equal("The configuration is configured to use a nonce, but no nonce was provided.")
    }

    "tag the report URI when configured to do so" in {
      val config = CspFixtures.AllowEverythingConfig
        .withApplicationName(CspFixtures.AppName)
        .withReportUri(CspFixtures.ReportUri)
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA))
      header should not equal(None)

      val encoder = new QueryStringEncoder(CspFixtures.ReportUri)
      encoder.addParam("a", new Base32().encodeAsString(CspFixtures.AppName.getBytes("UTF-8")))
      encoder.addParam("ro", "false")
      val taggedReportUri = encoder.toString

      header.get.value should include(s"report-uri $taggedReportUri;")
    }

    "tag the report URI with only the report-only value when there is no application name" in {
      val config = CspFixtures.AllowEverythingConfig
        .inReportOnlyMode
        .withReportUri(CspFixtures.ReportUri)
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA))
      header should not equal(None)

      val encoder = new QueryStringEncoder(CspFixtures.ReportUri)
      encoder.addParam("ro", "true")
      val taggedReportUri = encoder.toString

      header.get.value should include(s"report-uri $taggedReportUri;")
    }

    "not tag the report URI when configured as such" in {
      val reportUri = CspFixtures.ReportUri
      val config = CspFixtures.AllowEverythingConfig
        .withApplicationName(CspFixtures.AppName)
        .withReportUri(reportUri)
        .withoutReportUriTag
      val header = config.generateHeaderForRequest(Some(CspFixtures.ChromeUA))
      header should not equal(None)
      header.get.value should include(s"report-uri $reportUri;")
    }

    "remove the frame-ancestors directive for a user-agent that doesn't support it" in {
      val config = CspFixtures.AllowEverythingConfig
        .withSourceForDirective("frame-ancestors", "https://example.com")
      val header = config.generateHeaderForRequest(Some(CspFixtures.SafariUA))
      header should not equal(None)
      header.get.value should not include("frame-ancestors")
    }
  }

  "toString" should {
    "correctly add multiple directives" in {
      val config = CspConfiguration()
        .withSourcesForDirective("script-src", Set("https://foo.com", "https://bar.com"))
        .withSourcesForDirective("img-src", Set("https://larry.com", "https://curly.com", "https://moe.com"))
        .withSourceForDirective("child-src", CspConfiguration.SelfSource)
      val stringified = config.toString
      stringified should include regex("script-src[^;]+https://foo\\.com[^;]*;")
      stringified should include regex("script-src[^;]+https://bar\\.com[^;]*;")
      stringified should include regex("img-src[^;]+https://larry\\.com[^;]*;")
      stringified should include regex("img-src[^;]+https://curly\\.com[^;]*;")
      stringified should include regex("img-src[^;]+https://moe\\.com[^;]*;")
      stringified should include regex("child-src[^;]+'self'[^;]*;")
    }

    "correctly add a report-uri at the end" in {
      val reportUri = CspFixtures.ReportUri
      val config = CspConfiguration()
        .withSourcesForDirective("script-src", Set("https://foo.com", "https://bar.com"))
        .withSourcesForDirective("img-src", Set("https://larry.com", "https://curly.com", "https://moe.com"))
        .withSourceForDirective("child-src", CspConfiguration.SelfSource)
        .withReportUri(reportUri)
        .withoutReportUriTag
      val stringified = config.toString
      stringified should endWith (s"report-uri $reportUri;")
    }
  }
}
