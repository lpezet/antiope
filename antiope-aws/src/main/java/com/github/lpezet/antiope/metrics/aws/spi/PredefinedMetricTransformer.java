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

import static com.github.lpezet.antiope.metrics.aws.spi.MetricData.newMetricDatum;
import static com.github.lpezet.antiope.metrics.aws.spi.RequestMetricTransformer.Utils.endTimestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.AWSRequestMetrics.Field;
import com.github.lpezet.antiope.be.APIWebServiceRequest;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.MetricType;
import com.github.lpezet.antiope.metrics.TimingInfo;
/**
 * Used to transform the predefined metrics of the AWS SDK into instances of
 * {@link MetricDatum}.
 * 
 * See <a href=
 * "http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/publishingMetrics.html"
 * >http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/
 * publishingMetrics.html</a>
 * 
 * @see AWSRequestMetrics
 * @see RequestMetricCollector
 */
@ThreadSafe
public class PredefinedMetricTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PredefinedMetricTransformer.class);
    static final boolean INCLUDE_REQUEST_TYPE = true;
    static final boolean EXCLUDE_REQUEST_TYPE = !INCLUDE_REQUEST_TYPE;

    /**
     * Returns a non-null list of metric datum for the metrics collected for the
     * given request/response.
     * 
     * @param pMetricType the request metric type
     */
    public List<MetricDatum> toMetricData(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
        if (pMetricType instanceof APIRequestMetrics) {
            // Predefined metrics across all aws http clients
        	APIRequestMetrics oPredefined = (APIRequestMetrics) pMetricType;
            switch(oPredefined) {
                case HttpClientRetryCount:
                case HttpClientPoolAvailableCount:
                case HttpClientPoolLeasedCount:
                case HttpClientPoolPendingCount:
                    return metricOfCount(oPredefined, pRequest, pResponse);
                case RequestCount:  // intentionally fall thru to reuse the same routine as RetryCount
                case RetryCount:
                    return metricOfRequestOrRetryCount(oPredefined, pRequest, pResponse);
                case ResponseProcessingTime: // drop thru
                case RequestSigningTime: // drop thru
                    return latencyMetricOf(oPredefined, pRequest, pResponse, EXCLUDE_REQUEST_TYPE);
                case ClientExecuteTime:
                    return latencyOfClientExecuteTime(pRequest, pResponse);
                case HttpClientSendRequestTime:
                case HttpClientReceiveResponseTime:
                case HttpRequestTime:
                    return latencyMetricOf(oPredefined, pRequest, pResponse, INCLUDE_REQUEST_TYPE);
                case Exception:
                case ThrottleException:
                    return counterMetricOf(oPredefined, pRequest, pResponse, INCLUDE_REQUEST_TYPE);
                default:
                    break;
            }
        }
        // Predefined metrics for specific service clients
        /*
        for (MetricTransformerFactory aws: MetricTransformerFactory.values()) {
            if (metricType.name().startsWith(aws.name())) {
                List<MetricDatum> metricData = aws.getRequestMetricTransformer()
                        .toMetricData(metricType, request, response);
                if (metricData != null)
                    return metricData;
                break;
            }
        }
        */
        if (LOGGER.isDebugEnabled()) {
            APIWebServiceRequest oOrigReq = pRequest == null ? null : pRequest
                    .getOriginalRequest();
            String reqClassName = oOrigReq == null ? null : oOrigReq.getClass().getName();
            LOGGER.debug("No request metric transformer can be found for metric type "
                    + pMetricType.name() + " for " + reqClassName);
        }
        return Collections.emptyList();
    }

    /**
     * Returns a list with a single metric datum for the specified retry or
     * request count predefined metric; or an empty list if there is none.
     * 
     * @param pMetricType
     *            must be either {@link Field#RequestCount} or
     *            {@link Field#RetryCount}; or else GIGO.
     */
    protected List<MetricDatum> metricOfRequestOrRetryCount(APIRequestMetrics pMetricType, Request<?> pReq, Object pResp) {
        IMetrics m = pReq.getMetrics(); 
        TimingInfo ti = m.getTimingInfo();
        // Always retrieve the request count even for retry which is equivalent
        // to the number of requests minus one.
        Number oCounter = ti.getCounter(Field.RequestCount.name());
        if (oCounter == null) {
            // this is possible if one of the request handlers screwed up
            return Collections.emptyList();
        }
        int oRequestCount = oCounter.intValue();
        if (oRequestCount < 1) {
            LogFactory.getLog(getClass()).warn(
                "request count must be at least one");
            return Collections.emptyList();
        }
        final double oCount = pMetricType == APIRequestMetrics.RequestCount
                           ? oRequestCount
                           : oRequestCount-1 // retryCount = requestCount - 1
                           ;
        if (oCount < 1) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new MetricDatum()
                .withMetricName(pReq.getServiceName())
                .withDimensions(new Dimension()
                    .withName(Dimensions.MetricType.name())
                    .withValue(pMetricType.name()))
                .withUnit(StandardUnit.Count)
                .withValue(Double.valueOf(oCount))
                .withTimestamp(endTimestamp(ti)))
                ;
        }
    }

    protected List<MetricDatum> metricOfCount(APIRequestMetrics pMetricType, Request<?> pReq, Object pResp) {
        IMetrics m = pReq.getMetrics();
        TimingInfo ti = m.getTimingInfo();
        Number oCounter = ti.getCounter(pMetricType.name());
        if (oCounter == null) {
            return Collections.emptyList();
        }
        final double oCount = oCounter.doubleValue();
        if (oCount < 1) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new MetricDatum()
                .withMetricName(pReq.getServiceName())
                .withDimensions(new Dimension()
                    .withName(Dimensions.MetricType.name())
                    .withValue(pMetricType.name()))
                .withUnit(StandardUnit.Count)
                .withValue(Double.valueOf(oCount))
                .withTimestamp(endTimestamp(ti)))
                ;
        }
    }
    
    /**
     * Returns all the latency metric data recorded for the specified metric
     * event type; or an empty list if there is none. The number of metric datum
     * in the returned list should be exactly one when there is no retries, or
     * more than one when there are retries.
     * 
     * @param pIncludesRequestType
     *            true iff the "request" dimension is to be included;
     */
    protected List<MetricDatum> latencyMetricOf(MetricType pMetricType, Request<?> pReq, Object pResponse, boolean pIncludesRequestType) {
        IMetrics m = pReq.getMetrics();
        TimingInfo oRoot = m.getTimingInfo();
        final String oMetricName = pMetricType.name();
        List<TimingInfo> oSubMeasures =
            oRoot.getAllSubMeasurements(oMetricName);
        if (oSubMeasures != null) {
            List<MetricDatum> oResult =
                new ArrayList<MetricDatum>(oSubMeasures.size());
            for (TimingInfo oSub : oSubMeasures) {
                if (oSub.isEndTimeKnown()) { // being defensive
                    List<Dimension> oDims = new ArrayList<Dimension>();
                    oDims.add(new Dimension()
                            .withName(Dimensions.MetricType.name())
                            .withValue(oMetricName));
                    // Either a non request type specific datum is created per
                    // sub-measurement, or a request type specific one is 
                    // created but not both
                    if (pIncludesRequestType) {
                        oDims.add(new Dimension()
                                .withName(Dimensions.RequestType.name())
                                .withValue(requestType(pReq)));
                    }
                    MetricDatum oDatum = new MetricDatum()
                        .withMetricName(pReq.getServiceName())
                        .withDimensions(oDims)
                        .withUnit(StandardUnit.Milliseconds)
                        .withValue(oSub.getTimeTakenMillisIfKnown());
                    oResult.add(oDatum);
                }
            }
            return oResult;
        }
        return Collections.emptyList();
    }

    /**
     * Returns a request type specific metrics for
     * {@link Field#ClientExecuteTime} which is special in the sense that it
     * makes a more accurate measurement by taking the {@link TimingInfo} at the
     * root into account.
     */
    protected List<MetricDatum> latencyOfClientExecuteTime(Request<?> pReq, Object pResponse) {
        IMetrics m = pReq.getMetrics();
        TimingInfo oRoot = m.getTimingInfo();
        final String oMetricName = Field.ClientExecuteTime.name();
        if (oRoot.isEndTimeKnown()) { // being defensive
            List<Dimension> oDims = new ArrayList<Dimension>();
            oDims.add(new Dimension()
                    .withName(Dimensions.MetricType.name())
                    .withValue(oMetricName));
            // request type specific
            oDims.add(new Dimension()
                    .withName(Dimensions.RequestType.name())
                    .withValue(requestType(pReq)));
            MetricDatum oDatum = new MetricDatum()
                .withMetricName(pReq.getServiceName())
                .withDimensions(oDims)
                .withUnit(StandardUnit.Milliseconds)
                .withValue(oRoot.getTimeTakenMillisIfKnown());
            return Collections.singletonList(oDatum);
        }
        return Collections.emptyList();
    }
    
    /**
     * Returns the name of the type of request.
     */
    private String requestType(Request<?> pReq) {
        return pReq.getOriginalRequest().getClass().getSimpleName();
    }

    /**
     * Returns a list of metric datum recorded for the specified counter metric
     * type; or an empty list if there is none.
     * 
     * @param pIncludesRequestType
     *            true iff an additional metric datum is to be created that
     *            includes the "request" dimension
     */
    protected List<MetricDatum> counterMetricOf(MetricType pType, Request<?> pReq, Object pResp, boolean pIncludesRequestType) {
        IMetrics m = pReq.getMetrics(); 
        TimingInfo ti = m.getTimingInfo();
        final String oMetricName = pType.name();
        Number oCounter = ti.getCounter(oMetricName);
        if (oCounter == null) {
            return Collections.emptyList();
        }
        int oCount = oCounter.intValue();
        if (oCount < 1) {
            LogFactory.getLog(getClass()).warn("Count must be at least one");
            return Collections.emptyList();
        }
        final List<MetricDatum> oResult = new ArrayList<MetricDatum>();
        final Dimension oMetricDimension = new Dimension()
            .withName(Dimensions.MetricType.name())
            .withValue(oMetricName);
        // non-request type specific metric datum
        final MetricDatum oFirst = new MetricDatum()
            .withMetricName(pReq.getServiceName())
            .withDimensions(oMetricDimension)
            .withUnit(StandardUnit.Count)
            .withValue(Double.valueOf(oCount))
            .withTimestamp(endTimestamp(ti));
        oResult.add(oFirst);
        if (pIncludesRequestType) {
            // additional request type specific metric datum
            Dimension oRequestDimension = new Dimension()
                .withName(Dimensions.RequestType.name())
                .withValue(requestType(pReq));
            final MetricDatum oSecond = 
                newMetricDatum(oFirst, oMetricDimension, oRequestDimension);
            oResult.add(oSecond);
        }
        return oResult;
    }
}
