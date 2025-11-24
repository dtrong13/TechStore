package com.avodev.techstore.dtos;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Pagination {
    @Builder.Default
    private int pageNumer = 0;
    @Builder.Default
    private int pageSize = 20;
}
