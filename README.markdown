Synopsis
========

This project helps with generating binding classes from wicket templates via
annotation processing for compile time consistency checking.

It has been inspired by bindgen.

Prerequisites
=============

Java 1.6 compiler (for service provider support).

Usage
=====

Installation
------------

Just drop the jar including its dependencies onto your classpath.

Coding
------

Annotate your wicket component classes with the `@HasTemplate` annotation.

Configuration
-------------

Put a file named "wicket-id-bindings-generator.properties" in your project home.
In it you can configure the following properties:

* `template.folders` - the comma separated paths to search for the wicket
templates relative to the properties file. E.g. if your templates are right next
to your java files in src/main/java, then configure it with src/main/java. If
this property is unset, the generator will look right next to the built class
files, which is probably not what you want.
* `template.extension` - the file extension of the templates, defaults to html.
* `template.encoding` - the encoding to use for reading the templates, defaults
to UTF-8.
* `bindings.suffix` - a suffix appended to the generated bindings, defaults to
`WID`. If e.g. there is a wicket component with a template ant the name
`MyPage`, the generated binding will be generated in the package of the
component with the name `MyPageWID`.

A sane minimal properties file probably looks like this:

    template.folders=src/main/java

Integration with Eclipse
------------------------

You can configure Eclipse so that the bindings are generated whenever you save
changes in your wicket components annotated with the `@HasTemplate` annotation.

Go to `Project Properties -> Java Compiler -> Annotation Processing` and
enable annotation processing. Then, under `Factory Path`, add the generator
jar. Click the Advanced button to make sure the processor is recognized by
eclipse.

Compilation
===========

In order to generate the jar including its dependencies, use maven:

    mvn assembly:assembly
