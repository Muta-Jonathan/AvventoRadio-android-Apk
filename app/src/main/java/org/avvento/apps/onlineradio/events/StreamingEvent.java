package org.avvento.apps.onlineradio.events;

import org.avvento.apps.onlineradio.AvventoRadio;

public class StreamingEvent {
    private AvventoRadio radio;

    public StreamingEvent(AvventoRadio radio) {
        this.radio = radio;
    }
    public AvventoRadio getAvventoMedia() {
        return radio;
    }
}
