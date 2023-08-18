package com.filipmikolajzeglen.database;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.filipmikolajzeglen.video.Video;

@JsonDeserialize(as = Video.class)
public interface FMZIdentifiable {
    String getId();
}
