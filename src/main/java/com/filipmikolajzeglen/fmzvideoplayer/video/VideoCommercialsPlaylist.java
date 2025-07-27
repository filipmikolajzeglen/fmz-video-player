package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import lombok.ToString;

@ToString
public class VideoCommercialsPlaylist
{
   private List<String> commercialPaths = new ArrayList<>();

   void loadCommercials()
   {
      String commercialFolderPath = PlayerConstants.Paths.VIDEO_MAIN_SOURCE + File.separator
            + PlayerConstants.Paths.COMMERCIALS_FOLDER_NAME;
      File commercialDir = new File(commercialFolderPath);
      if (commercialDir.exists() && commercialDir.isDirectory())
      {
         File[] commercialFiles = commercialDir.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return file.isFile() && (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv"));
         });
         if (commercialFiles != null)
         {
            this.commercialPaths = Arrays.stream(commercialFiles)
                  .map(File::getAbsolutePath)
                  .collect(Collectors.toList());
         }
      }
   }

   List<String> getRandomCommercialPlaylist()
   {
      if (commercialPaths.isEmpty())
      {
         return Collections.emptyList();
      }

      List<String> shuffledAds = new ArrayList<>(commercialPaths);
      Collections.shuffle(shuffledAds);

      return shuffledAds.stream()
            .limit(PlayerConstants.Playback.COMMERCIAL_COUNT_BETWEEN_EPISODES)
            .collect(Collectors.toList());
   }

   public Optional<List<String>> getCommercialsToPlay(boolean hasNextEpisode)
   {
      if (PlayerConstants.Playback.COMMERCIALS_ENABLED && hasNextEpisode)
      {
         List<String> commercials = getRandomCommercialPlaylist();
         if (!commercials.isEmpty())
         {
            return Optional.of(commercials);
         }
      }
      return Optional.empty();
   }

}