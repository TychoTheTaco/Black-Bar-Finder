package com.tycho.bbf;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public abstract class ContentFinder {

    private final Map<String, Property> properties = new HashMap<>();

    public abstract Rectangle findContent(final Image frame);

    public abstract class Property<T>{

        private final String tag;

        private T value;

        public Property(String tag, T value) {
            this.tag = tag;
            this.value = value;
        }

        public String getTag() {
            return tag;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    public class RangedProperty extends Property<Number>{

        private final Number min;
        private final Number max;

        public RangedProperty(String tag, Number value, Number min, Number max) {
            super(tag, value);
            this.min = min;
            this.max = max;
        }

        public Number getMin() {
            return min;
        }

        public Number getMax() {
            return max;
        }
    }

    public Map<String, Property> getProperties() {
        return properties;
    }
}
