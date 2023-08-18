package com.filipmikolajzeglen.fmzvideoplayer.database;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;

/**
 * FMZDatabase is a generic file-based database utility class. It provides the ability to perform
 * basic CRUD operations (Create, Read, Update, Delete) on documents of type DOCUMENT that extends
 * 'FMZIdentifiable' and serializable interface. Each instance of this class represents a single table
 * within the database. It uses JSON as the file format for storing the documents and Jackson library
 * for serialization and deserialization. It also offers a simple querying method by id using 'findById' method,
 * and loading all documents using 'findAll' method.
 *
 * @param <DOCUMENT> the type of documents to store which must be serializable and identifiable
 */
public class FMZDatabase<DOCUMENT extends FMZIdentifiable & Serializable> {

    private static final Logger LOGGER = new Logger();

    private String filename;
    private String directoryPath;
    private String databaseName;
    private String tableName;

    public void initialize() {
        File directory = new File(directoryPath + File.separator + databaseName);
        if (!directory.exists()) {
            LOGGER.info(String.format("Directory path for built-in database '%s' is creating.", databaseName));
            directory.mkdirs();
        }
        this.filename = directoryPath + File.separator + databaseName + File.separator + tableName + ".txt";

        File file = new File(filename);
        if (!file.exists()) {
            try {
                LOGGER.info(String.format("File txt representing collection '%s' is creating.", tableName));
                file.createNewFile();
                saveAll(new ArrayList<>());
            } catch (IOException e) {
                LOGGER.error(String.format("Error occurred when initializing documents:\n%s", e.getMessage()));
            }
        }
    }

    public void save(DOCUMENT document) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<DOCUMENT> currentData = loadDataFromFile();

            int existingIndex = -1;
            for (int i = 0; i < currentData.size(); i++) {
                if (currentData.get(i).equals(document)) {
                    existingIndex = i;
                    break;
                }
            }

            if (existingIndex != -1) {
                currentData.remove(existingIndex);
                currentData.add(existingIndex, document);
            } else {
                currentData.add(document);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), currentData);
            LOGGER.info(String.format("Document with id '%s' was saved successful in collection '%s'.", document.getId(), tableName));
        } catch (IOException e) {
            LOGGER.error(String.format("Error occurred when saving documents:\n%s", e.getMessage()));
        }
    }

    public void saveAll(List<DOCUMENT> documents) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<DOCUMENT> currentData = loadDataFromFile();

            for (DOCUMENT document : documents) {
                if (!currentData.contains(document)) {
                    currentData.add(document);
                }
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), currentData);
            LOGGER.info(String.format("All documents were saved successful in collection '%s'.", tableName));
        } catch (IOException e) {
            LOGGER.error(String.format("Error occurred when saving all documents:\n%s", e.getMessage()));
        }
    }

    public void delete(DOCUMENT document) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<DOCUMENT> currentData = loadDataFromFile();
            currentData.remove(document);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), currentData);
            LOGGER.info(String.format("Document with id '%s' was removed successful from collection '%s'.", document.getId(), tableName));
        } catch (IOException e) {
            LOGGER.error(String.format("Error occurred when deleting documents:\n%s", e.getMessage()));
        }
    }

    public Optional<DOCUMENT> find(DOCUMENT document) {
        List<DOCUMENT> currentData = loadDataFromFile();
        return currentData.stream().filter(currentDocument -> currentDocument.equals(document)).findFirst();
    }

    public Optional<DOCUMENT> findById(String id) {
        List<DOCUMENT> currentData = loadDataFromFile();
        return currentData.stream().filter(document -> Objects.equals(document.getId(), id)).findFirst();
    }

    public List<DOCUMENT> findAll() {
        LOGGER.info(String.format("Finding all documents from collection '%s'.", tableName));
        return loadDataFromFile();
    }

    private List<DOCUMENT> loadDataFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(filename);
            if (file.length() > 0) {
                LOGGER.info(String.format("Collection '%s' is loading.", tableName));
                return mapper.readValue(file, new TypeReference<ArrayList<DOCUMENT>>() {
                });
            } else {
                LOGGER.info(String.format("Collection '%s' is empty.", tableName));
                return new ArrayList<>();
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Error occurred when loading data from file:\n%s", e.getMessage()));
            return new ArrayList<>();
        }
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}