package com.twitter.csp

class NoNonceProvidedException extends
  IllegalArgumentException("The configuration specifies to use a nonce, but no nonce was given.")
