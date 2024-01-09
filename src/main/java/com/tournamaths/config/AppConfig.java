package com.tournamaths.config;

import java.util.List;
import org.json.JSONObject;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.RdsException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
@Profile("prod")  // only use this for production - locally rely on application.properties.
public class AppConfig {

    // Load username from application.properties
    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${aws.secretName}")
    private String secretName;

    @Value("${aws.region}")
    private String region;

    @Bean
    public DataSource dataSource() {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);
        String secretString = getSecretValueResponse.secretString();
        String password = (new JSONObject(secretString)).getString("db_admin_user_password");

        // Return a configured DataSource using the secret values.
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(getDatabaseURL());
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(password); // get from secret
        return dataSource;
    }

    private String getDatabaseURL() {
        RdsClient rdsClient = RdsClient.builder()
                .region(Region.of(region))
                .build();
        
        try {
            DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder()
                    .dbInstanceIdentifier("tournamath-db")
                    .build();

            DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
            List<DBInstance> dbInstances = response.dbInstances();

            if (dbInstances.size() != 1){
                System.err.println("Should have 1 database of identifier tournamath-db, but have "+dbInstances.size());
                System.exit(1);
            }

            DBInstance dbInstance = dbInstances.get(0);

            // Return JDBC URL for connecting to the database
            return "jdbc:postgresql://"+dbInstance.endpoint().address()+":"+dbInstance.endpoint().port()+"/"+dbInstance.dbName();

        } catch (RdsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
            return "";  // Keep type system happy.
        }
    }
}
