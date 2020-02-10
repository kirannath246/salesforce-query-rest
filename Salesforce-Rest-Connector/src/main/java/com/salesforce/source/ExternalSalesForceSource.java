/**
 * © Copyright 2015 Flytxt BV. ALL RIGHTS RESERVED.
 *
 * All rights, title and interest (including all intellectual property rights) in this software and any derivative works based upon or derived from this software
 * belongs exclusively to Flytxt BV. Access to this software is forbidden to anyone except current employees of Flytxt BV and its affiliated companies who have
 * executed non-disclosure agreements explicitly covering such access. While in the employment of Flytxt BV or its affiliate companies as the case may be,
 * employees may use this software internally, solely in the course of employment, for the sole purpose of developing new functionalities, features, procedures,
 * routines, customizations or derivative works, or for the purpose of providing maintenance or support for the software. Save as expressly permitted above,
 * no license or right thereto is hereby granted to anyone, either directly, by implication or otherwise. On the termination of employment, the license granted
 * to employee to access the software shall terminate and the software should be returned to the employer, without retaining any copies.
 *
 * This software is (i) proprietary to Flytxt BV; (ii) is of significant value to it; (iii) contains trade secrets of Flytxt BV; (iv) is not publicly available;
 * and (v) constitutes the confidential information of Flytxt BV. Any use, reproduction, modification, distribution, public performance or display of this software
 * or through the use of this software without the prior, express written consent of Flytxt BV is strictly prohibited and may be in violation of applicable laws.
 */
package com.salesforce.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.salesforce.SObject;

import com.salesforce.commons.ConnectionParameters;
import com.salesforce.commons.ExternalAttributeType;
import com.salesforce.commons.ExternalDataSource;
import com.salesforce.commons.ExternalObject;
import com.salesforce.commons.ExternalObjectAttribute;
import com.salesforce.data.ExtDataProcessor;
import com.salesforce.data.SalesforceDataSet;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * The ExternalSalesForceSource class
 *
 * @author kiran
 *
 */
public class ExternalSalesForceSource implements ExternalDataSource {

    private final Properties properties;

    private PartnerConnection partnerConnection;

    private BulkConnection connection;

    private Set<ExternalObject> objects;

    private static final Map<String, Set<ExternalObjectAttribute>> attributeListCache = new HashMap<String, Set<ExternalObjectAttribute>>();

    public ExternalSalesForceSource(Properties connectionProperties) {
        properties = connectionProperties;
    }

    @Override
    public void openDataSource() throws Exception {
        connection = getBulkConnection(properties.getProperty(ConnectionParameters.USERNAME),
                properties.getProperty(ConnectionParameters.PASSWORD) + properties.getProperty(ConnectionParameters.SECURITY_TOKEN));
    }

    @Override
    public void closeDataSource() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void query(String dataSelectQuery, ExtDataProcessor edp) throws Exception {

        openDataSource();
        edp.start();
        int split = 0;
        String object;
        if (dataSelectQuery.indexOf("from") == -1) {
            split = dataSelectQuery.indexOf("FROM");
        } else {
            split = dataSelectQuery.indexOf("from");
        }
        object = dataSelectQuery.substring(split + 5);
        if (object.indexOf("+") != -1) {
            object = object.substring(0, object.indexOf("+"));
        }

        processQuery(object, dataSelectQuery, edp);
        edp.finish();

    }

    @Override
    public Set<ExternalObject> getObjects() throws Exception {
        openDataSource();
        objects = new HashSet<ExternalObject>();
        final DescribeGlobalSObjectResult[] sobjects = partnerConnection.describeGlobal().getSobjects();
        for (final DescribeGlobalSObjectResult sobject : sobjects) {
            final ExternalObject eObject = new ExternalObject();
            eObject.setObjectName(sobject.getName());
            eObject.setType("object");
            objects.add(eObject);
        }
        return objects;
    }

    @Override
    public Set<ExternalObjectAttribute> getObjectAttribute(String objectName) throws Exception {
        if (attributeListCache.containsKey(objectName)) {
            return attributeListCache.get(objectName);
        } else {
            openDataSource();
            final DescribeSObjectResult result = partnerConnection.describeSObject(objectName);
            final Field[] fields = result.getFields();
            final Set<ExternalObjectAttribute> attributeList = new HashSet<ExternalObjectAttribute>();
            for (final Field field : fields) {
                final ExternalObjectAttribute attributeObj = new ExternalObjectAttribute();
                attributeObj.setAttributeName(field.getName());
                attributeObj.setType(getAttributeType(field.getType().toString()));
                attributeList.add(attributeObj);

            }
            if (!attributeListCache.containsKey(objectName)) {
                attributeListCache.put(objectName, attributeList);
            }
            return attributeList;
        }

    }

    private ExternalAttributeType getAttributeType(String type) {
        switch (type) {
        case "id":
        case "string":
        case "address":
        case "reference":
        case "picklist":
        case "textarea":
        case "url":
        case "phone":
        case "location":
            return ExternalAttributeType.STRING;

        case "int":
        case "double":
            return ExternalAttributeType.NUMBER;

        case "datetime":
        case "date":
            return ExternalAttributeType.DATE;

        case "boolean":
            return ExternalAttributeType.BOOLEAN;

        default:
            return null;

        }

    }

    @Override
    public boolean testConnection() {
        try {
            openDataSource();
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Creates a Bulk API job and query batches for a String.
     *
     * @param edp
     */
    public void processQuery(String sobjectType, String query, ExtDataProcessor edp) throws AsyncApiException, ConnectionException, IOException, InterruptedException {

//        System.setProperty("password", properties.getProperty(ConnectionParameters.PASSWORD));
//        System.setProperty("username", properties.getProperty(ConnectionParameters.USERNAME));
//        System.setProperty("clientId", properties.getProperty(ConnectionParameters.CONSUMER_KEY));
//        System.setProperty("clientSecret", properties.getProperty(ConnectionParameters.CONSUMER_SECRET));
        final org.salesforce.Util util = new SforceUtil(properties);
        final SObject sObject = new SObject(sobjectType, util);
        String nextRecordsQuery = null;
        do {
            final String response = sObject.executeSOQL(query);
            final JSONObject responseObj = new JSONObject(response);
            final JSONArray array = responseObj.getJSONArray("records");
            nextRecordsQuery = responseObj.has("nextRecordsUrl") ? responseObj.getString("nextRecordsUrl") : null;
            if (nextRecordsQuery != null) {
                query = nextRecordsQuery;
            }
            array.forEach(item -> {
                final JSONObject object = (JSONObject) item;
                final List<String> rowFields = new ArrayList<>();
                final Set<String> attributeKeys = object.keySet();
                attributeKeys.forEach(key -> {
                    if (!key.equals("attributes")) {
                        rowFields.add(object.getString(key));
                    }
                });
                final SalesforceDataSet dataset = new SalesforceDataSet(rowFields);
                try {
                    edp.processRow(dataset);
                } catch (final Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            });
        } while (nextRecordsQuery != null);

    }

    private void closeJob(BulkConnection connection, String jobId) throws AsyncApiException {
        final JobInfo job = new JobInfo();
        job.setId(jobId);
        job.setState(JobStateEnum.Closed);
        connection.updateJob(job);
    }

    /**
     * Create the BulkConnection used to call Bulk API operations.
     */
    private BulkConnection getBulkConnection(String userName, String password) throws ConnectionException, AsyncApiException {
        final ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setUsername(userName);
        partnerConfig.setPassword(password);
        partnerConfig.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/47.0");
        // Creating the connection automatically handles login and stores
        // the session in partnerConfig
        partnerConnection = new PartnerConnection(partnerConfig);
        // When PartnerConnection is instantiated, a login is implicitly
        // executed and, if successful,
        // a valid session is stored in the ConnectorConfig instance.
        // Use this key to initialize a BulkConnection:
        final ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(partnerConfig.getSessionId());
        // The endpoint for the Bulk API service is the same as for the normal
        // SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
        final String soapEndpoint = partnerConfig.getServiceEndpoint();
        final String apiVersion = "47.0";
        final String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
        config.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        config.setCompression(true);
        // Set this to true to see HTTP requests and responses on stdout
        config.setTraceMessage(false);
        return new BulkConnection(config);
    }

}
