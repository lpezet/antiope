/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.github.lpezet.antiope.metrics.aws;

import java.util.Comparator;

import com.amazonaws.services.cloudwatch.model.Dimension;

/**
 * Used to compare {@link Dimension}.
 *
 */
enum DimensionComparator implements Comparator<Dimension> {
    INSTANCE
    ;
    /** Used to indicate the two objects being compared are both non-nulls. */
    private static final int NON_NULLS = 2;

    @Override
    public int compare(Dimension pDim1, Dimension pDim2) {
        int oResult = nullCompare(pDim1, pDim2);
        if (oResult == NON_NULLS) {
            oResult = nullSafeCompare(pDim1.getName(), pDim2.getName());
            if (oResult == 0) {
                return nullSafeCompare(pDim1.getValue(), pDim2.getValue());
            }
        }
        return oResult;
    }

    /**
     * Compares the two given strings for order, handling null as necessary.
     * 
     * @return a negative integer, zero, or a positive integer as the first
     *         object is less than, equal to, or greater than the second object.
     */
    private int nullSafeCompare(String pFirst, String pSecond) {
        int oResult = nullCompare(pFirst, pSecond);
        return oResult == NON_NULLS ? pFirst.compareTo(pSecond) : oResult;
    }

    /**
     * Partially compares the two given objects for order, handling null as
     * necessary.
     * 
     * @return a -1 if the first object is null but not the second, 0 if both
     *         objects are identical, 1 if the second object is null but not the
     *         first. Otherwise, {@link #NON_NULLS} is returned which means both
     *         objects are non-null but not identical.
     */
    private int nullCompare(Object pFirst, Object pSecond) {
        if (pFirst == pSecond) {
            return 0;
        }
        if (pFirst == null) {
            return -1;
        }
        if (pSecond == null) {
            return 1;
        }
        return NON_NULLS;
    }
}