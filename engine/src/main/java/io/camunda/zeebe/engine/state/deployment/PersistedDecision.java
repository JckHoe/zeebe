/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.state.deployment;

import static io.camunda.zeebe.util.buffer.BufferUtil.bufferAsString;

import io.camunda.zeebe.db.DbValue;
import io.camunda.zeebe.msgpack.UnpackedObject;
import io.camunda.zeebe.msgpack.property.IntegerProperty;
import io.camunda.zeebe.msgpack.property.LongProperty;
import io.camunda.zeebe.msgpack.property.StringProperty;
import io.camunda.zeebe.protocol.impl.record.value.deployment.DecisionRecord;
import io.camunda.zeebe.protocol.record.value.TenantOwned;
import org.agrona.DirectBuffer;

public final class PersistedDecision extends UnpackedObject implements DbValue {

  private final StringProperty decisionIdProp = new StringProperty("decisionId");
  private final StringProperty decisionNameProp = new StringProperty("decisionName");
  private final IntegerProperty versionProp = new IntegerProperty("version");
  private final LongProperty decisionKeyProp = new LongProperty("decisionKey");

  private final StringProperty decisionRequirementsIdProp =
      new StringProperty("decisionRequirementsId");
  private final LongProperty decisionRequirementsKeyProp =
      new LongProperty("decisionRequirementsKey");

  private final StringProperty tenantIdProp =
      new StringProperty("tenantId", TenantOwned.DEFAULT_TENANT_IDENTIFIER);

  public PersistedDecision() {
    declareProperty(decisionIdProp)
        .declareProperty(decisionNameProp)
        .declareProperty(versionProp)
        .declareProperty(decisionKeyProp)
        .declareProperty(decisionRequirementsIdProp)
        .declareProperty(decisionRequirementsKeyProp)
        .declareProperty(tenantIdProp);
  }

  public void wrap(final DecisionRecord record) {
    decisionIdProp.setValue(record.getDecisionId());
    decisionNameProp.setValue(record.getDecisionName());
    versionProp.setValue(record.getVersion());
    decisionKeyProp.setValue(record.getDecisionKey());
    decisionRequirementsIdProp.setValue(record.getDecisionRequirementsIdBuffer());
    decisionRequirementsKeyProp.setValue(record.getDecisionRequirementsKey());
    tenantIdProp.setValue(record.getTenantId());
  }

  public PersistedDecision copy() {
    final var copy = new PersistedDecision();
    copy.decisionIdProp.setValue(getDecisionId());
    copy.decisionNameProp.setValue(getDecisionName());
    copy.decisionKeyProp.setValue(getDecisionKey());
    copy.versionProp.setValue(getVersion());
    copy.decisionRequirementsIdProp.setValue(getDecisionRequirementsId());
    copy.decisionRequirementsKeyProp.setValue(getDecisionRequirementsKey());
    copy.tenantIdProp.setValue(getTenantId());
    return copy;
  }

  public DirectBuffer getDecisionId() {
    return decisionIdProp.getValue();
  }

  public DirectBuffer getDecisionName() {
    return decisionNameProp.getValue();
  }

  public int getVersion() {
    return versionProp.getValue();
  }

  public long getDecisionKey() {
    return decisionKeyProp.getValue();
  }

  public DirectBuffer getDecisionRequirementsId() {
    return decisionRequirementsIdProp.getValue();
  }

  public long getDecisionRequirementsKey() {
    return decisionRequirementsKeyProp.getValue();
  }

  public String getTenantId() {
    return bufferAsString(tenantIdProp.getValue());
  }

  public void setTenantId(final String tenantId) {
    tenantIdProp.setValue(tenantId);
  }
}
