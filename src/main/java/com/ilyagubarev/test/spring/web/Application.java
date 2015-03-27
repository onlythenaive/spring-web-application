package com.ilyagubarev.test.spring.web;

import java.util.LinkedList;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Application implements WebApplicationInitializer {

    @Override
    public void onStartup(final ServletContext servletContext) {
        new ListenerBuilder()
                .add(Configuration.class)
                .build(servletContext);
        new DispatcherBuilder("data-dispatcher")
                .add(Configuration.class)
                .add("/data")
                .build(servletContext);
        new DispatcherBuilder("page-dispatcher")
                .add(Configuration.class)
                .add("/pages")
                .build(servletContext);
    }

    private static class ContextBuilder {

        private final List<Class<?>> configurations = new LinkedList<>();

        ContextBuilder add(Class<?> configuration) {
            configurations.add(configuration);
            return this;
        }

        WebApplicationContext build() {
            AnnotationConfigWebApplicationContext result;
            result = new AnnotationConfigWebApplicationContext();
            configurations.forEach(c -> result.register(c));
            result.refresh();
            return result;
        }
    }

    private static class ListenerBuilder {

        private final ContextBuilder ctx = new ContextBuilder();

        ListenerBuilder add(Class<?> configuration) {
            ctx.add(configuration);
            return this;
        }

        void build(ServletContext servletContext) {
            servletContext.addListener(new ContextLoaderListener(ctx.build()));
        }
    }

    private static class DispatcherBuilder {

        private final String name;
        private final ContextBuilder ctx = new ContextBuilder();
        private final List<String> patterns = new LinkedList<>();

        DispatcherBuilder(String name) {
            this.name = name;
        }

        DispatcherBuilder add(Class<?> configuration) {
            ctx.add(configuration);
            return this;
        }

        DispatcherBuilder add(String pattern) {
            patterns.add(pattern);
            return this;
        }

        Dynamic build(ServletContext servletContext) {
            Servlet dispatcher = new DispatcherServlet(ctx.build());
            Dynamic result = servletContext.addServlet(name, dispatcher);
            patterns.forEach(p -> result.addMapping(p));
            return result;
        }
    }
}
