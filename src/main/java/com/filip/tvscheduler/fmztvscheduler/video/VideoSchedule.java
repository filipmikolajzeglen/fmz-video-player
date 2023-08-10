package com.filip.tvscheduler.fmztvscheduler.video;

import java.util.List;
import java.util.Objects;

public class VideoSchedule {

    private Integer id;
    private String date;
    private List<Video> videoListInQueue;

    public VideoSchedule() {
    }

    public VideoSchedule(Integer id, String date, List<Video> videoListInQueue) {
        this.id = id;
        this.date = date;
        this.videoListInQueue = videoListInQueue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Video> getVideoListInQueue() {
        return videoListInQueue;
    }

    public void setVideoListInQueue(List<Video> videoListInQueue) {
        this.videoListInQueue = videoListInQueue;
    }

    @Override
    public String toString() {
        return "TVSchedule{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", videoListInQueue=" + videoListInQueue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoSchedule)) return false;
        VideoSchedule that = (VideoSchedule) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDate(), that.getDate())
                && Objects.equals(getVideoListInQueue(), that.getVideoListInQueue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDate(), getVideoListInQueue());
    }
}
