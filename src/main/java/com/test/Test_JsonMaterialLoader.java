package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.JsonMaterialKey;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.plugins.JsonMaterialLoader;
import com.jme3.system.JmeContext;

/**
 * 
 * @author capdevon
 */
public class Test_JsonMaterialLoader extends SimpleApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_JsonMaterialLoader app = new Test_JsonMaterialLoader();
        app.start(JmeContext.Type.Headless);
    }
    
    @Override
    public void simpleInitApp() {
//        for (int i = 0; i < 2; i++) {
//            Material mat = assetManager.loadMaterial("Models/Ferrari/Car.j3m");
//            System.out.println(mat);
//        }
//        
//        for (int i = 0; i < 2; i++) {
//            MaterialDef def = assetManager.loadAsset(new AssetKey<MaterialDef>("MatDefs/Lighting.j3md"));
//            System.out.println(def);
//        }
        
        assetManager.registerLoader(JsonMaterialLoader.class, "json");
        //loadMaterialDef();
        loadMaterial();
        stop();
    }

    /**
     */
    private void loadMaterialDef() {
        AssetKey<MaterialDef> key2 = new AssetKey<>("MatDefs/New/PBRLighting.json");
        MaterialDef matDef = assetManager.loadAsset(key2);
        
        MaterialUtils.print(matDef);
    }

    /**
     */
    private void loadMaterial() {
        String fileName = "Materials/Material.json";
        JsonMaterialLoader loader = new JsonMaterialLoader();
        Material mat = loader.loadMaterial(assetManager, new JsonMaterialKey(fileName));
        
//        Material mat = assetManager.loadAsset(new JsonMaterialKey(fileName));
//        Material mat = assetManager.loadMaterial(fileName);
        
        MaterialUtils.print(mat);
    }
    
}
