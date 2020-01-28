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
package com.salesforce.data;

import java.util.List;

/**
 * The SalesforceDataSet class
 *
 * @author kiran
 *
 */
public class SalesforceDataSet implements ExternalDataSet {

    // private final String row;

    private final List<String> rowFields;

    public SalesforceDataSet(List<String> rowFields) {
        // this.row = line;
        // rowFields = row.split("\",\"");
        // rowFields[0] = rowFields[0].substring(1);
        // if (!rowFields[rowFields.length - 1].startsWith("\"")) {
        // rowFields[rowFields.length - 1] = rowFields[rowFields.length - 1].substring(0, rowFields[rowFields.length - 1].length() - 1);
        // } else {
        // rowFields[rowFields.length - 1] = rowFields[rowFields.length - 1].replace('"', ' ');
        // }

        this.rowFields = rowFields;

    }

    @Override
    public boolean hasNext() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getColumnCount() throws Exception {

        return rowFields.size();
    }

    @Override
    public String getString(int position) throws Exception {
        return rowFields.get(position);
    }

    @Override
    public Integer getInteger(int position) throws Exception {
        return Integer.parseInt(rowFields.get(position));
    }

    @Override
    public Long getLong(int position) throws Exception {
        return Long.parseLong(rowFields.get(position));
    }

    @Override
    public Double getDouble(int position) throws Exception {
        return Double.parseDouble(rowFields.get(position));
    }

}
