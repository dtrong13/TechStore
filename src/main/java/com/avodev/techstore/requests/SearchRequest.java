package com.avodev.techstore.requests;

import com.avodev.techstore.dtos.Pagination;
import com.avodev.techstore.dtos.SortField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequest<T> {
    T filter;
    Pagination pagination;
    List<SortField> sorts;
}
