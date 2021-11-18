package uni.project.a.b.domain;

import lombok.Data;

import java.util.List;

@Data
public class AppHeader {

    private final String headerName;

    private final List<byte[]> headerValues;

    private List<Integer> headerValues2;

    public AppHeader(String headerName, List<byte[]> headerValues) {
        this.headerName = headerName;
        this.headerValues = headerValues;
    }

    public AppHeader(String headerName, List<byte[]> headerValues, List<Integer> headerValues2) {
        this.headerName = headerName;
        this.headerValues = headerValues;
        this.headerValues2 = headerValues2;
    }

}
