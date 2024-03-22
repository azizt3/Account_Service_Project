package account;

import account.exceptionhandler.exception.AccessDeniedExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig {

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;


    public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){ return new AccessDeniedExceptionHandler(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
            .exceptionHandling(e -> e.authenticationEntryPoint(restAuthenticationEntryPoint))
            .csrf(cfg -> cfg.disable())
            .headers(cfg -> cfg.frameOptions().disable())
            .authorizeHttpRequests(
                matcherRegistry ->
                    matcherRegistry
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment")
                        .hasAnyAuthority("ROLE_USER","ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/**")
                        .hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments")
                        .hasAuthority("ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments")
                        .hasAuthority("ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role")
                        .hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user/**")
                        .hasAuthority("ROLE_ADMINISTRATOR")
                        .anyRequest().permitAll()
            )
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and()
            .sessionManagement(
                sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }


}
