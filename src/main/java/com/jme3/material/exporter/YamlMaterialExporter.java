package com.jme3.material.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

//        DumperOptions options = new DumperOptions();
//        options.setIndent(2);
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//        options.setPrettyFlow(true);
//        
//        Representer representer = new Representer() {
//            @Override
//            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
//                // if value of property is null, ignore it.
//                if (propertyValue == null) {
//                    return null;
//                }
//                return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
//            }
//        };
//        representer.addClassTag(JsonMaterial.class, Tag.MAP);

//        Yaml yaml = new Yaml(representer, options);
//        String yamlString = yaml.dumpAsMap(mat);
//        System.out.println(yamlString);

//        try (FileWriter writer = new FileWriter("TestMaterial.yaml")){
//            yaml.dump(mat, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
        // Convert POJO to JSON using Jackson
//      ObjectMapper mapper = new ObjectMapper();
//      String json = mapper.writeValueAsString(mat);
//
//      Yaml yaml = new Yaml();
//      Object yamlObject = yaml.load(json);
//      System.out.println(yaml.dump(yamlObject));

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String yaml = mapper.writeValueAsString(mat);
        logger.log(Level.INFO, yaml);
        
        // Write YAML String to file
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(yaml);
        }
    }

}
