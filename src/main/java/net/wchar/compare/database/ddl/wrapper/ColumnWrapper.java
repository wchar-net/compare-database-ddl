package net.wchar.compare.database.ddl.wrapper;

import lombok.*;
import lombok.experimental.Accessors;


/**
 * 列名称、类型、是否可为空、默认值
 * dataScale带小数列: NUMBER(10,2)
 *
 * @author Elijah
 */
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
