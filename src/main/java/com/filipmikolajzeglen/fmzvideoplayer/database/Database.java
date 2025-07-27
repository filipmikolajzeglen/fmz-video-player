package com.filipmikolajzeglen.fmzvideoplayer.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
public class Database<DOCUMENT>
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
         LOGGER.info("Database: Created new database file " + filename);
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
         LOGGER.info("Database: Data saved to file " + filename);
      }
      catch (IOException e)
      {
         LOGGER.error("Database: Error while saving: " + e.getMessage());
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