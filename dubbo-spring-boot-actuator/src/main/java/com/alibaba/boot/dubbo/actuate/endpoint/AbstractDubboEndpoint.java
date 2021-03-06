/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.boot.dubbo.actuate.endpoint;

import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

/**
 * Abstract Dubbo {@link Endpoint @Endpoint}
 *
 *
 * @since 0.2.0
 */
public abstract class AbstractDubboEndpoint implements ApplicationContextAware, EnvironmentAware {

    protected ApplicationContext applicationContext;

    protected ConfigurableEnvironment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.environment = (ConfigurableEnvironment) environment;
        }
    }

    /**
     * 解析 Bean 的元数据
     *
     * @param bean Bean 对象
     * @return 元数据
     */
    protected Map<String, Object> resolveBeanMetadata(final Object bean) {
        // 创建 Map
        final Map<String, Object> beanMetadata = new LinkedHashMap<>();
        try {
            // 获得 BeanInfo 对象
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            // 获得 PropertyDescriptor 数组
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            // 遍历 PropertyDescriptor 数组
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                // 获得 Method 对象
                Method readMethod = propertyDescriptor.getReadMethod();
                // 读取属性，添加到 beanMetadata 中
                if (readMethod != null && isSimpleType(propertyDescriptor.getPropertyType())) {
                    String name = Introspector.decapitalize(propertyDescriptor.getName());
                    Object value = readMethod.invoke(bean);
                    beanMetadata.put(name, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return beanMetadata;
    }

    protected Map<String, ServiceBean> getServiceBeansMap() {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ServiceBean.class);
    }

    protected ReferenceAnnotationBeanPostProcessor getReferenceAnnotationBeanPostProcessor() {
        return applicationContext.getBean(ReferenceAnnotationBeanPostProcessor.BEAN_NAME, ReferenceAnnotationBeanPostProcessor.class);
    }

    protected Map<String, ProtocolConfig> getProtocolConfigsBeanMap() {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ProtocolConfig.class);
    }

    private static boolean isSimpleType(Class<?> type) {
        return isPrimitiveOrWrapper(type) // 基本类型 or 包装类型
                || type == String.class
                || type == BigDecimal.class
                || type == BigInteger.class
                || type == Date.class
                || type == URL.class
                || type == Class.class
                ;
    }

}