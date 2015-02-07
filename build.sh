#!/bin/sh

# Clean previous build
rm -rf dist/

# Compile project
sbt compile

# Make new dist folder
mkdir dist/
mkdir dist/libs

# Copy dep JARs to dist
cp libs/jsoup-1.8.1.jar libs/scala-library.jar dist/libs
cp MANIFEST.MF dist/

# Copy generated class files from target
cp -R target/scala-2.11/classes/* dist/

cd dist/

# Create JAR with default manifest
jar cmf MANIFEST.MF app.jar com/idealicious/*.class design.fxml

# Create ZIP file for dist
zip -9 -r impact.zip libs/ app.jar

# Clean up artifacts
rm -rf com/ views/ MANIFEST.MF

# Go back to root folder
cd ..
