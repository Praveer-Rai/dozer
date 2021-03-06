/*
 * Copyright 2005-2017 Dozer Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dozermapper.spring;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.dozer.BeanFactory;
import org.dozer.CustomConverter;
import org.dozer.DozerBeanMapper;
import org.dozer.DozerEventListener;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:buzdin@gmail.com">Dmitry Buzdin</a>
 */
public class DozerBeanMapperFactoryBeanTest {
    
    private DozerBeanMapperFactoryBean factory;
    private Resource mockResource;
    private ApplicationContext mockContext;

    @Before
    public void setUp() throws Exception {
        factory = new DozerBeanMapperFactoryBean();
        mockResource = mock(Resource.class);
        mockContext = mock(ApplicationContext.class);
        factory.setApplicationContext(mockContext);
    }

    @Test
    public void testOk() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("mappings/mappingSpring.xml");
        when(mockResource.getURL()).thenReturn(url);

        factory.setCustomConverters(Collections.EMPTY_LIST);
        factory.setCustomConvertersWithId(Collections.EMPTY_MAP);
        factory.setEventListeners(Collections.EMPTY_LIST);
        factory.setFactories(Collections.EMPTY_MAP);
        factory.setMappingFiles(new Resource[] {mockResource});
        factory.setMappingBuilders(Collections.EMPTY_LIST);

        factory.afterPropertiesSet();

        assertEquals(Mapper.class, factory.getObjectType());
        assertTrue(factory.isSingleton());

        DozerBeanMapper mapper = (DozerBeanMapper)factory.getObject();
        List<?> files = mapper.getMappingFiles();

        assertEquals(1, files.size());
        assertEquals("file:" + url.getFile(), files.iterator().next());
    }

    @Test
    public void testEmpty() throws Exception {
        factory.afterPropertiesSet();
    }

    @Test
    public void shouldInjectBeans() throws Exception {
        HashMap<String, CustomConverter> converterHashMap = new HashMap<String, CustomConverter>();
        converterHashMap.put("a", mock(CustomConverter.class));

        HashMap<String, BeanFactory> beanFactoryMap = new HashMap<String, BeanFactory>();
        beanFactoryMap.put("a", mock(BeanFactory.class));

        HashMap<String, DozerEventListener> eventListenerMap = new HashMap<String, DozerEventListener>();
        eventListenerMap.put("a", mock(DozerEventListener.class));

        HashMap<String, BeanMappingBuilder> mappingBuilders = new HashMap<String, BeanMappingBuilder>();
        mappingBuilders.put("a", mock(BeanMappingBuilder.class));

        when(mockContext.getBeansOfType(CustomConverter.class)).thenReturn(converterHashMap);
        when(mockContext.getBeansOfType(BeanFactory.class)).thenReturn(beanFactoryMap);
        when(mockContext.getBeansOfType(DozerEventListener.class)).thenReturn(eventListenerMap);
        when(mockContext.getBeansOfType(BeanMappingBuilder.class)).thenReturn(mappingBuilders);

        factory.afterPropertiesSet();

        DozerBeanMapper mapper = (DozerBeanMapper)factory.getObject();
        assertEquals(1, mapper.getCustomConverters().size());
        assertEquals(1, mapper.getCustomConverters().size());
        assertEquals(1, mapper.getCustomConvertersWithId().size());
        assertEquals(1, mapper.getEventListeners().size());
    }
}
