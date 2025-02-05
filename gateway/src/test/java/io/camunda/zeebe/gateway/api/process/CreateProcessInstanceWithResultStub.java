/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.gateway.api.process;

import io.camunda.zeebe.gateway.api.util.StubbedBrokerClient;
import io.camunda.zeebe.gateway.api.util.StubbedBrokerClient.RequestStub;
import io.camunda.zeebe.gateway.impl.broker.request.BrokerCreateProcessInstanceWithResultRequest;
import io.camunda.zeebe.gateway.impl.broker.response.BrokerResponse;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.ProcessInstanceCreationRecord;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.ProcessInstanceResultRecord;

public final class CreateProcessInstanceWithResultStub
    implements RequestStub<
        BrokerCreateProcessInstanceWithResultRequest, BrokerResponse<ProcessInstanceResultRecord>> {

  public static final long PROCESS_INSTANCE_KEY = 123;
  public static final String PROCESS_ID = "process";
  public static final int PROCESS_VERSION = 1;
  public static final long PROCESS_KEY = 456;

  @Override
  public void registerWith(final StubbedBrokerClient brokerClient) {
    brokerClient.registerHandler(BrokerCreateProcessInstanceWithResultRequest.class, this);
  }

  public long getProcessInstanceKey() {
    return PROCESS_INSTANCE_KEY;
  }

  public String getProcessId() {
    return PROCESS_ID;
  }

  public int getProcessVersion() {
    return PROCESS_VERSION;
  }

  public long getProcessDefinitionKey() {
    return PROCESS_KEY;
  }

  @Override
  public BrokerResponse<ProcessInstanceResultRecord> handle(
      final BrokerCreateProcessInstanceWithResultRequest request) throws Exception {
    final ProcessInstanceCreationRecord piCreationRecord = request.getRequestWriter();
    final ProcessInstanceResultRecord response = new ProcessInstanceResultRecord();
    response
        .setBpmnProcessId(PROCESS_ID)
        .setVariables(piCreationRecord.getVariablesBuffer())
        .setVersion(PROCESS_VERSION)
        .setTenantId(piCreationRecord.getTenantId())
        .setProcessDefinitionKey(PROCESS_KEY)
        .setProcessInstanceKey(PROCESS_INSTANCE_KEY);

    return new BrokerResponse<>(response, 0, PROCESS_INSTANCE_KEY);
  }
}
