Spring MVC application using JDBC operation using JDBCTemplate for CRUD operation 

docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres

execute student.sql

Deploy the war file in tomcat or jetty.

The URL is http://localhost:8080/jdbctraining-1.0.0-BUILD-SNAPSHOT/

Click on students. This is async (but works with full ResultSet at once, so no streaming, and is blocking - simply the limitations when wrapping the JDBC).