package org.pm4j.core.pm.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;

public class AnnotationUtil {

  private static final Object[] EMPTY_OBJ_ARRAY = new Object[0];

  /**
   * Searches for an annotation within the inheritance tree of a class.
   * <p>
   * TODO: Annotations declared in interfaces are not yet considered.
   *
   * @param <A>
   *          The annotation type.
   * @param classToAnalyze
   *          In this class or one of its super classes the annotation will be
   *          searched.
   * @param annotationClass
   *          The runtime class of the annotation.
   * @return The found annotation or <code>null</code>.
   */
  public static <A extends Annotation> A findAnnotationInClassTree(Class<?> classToAnalyze, Class<A> annotationClass) {
    A a = null;
    Class<?> c = classToAnalyze;
    while (a == null) {
      a = c.getAnnotation(annotationClass);
      if (a == null) {
        c = c.getSuperclass();
        if (c == null || c.equals(Object.class)) {
          break;
        }
      }
    }

    return a;
  }

  /**
   * Just finds an annotation for this class.
   * <p>
   * Subclasses provide different implementations e.g. to find annotations that
   * are attached to a matching field declaration of the parent model.
   *
   * @param <T>
   *          The annotation type.
   * @param annotationClass
   *          The annotation class to find.
   * @return The annotation instance of <code>null</code> when not found.
   */
  public static <T extends Annotation> T findAnnotation(PmObjectBase pm, Class<T> annotationClass) {
    return pm.getPmMetaDataWithoutPmInitCall().isPmField
              ? findAnnotation(pm, annotationClass, pm.getPmParent().getClass())
              : findAnnotationInClassTree(pm.getClass(), annotationClass);
  }

  /**
   * Provides an annotation that is defined for this presentation model class or
   * the field of the parent presentation model class.
   *
   * @param <T>
   *          The annotation type.
   * @param annotationClass
   *          The annotation class to find.
   * @param parentClass
   *          The class the contains this presentation model. E.g. the element
   *          PM class for an attribute PM class.
   * @return The annotation instance of <code>null</code> when not found.
   */
  public static <T extends Annotation> T findAnnotation(PmObjectBase pm, Class<T> annotationClass, Class<?> parentClass) {
    assert parentClass != null;

    T foundAnnotation = null;

    // find it in the field declaration
    try {
      Field field = parentClass.getField(pm.getPmName());
      foundAnnotation = field.getAnnotation(annotationClass);
    } catch (NoSuchFieldException e) {
      // may be OK, because the attribute may use something like getters or
      // xPath.
    }

    // next try if necessary: find it in the attribute presentation model class
    if (foundAnnotation == null) {
      foundAnnotation = findAnnotationInClassTree(pm.getClass(), annotationClass);
    }

    return foundAnnotation;
  }



  /**
   * Searches for the first {@link CacheMode} property with the given
   * name within from the given annotation set.<br>
   * The first annotation property that has not the value
   * {@link CacheMode#NOT_SPECIFIED} will be returned. When there is no match,
   * the provided default will be returned.
   *
   * @param propertyName name of the {@link CacheMode} property to handle. E.g. 'all' or 'title'.
   * @param annotations  the set of found found {@link CacheMode} annotations to analyze.
   * @param defaultValue the default value to be used in case of not finding a non-default
   *                     definition.
   * @return The first found annotation value for the given property or the specified default.
   */
  public static CacheMode getCacheModeFromCacheAnnotations(
          String propertyName,
          Collection<PmCacheCfg> annotations,
          CacheMode defaultValue) {
    CacheMode value = defaultValue;

    try {
      Method m = null;
      for (PmCacheCfg cfg : annotations) {
        // use reflection method finder only once:
        if (m == null) {
          m = cfg.getClass().getMethod(propertyName);
        }
        CacheMode v = (CacheMode)m.invoke(cfg, EMPTY_OBJ_ARRAY);
        if (v == CacheMode.NOT_SPECIFIED) {
          v = cfg.all();
        }

        if (v != CacheMode.NOT_SPECIFIED) {
          value = v;
          break;
        }
      }
    } catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }

    return value;
  }
}
