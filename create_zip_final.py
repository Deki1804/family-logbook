#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Complete ZIP creation script for Family Logbook Project
Includes all source files, resources, and documentation
"""

import zipfile
import os
import sys

def should_exclude(path):
    """Check if path should be excluded"""
    exclude_patterns = [
        'build', '.gradle', '.idea', '*.iml',
        'local.properties', 'google-services.json',  # Exclude actual, include template
        '*.apk', '*.aab', '*.jks', '*.keystore',
        '__pycache__', '.git'
    ]
    path_lower = path.lower().replace('\\', '/')
    for pattern in exclude_patterns:
        if pattern in path_lower:
            return True
    return False

def add_directory(zip_file, dir_path, base_path=""):
    """Recursively add directory to zip"""
    if not os.path.exists(dir_path):
        return 0
    
    count = 0
    for root, dirs, files in os.walk(dir_path):
        # Filter out excluded directories
        dirs[:] = [d for d in dirs if not should_exclude(os.path.join(root, d))]
        
        for file in files:
            file_path = os.path.join(root, file)
            if should_exclude(file_path):
                continue
            
            # Calculate relative path for zip
            if base_path:
                arcname = os.path.relpath(file_path, base_path)
            else:
                arcname = file_path
            
            try:
                zip_file.write(file_path, arcname)
                count += 1
            except Exception as e:
                print(f"Error adding {file_path}: {e}")
    
    return count

def main():
    zip_name = "FamilyLogbook_Project.zip"
    
    # Remove existing ZIP
    if os.path.exists(zip_name):
        os.remove(zip_name)
        print("Removed existing ZIP file")
    
    print("\n" + "=" * 60)
    print("Creating Family Logbook Project ZIP Archive")
    print("=" * 60 + "\n")
    
    total_files = 0
    
    with zipfile.ZipFile(zip_name, 'w', zipfile.ZIP_DEFLATED) as z:
        # 1. app/src - ALL source files
        print("1. Adding app/src (all source files, resources, manifest)...")
        count = add_directory(z, "app/src", ".")
        total_files += count
        print(f"   Added {count} files from app/src\n")
        
        # 2. app build files
        print("2. Adding app build files...")
        app_files = ["app/build.gradle.kts", "app/proguard-rules.pro", "app/google-services.json.template"]
        for f in app_files:
            if os.path.exists(f):
                z.write(f, f)
                total_files += 1
                print(f"   Added: {f}")
        print()
        
        # 3. Root Gradle files
        print("3. Adding root Gradle files...")
        root_files = ["build.gradle.kts", "settings.gradle.kts", "gradle.properties"]
        for f in root_files:
            if os.path.exists(f):
                z.write(f, f)
                total_files += 1
                print(f"   Added: {f}")
        print()
        
        # 4. Gradle wrapper
        print("4. Adding Gradle wrapper...")
        if os.path.exists("gradle/wrapper"):
            count = add_directory(z, "gradle/wrapper", ".")
            total_files += count
            print(f"   Added {count} files from gradle/wrapper\n")
        
        # 5. Gradle wrapper scripts
        print("5. Adding Gradle wrapper scripts...")
        wrapper_scripts = ["gradlew", "gradlew.bat"]
        for f in wrapper_scripts:
            if os.path.exists(f):
                z.write(f, f)
                total_files += 1
                print(f"   Added: {f}")
        print()
        
        # 6. Firestore rules
        print("6. Adding Firestore rules...")
        if os.path.exists("firestore.rules"):
            z.write("firestore.rules", "firestore.rules")
            total_files += 1
            print("   Added: firestore.rules\n")
        
        # 7. Git files
        print("7. Adding Git files...")
        git_files = [".gitignore", ".gitattributes"]
        for f in git_files:
            if os.path.exists(f):
                z.write(f, f)
                total_files += 1
                print(f"   Added: {f}")
        print()
        
        # 8. Documentation and scripts
        print("8. Adding documentation and scripts...")
        doc_extensions = ['.md', '.txt', '.py', '.bat', '.ps1']
        for f in os.listdir("."):
            if os.path.isfile(f) and (f.endswith(tuple(doc_extensions)) or f in ['.gitignore', '.gitattributes']):
                if not should_exclude(f):
                    z.write(f, f)
                    total_files += 1
        print(f"   Added documentation files\n")
    
    # Verify ZIP was created
    if os.path.exists(zip_name):
        size_mb = os.path.getsize(zip_name) / (1024 * 1024)
        
        print("=" * 60)
        print("✅ ZIP CREATED SUCCESSFULLY!")
        print("=" * 60)
        print(f"\n📁 Location: {os.path.abspath(zip_name)}")
        print(f"📦 Size: {size_mb:.2f} MB")
        print(f"📄 Total files: {total_files}\n")
        
        # Verify important files
        print("🔍 Verifying important files:")
        print("-" * 60)
        
        important_files = {
            "SpeechRecognizerHelper.kt": "app/src/main/java/com/familylogbook/app/data/speech/SpeechRecognizerHelper.kt",
            "VaccinationCalendar.kt": "app/src/main/java/com/familylogbook/app/domain/vaccination/VaccinationCalendar.kt",
            "ShoppingDealsChecker.kt": "app/src/main/java/com/familylogbook/app/data/shopping/ShoppingDealsChecker.kt",
            "SmartHomeManager.kt": "app/src/main/java/com/familylogbook/app/data/smarthome/SmartHomeManager.kt",
            "AndroidManifest.xml": "app/src/main/AndroidManifest.xml",
            "build.gradle.kts (app)": "app/build.gradle.kts",
            "build.gradle.kts (root)": "build.gradle.kts"
        }
        
        with zipfile.ZipFile(zip_name, 'r') as z:
            all_files = z.namelist()
            
            for name, path in important_files.items():
                found = any(path in f for f in all_files)
                status = "✅" if found else "❌"
                print(f"  {status} {name}")
        
        print("\n" + "=" * 60)
        print("✅ DONE!")
        print("=" * 60 + "\n")
        
        return 0
    else:
        print("\n❌ ERROR: ZIP was not created!")
        return 1

if __name__ == "__main__":
    sys.exit(main())
