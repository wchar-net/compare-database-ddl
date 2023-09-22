package net.wchar.compare.database.ddl.util;

import org.springframework.util.CollectionUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 文件工具类
 * @author Elijah
 */
public class FileUtil {
    public static void appendListToFile(List<String> list, String filePath) throws IOException {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
        for (String line : list) {
            writer.write(line);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
}
