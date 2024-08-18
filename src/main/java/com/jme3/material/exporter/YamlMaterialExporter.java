package com.jme3.material.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jme3.material.Material;
import com.jme3.material.json.JsonMaterial;

/**
 * 
 * @author capdevon
 */
public class YamlMaterialExporter extends AbstractMaterialExporter {
    
    private static final Logger logger = Logger.getLogger(YamlMaterialExporter.class.getName());

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

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String yaml = mapper.writeValueAsString(data);
        logger.log(Level.INFO, yaml);
        
        // Write YAML String to file
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(yaml);
        }
    }

}
