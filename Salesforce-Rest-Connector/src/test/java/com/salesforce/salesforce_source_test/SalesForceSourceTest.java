package com.salesforce.salesforce_source_test;

import java.text.SimpleDateFormat;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.salesforce.commons.ConnectionParameters;
import com.salesforce.data.ExtDataProcessor;
import com.salesforce.data.ExternalDataSet;
import com.salesforce.source.ExternalSalesForceSource;

public class SalesForceSourceTest {

    static final Properties properties = new Properties();

    static final ExternalSalesForceSource salesforce = new ExternalSalesForceSource(properties);

    static final String previousDateString = "1970-01-01T00:00:00.000Z";

    static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @BeforeClass
    public static void beforeClass() {

        properties.setProperty(ConnectionParameters.PASSWORD, "king@135");
        properties.setProperty(ConnectionParameters.USERNAME, "kiran246@flytxt.com");
        properties.setProperty(ConnectionParameters.CONSUMER_KEY, "3MVG9n_HvETGhr3DY5cNjH9EJ_gXqoUvU8olBacBAGI7.7DVNE4_IM1.mRNk.HkIBAD1ZhlnFSrnTud7VlWFH");
        properties.setProperty(ConnectionParameters.CONSUMER_SECRET, "4E620363D59BA7EB5B6F1EDD29B54BD8571AF1A64BB9C6ABC16D8C858AA4EE71");
        properties.setProperty(ConnectionParameters.SECURITY_TOKEN, "eLPbyjEpAKMCqDeeIaI8ghoMb");
        properties.setProperty(ConnectionParameters.SALES_FORCE_URL, "https://login.salesforce.com");
        properties.setProperty(ConnectionParameters.SALES_FORCE_VERSION, "44.0");

    }

    @Test
    public void salesforceAttributeListing() throws Exception {
        System.out.println(salesforce.getObjectAttribute("Account"));
    }

    @Test
    public void salesforceObjectlisting() throws Exception {
        System.out.println(salesforce.getObjects());
        System.out.println(salesforce.getObjects().size());
    }

    @Test
    public void salesforceQueryExecution() throws Exception {

        salesforce.query("SELECT+Id+FROM+Account", new ExtDataProcessor() {

            @Override
            public void start() {
            }

            @Override
            public void processRow(ExternalDataSet data) throws Exception {
                for (int i = 0; i < data.getColumnCount(); i++) {
                    if (i == 4) {
                        System.out.print(format.parse(data.getString(i)));
                    } else {
                        System.out.print(data.getString(i) + ";");
                    }
                }
                System.out.print("\n");
            }

            @Override
            public void finish() {
            }
        });
    }

    @Test
    public void connectionTest() {
        System.out.println(salesforce.testConnection());
    }
}
