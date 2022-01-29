package hu.schbme.pay.station.config;

import hu.schbme.pay.station.service.AdministratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String ADMIN_ROLE = "ADMIN";

    @Value("${paymentsystem.admin.username:admin}")
    private String adminUsername;

    @Value("${paymentsystem.admin.password:1234}")
    private String adminPassword;

    private final AdministratorService administratorService;
    private final DaoAuthenticationProvider authProvider;

    public SecurityConfig(AdministratorService administratorService, DaoAuthenticationProvider authProvider) {
        this.administratorService = administratorService;
        this.authProvider = authProvider;
    }

    @PostConstruct
    public void init() {
        if (!adminUsername.isBlank() && !administratorService.usernameExist(adminUsername)) {
            log.info("Register new admin user '{}'", adminUsername);
            administratorService.registerNewUserAccount(adminUsername, adminPassword);
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .and().authorizeRequests()
                    .antMatchers("/admin/**").hasRole(ADMIN_ROLE)
                    .antMatchers("/api/**", "/images/**", "/logout", "/login", "/login-error", "/").permitAll()
                .and().formLogin()
                    .loginPage("/login")
                    .failureUrl("/login-error")
                    .successForwardUrl("/admin/")
                .and().logout()
                    .logoutSuccessUrl("/login")
                .and()
                    .cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()).and()
                    .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

}