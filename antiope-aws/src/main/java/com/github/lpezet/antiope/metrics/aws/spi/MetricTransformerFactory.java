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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Service specific metric transformer factory.
 */
public enum MetricTransformerFactory {
    DynamoDB,
    ;
    private static final String REQUEST_TRANSFORMER_CLASSNAME_SUFFIX = "RequestMetricTransformer";
    public static volatile String mTransformerPackage;
    
    public static String getTransformerPackage() {
        return mTransformerPackage;
    }

    public static void setTransformerPackage(String pTransformPackage) {
        if (pTransformPackage == null)
            throw new IllegalArgumentException();
        MetricTransformerFactory.mTransformerPackage = pTransformPackage;
    }
    
    /**
     * By default, the transformer class for each AWS specific service is
     * assumed to reside in the Java package
     * {@link #DEFAULT_METRIC_TRANSFORM_PROVIDER_PACKAGE} and follow the naming
     * convention of &lt;AwsPrefix>MetricTransformer. The "AwsPrefix" is the
     * same as the enum literal name. Since each service specific request metric
     * transformer internally contains static reference to service specific
     * classes, this dynamic class loading mechansim allows service specific
     * transformers to be skipped in case some service specific class files are
     * absent in the classpath.
     */
    private volatile RequestMetricTransformer mRequestMetricTransformer;

    /**
     * Returns the fully qualified class name of the request metric
     * transformer, given the specific AWS prefix.
     */
    public static String buildRequestMetricTransformerFQCN(String pPrefix, String pPackageName) {
        return pPackageName + "." + pPrefix + REQUEST_TRANSFORMER_CLASSNAME_SUFFIX;
    }

    /**
     * @param pFqcn fully qualified class name.
     */
    private RequestMetricTransformer loadRequestMetricTransformer(String pFqcn) {
       Log log = LogFactory.getLog(MetricTransformerFactory.class);
       if (log.isDebugEnabled()) {
           log.debug("Loading " + pFqcn);
       }
        try {
            Class<?> c = Class.forName(pFqcn);
            return (RequestMetricTransformer)c.newInstance();
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to load " + pFqcn
                        + "; therefore ignoring " + this.name()
                        + " specific predefined metrics", e);
            }
        }
        return RequestMetricTransformer.NONE;
   }

    public RequestMetricTransformer getRequestMetricTransformer() {
        RequestMetricTransformer oTransformer = mRequestMetricTransformer;
        String oPackageName = mTransformerPackage;
        if (oTransformer != null
        &&  oPackageName.equals(oTransformer.getClass().getPackage().getName())) {
            return oTransformer;
        }
        String oFqcn = MetricTransformerFactory
            .buildRequestMetricTransformerFQCN(name(), oPackageName);
        return this.mRequestMetricTransformer = loadRequestMetricTransformer(oFqcn);
    }
}