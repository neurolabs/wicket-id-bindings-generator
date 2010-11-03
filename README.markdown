Synopsis
========

This project helps with generating binding classes from wicket templates and
translation keys via annotation processing for compile time consistency
checking.

It has been inspired by bindgen.

Prerequisites
=============

Java 1.6 compiler (for service provider support).

Dependencies
============

* joist util - http://joist.ws/codeGeneration.html.
    * slf4j. 

Usage
=====

Installation
------------

Just drop the jar including its dependencies onto your classpath.

Coding
------

Annotate your wicket component classes with the `@HasTemplate` annotation if
they define a template and with the `@HasTranslation` annotation if they define
a translation file.

Configuration
-------------

Put a file named "wicket-id-bindings-generator.properties" in your project home.
In it you can configure the following properties:

* `source folders` - the comma separated paths to search for the wicket 
templates and translation files relative to the properties file. E.g. if your
templates are right next to your java files in src/main/java, then configure it
with src/main/java. If this property is unset, the generator will look right
next to the built class files, which is probably not what you want.
* `source.encoding` - the default encoding to use for reading the templates,
defaults to `UTF-8`.
* `template.enabled` - enable the generation of template bindings, defaults to
`true`.
* `template.extension` - the file extension of the templates, defaults to
`html`.
* `template.bindingsuffix` - a suffix appended to the generated bindings,
defaults to `WID`. If e.g. there is a wicket component with a template and the
name `MyPage`, the generated binding will be generated in the package of the
component with the name `MyPageWID`.
* `translation.enabled` - enable the generation of translation bindings,
defaults to `true`.
* `translation.bindingsuffix` - a suffix appended to the generated bindings,
defaults to `I18N`.
* `translation.type` - the file types of the translation files. Can be either
`xml` or `properties`, defaults to `properties`.
* `debug` - if set to `true`, debug messages are logged while processing.

A sane minimal properties file probably looks like this:

    template.folders=src/main/java

Integration with Eclipse
------------------------

You can configure Eclipse so that the bindings are generated whenever you save
changes in your wicket components annotated with either one of the annotations.

Go to `Project Properties -> Java Compiler -> Annotation Processing` and
enable annotation processing. Then, under `Factory Path`, add the generator
jar plus its dependencies. Click the Advanced button to make sure the processors
are recognized by eclipse.

Compilation
===========

In order to generate the jar, use maven:

    mvn package

