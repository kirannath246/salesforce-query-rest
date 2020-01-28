package com.salesforce;

import java.util.Properties;

import com.salesforce.commons.ExternalDataSource;
import com.salesforce.source.ExternalSalesForceSource;



public class ExternalDataSourceFactory {

    public ExternalDataSource getExternalDataSource(Properties properties) {

        return new ExternalSalesForceSource(properties);
    }

}
