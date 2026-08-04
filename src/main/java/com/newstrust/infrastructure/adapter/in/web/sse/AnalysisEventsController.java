package com.newstrust.infrastructure.adapter.in.web.sse;

import com.newstrust.infrastructure.adapter.in.web.openapi.AnalysisEventsApi;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class AnalysisEventsController implements AnalysisEventsApi {

    private final AnalysisEventBroadcaster broadcaster;

    public AnalysisEventsController(AnalysisEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public SseEmitter streamAnalysisEvents() {
        return broadcaster.subscribe();
    }
}
