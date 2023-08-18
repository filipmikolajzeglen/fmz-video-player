# FMZ Video player - v2.1.0

## Software prepared to generate private tv

This is a software prepared to generate private TV based on local videos.
FMZ Video player generate full schedule prepared from local video sources and configuration.
The software was created out of nostalgia to allow to some extent resurrection of channels such as
FoxKids, Jetix, CartoonNetwork.

<p align="center">
  <img src="src/main/resources/fmzPlayerIcon.png">
</p>

## Configuration

Current version 2.1.0 has all configuration parameters in `com.filipmikolajzeglen.video.VideoPlayerConfiguration`.
In future this configuration will be moved to application.properties and then used for additional config view.

**PRIMARY_COLOR** is a String representing color of time slider and volume slider saved as hex.

- `PRIMARY_COLOR = "#7C9EF7"`

**MAX_SINGLE_SERIES_PER_DAY** is a variable used to set amount of episode from single series allowed to play
per day. For now default value is 2, and it means that two episodes from single series will be play one by one. 

- `MAX_SINGLE_SERIES_PER_DAY = 2`

**MAX_EPISODES_PER_DAY** is a variable used to set max amount of all videos allowed to play
per day

- `MAX_EPISODES_PER_DAY = 30`

**VIDEO_MAIN_SOURCE** is static variable used to tell FMZ Video player where is a main directory with all videos

- `VIDEO_MAIN_SOURCE = "E:\\FoxKids"`

**FMZ_DATABASE_NAME** is a variable to set name of built-in database provided by FMZDatabase.

- `FMZ_DATABASE_NAME = "FMZDB"`

**FMZ_TABLE_NAME** is a variable to set name of table in built-in database.

- `FMZ_TABLE_NAME = "FoxKids"`

**FMZ_DIRECTORY_PATH** is a variable that tells where the database is to be created.

- `FMZ_DIRECTORY_PATH = "E:\\"`

Software was prepared to use structure:
    
    ├── MAIN_DIRECTORY
    │      ├── VIDEO_SERIES_1
    │      ├── VIDEO_SERIES_2
    │      ├── VIDEO_SERIES_3
    │      └── ...


    ├── FOXKIDS
    │      └── POWER RANGERS MIGHTY MORPHIN
    │                ├── S01E01-Episode-name
    │                ├── S01E02-Episode-name
    │                └── ...
    │      ├── X-MAN
    │      ├── SIMPSONS
    │      └── ...

Local videos must follow this pattern:

       SXXEYY-Episode-name
       SEASON XX EPISODE YY-EPISODE-NAME
       
       For example:
       S01E01-The-Blade-Raider

<p align="center">
  <img src="src/main/resources/FMZVideoPlayerScreen1.png">
</p>

## How to run version 2.1.0
Until there is an interface that allows easy management of the software, the only sensible option to use the player is 
by configuring the FMZ Video Player and building the artifact yourself

1. Open IDE and provide configuration in `com.filipmikolajzeglen.video.VideoPlayerConfiguration`
2. Build project using Maven `mvn clean install`
3. After building project go to target folder and move **fmz-video-player-1.0.0-SNAPSHOT.jar** file e.g. to Desktop
4. Open terminal in folder with .jar file and use cmd `java -jar fmz-video-player-1.0.0-SNAPSHOT.jar`

## Plan for next version
- Creating additional config panel to simplify FMZ Video Player configuration
- Adding Web Client to remote playing videos
- Upload FMZ Video Player exe

### Version 2.1.0
- Replaced external video players with own built-in player.
- Added FMZDatabase a dedicated built-in database created to store information about episodes and their watched status
- Added functionality to recognize which episode should be run next time
- Improved built-in Logger for log FMZ Video Player events
- Cleanup whole project

### Version 1.0.0
- Added Schedule based on local video sources.
- Added Simple logger.
