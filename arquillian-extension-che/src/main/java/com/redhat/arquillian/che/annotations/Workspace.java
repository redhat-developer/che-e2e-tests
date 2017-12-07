package com.redhat.arquillian.che.annotations;

import com.redhat.arquillian.che.resource.Stack;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Workspace {

    Stack stackID() default Stack.VERTX;
    boolean removeAfterTest() default false;
}
