package Model;

import java.util.List;

public class Changes {
    private List<Playlist> adds;
    private List<Update> updates;

    public List<Playlist> getAdds() {
        return adds;
    }

    public void setAdds(List<Playlist> adds) {
        this.adds = adds;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }
}
