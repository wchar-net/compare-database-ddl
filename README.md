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
