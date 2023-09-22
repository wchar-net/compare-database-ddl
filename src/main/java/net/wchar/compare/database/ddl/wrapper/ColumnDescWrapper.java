package net.wchar.compare.database.ddl.wrapper;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 列描述
 * @author Elijah
 */
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
