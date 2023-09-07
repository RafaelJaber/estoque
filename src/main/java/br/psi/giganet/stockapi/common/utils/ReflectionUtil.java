package br.psi.giganet.stockapi.common.utils;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.functors.InstantiateFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.transaction.Transactional;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "DuplicatedCode"})
@Transactional
public class ReflectionUtil {

    /**
     * @param clazz
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> transform(Class<T> clazz, List<? extends Object> list) {
        return null;
    }

    /**
     * Transforma uma lista de java.lang.Object[] para novas instâncias de T de acordo com suas
     * propriedades informadas. A ordem das informações no array e a tipagem será mantida.
     *
     * @param clazz
     * @param list
     * @param properties
     * @param <T>
     * @return
     */
    public static <T> List<T> transform(Class<T> clazz, List<? extends Object[]> list, List<String> properties) {
        List<T> collectionTransformed = new ArrayList<T>();

        for (Object[] object : list) {
            T t = newInstance(clazz);

            BeanWrapper propertyAccess = PropertyAccessorFactory.forBeanPropertyAccess(t);
            for (int i = 0; i < object.length; i++) {
                String propertyName = properties.get(i);

                if (propertyAccess.isWritableProperty(propertyName)) {
                    Object value = object[i];

                    if (object[i] != null) {
                        if (propertyAccess.getPropertyType(propertyName).equals(object[i].getClass())) {
                            propertyAccess.setPropertyValue(propertyName, value);
                        }
                    }
                }
            }
            collectionTransformed.add(t);
        }
        return collectionTransformed;
    }

    /**
     * Transforma um java.lang.Object para uma nova instância de objeto T
     *
     * @param clazz
     * @param args
     * @param <T>
     * @return
     */
    public static <T, P> T transform(Class<T> clazz, P p) {
        T t = newInstance(clazz);

        BeanWrapper propertyAccessTransformed = PropertyAccessorFactory.forBeanPropertyAccess(t);
        BeanWrapper propertyAccessOrigin = PropertyAccessorFactory.forBeanPropertyAccess(p);

        PropertyDescriptor[] propertyDescriptors = propertyAccessTransformed.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            if (propertyAccessTransformed.isReadableProperty(propertyName) && propertyAccessOrigin.isWritableProperty(propertyName)) {
                Object value = propertyAccessOrigin.getPropertyValue(propertyName);
                if (propertyAccessOrigin.getPropertyType(propertyName) == propertyAccessTransformed.getPropertyType(propertyName)) {
                    propertyAccessTransformed.setPropertyValue(propertyName, value);
                }
            }
        }
        return t;
    }

    /**
     *
     * @param p
     * @param t
     * @param <P>
     * @param <T>
     * @return
     */
    public static <P, T> P merge(P p, T t) {
        BeanWrapper propertyAccessTransformed = PropertyAccessorFactory.forBeanPropertyAccess(p);
        BeanWrapper propertyAccessOrigin = PropertyAccessorFactory.forBeanPropertyAccess(t);

        PropertyDescriptor[] propertyDescriptors = propertyAccessTransformed.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            if (propertyAccessTransformed.isReadableProperty(propertyName) && propertyAccessOrigin.isWritableProperty(propertyName)) {
                Object value = propertyAccessOrigin.getPropertyValue(propertyName);
                if (propertyAccessOrigin.getPropertyType(propertyName) == propertyAccessTransformed.getPropertyType(propertyName)) {
                    propertyAccessTransformed.setPropertyValue(propertyName, value);
                }
            }
        }
        return p;
    }

    /**
     * Invoca o construtor da classe passada como parâmetro com os argumentos
     * passados como parâmetro
     *
     * @param <T>
     * @param clazz classe a ter seu construtor executado
     * @param args  argumentos para o construtor
     * @return instância da classe
     */
    public static <T, P> T invokeConstructor(Class<T> clazz, P... args) throws NoSuchMethodException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException {
        return clazz.cast(ConstructorUtils.invokeConstructor(clazz, args));
    }

    /**
     * Instanciar uma classe.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T newInstance(Class<T> clazz) {
        Factory factory = InstantiateFactory.getInstance(clazz, null, null);

        return (T) factory.create();
    }
}
