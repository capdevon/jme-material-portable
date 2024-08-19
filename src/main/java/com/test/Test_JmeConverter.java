package com.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.MaterialKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.material.exporter.JsonMaterialExporter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;

/**
 * A simple application to convert JME models and materials.
 * This application loads a model, processes its materials, and exports them to JSON and J3O formats.
 * 
 * @author capdevon
 */
public class Test_JmeConverter extends SimpleApplication {

    private static final String RESOURCE_DIR    = "src/main/resources/";
    private static final String ASSET_DIR       = "Models/YBot";
    private static final String CHARACTER_MODEL = "YBot"; // Main Character
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Test_JmeConverter app = new Test_JmeConverter();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        Spatial myModel = assetManager.loadModel(ASSET_DIR + "/" + CHARACTER_MODEL + ".gltf");

        removeEmptyNode(myModel);

        String dirName = RESOURCE_DIR + ASSET_DIR;
        myModel.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                System.out.println("Processing: " + geom);
                String fileName = geom.getName();
                
                Material mat = geom.getMaterial();
                mat.setName(fileName);
                
                File file = new File(dirName, fileName + ".json");
                MaterialKey key = new MaterialKey(ASSET_DIR + "/" + file.getName());
                mat.setKey(key);
                writeJ3m(mat, file);
            }
        });

        File fout = new File(dirName, CHARACTER_MODEL + ".j3o");
        writeJ3o(myModel, fout);

        stop();
        System.out.println("Done!");
    }
    
    /**
     * Removes empty nodes from the model.
     * 
     * @param model the spatial model to process
     */
    private void removeEmptyNode(Spatial model) {
        model.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (node.getQuantity() == 0) {
                    node.removeFromParent();
                }
            }
        });
    }

    /**
     * Writes the model to a J3O file.
     * 
     * @param model the spatial model to export
     * @param file the file to write to
     */
    private void writeJ3o(Spatial model, File file) {
        try {
            BinaryExporter exporter = BinaryExporter.getInstance();
            exporter.save(model, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Writes the material to a JSON file.
     * 
     * @param mat the material to export
     * @param file the file to write to
     */
    private void writeJ3m(Material mat, File file) {
        try {
            JsonMaterialExporter exporter = new JsonMaterialExporter();
            exporter.setMatDefNameProcessor(new MatDefNameProcessor(Format.JSON));
            exporter.save(mat, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static enum Format { JSON, YAML }
    
    /**
     * This class processes material definition asset names by potentially replacing
     * them with new names based on a pre-defined mapping.
     */
    class MatDefNameProcessor implements UnaryOperator<String> {

        private Map<String, String> map = new HashMap<>();

        /**
         * Creates a new MatDefNameProcessor instance using the provided format.
         * 
         * @param format The format to be used for generating the suffix.
         */
        public MatDefNameProcessor(Format format) {
            String ext = "." + format.name().toLowerCase();
            map.put("Common/MatDefs/Light/Lighting.j3md", "MatDefs/New/Lighting" + ext);
            map.put("Common/MatDefs/Light/PBRLighting.j3md", "MatDefs/New/PBRLighting" + ext);
        }

        @Override
        public String apply(String assetName) {
            return map.getOrDefault(assetName, assetName);
        }

    }

}
