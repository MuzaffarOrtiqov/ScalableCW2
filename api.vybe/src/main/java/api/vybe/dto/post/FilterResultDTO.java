package api.vybe.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class FilterResultDTO<T> {
    private List<T> list;
    private Long count;

    public FilterResultDTO(List<T> list, Long count) {
        this.list = list;
        this.count = count;
    }
}
