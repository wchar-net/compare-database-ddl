package net.wchar.compare.database.ddl.wrapper;

import lombok.*;
import lombok.experimental.Accessors;

import java.sql.Clob;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ColumnWrapper {
    private String columnName;
    private String dataType;
    private String dataLength;
    private String nullable;
    private String dataScale;
    private String dataDefault;
}
