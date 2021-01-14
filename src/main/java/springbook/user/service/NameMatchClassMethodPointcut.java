package springbook.user.service;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.util.PatternMatchUtils;

public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut {

    public void setMappedClassName(String mappedClassName) {
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }

    static class SimpleClassFilter implements ClassFilter {

        private String mappedClassName;

        private SimpleClassFilter(String mappedClassName) {
            this.mappedClassName = mappedClassName;
        }

        @Override
        public boolean matches(Class<?> aClass) {
            return PatternMatchUtils.simpleMatch(mappedClassName, aClass.getSimpleName());
        }
    }
}
