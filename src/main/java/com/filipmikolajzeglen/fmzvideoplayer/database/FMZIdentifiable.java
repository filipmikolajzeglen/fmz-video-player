package com.filipmikolajzeglen.fmzvideoplayer.database;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;

@JsonDeserialize(as = Video.class)
public interface FMZIdentifiable
{
   String getId();
}
