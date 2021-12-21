package org.avvento.apps.onlineradio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Info {
    private String version;
    private Urls urls;
    private String info;
    private String[] ticker;
    private String warning;
}
