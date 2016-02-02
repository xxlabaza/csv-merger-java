/*
 * Copyright 2016 xxlabaza.
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
package ru.xxlabaza.csv.merger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 02.02.2016
 */
public class Main {

    private static final String TEMPORARY_FOLDER_NAME;

    static {
        TEMPORARY_FOLDER_NAME = "./tmp";
    }

    public static void main (String[] args) throws Exception {
        File temporaryFolder = new File(TEMPORARY_FOLDER_NAME);
        if (!temporaryFolder.exists()) {
            temporaryFolder.mkdirs();
        }

        String firstFilePrefix = "A-";
        String secondFilePrefix = "B-";

        parseFileAndCreateFoldersTree(args[0], firstFilePrefix);
        parseFileAndCreateFoldersTree(args[1], secondFilePrefix);

        Path resultFile = Paths.get(args[2]);
        Files.createFile(resultFile);

        for (File folder : temporaryFolder.listFiles()) {
            String[] fileNames = folder.list();
            if (fileNames.length < 2) {
                continue;
            }

            List<String> firstPrefixFileNames = filterNames(fileNames, firstFilePrefix);
            if (firstPrefixFileNames.isEmpty()) {
                continue;
            }

            List<String> secondPrefixFileNames = filterNames(fileNames, secondFilePrefix);
            if (secondPrefixFileNames.isEmpty()) {
                continue;
            }

            List<String> toWrite = new ArrayList<>(firstPrefixFileNames.size() * secondPrefixFileNames.size());
            firstPrefixFileNames.forEach(firstValue -> {
                secondPrefixFileNames.forEach(secondValue -> {

                    toWrite.add(new StringBuilder()
                            .append(folder.getName()).append(',')
                            .append(firstValue).append(',')
                            .append(secondValue)
                            .toString()
                    );
                });
            });

            Files.write(resultFile, toWrite, StandardOpenOption.APPEND);
        }
    }

    private static void parseFileAndCreateFoldersTree (String csvFileName, String outputFilesPrefix) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(csvFileName);
             Scanner scanner = new Scanner(inputStream, "UTF-8")) {

            while (scanner.hasNextLine()) {
                String[] tokens = scanner.nextLine().split(",");

                File folder = new File(TEMPORARY_FOLDER_NAME, tokens[0]);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String outputFileName = new StringBuilder(outputFilesPrefix).append(tokens[1]).toString();

                new File(folder, outputFileName).createNewFile();
            }
        }
    }

    private static List<String> filterNames (String[] fileNames, String prefix) {
        return Stream.of(fileNames)
                .filter(name -> name.startsWith(prefix))
                .map(name -> name.substring(prefix.length()))
                .collect(toList());
    }
}
