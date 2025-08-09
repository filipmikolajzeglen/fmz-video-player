package com.filipmikolajzeglen.fmzvideoplayer.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;

public class Database<DOCUMENT>
{

   private static final Logger LOGGER = new Logger();
   private static final Map<String, Database<?>> INSTANCES = new ConcurrentHashMap<>();

   private final String filename;
   private final Class<DOCUMENT> documentClass;
   private final ObjectMapper objectMapper = new ObjectMapper();
   private List<DOCUMENT> data = new ArrayList<>();

   private Database(String databaseName, String tableName, String directoryPath, Class<DOCUMENT> documentClass)
   {
      this.documentClass = documentClass;
      this.filename = directoryPath + File.separator + databaseName + "_" + tableName + ".json";
      load();
      LOGGER.info("Database: Initialized file " + filename + " with " + data.size() + " items.");
   }

   @SuppressWarnings("unchecked")
   public static synchronized <T> Database<T> getInstance(String databaseName, String tableName, String directoryPath,
         Class<T> documentClass)
   {
      String key = databaseName + ":" + tableName + ":" + directoryPath;
      return (Database<T>) INSTANCES.computeIfAbsent(key,
            k -> new Database<>(databaseName, tableName, directoryPath, documentClass));
   }

   public void ensureFileExists()
   {
      File file = new File(filename);
      if (!file.exists())
      {
         saveToFile();
      }
   }

   private void load()
   {
      File file = new File(filename);
      if (file.exists())
      {
         try
         {
            data = objectMapper.readValue(file,
                  objectMapper.getTypeFactory().constructCollectionType(List.class, documentClass));
            LOGGER.info("Database: Data loaded from file " + filename);
         }
         catch (IOException e)
         {
            LOGGER.error("Database: Error while loading data: " + e.getMessage());
            data = new ArrayList<>();
         }
      }
      else
      {
         data = new ArrayList<>();
         LOGGER.info("Database: " + filename + " not found. Initialized empty database.");
      }
   }

   private void saveToFile()
   {
      try
      {
         File file = new File(filename);
         file.getParentFile().mkdirs();
         objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
         LOGGER.info("Database: Data saved to file " + filename);
      }
      catch (IOException e)
      {
         LOGGER.error("Database: Error while saving: " + e.getMessage());
      }
   }

   // CRUD
   public void create(DOCUMENT item)
   {
      data.add(item);
      saveToFile();
      LOGGER.info("Database: Item created successfully.");
   }

   public void createAll(List<DOCUMENT> items)
   {
      data.addAll(items);
      saveToFile();
      LOGGER.info("Database: Items created: " + items.size());
   }

   public DOCUMENT readFirst()
   {
      return data.getFirst();
   }

   public List<DOCUMENT> readAll()
   {
      return new ArrayList<>(data);
   }

   public void update(DOCUMENT item)
   {
      if (data == null || data.isEmpty())
      {
         data = new ArrayList<>();
         data.add(item);
         saveToFile();
         LOGGER.info("Database: Item updated (created new config).");
      }
      else
      {
         int index = data.indexOf(item);
         if (index >= 0)
         {
            data.set(index, item);
            saveToFile();
            LOGGER.info("Database: Item updated (overwritten config).");
         }
      }
   }

   public void delete(DOCUMENT item)
   {
      if (data.remove(item))
      {
         saveToFile();
         LOGGER.info("Database: Item deleted successfully.");
      }
   }

   public void deleteAll()
   {
      data.clear();
      saveToFile();
      LOGGER.info("Database: All items cleared.");
   }
}