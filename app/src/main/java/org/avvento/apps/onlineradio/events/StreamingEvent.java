package org.avvento.apps.onlineradio.events;

import org.avvento.apps.onlineradio.AvventoMedia;

public class StreamingEvent {
    private AvventoMedia radio;

    public StreamingEvent(AvventoMedia radio) {
        this.radio = radio;
    }
    public AvventoMedia getAvventoMedia() {
        return radio;
    }
}
