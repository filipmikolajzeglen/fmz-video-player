package com.filip.tvscheduler.fmztvscheduler.fmzdatabase;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.filip.tvscheduler.fmztvscheduler.video.Video;

@JsonDeserialize(as = Video.class)
public interface FMZIdentifiable {
    String getId();
}
