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
package org.dozer.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.util.DefaultClassLoader;
import org.dozer.util.DefaultProxyResolver;
import org.dozer.util.DozerConstants;

public class Settings {

    private Integer converterByDestTypeCacheMaxSize = SettingsDefaults.CONVERTER_BY_DEST_TYPE_CACHE_MAX_SIZE;
    private Integer superTypesCacheMaxSize = SettingsDefaults.SUPER_TYPE_CHECK_CACHE_MAX_SIZE;
    private Boolean elEnabled = SettingsDefaults.EL_ENABLED;
    private String classLoaderBeanName = SettingsDefaults.CLASS_LOADER_BEAN;
    private String proxyResolverBeanName = SettingsDefaults.PROXY_RESOLVER_BEAN;

    public Settings() {

    }

    public Settings(Integer converterByDestTypeCacheMaxSize, Integer superTypesCacheMaxSize, Boolean elEnabled, String classLoaderBeanName, String proxyResolverBeanName) {
        this.converterByDestTypeCacheMaxSize = converterByDestTypeCacheMaxSize;
        this.superTypesCacheMaxSize = superTypesCacheMaxSize;
        this.elEnabled = elEnabled;
        this.classLoaderBeanName = classLoaderBeanName;
        this.proxyResolverBeanName = proxyResolverBeanName;
    }

    public Integer getConverterByDestTypeCacheMaxSize() {
        return converterByDestTypeCacheMaxSize;
    }

    public Integer getSuperTypesCacheMaxSize() {
        return superTypesCacheMaxSize;
    }

    public Boolean getElEnabled() {
        return elEnabled;
    }

    public String getClassLoaderBeanName() {
        return classLoaderBeanName;
    }

    public String getProxyResolverBeanName() {
        return proxyResolverBeanName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("converterByDestTypeCacheMaxSize", converterByDestTypeCacheMaxSize)
                .append("superTypesCacheMaxSize", superTypesCacheMaxSize)
                .append("elEnabled", elEnabled)
                .append("classLoaderBeanName", classLoaderBeanName)
                .append("proxyResolverBeanName", proxyResolverBeanName)
                .toString();
    }
}
