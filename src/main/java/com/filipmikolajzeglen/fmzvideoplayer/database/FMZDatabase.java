package com.filipmikolajzeglen.fmzvideoplayer.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * FMZDatabase is a generic file-based database utility class. It provides the ability to perform basic CRUD operations
 * (Create, Read, Update, Delete) on documents of type DOCUMENT that extends 'FMZIdentifiable' and serializable
 * interface. Each instance of this class represents a single table within the database. It uses JSON as the file format
 * for storing the documents and Jackson library for serialization and deserialization. It also offers a simple querying
 * method by id using 'findById' method, and loading all documents using 'findAll' method.
 *
 * @param <DOCUMENT> the type of documents to store which must be serializable and identifiable
 */
@Setter
@RequiredArgsConstructor
public class FMZDatabase<DOCUMENT>
{

   private static final Logger LOGGER = new Logger();

   private String databaseName;
   private String tableName;
   private String directoryPath;
   private String filename;
   private final Class<DOCUMENT> documentClass;
   private final ObjectMapper objectMapper = new ObjectMapper();
   private List<DOCUMENT> data = new ArrayList<>();

   public void initialize()
   {
      this.filename = directoryPath + File.separator + databaseName + "_" + tableName + ".json";
      File file = new File(filename);
      if (file.exists())
      {
         try
         {
            data = objectMapper.readValue(file,
                  objectMapper.getTypeFactory().constructCollectionType(List.class, documentClass));
            LOGGER.info("FMZDatabase: Data loaded from file " + filename);
         }
         catch (IOException e)
         {
            LOGGER.error("FMZDatabase: Error while loading data: " + e.getMessage());
            data = new ArrayList<>();
         }
      }
      else
      {
         data = new ArrayList<>();
         LOGGER.info("FMZDatabase: Created new database file " + filename);
         saveAll(data);
      }
   }

   public void save(DOCUMENT item)
   {
      int index = data.indexOf(item);
      if (index >= 0)
      {
         data.set(index, item);
      }
      else
      {
         data.add(item);
      }
      saveAll(data);
   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   public void saveAll(List<DOCUMENT> items)
   {
      data = new ArrayList<>(items);
      try
      {
         File file = new File(filename);
         file.getParentFile().mkdirs();
         objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
         LOGGER.info("FMZDatabase: Data saved to file " + filename);
      }
      catch (IOException e)
      {
         LOGGER.error("FMZDatabase: Error while saving: " + e.getMessage());
      }
   }


   public List<DOCUMENT> findAll()
   {
      return new ArrayList<>(data);
   }

   public void delete(DOCUMENT item)
   {
      data.remove(item);
      saveAll(data);
   }

   public void clear()
   {
      data.clear();
      saveAll(data);
   }
}