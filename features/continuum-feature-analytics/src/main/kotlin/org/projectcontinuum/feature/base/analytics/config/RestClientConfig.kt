package org.projectcontinuum.feature.base.analytics.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

/**
 * Configuration for RestClient bean used by REST Node.
 *
 * Provides a configured RestClient with sensible defaults for HTTP operations:
 * - 30 second connection timeout
 * - 30 second read timeout
 * - JDK HttpClient request factory (HTTP/2 capable, Spring Boot 4 default)
 *
 * This bean can be easily mocked or replaced in tests for better testability.
 */
@Configuration
class RestClientConfig {

  /**
   * Creates a RestClient bean with configured timeouts.
   *
   * @return Configured RestClient instance
   */
  @Bean
  fun restClient(): RestClient {
    val httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(30))
      .build()

    val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
      setReadTimeout(Duration.ofSeconds(30))
    }

    return RestClient.builder()
      .requestFactory(requestFactory)
      .build()
  }
}
