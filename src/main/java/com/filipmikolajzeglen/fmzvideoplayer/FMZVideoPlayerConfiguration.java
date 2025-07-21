package com.filipmikolajzeglen.fmzvideoplayer;

public class FMZVideoPlayerConfiguration
{
   public static final class UI
   {
      public static final String PRIMARY_COLOR = "#7C9EF7";
      public static final String GRADIENT_COLOR = "rgba(255, 255, 255, 0.28)";
      public static final String APPLICATION_TITLE = "FMZ Video Player";
      public static final String APPLICATION_ICON = "fmzPlayerIcon.png";
      public static final String APPLICATION_FXML = "/com/filipmikolajzeglen/fmzvideoplayer/fmz-video-player.fxml";
      public static final String APPLICATION_ERROR = "Cannot start the application: ";
   }

   public static final class Playback
   {
      public static final double DEFAULT_VOLUME_VALUE = 0.5;
      public static final double MUTE_VOLUME_VALUE = 0.0;
      public static final double RESET_TIME_VALUE = 1;
      public static int MAX_SINGLE_SERIES_PER_DAY = 2;
      public static int MAX_EPISODES_PER_DAY = 30;

      public static final String SPEED_LEVEL_1 = "1X";
      public static final double SPEED_LEVEL_1_VALUE = 1.0;
      public static final String SPEED_LEVEL_2 = "1.05X";
      public static final double SPEED_LEVEL_2_VALUE = 1.05;
      public static final String SPEED_LEVEL_3 = "1.1X";
      public static final double SPEED_LEVEL_3_VALUE = 1.1;
      public static final String SPEED_LEVEL_4 = "1.15X";
      public static final double SPEED_LEVEL_4_VALUE = 1.15;
   }

   public static final class Paths
   {
      public static String VIDEO_MAIN_SOURCE = "E:\\FoxKids";
      public static String FMZ_DATABASE_NAME = "FMZDB";
      public static String FMZ_TABLE_NAME = "FoxKids";
      public static String FMZ_DIRECTORY_PATH = "E:\\";
   }

   public static final class Icons
   {
      public static String PATH_TO_ICONS = "/svg/filled";
      public static String PLAY() { return PATH_TO_ICONS + "/play.svg"; }
      public static String PAUSE() { return PATH_TO_ICONS + "/pause.svg"; }
      public static String NEXT() { return PATH_TO_ICONS + "/next.svg"; }
      public static String REPLAY() { return PATH_TO_ICONS + "/replay.svg"; }
      public static String FULLSCREEN() { return PATH_TO_ICONS + "/fullscreen.svg"; }
      public static String MINIMIZE() { return PATH_TO_ICONS + "/minimize.svg"; }
      public static String MUTE() { return PATH_TO_ICONS + "/mute.svg"; }
      public static String VOLUME1() { return PATH_TO_ICONS + "/volume1.svg"; }
      public static String VOLUME2() { return PATH_TO_ICONS + "/volume2.svg"; }
      public static String NORMALIZE1() { return PATH_TO_ICONS + "/normalize1.svg"; }
      public static String NORMALIZE2() { return PATH_TO_ICONS + "/normalize2.svg"; }
      public static String NORMALIZE3() { return PATH_TO_ICONS + "/normalize3.svg"; }
      public static String NORMALIZE4() { return PATH_TO_ICONS + "/normalize4.svg"; }
      public static String NORMALIZE5() { return PATH_TO_ICONS + "/normalize5.svg"; }
      public static String NORMALIZE6() { return PATH_TO_ICONS + "/normalize6.svg"; }
      public static String NORMALIZE7() { return PATH_TO_ICONS + "/normalize7.svg"; }
      public static String NORMALIZE8() { return PATH_TO_ICONS + "/normalize8.svg"; }
      public static String NORMALIZE9() { return PATH_TO_ICONS + "/normalize9.svg"; }
      public static String NORMALIZE10() { return PATH_TO_ICONS + "/normalize10.svg"; }
      public static String NORMALIZE11() { return PATH_TO_ICONS + "/normalize11.svg"; }
   }
}