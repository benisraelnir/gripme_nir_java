package com.github.benisraelnir.gripme.core.renderer;

import java.util.Map;

public interface Renderer {
    String render(String content, Map<String, Object> context) throws Exception;

    String renderRaw(String content) throws Exception;
}
