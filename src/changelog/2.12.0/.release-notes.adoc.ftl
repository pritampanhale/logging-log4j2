////
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

         https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
////

= ${release.version}<#if release.date?has_content> (${release.date})</#if>

This release contains bugfixes and minor enhancements.

Due to a break in compatibility in the SLF4J binding, Log4j now ships with two versions of the SLF4J to Log4j adapters.
`log4j-slf4j-impl` should be used with SLF4J 1.7.x and earlier and `log4j-slf4j18-impl` should be used with SLF4J 1.8.x and later.

New features include support for reconfiguration of Log4j configuraton files accessed via HTTP(s) and Spring Cloud Config specifically, a new Docker Lookup for obtaining information about the current Docker container, new reconfiguration methods added to the Configurator class, the ability to pad integers with leading zeros in filenames generated by the rolling file appenders, and allowing the TCP `SocketAppender` to connect to multiple IP addresses when they are returned from DNS.

More details on the new features and fixes are itemized below.

Note that the XML, JSON and YAML formats changed in the 2.11.0 release: they no longer have the `timeMillis` attribute and instead have an `Instant` element with `epochSecond` and `nanoOfSecond` attributes.

The Log4j 2.12.0 API, as well as many core components, maintains binary compatibility with previous releases.

Log4j 2.12.0 requires a minimum of Java 7 to build and run.
Log4j 2.3 was the last release that supported Java 6.

Basic compatibility with Log4j 1.x is provided through the `log4j-1.2-api` component, however it does
not implement some of the very implementation specific classes and methods.
The package names and Maven `groupId` have been changed to `org.apache.logging.log4j` to avoid any conflicts with Log4j 1.x.

For complete information on Apache Log4j 2, including instructions on how to submit bug reports, patches, or suggestions for improvement, see http://logging.apache.org/log4j/2.x/[the Apache Log4j 2 website].

<#include "../.changelog.adoc.ftl">