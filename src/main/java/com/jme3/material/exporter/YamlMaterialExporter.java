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
import com.jme3.material.exporter.MatDefNameProcessor.Format;
import com.jme3.material.json.JsonMaterial;

/**
 * 
 * @author capdevon
 */
public class YamlMaterialExporter extends AbstractMaterialExporter {
    
    private static final Logger logger = Logger.getLogger(YamlMaterialExporter.class.getName());

    public YamlMaterialExporter() {
        matDefNameProcessor = new MatDefNameProcessor(Format.YAML);
    }
    
    /**
     * Saves the given Material object to the specified File in YAML format.
     *
     * @param material the Material object to be saved
     * @param file     the File to which the YAML representation of the Material
     *                 object will be written
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void save(Material material, File file) throws IOException {
        JsonMaterial mat = toJson(material);

        Map<String, Object> data = new HashMap<>();
        data.put("material", mat);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String yaml = mapper.writeValueAsString(data);
        logger.log(Level.FINE, yaml);
        
        // Write YAML String to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(yaml);
        }
    }

}
