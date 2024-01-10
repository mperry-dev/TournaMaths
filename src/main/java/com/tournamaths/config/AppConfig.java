package com.tournamaths.config;

import java.util.List;
import java.util.Properties;
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
    // Loaded from application-prod.properties
    @Value("${aws.region}")
    private String region;

    @Bean
    public DataSource dataSource() {
        DBInstance dbInstance = getDBInstance();

        JSONObject masterSecret = getMasterSecret(dbInstance);

        // NOTE if a managed database password is used and the password changes, existing PostgreSQL connections are not affected.
        String username = masterSecret.getString("username");
        String password = masterSecret.getString("password");

        // Return a configured DataSource using the secret values.
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(getDatabaseURL(dbInstance));
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Setup SSL communication for defence-in-depth.
        Properties sslProps = new Properties();
        sslProps.setProperty("ssl", "true");
        sslProps.setProperty("sslmode", "verify-full");
        sslProps.setProperty("sslrootcert", "/home/ec2-user/us-east-1-bundle.pem");
        sslProps.setProperty("target_session_attrs", "read-write");
        dataSource.setDataSourceProperties(sslProps);

        return dataSource;
    }

    private String getDatabaseURL(DBInstance dbInstance) {
        // Return JDBC URL for connecting to the database
        return "jdbc:postgresql://"+dbInstance.endpoint().address()+":"+dbInstance.endpoint().port()+"/"+dbInstance.dbName();
    }

    private JSONObject getMasterSecret(DBInstance dbInstance){
        String masterSecretARN = dbInstance.masterUserSecret().secretArn();

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(masterSecretARN)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        String secretString = response.secretString();

        return new JSONObject(secretString);
    }

    private DBInstance getDBInstance(){
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

            return dbInstances.get(0);

        } catch (RdsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
            return null;  // Keep type system happy.
        }
    }
}
