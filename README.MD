# Lyra

A xml database.

## 使用说明

### lyrad

* `$ java -jar lyrad.jar`

### client

* `$ lyra [-h <address>] [-u <user> [-p]] `
* `address`: 服务器ip地址， 默认为127.0.0.1
* `user`: 用户名
* `p`: 是否输入密码

> 默认用户root的初始密码为`123456`

### JDBC 样例

```java
import java.sql.*;
import com.sea.lyra.jdbc.LyraDataSource;

public class test {
    public static void main(String[] args) {
        // 传统方式
        try {
            Class.forName("com.sea.lyra.Driver");
            Connection connection = DriverManager
                    .getConnection("jdbc:lyra://localhost:5494/lyra",
                            "root",
                            "123456");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from `user`");
            while (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount(); // 获取列数
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i); // 根据索引获取列名
                    String data = resultSet.getString(columnName);
                    System.out.println(data);
                }
                String username = resultSet.getString("username"); // 根据列名获取数据
                String user = resultSet.getString(1); // 根据索引获取数据
                System.out.println(username); 
                System.out.println(user);
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //数据源方式
        try{ 
            DataSource dataSource = new LyraDataSource("jdbc:lyra://localhost:5494/person");
            Connection connection = dataSource.getConnection("root", "123456");
            PreparedStatement statement = connection.prepareStatement("insert into info values (?,?)");
            statement.setString(1, "Alice");
            statement.setInt(2, 16);
            System.out.println(statement.executeUpdate());
            statement.setString(1, "Bob");
            statement.setInt(2, 18);
            System.out.println(statement.executeUpdate());
            connection.close();
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}

```

### SQL (不区分大小写)

#### DAL:

* `USE <DB_NAME>`
* `SHOW DATABASES`
* `SHOW TABLES`

#### DCL:

* `CREATE USER <USERNAME> IDENTIFED BY "PASSWORD"`
* `ALTER USER <USERNAME> IDENTIFED BY "PASSWORD"`

> 删除用户请使用DELETE操作`lyra`数据库的`user`表

#### DDL:

* `CREATE DATABASE <DB_NAME>`
* `CREATE TABLE <TABLE_NAME> (<COLUMN_NAME> <TYPE>[,<COLUMN_NAME> <TYPE>]...)`
* `DROP DATABASE <DB_NAME>`
* `DROP TABLE <TABLE_NAME>`

> TYPE 目前支持INT与VARCHAR(len)

#### DML:

* `INSERT INTO <TABLE_NAME>[(<COLUMN_NAME>[,<COLUMN_NAME>]...)] VALUES("VALUE"[,"VALUE"]...)`
* `DELETE FROM <TABLE_NAME> [WHERE <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE" [{AND|OR} <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE"]...]`
* `UPDATE <TABLE_NAME> SET <COLUMN_NAME>="VALUE"[,<COLUMN_NAME>="VALUE"]... [WHERE <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE" [{AND|OR} <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE"]...]`

#### DQL:

* `SELECT {*|<COLUMN_NAME>[,<COLUMN_NAME>]...} FROM <TABLE_NAME> [WHERE <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE" [{AND|OR} <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE"]...] [ORDER BY <COLUMN>[,<COLUMN>]... [{ASC|DESC}]`
