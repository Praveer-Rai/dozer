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
package org.dozer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.beanutils.PropertyUtils;
import org.dozer.builder.DestBeanBuilderCreator;
import org.dozer.classmap.ClassMapBuilder;
import org.dozer.classmap.generator.BeanMappingGenerator;
import org.dozer.config.BeanContainer;
import org.dozer.config.Settings;
import org.dozer.factory.DestBeanCreator;
import org.dozer.loader.CustomMappingsLoader;
import org.dozer.loader.MappingsParser;
import org.dozer.loader.xml.XMLParser;
import org.dozer.loader.xml.XMLParserFactory;
import org.dozer.propertydescriptor.PropertyDescriptorFactory;
import org.dozer.vo.TestObject;
import org.dozer.vo.generics.deepindex.TestObjectPrime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Dmitry Buzdin
 * @author Arm Suwarnaratana
 */
public class DozerBeanMapperTest extends Assert {

  private DozerBeanMapper mapper;
  private static final int THREAD_COUNT = 10;
  private List<Throwable> exceptions;

  @Before
  public void setUp() {
    // todo the test should be redesigned once DozerBeanMapper is immutable #434
    mapper = (DozerBeanMapper) DozerBeanMapperBuilder.buildDefault();
    exceptions = new ArrayList<Throwable>();
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        exceptions.add(e);
      }
    });
  }

  @After
  public void tearDown() {
    for (Throwable t : exceptions) {
      t.printStackTrace();
    }
  }

  @Test
  public void shouldInitializeOnce() throws Exception {
    final CallTrackingMapper mapper = new CallTrackingMapper();
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

    HashSet<Callable<Object>> callables = new HashSet<Callable<Object>>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      callables.add(new Callable<Object>() {
        public Object call() throws Exception {
          latch.countDown();
          latch.await();
          Mapper processor = mapper.getMappingProcessor();
          assertNotNull(processor);
          return null;
        }
      });
    }
    executorService.invokeAll(callables);
    assertEquals(1, mapper.getCalls());
    assertTrue(exceptions.isEmpty());
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    Mapper mapper = DozerBeanMapperBuilder.create()
            .withMappingFiles("testDozerBeanMapping.xml")
            .build();

    final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
      new Thread(new Runnable() {
        public void run() {
          try {
            mapper.map(new TestObject(), TestObjectPrime.class);
          } finally {
            latch.countDown();
          }
        }
      }).start();

    }
    latch.await();
    assertTrue(exceptions.isEmpty());
  }

  class CallTrackingMapper extends DozerBeanMapper {
    AtomicInteger calls = new AtomicInteger(0);

    CallTrackingMapper() {
      // todo this is awful, but will be removed when DozerBeanMapper is immutable (#400)
      super(Collections.emptyList(),
              new Settings(),
              new CustomMappingsLoader(
                      new MappingsParser(new BeanContainer(), new DestBeanCreator(new BeanContainer()), new PropertyDescriptorFactory()),
                      new ClassMapBuilder(new BeanContainer(), new DestBeanCreator(new BeanContainer()),
                              new BeanMappingGenerator(new BeanContainer(), new DestBeanCreator(new BeanContainer()), new PropertyDescriptorFactory()), new PropertyDescriptorFactory()),
                      new BeanContainer()),
              new XMLParserFactory(new BeanContainer()),
              new DozerInitializer(), new BeanContainer(),
              new XMLParser(new BeanContainer(), new DestBeanCreator(new BeanContainer()), new PropertyDescriptorFactory()), new DestBeanCreator(new BeanContainer()),
              new DestBeanBuilderCreator(),
              new BeanMappingGenerator(new BeanContainer(), new DestBeanCreator(new BeanContainer()), new PropertyDescriptorFactory()), new PropertyDescriptorFactory(),
              new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, new HashMap<>());
    }

    @Override
    void loadCustomMappings() {
      calls.incrementAndGet();
    }

    public int getCalls() {
      return calls.get();
    }
  }


  @Test
  public void shouldReturnImmutableResources() throws Exception {
    mapper.map("Hello", String.class);

    assertImmutable("mappingFiles", mapper);
    assertImmutable("customConverters", mapper);
    assertImmutable("customConvertersWithId", mapper);
    assertImmutable("eventListeners", mapper);
  }

  private void assertImmutable(String name, DozerBeanMapper mapper) throws Exception {
    Object property = PropertyUtils.getProperty(mapper, name);
    assertNotNull(property);
    try {
      if (property instanceof List) {
        ((List) property).add("");
      } else if (property instanceof Map) {
        ((Map) property).put("", "");
      }
      fail();
    } catch (UnsupportedOperationException e) {
    }
  }

  @Test
  public void shouldSetEventListeners() {
    DozerEventListener listener = mock(DozerEventListener.class);

    Mapper beanMapper = DozerBeanMapperBuilder.create()
            .withEventListener(listener)
            .build();
    beanMapper.map(new Object(), new Object());

    verify(listener).mappingStarted(any());
    verify(listener).mappingFinished(any());
    verifyNoMoreInteractions(listener);
  }

}
