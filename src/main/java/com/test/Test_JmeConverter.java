package com.test;

import java.io.File;
import java.io.IOException;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.MaterialKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.material.exporter.JsonMaterialExporter2;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;

/**
 * 
 * @author capdevon
 */
public class Test_JmeConverter extends SimpleApplication {

    private static final String resources = "src/main/resources/";
    private static final String inputDir = "Models/YBot";
    private static final String characterModel = "YBot"; // Main Character (T-pose)
    
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
        Spatial myModel = assetManager.loadModel(inputDir + "/" + characterModel + ".gltf");

        removeEmptyNode(myModel);

        String dirName = resources + inputDir;
        myModel.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                System.out.println(geom);
                Material mat = geom.getMaterial();
                mat.setName(geom.getName());
                
                File file = new File(dirName, geom.getName() + ".json");
                MaterialKey key = new MaterialKey(inputDir + "/" + file.getName());
                mat.setKey(key);
                writeJ3m(mat, file);
            }
        });

        File fout = new File(dirName, characterModel + ".j3o");
        writeJ3o(myModel, fout);

        stop();
    }
    
    private void removeEmptyNode(Spatial model) {
        model.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (node.getChildren().isEmpty()) {
                    System.out.println("Removing " + node);
                    node.removeFromParent();
                }
            }
        });
    }

    /**
     * Save spatial to j3o file.
     *
     * @param sp
     * @param file
     */
    private void writeJ3o(Spatial sp, File file) {
        try {
            System.out.println("Converting j3o: " + file.getAbsolutePath());
            BinaryExporter exporter = BinaryExporter.getInstance();
            exporter.save(sp, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save Material to j3m file.
     *
     * @param material
     * @param file
     */
    private void writeJ3m(Material mat, File file) {
        try {
            System.out.println("Writing material:" + file.getAbsolutePath());
            System.out.println(mat.getKey().getFolder() + " " + mat.getKey().getName() + " " + mat.getKey().getExtension());
            JsonMaterialExporter2 exporter = new JsonMaterialExporter2();
            exporter.save(mat, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
