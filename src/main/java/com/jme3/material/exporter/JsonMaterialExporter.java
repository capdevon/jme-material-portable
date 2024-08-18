package com.jme3.material.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.material.Material;
import com.jme3.material.json.JsonMaterial;

/**
 * 
 * @author capdevon
 */
public class JsonMaterialExporter extends AbstractMaterialExporter {
    
    private static final Logger logger = Logger.getLogger(JsonMaterialExporter.class.getName());
        
    /**
     * 
     * @param material
     * @param f
     * @throws IOException
     */
    public void save(Material material, File f) throws IOException {
        JsonMaterial mat = toJson(material);
        
        Map<String, Object> data = new HashMap<>();
        data.put("Material", mat);

        // Convert JsonObject to String
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(data);
        logger.log(Level.INFO, jsonString);

        // Write JSON String to file
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(jsonString);
        }
    }

}