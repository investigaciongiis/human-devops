package com.suken27.humanfactorsjava.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerate that registers the type of answer that the question aims for.
 */
public enum TypeOfAnswer {
    VALUE_RANGE, FREQUENCY_RANGE, BINARY;

    private Map<Double, String> options;

    public Map<Double, String> getOptions() {
        if (options != null) {
            return options;
        }
        options = new HashMap<>();
        switch (this) {
            case VALUE_RANGE:
                options.put(0.0, "Not at all");
                options.put(0.25, "A little"); 
                options.put(0.5, "Somewhat");
                options.put(0.75, "Mostly");
                options.put(1.0, "Completely");
                return options;
            case FREQUENCY_RANGE:
                options.put(0.0, "Never");
                options.put(0.25, "Rarely");
                options.put(0.5, "Sometimes");
                options.put(0.75, "Frequently");
                options.put(1.0, "Always");
                return options;
            case BINARY:
                options.put(0.0, "No");
                options.put(1.0, "Yes");
                return options;
            default:
                return options;
        }
    }

}
