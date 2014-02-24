/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrey Klimachev - support for multi-module maven projects.
 *
 *******************************************************************************/

package org.jacoco.maven.util;

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility functions related to files.
 */
public final class FileUtil {

    /**
     * Creates a new instance of {@link FileUtil}
     */
    private FileUtil() {}


    /**
     * Extract jar content into build output directory.
     *
     * @param project   Maven project
     * @param jarPath   Path to jar file.
     */
    public static void extractClassesFromJar(MavenProject project, String jarPath)
    {
        try {
            JarFile jar = new JarFile(jarPath);

            Enumeration enum1 = jar.entries();
            while (enum1.hasMoreElements()) {

                JarEntry file = (JarEntry) enum1.nextElement();
                File f = new File(project.getBuild().getOutputDirectory() + java.io.File.separator + file.getName());
                if (file.isDirectory()) {
                    f.mkdir();
                    continue;
                }
                InputStream is = jar.getInputStream(file);
                FileOutputStream fos = new FileOutputStream(f);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.close();
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete copied content from built output directory.
     *
     * @param project   Maven project
     * @param jarPath   Path to jar file.
     */
    public static void deleteDependencyFile(MavenProject project, String jarPath)
    {
        try {
            JarFile jar = new JarFile(jarPath);

            Enumeration enum1 = jar.entries();
            while (enum1.hasMoreElements()) {

                JarEntry file = (JarEntry) enum1.nextElement();
                if(file.getName().endsWith(".class"))
                {
                    File f = new File(project.getBuild().getOutputDirectory() + java.io.File.separator + file.getName());
                    if (!file.isDirectory() && f.exists()) {
                        f.delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
