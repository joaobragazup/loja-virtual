package br.com.zup.edu.nossalojavirtual.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig{

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        // @formatter:off
        http.cors()
                .and()
                .csrf().disable()
                .httpBasic().disable()
                .rememberMe().disable()
                .formLogin().disable()
                .logout().disable()
                .requestCache().disable()
                .headers().frameOptions().deny()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/users").permitAll() //1. Cadastrar um novo usuario no sistema
                .antMatchers(HttpMethod.POST, "/api/categories").hasAuthority("SCOPE_categories:write") //2. Cadastrar novas categorias de produtos
                .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("SCOPE_products:write") //3. Cadastrar produtos
                .antMatchers(HttpMethod.POST, "/api/opinions").hasAuthority("SCOPE_products:write") //4. Cadastrar opiniões sobre os produtos
                .antMatchers(HttpMethod.POST, "/api/products/**/questions").hasAuthority("SCOPE_products:write") //5. Cadastrar perguntas para sobre os produtos
                .antMatchers(HttpMethod.GET, "/api/products/**").hasAuthority("SCOPE_products:read") //6. Buscar produtos por ID
                .antMatchers(HttpMethod.POST, "/api/purchase").hasAuthority("SCOPE_purchase:write") //7. Realizar comprar
                .antMatchers(HttpMethod.POST, "/api/purchases/confirm-payment").hasAuthority("SCOPE_purchase:write") //8. PaymentGatewayReturnController
                .anyRequest()// -> para criar o h2 eu preciso remover isso //TODO Entender o pq disso
                .authenticated()// -> para criar o h2 eu preciso remover isso //TODO Entender o pq disso
                .and()
                .oauth2ResourceServer()
                .jwt(jwt -> jwt.jwkSetUri("http://localhost:18080/auth/realms/minha-loja-virtual/protocol/openid-connect/certs"))
//                .and() //TODO Mostra a Configuracao do H2
//                .authorizeRequests() //Configuracao do h2
//                .antMatchers("/").permitAll() //Configuracao do h2
//                .antMatchers("/h2-console/**").permitAll() //Configuracao do h2
//                .and() //Configuracao do h2
//                .csrf().disable() //Configuracao do h2
//                .headers().frameOptions().disable() //Configuracao do h2
        ;
        // @formatter:on
        return http.build();
    }

}
