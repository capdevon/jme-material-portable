package com.test;

import java.io.IOException;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.exporter.JsonMaterialExporter;
import com.jme3.system.JmeContext;

public class Test_JsonMaterial extends SimpleApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
//        String home = System.getProperty("user.dir");
//        String fileName = home + "/src/main/resources/MatDefs/Material2.json";
//        fromJson(fileName);
        
        Test_JsonMaterial app = new Test_JsonMaterial();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        Material mat = assetManager.loadMaterial("Models/Ferrari/Car.j3m");
        JsonMaterialExporter exporter = new JsonMaterialExporter();
        try {
            exporter.save(mat, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }
    
    /**
     */
//    public static void fromJson(String fileName) {
//        
//        try (FileReader reader = new FileReader(fileName)) {
//            Gson gson = new Gson();
//
//            // Deserialize JSON into a Material object
//            JsonMaterial material = gson.fromJson(reader, JsonMaterial.class);
//
//            // Access data from the Material object
//            System.out.println("Material Name: " + material.getName());
//            System.out.println("Material Definition: " + material.getDef());
//
//            // Access MaterialParameters list
//            List<JsonMatParam> parameters = material.getMaterialParameters();
//            for (JsonMatParam param : parameters) {
//                System.out.println(param);
//            }
//
//            // Access AdditionalRenderState
//            JsonRenderState renderState = material.getAdditionalRenderState();
//            System.out.println(renderState);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    
}