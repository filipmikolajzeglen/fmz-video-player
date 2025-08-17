package com.filipmikolajzeglen.fmzvideoplayer.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Database<DOCUMENT>
{
   private static final Map<String, Database<?>> INSTANCES = new ConcurrentHashMap<>();
   public static final String APP_DATA_DIRECTORY = System.getenv("APPDATA") + File.separator + "FMZVideoPlayer";
   public static final String FMZ_DATABASE_NAME = "FMZDB";

   private final String filename;
   private final Class<DOCUMENT> documentClass;
   private final ObjectMapper objectMapper = new ObjectMapper();
   private List<DOCUMENT> data = new ArrayList<>();
   private final Object lock = new Object();

   private Database(Class<DOCUMENT> documentClass)
   {
      this.documentClass = documentClass;
      this.filename = APP_DATA_DIRECTORY + File.separator + FMZ_DATABASE_NAME + "_" + documentClass.getSimpleName() + ".json";
      load();
      log.info("Initialized file {} with {} items.", filename, data.size());
   }

   @SuppressWarnings("unchecked")
   public static synchronized <T> Database<T> getInstance(Class<T> documentClass)
   {
      String key = FMZ_DATABASE_NAME + ":" + documentClass.getSimpleName() + ":" + APP_DATA_DIRECTORY;
      return (Database<T>) INSTANCES.computeIfAbsent(key, k -> new Database<>(documentClass));
   }

   public void ensureFileExists()
   {
      synchronized (lock)
      {
         File file = new File(filename);
         if (!file.exists())
         {
            saveToFile();
         }
      }
   }

   private void load()
   {
      synchronized (lock)
      {
         File file = new File(filename);
         if (file.exists())
         {
            try
            {
               data = objectMapper.readValue(file,
                     objectMapper.getTypeFactory().constructCollectionType(List.class, documentClass));
               log.info("Data loaded from file {}", filename);
            }
            catch (IOException e)
            {
               log.error("Error while loading data: {}", e.getMessage());
               data = new ArrayList<>();
            }
         }
         else
         {
            data = new ArrayList<>();
            log.info("Database file {} not found. Initialized empty database.", filename);
         }
      }
   }

   private void saveToFile()
   {
      try
      {
         File file = new File(filename);
         file.getParentFile().mkdirs();
         objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
      }
      catch (IOException e)
      {
         log.error("Error while saving: {}", e.getMessage());
      }
   }

   // CRUD
   public void create(DOCUMENT item)
   {
      synchronized (lock)
      {
         data.add(item);
         saveToFile();
         log.info("Item created successfully in file {}.", filename);
         log.info("CREATE {}", item);
      }
   }

   public void createAll(List<DOCUMENT> items)
   {
      synchronized (lock)
      {
         data.addAll(items);
         saveToFile();
         log.info("Items created: {} in file {}", items.size(), filename);
      }
   }

   public Optional<DOCUMENT> readFirst()
   {
      synchronized (lock)
      {
         if (data.isEmpty())
         {
            return Optional.empty();
         }
         return Optional.of(data.getFirst());
      }
   }

   public List<DOCUMENT> readAll()
   {
      synchronized (lock)
      {
         return new ArrayList<>(data);
      }
   }

   public void update(DOCUMENT item)
   {
      synchronized (lock)
      {
         if (data == null || data.isEmpty())
         {
            data = new ArrayList<>();
            data.add(item);
            saveToFile();
            log.info("Item updated (created new config) in file {}.", filename);
            log.info("BEFORE NULL");
            log.info("AFTER {}", item);
         }
         else
         {
            int index = data.indexOf(item);
            if (index >= 0)
            {
               var beforeUpdate = data.get(index);
               data.set(index, item);
               saveToFile();
               log.info("Item updated (existing config overwritten) in file {}.", filename);
               log.info("BEFORE {}", beforeUpdate);
               log.info("AFTER {}", item);
            }
         }
      }
   }

   public void delete(DOCUMENT item)
   {
      synchronized (lock)
      {
         if (data.remove(item))
         {
            saveToFile();
            log.info("Item deleted successfully from file {}.", filename);
            log.info("REMOVED {}", item);
         }
      }
   }

   public void deleteAll()
   {
      synchronized (lock)
      {
         data.clear();
         saveToFile();
         log.info("All items cleared in file {}.", filename);
      }
   }
}