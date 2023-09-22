package net.wchar.compare.database.ddl.wrapper;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ColumnDescWrapper {
    private String columnName;
    private String columnDesc;
}