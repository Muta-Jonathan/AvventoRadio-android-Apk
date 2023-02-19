package org.avvento.apps.onlineradio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class Info {
    private String version;
    private Urls urls;
    private String info;
    private String[] ticker;
    private String warning;
}
