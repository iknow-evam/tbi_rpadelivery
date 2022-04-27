package com.evam.marketing.communication.template.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

@Service
@Slf4j
public class DbLogging {
    String DBUser, DBPass, preparedStatementSQL = null;
    static String DBUrl = null;
    Connection connection;
    PreparedStatement pstmt;

    public DbLogging() {
        try {
            fetchCredentials();
            connectDB();
        } catch (Exception ex) {
            log.error("Exception on DB connection:{} ", ex.getMessage());
        }
    }

    void connectDB() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(DBUrl, DBUser, DBPass);
            connection.setAutoCommit(false);
            pstmt = connection.prepareStatement(preparedStatementSQL);
        } catch (Exception ex) {
            log.error("Exception on DB connection:{} ", ex.getMessage());
        }
    }

    void fetchCredentials() {
        try {
            FileReader fileReader = new FileReader("config/application.yml");
            Properties prop = new Properties();
            prop.load(fileReader);

            DBUrl = prop.getProperty("url");
            log.info("fetchCredentials DBUrl: {}", DBUrl);
            DBUser = prop.getProperty("username");
            log.info("fetchCredentials DBUser: {}", DBUser);
            DBPass = prop.getProperty("password");
            log.info("fetchCredentials DBPass: {}", DBPass);
            preparedStatementSQL = "INSERT INTO LOG_WHATSAPP_PUSH_NOTIFICATION VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        } catch (Exception ex) {
            log.error("Exception on fetching DB credentials:{} ", ex.getMessage());
        }
    }

    public void populateDBLog(
            String campaignName,
            String offerId,
            String silentMode,
            String msisdn,
            String messageText,
            String notificationType,
            String status,
            String fcmId,
            String notificationTitle,
            String notificationText,
            String urlIdentifier,
            String deepUrl,
            String uaciInteractionPointName,
            String uaciInteractiveChannelName,
            String sessionId,
            String commercialText,
            String offerPayload,
            String logger,
            String endpoint,
            String response) throws SQLException {
        try {
            log.info("setting parameters of statement");
            pstmt.setString(1, campaignName);
            pstmt.setString(2, offerId);
            pstmt.setString(3, silentMode);
            pstmt.setString(4, msisdn);
            pstmt.setString(5, messageText);
            pstmt.setString(6, notificationType);
            pstmt.setString(7, status);
            pstmt.setString(8, fcmId);
            pstmt.setString(9, notificationTitle);
            pstmt.setString(10, notificationText);
            pstmt.setString(11, urlIdentifier);
            pstmt.setString(12, deepUrl);
            pstmt.setString(13, uaciInteractionPointName);
            pstmt.setString(14, uaciInteractiveChannelName);
            pstmt.setString(15, sessionId);
            pstmt.setString(16, commercialText);
            pstmt.setString(17, offerPayload);
            pstmt.setString(18, logger);
            pstmt.setString(19, endpoint);
            pstmt.setString(20, response);

            log.info("executing statement");
            pstmt.execute();
            log.info("committing connection");
            connection.commit();
            log.info("closing connection");
            connection.close();
            log.info("successfully logged.");
        } catch (Exception ex) {
            log.error("Exception on data insertion {}:{} ", msisdn, ex.getMessage(), ex);
            connection.rollback();
            connection.close();
            pstmt.close();
        }
    }
}
