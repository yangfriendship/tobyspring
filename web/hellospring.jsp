<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="springbook.web.HelloSpring" %><%--
  Created by IntelliJ IDEA.
  User: yangf
  Date: 2021-01-20
  Time: 오후 5:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<%
    ApplicationContext context = WebApplicationContextUtils
            .getWebApplicationContext(request.getSession().getServletContext());
    HelloSpring hello = context.getBean(HelloSpring.class);
    out.print(hello.sayHello("Root Context"));
%>

</body>
</html>
