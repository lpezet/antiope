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
package com.github.lpezet.antiope.metrics.aws.spi;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;

/**
 * Metric Datum related utilities.
 */
public enum MetricData {
    ;
    /**
     * Returns a new metric datum cloned from the given metric datum, but
     * replacing the dimensions with the newly specified ones.
     */
    public static MetricDatum newMetricDatum(MetricDatum pFrom, Dimension ... pDimensions) {
        return new MetricDatum()
            .withMetricName(pFrom.getMetricName())
            .withDimensions(pDimensions)
            .withUnit(pFrom.getUnit())
            .withValue(pFrom.getValue())
            .withStatisticValues(pFrom.getStatisticValues())
            .withTimestamp(pFrom.getTimestamp())
            ;
    }
}