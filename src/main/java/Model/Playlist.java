package Model;

import java.util.List;

public class Playlist {
    private String id;
    private String user_id;
    private List<String> song_ids;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<String> getSong_ids() {
        return song_ids;
    }

    public void setSong_ids(List<String> song_ids) {
        this.song_ids = song_ids;
    }
}
