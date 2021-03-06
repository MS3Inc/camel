/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.aws.sqs.integration;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.apache.camel.component.aws.sqs.SqsConfiguration;
import org.apache.camel.test.infra.aws.clients.AWSClientUtils;
import org.apache.camel.test.infra.common.SharedNameGenerator;
import org.apache.camel.test.infra.common.SharedNameRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSqsConfiguration extends SqsConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TestSqsConfiguration.class);
    private final AmazonSQS client = AWSClientUtils.newSQSClient();

    public TestSqsConfiguration() {
        SharedNameGenerator sharedNameGenerator = SharedNameRegistry.getInstance().getSharedNameGenerator();

        String name = sharedNameGenerator.getName();
        LOG.debug("Using the following shared resource name for the test: {}", name);
        setQueueName(name);
    }

    @Override
    public String getQueueUrl() {
        try {
            return client.getQueueUrl(getQueueName()).getQueueUrl();
        } catch (QueueDoesNotExistException e) {
            return client.createQueue(getQueueName()).getQueueUrl();
        }
    }

    @Override
    public AmazonSQS getAmazonSQSClient() {
        return client;
    }
}
