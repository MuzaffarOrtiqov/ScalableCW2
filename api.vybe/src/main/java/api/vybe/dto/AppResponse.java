package api.vybe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponse<T> {
    private T data;
    private String message;

    public AppResponse(String message) {
        this.message = message;
    }

    public AppResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }
}
