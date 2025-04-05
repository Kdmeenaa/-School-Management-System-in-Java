#!/bin/bash

# School Management System Packaging Script
# This script compiles and packages the School Management System application

 
echo "==========================================="

# Create necessary directories
mkdir -p bin
mkdir -p dist
mkdir -p libs

# Download required libraries if not already present
if [ ! -d "libs" ] || [ -z "$(ls -A libs)" ]; then
  echo "Downloading required libraries..."

  # Create temporary directory for libraries
  mkdir -p temp_libs

  # SQLite JDBC
  echo "Downloading SQLite JDBC..."
  curl -L https://github.com/xerial/sqlite-jdbc/releases/download/3.40.0.0/sqlite-jdbc-3.40.0.0.jar -o temp_libs/sqlite-jdbc.jar

  # FlatLaf
  echo "Downloading FlatLaf..."
  curl -L https://repo1.maven.org/maven2/com/formdev/flatlaf/3.0/flatlaf-3.0.jar -o temp_libs/flatlaf.jar

  # JBCrypt
  echo "Downloading JBCrypt..."
  curl -L https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar -o temp_libs/jbcrypt.jar

  # Apache POI
  echo "Downloading Apache POI..."
  curl -L https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.3/poi-5.2.3.jar -o temp_libs/poi.jar
  curl -L https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.3/poi-ooxml-5.2.3.jar -o temp_libs/poi-ooxml.jar

  # iText
  echo "Downloading iText..."
  curl -L https://repo1.maven.org/maven2/com/itextpdf/itextpdf/5.5.13.3/itextpdf-5.5.13.3.jar -o temp_libs/itextpdf.jar

  # JFreeChart
  echo "Downloading JFreeChart..."
  curl -L https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar -o temp_libs/jfreechart.jar

  # Move files to libs directory
  mv temp_libs/* libs/
  rm -rf temp_libs

  echo "Libraries downloaded successfully!"
fi

# Create classpath string
CLASSPATH=""
for jar in libs/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done
CLASSPATH="${CLASSPATH:1}" # Remove leading colon

# Compile Java source files
echo "Compiling Java source files..."
javac -d bin -cp "$CLASSPATH" src/main/java/com/school/**/*.java

if [ $? -ne 0 ]; then
  echo "Compilation failed!"
  exit 1
fi

echo "Creating manifest file..."
echo "Main-Class: com.school.Main" > manifest.txt
echo "Class-Path: $CLASSPATH" >> manifest.txt

# Create JAR file
echo "Creating JAR file..."
jar cvfm dist/SchoolManagementSystem.jar manifest.txt -C bin .

if [ $? -ne 0 ]; then
  echo "JAR creation failed!"
  exit 1
fi

# Copy libraries
echo "Copying libraries to distribution directory..."
mkdir -p dist/libs
cp libs/* dist/libs/

# Create startup script for Linux/Mac
echo "Creating startup script for Linux/Mac..."
cat > dist/start.sh << 'EOF'
#!/bin/bash
java -jar SchoolManagementSystem.jar
EOF
chmod +x dist/start.sh

# Create startup script for Windows
echo "Creating startup script for Windows..."
cat > dist/start.bat << 'EOF'
@echo off
java -jar SchoolManagementSystem.jar
pause
EOF

# Create ZIP archive for distribution
echo "Creating distribution ZIP archive..."
cd dist
zip -r ../SchoolManagementSystem.zip *
cd ..

echo ""
echo "Packaging completed successfully!"
echo "Distribution archive created: SchoolManagementSystem.zip"
echo ""
echo "To run the application:"
echo "1. Extract the ZIP archive"
echo "2. Run start.sh (Linux/Mac) or start.bat (Windows)"
echo ""
echo "Default login credentials:"
echo "Username: admin"
echo "Password: admin123"
