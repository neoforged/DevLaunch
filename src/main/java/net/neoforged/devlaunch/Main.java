/*
   Copyright 2017 Remko Popma
   Copyright (c) NeoForged and contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.neoforged.devlaunch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            throw new IllegalArgumentException("DevLaunch requires at least one argument: an @-file to expand, or the main class.");
        }

        List<String> newArgs = new ArrayList<>(args.length);

        for (String arg : args) {
            addOrExpand(arg, newArgs, new HashSet<>());
        }

        if (newArgs.isEmpty()) {
            throw new IllegalArgumentException("DevLaunch requires at least one argument: the main class.");
        }

        Method mainMethod;
        try {
            mainMethod = Class.forName(newArgs.get(0)).getMethod("main", String[].class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not find main class or main method. Given main class: " + newArgs.get(0), e);
        }

        try {
            mainMethod.invoke(null, (Object) newArgs.subList(1, newArgs.size()).toArray(String[]::new));
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not invoke main class: " + newArgs.get(0), e);
        }
    }

    // The functions below are copied and adapted from picocli.
    private static void addOrExpand(String arg, List<String> arguments, Set<String> visited) {
        if (!arg.equals("@") && arg.startsWith("@")) {
            arg = arg.substring(1);
            if (!arg.startsWith("@")) {
                expandArgumentFile(arg, arguments, visited);
                return;
            }
        }
        arguments.add(arg);
    }

    private static void expandArgumentFile(String fileName, List<String> arguments, Set<String> visited) {
        File file = new File(fileName);
        if (!file.canRead()) {
            arguments.add("@" + fileName);
        } else if (!visited.contains(file.getAbsolutePath())) {
            expandValidArgumentFile(fileName, file, arguments, visited);
        }
    }

    private static void expandValidArgumentFile(String fileName, File file, List<String> arguments, Set<String> visited) {
        List<String> result = new ArrayList<>();
        try (Reader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            visited.add(file.getAbsolutePath());
            StreamTokenizer tok = new StreamTokenizer(reader);
            tok.resetSyntax();
            tok.wordChars(' ', 255);
            tok.whitespaceChars(0, ' ');
            tok.quoteChar('"');
            tok.quoteChar('\'');
            tok.commentChar('#');
            while (tok.nextToken() != StreamTokenizer.TT_EOF) {
                addOrExpand(tok.sval, result, visited);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not read argument file @" + fileName, ex);
        }
        arguments.addAll(result);
    }
}
