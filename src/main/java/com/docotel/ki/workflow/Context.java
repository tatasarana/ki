package com.docotel.ki.workflow;

public interface Context {
    void setAttribute(String name, Object value);

    Object getAttribute(String name);
}
