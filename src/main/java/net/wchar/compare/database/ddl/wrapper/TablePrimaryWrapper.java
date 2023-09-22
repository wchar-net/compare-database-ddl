package net.wchar.compare.database.ddl.wrapper;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 表得主键信息 名称、类型、列名称
 * primaryType 主键类型列: primary、unique
 *
 * @author Elijah
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TablePrimaryWrapper {
    private String primaryName;
    private String primaryType;
    private String columnName;
}
