package cc.invictusgames.invictus.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 11.10.2020 / 19:59
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

public class BasicReflection {

    /**
     * Used to invoke a field
     *
     * @param field The field to invoke
     * @param object The object where the field is applicable
     * @return invoked field
     * @throws IllegalArgumentException in case we cannot access the field (Should not happen)
     */
    public static Object invokeField(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used to fetch a field
     *
     * @param clazz Class where field is applicable
     * @param fieldName Name of the field we're trying to fetch
     * @return Optional field
     * @throws IllegalArgumentException in case the field is not found
     */
    public static Field fetchField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);

            if (!field.isAccessible())
                field.setAccessible(true);

            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void updateField(Field field, Object object, Object newValue) {
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used to fetch a constructor
     *
     * @param clazz Class where the constructor is applicable
     * @param parameters Constructor in the constructor we're trying to fetch
     * @return the fetched constructor
     * @throws IllegalArgumentException in case the constructor is not found
     */
    public static Constructor<?> fetchConstructor(Class<?> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used to invoke a constructor
     *
     * @param constructor The constructor to invoke
     * @param parameters The parameters we need to use to invoke the constructor
     * @return invoked constructor
     * @throws IllegalArgumentException in case the constructor is not found
     */
    public static Object invokeConstructor(Constructor<?> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used to fetch a method
     *
     * @param clazz Class where the method is applicable
     * @param methodName The name of the method we're fetching
     * @param parameters The parameters of the method
     * @return the fetched method
     */
    public static Method fetchMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameters);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Used to invoke a method
     *
     * @param method The method to invoke
     * @param object The object which contains the method we need to invoke
     * @param parameters The parameters needed to invoke the method
     * @return The method's returning object
     */
    public static Object invokeMethod(Method method, Object object, Object... parameters) {
        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used to find a class by its name
     *
     * @param name Class' name
     * @return the found class
     * @throws IllegalArgumentException in case the class is not found
     */
    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Finds a class and calls it back using a {@link BiConsumer}
     *
     * @param name Class' name
     * @param callback callback after class is found
     */
    public static void getClassCallback(String name, BiConsumer<Class<?>, Throwable> callback) {
        CompletableFuture<Class<?>> completableFuture = new CompletableFuture<>();

        completableFuture.complete(BasicReflection.getClass(name));
        completableFuture.whenCompleteAsync(callback);
    }
}
