package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.JsonMaterialKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
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
        JsonMaterialKey key = new JsonMaterialKey("MatDefs/Material.json");
        Material mat = assetManager.loadAsset(key);
        System.out.println(mat);
        System.out.println(mat.getKey());
        System.out.println(mat.getAdditionalRenderState());

        for (MatParam param : mat.getParams()) {
            System.out.println(param);
        }
        
        for (int i = 0; i < 2; i++) {
            MaterialDef def = assetManager.loadAsset(new AssetKey<MaterialDef>("MatDefs/MatDef.json"));
//            MaterialDef def = assetManager.loadAsset(new JsonMaterialDefKey("MatDefs/MatDef.json"));
            System.out.println(def);
            
            StringBuilder sb = new StringBuilder();
            sb.append("name : ").append(def.getName()).append("\n");
            for (MatParam param : def.getMaterialParams()) {
                sb.append(param);
                if (param instanceof MatParamTexture) {
                    sb.append(" ").append(((MatParamTexture) param).getColorSpace());
                }
                sb.append("\n");
            }
            System.out.println(sb.toString());
        }
        stop();
    }
    
}
