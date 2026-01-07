package api.vybe.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VideoStatus {
    PUBLISHED,
     DRAFT;

    @JsonCreator
    public static VideoStatus fromValue(String value) {
        return VideoStatus.valueOf(value.toUpperCase());
    }
}
