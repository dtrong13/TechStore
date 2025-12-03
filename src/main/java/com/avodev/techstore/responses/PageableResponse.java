package com.avodev.techstore.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageableResponse<T> {
    List<T> content;
    int pageNumber;
    int pageSize;
    int totalPages;
    long totalElements;
    int numberOfElements;
}
