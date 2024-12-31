package com.jaeyeon.blackfriday.common.config

import com.jaeyeon.blackfriday.common.interceptor.AuthenticationInterceptor
import com.jaeyeon.blackfriday.common.interceptor.PrimeMembershipInterceptor
import com.jaeyeon.blackfriday.common.interceptor.SellerInterceptor
import com.jaeyeon.blackfriday.common.resolver.CurrentUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val primeMembershipInterceptor: PrimeMembershipInterceptor,
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
    private val sellerInterceptor: SellerInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/v1/members/signup", "/api/v1/members/login")

        registry.addInterceptor(primeMembershipInterceptor)
            .addPathPatterns("/api/**")

        registry.addInterceptor(sellerInterceptor)
            .addPathPatterns("/api/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }
}
