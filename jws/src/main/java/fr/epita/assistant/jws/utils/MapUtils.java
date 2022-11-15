package fr.epita.assistant.jws.utils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MapUtils {

    public List<String> getMap() {
        List<String> allLines = new ArrayList<>();
        try {
            String toto = System.getenv("JWS_MAP_PATH");
            if (toto == null) {
                toto = "src/test/resources/map1.rle";
            }
            allLines = Files.readAllLines(Paths.get(toto));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return allLines;
    }
}
