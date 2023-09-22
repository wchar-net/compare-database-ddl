## 比较两个库表信息
```shell
目前只支持 Oracle
只能同库比较
```
### No.1 修改 application.yml
```shell
spring.datasource.hikari.master 以主数据源为主
spring.datasource.hikari.slave  副数据源
```

### No.2 启动
```shell
CompareDatabaseDDLApplicationTests ->  class
contextLoads                       ->  method
```

### No.3 查看结果
```shell
运行完成后刷新 IDEA 目录结构 Reload from Disk
默认会生成在 ./compare 目录下
./compare/table  副数据源缺少得表
./compare/column 副数据源表缺少得 列、主键、唯一约束
```