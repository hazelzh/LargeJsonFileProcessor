import Model.Changes;
import Model.Playlist;
import Model.Update;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistProcessor {
    /**
     * Since mixtape.json may be a large file, I avoided to load the entire json into memory, so I use jackson stream
     * API to solve the problem
     * */
    public static void main(String[] args) throws IOException {
        String mixtapePath = args[0]; // "/Users/zhaohaiyan/IdeaProjects/LargeJsonFileProcessor/src/main/resources/mixtape.json";
        String changesPath = args[1]; // "/Users/zhaohaiyan/IdeaProjects/LargeJsonFileProcessor/src/main/resources/changes.json";
        String outputPath = args[2] + "/output.json"; // "/Users/zhaohaiyan/IdeaProjects/LargeJsonFileProcessor/src/main/resources/output.json";

        String playlistFiledName = "playlists";
        JsonFactory jsonfactory = new JsonFactory();
        /*
        * for reading JSON content
        * */
        JsonParser jsonParser = jsonfactory.createParser(new File(mixtapePath));

        /*
        * for writing JSON content
        * */
        JsonGenerator jsonGenerator = jsonfactory.createGenerator(new File(outputPath), JsonEncoding.UTF8);

        try {
            /*
            * Enumeration for basic token types used for returning results of parsing JSON content.
            * */
            JsonToken jsonToken = jsonParser.nextToken();

            if (jsonToken == JsonToken.START_OBJECT) { // START_OBJECT == "{"
                jsonGenerator.writeStartObject();
                jsonToken = jsonParser.nextToken();
            }
            while (jsonParser.hasCurrentToken()) {
                if (jsonToken == JsonToken.FIELD_NAME) {
                    String fieldName = jsonParser.getCurrentName();
                    jsonGenerator.writeFieldName(fieldName);
                    if (playlistFiledName.equals(fieldName)) {
                        updatePlaylists(jsonGenerator, jsonParser, changesPath);
                    }
                } else if (jsonToken == JsonToken.START_ARRAY) {
                    jsonGenerator.writeStartArray();
                } else if (jsonToken == JsonToken.START_OBJECT) {
                    jsonGenerator.writeStartObject();
                } else if (jsonToken == JsonToken.VALUE_STRING) {
                    jsonGenerator.writeString(jsonParser.getValueAsString());
                } else if (jsonToken == JsonToken.END_OBJECT) {
                    jsonGenerator.writeEndObject();
                } else if (jsonToken == JsonToken.END_ARRAY) {
                    jsonGenerator.writeEndArray();
                }
                jsonToken = jsonParser.nextToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jsonGenerator.close();
            jsonParser.close();
        }
    }

    /**
     *  change playlist based on the changes file
     * */
    private static void updatePlaylists(JsonGenerator jsonGenerator, JsonParser jsonParser, String changesFile) throws IOException {
        jsonParser.nextToken(); // skip fieldName
        JsonToken jsonToken = jsonParser.nextToken(); //skip "[" and set "{" as jsonToken
        Map<String, List<Update>> updates = new HashMap<>(); // operations including removing and adding a song to a playlist
        List<Playlist> newPlaylists = new ArrayList<>(); // operations including adding a new playlist
        getChanges(updates, newPlaylists, changesFile); // setting updates and newPlaylists
        jsonGenerator.writeStartArray(); // write "[" to file
        while(jsonToken != JsonToken.END_ARRAY) {
            Playlist curPlaylist = readPlaylist(jsonParser);
            if (updates.containsKey(curPlaylist.getId())) {
                for (Update u: updates.get(curPlaylist.getId())) {
                    if ("addSong".equals(u.getOperation())) {
                        curPlaylist.getSong_ids().add(u.getData());
                    } else if ("remove".equals(u.getOperation())) {
                        curPlaylist = null;
                    }
                }
            }
            if (curPlaylist != null) {
                writePlaylist(curPlaylist, jsonGenerator);
            }
            jsonToken = jsonParser.currentToken();
        }
        for (Playlist curPlaylist : newPlaylists) {
            writePlaylist(curPlaylist, jsonGenerator);
        }
        jsonGenerator.writeEndArray();


    }

    /**
     * append a playlist to file
     * */
    private static void writePlaylist(Playlist playlist, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", playlist.getId());
        jsonGenerator.writeStringField("user_id", playlist.getUser_id());
        jsonGenerator.writeFieldName("song_ids");
        jsonGenerator.writeStartArray();
        if (playlist.getSong_ids() != null) {
            for (String song : playlist.getSong_ids()) {
                jsonGenerator.writeString(song);
            }
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    /**
     * read changes file to 2 variables: updates and newPlaylists
     * */
    private static void getChanges(Map<String, List<Update>> updates, List<Playlist> newPlaylists, String changesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Changes changes = mapper.readValue(new File(changesFile), Changes.class);
        for (Update u : changes.getUpdates()) {
            List<Update> updateList = null;
            if (updates.containsKey(u.getId())) {
                updateList = updates.get(u.getId());
            } else {
                updateList = new ArrayList<>();
                updates.put(u.getId(), updateList);
            }
            updateList.add(u);
        }
        newPlaylists.addAll(changes.getAdds());
    }

    /**
     * read a playlist from file
     * */
    private static Playlist readPlaylist(JsonParser jsonParser) throws IOException {

        Playlist playlist = new Playlist();
        JsonToken jsonToken = jsonParser.getCurrentToken();
        if (jsonToken == JsonToken.START_OBJECT) {
            jsonToken = jsonParser.nextToken();
        }
        while (jsonToken != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            jsonToken = jsonParser.nextToken();
            if ("id".equals(fieldName)) {
                playlist.setId(jsonParser.getText());
            } else if ("user_id".equals(fieldName)) {
                playlist.setUser_id(jsonParser.getText());
            } else if ("song_ids".equals(fieldName)) {
                playlist.setSong_ids(readArray(jsonParser));
            }
            jsonToken = jsonParser.nextToken();
        }
        jsonParser.nextToken(); // skip END_OBJECT("}")
        return playlist;
    }

    /**
     * read an string array from file
     * */
    private static List<String> readArray(JsonParser jsonParser) throws IOException {
        List<String> array = new ArrayList<>();
        JsonToken token = jsonParser.nextToken(); // skip '['
        while (token != JsonToken.END_ARRAY) {
            array.add(jsonParser.getText());
            token = jsonParser.nextToken();
        }
        return array;
    }

}
