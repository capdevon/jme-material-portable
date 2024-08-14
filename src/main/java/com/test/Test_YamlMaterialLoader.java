package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.YamlMaterialDefKey;
import com.jme3.asset.YamlMaterialKey;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.plugins.YamlMaterialLoader;
import com.jme3.system.JmeContext;

/**
 * 
 * @author capdevon
 */
public class Test_YamlMaterialLoader extends SimpleApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_YamlMaterialLoader app = new Test_YamlMaterialLoader();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(YamlMaterialLoader.class, "yaml");
        //loadMaterialDef();
        loadMaterial();
        stop();
    }
    
    /**
     */
    private void loadMaterialDef() {
//        YamlMaterialLoader loader = new YamlMaterialLoader();
//        MaterialDef matDef = loader.loadMaterialDef(assetManager, "MatDefs/New/PBRLighting2.yaml");
        
//        YamlMaterialDefKey key = new YamlMaterialDefKey("MatDefs/New/PBRLighting.yaml");
        AssetKey<MaterialDef> key2 = new AssetKey<>("MatDefs/New/PBRLighting.yaml");
        MaterialDef matDef = assetManager.loadAsset(key2);
        
        MaterialUtils.print(matDef);
    }

    /**
     */
    private void loadMaterial() {
//        YamlMaterialLoader loader = new YamlMaterialLoader();
//        Material mat = loader.loadMaterial(assetManager, "Materials/Material.yaml");
        
        YamlMaterialKey key = new YamlMaterialKey("Materials/Material.yaml");
        Material mat = assetManager.loadAsset(key);
        
        MaterialUtils.print(mat);
    }

}
