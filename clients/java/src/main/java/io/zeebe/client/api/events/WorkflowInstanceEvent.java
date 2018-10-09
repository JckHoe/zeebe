/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.client.api.events;

public interface WorkflowInstanceEvent {

  /** Key of the workflow which this instance was created for */
  long getWorkflowKey();

  /** BPMN process id of the workflow which this instance was created for */
  String getBpmnProcessId();

  /** Version of the workflow which this instance was created for */
  int getVersion();

  /** Partition on which the workflow instance was created; */
  int getPartitionId();

  /** Unique key of the created workflow instance on the partition */
  long getWorkflowInstanceKey();
}
