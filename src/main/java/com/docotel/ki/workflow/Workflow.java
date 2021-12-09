package com.docotel.ki.workflow;

import java.util.Map;

public interface Workflow {
    boolean processWorkflow(Map<String, Object> parameters);
}
