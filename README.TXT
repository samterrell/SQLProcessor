Note that it is possible to build and deploy an SQLProcessor jar that
only requires v1.4 of the JDK. However, by default, the following is required:

Build process using build.xml:

- JDK 1.4, http://java.sun.com/
- Ant 1.5, http://ant.apache.org/, not included
- JUnit, http://junit.sourceforge.net/, included
- MockObjects v0.5, http://mockobjects.com, included
- BeanShell 2.0b1, http://beanshell.org, included
- Jakarta Log4j 1.2.8, http://http://jakarta.apache.org/log4j/docs/, included

SQLProcessor.jar requires:
- JDK 1.4
- BeanSehll 2.0b1 (for MultiBeanSQLProcessor)
- optionally, Log4j

See LICENSE.TXT for the licensing requirements for SQLProcessor and libraries.