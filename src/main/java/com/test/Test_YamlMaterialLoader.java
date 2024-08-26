package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
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
    
    private void loadMaterialDef() {
        AssetKey<MaterialDef> key2 = new AssetKey<>("MatDefs/New/PBRLighting2.yaml");
        MaterialDef matDef = assetManager.loadAsset(key2);
        
        MaterialSerializer.print(matDef);
    }

    private void loadMaterial() {
        String fileName = "Materials/Material.yaml";
        YamlMaterialLoader loader = new YamlMaterialLoader();
        Material mat = loader.loadMaterial(assetManager, new YamlMaterialKey(fileName));
        
//        Material mat = assetManager.loadAsset(new YamlMaterialKey(fileName));
//        Material mat = assetManager.loadMaterial(fileName);
        
        String[] defs = {
                "MatDefs/New/Lighting.yaml",
                "MatDefs/New/PBRLighting2.yaml",
                "MatDefs/New/Unshaded.yaml",
                "MatDefs/New/ShowNormals.yaml"
        };
        for (String defName : defs) {
            Material material = new Material(assetManager, defName);
            MaterialSerializer.print(material);
        }
    }

}
