package com.jaeyeon.blackfriday.common.config

import com.jaeyeon.blackfriday.common.interceptor.AuthenticationInterceptor
import com.jaeyeon.blackfriday.common.interceptor.OrderRateLimitInterceptor
import com.jaeyeon.blackfriday.common.interceptor.PrimeMembershipInterceptor
import com.jaeyeon.blackfriday.common.interceptor.SellerInterceptor
import com.jaeyeon.blackfriday.common.resolver.CurrentUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val primeMembershipInterceptor: PrimeMembershipInterceptor,
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
    private val sellerInterceptor: SellerInterceptor,
    private val orderRateLimitInterceptor: OrderRateLimitInterceptor,
) : WebMvcConfigurer {

    companion object {
        private val SWAGGER_PATHS = listOf(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
        )

        private val PUBLIC_PATHS = listOf(
            "/api/v1/members/signup",
            "/api/v1/members/login",
            "/products/**",
        ) + SWAGGER_PATHS
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**", "/members/**")
            .excludePathPatterns(PUBLIC_PATHS)

        registry.addInterceptor(primeMembershipInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(SWAGGER_PATHS)

        registry.addInterceptor(sellerInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(SWAGGER_PATHS)

        registry.addInterceptor(orderRateLimitInterceptor)
            .addPathPatterns("/api/v1/orders/**")
            .excludePathPatterns(SWAGGER_PATHS)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations(
                "classpath:/static/",
                "classpath:/public/",
                "classpath:/resources/",
            )
    }
}
