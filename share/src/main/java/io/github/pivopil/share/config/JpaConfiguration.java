package io.github.pivopil.share.config;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created on 14.04.16.
 */
@Configuration
@EntityScan({"io.github.pivopil.share.entities"})
@EnableJpaRepositories("io.github.pivopil.share.persistence")
public class JpaConfiguration {

    @Bean
    public Db postgresAsyncConnectionPool(){
        return new ConnectionPoolBuilder()
                .hostname("localhost")
                .port(5432)
                .database("oauth2rx")
                .username("postgres")
                .password("1")
                .poolSize(20)
                .build();
    }



}
