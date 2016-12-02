package org.hsweb.web.controller;

import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.core.utils.ThreadLocalUtils;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Created by zhouhao on 16-5-6.
 */
@Configuration
@ComponentScan(
        basePackages = {"org.hsweb.web.controller"}
)
public class ControllerAutoConfiguration extends WebMvcConfigurerAdapter {

    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource() {
        ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
        reloadableResourceBundleMessageSource.setBasenames("classpath*:messages/", "classpath*:valid/validation");
        reloadableResourceBundleMessageSource.setUseCodeAsDefaultMessage(false);
        reloadableResourceBundleMessageSource.setFallbackToSystemLocale(true);
        return reloadableResourceBundleMessageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
                //ThreadLocalUtils.clear();
            }

            @Override
            public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
                ThreadLocalUtils.clear();
                DynamicDataSource.useDefault();
            }
        });

        ThemeChangeInterceptor themeChangeInterceptor = new ThemeChangeInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
                String newTheme = request.getParameter(getParamName());
                if (newTheme != null) {
                    ThemeResolver themeResolver = cookieThemeResolver();
                    if (themeResolver != null) {
                        themeResolver.setThemeName(request, response, newTheme);
                    }
                }
                return true;
            }
        };
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
                String newLocale = request.getParameter(getParamName());
                if (newLocale != null) {
                    if (checkHttpMethod(request.getMethod())) {
                        LocaleResolver localeResolver = cookieLocaleResolver();
                        if (localeResolver != null) {
                            try {
                                localeResolver.setLocale(request, response, StringUtils.parseLocaleString(newLocale));
                            } catch (IllegalArgumentException ex) {
                                if (isIgnoreInvalidLocale()) {
                                    logger.debug("Ignoring invalid locale value [" + newLocale + "]: " + ex.getMessage());
                                } else {
                                    throw ex;
                                }
                            }
                        }
                    }
                }
                // Proceed in any case.
                return true;
            }

            private boolean checkHttpMethod(String currentMethod) {
                String[] configuredMethods = getHttpMethods();
                if (ObjectUtils.isEmpty(configuredMethods)) {
                    return true;
                }
                for (String configuredMethod : configuredMethods) {
                    if (configuredMethod.equalsIgnoreCase(currentMethod)) {
                        return true;
                    }
                }
                return false;
            }
        };
        localeChangeInterceptor.setHttpMethods("GET");
        registry.addInterceptor(localeChangeInterceptor);
        registry.addInterceptor(themeChangeInterceptor);
    }

    @Bean(name = "localeResolver")
    public CookieLocaleResolver cookieLocaleResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.CHINA);
        return resolver;
    }

    @Bean
    public ResourceBundleThemeSource themeSource() {
        ResourceBundleThemeSource resourceBundleThemeSource = new ResourceBundleThemeSource();
        resourceBundleThemeSource.setBasenamePrefix("theme.");
        return resourceBundleThemeSource;
    }

    @Bean(name = "themeResolver")
    public CookieThemeResolver cookieThemeResolver() {
        CookieThemeResolver cookieThemeResolver = new CookieThemeResolver();
        cookieThemeResolver.setDefaultThemeName("default");
        return cookieThemeResolver;
    }

}
