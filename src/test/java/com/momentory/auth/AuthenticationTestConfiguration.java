package com.momentory.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(AuthenticationTestController.class)
class AuthenticationTestConfiguration {
}
