package com.avodev.techstore.dtos;

import com.avodev.techstore.enums.SortDirection;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SortField {
    String field;
    SortDirection direction;
}
