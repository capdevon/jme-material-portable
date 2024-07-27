package com.test;

import java.util.Collection;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.JsonMaterialKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.material.plugins.JsonMaterialLoader;
import com.jme3.shader.UniformBinding;
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
//        loadMaterial();
        
        for (int i = 0; i < 2; i++) {
            loadMaterialDef();
        }
        stop();
    }

    /**
     */
    private void loadMaterialDef() {
        MaterialDef matDef = assetManager.loadAsset(new AssetKey<MaterialDef>("MatDefs/MatDef.json"));
//        MaterialDef def = assetManager.loadAsset(new JsonMaterialDefKey("MatDefs/MatDef.json"));
        System.err.println(matDef);

        StringBuilder sb = new StringBuilder();
        sb.append("name : ").append(matDef.getName()).append("\n");
        Collection<MatParam> matParams = matDef.getMaterialParams();
        
        for (MatParam param : matParams) {
            sb.append(param);
            if (param instanceof MatParamTexture) {
                sb.append(" ").append(((MatParamTexture) param).getColorSpace());
            }
            sb.append("\n");
        }
        System.out.println(sb.toString() + "\n");

        for (String defName : matDef.getTechniqueDefsNames()) {
            System.out.println("____" + defName + "____");
            
            List<TechniqueDef> defs = matDef.getTechniqueDefs(defName);
            for (TechniqueDef technique : defs) {
                System.out.println(technique);

                // World params
                if (!technique.getWorldBindings().isEmpty()) {
                    for (UniformBinding uniformBinding : technique.getWorldBindings()) {
                        System.out.println("\t" + uniformBinding.toString());
                    }
                }

                // Defines
                if (technique.getDefineNames().length != 0) {
                    for (int i = 0; i < technique.getDefineNames().length; i++) {
                        String matParamName = getMatParamNameForDefineId(technique, matParams, i);
                        if (matParamName != null) {
                            String defineName = technique.getDefineNames()[i];
                            System.out.println(defineName + ": " + matParamName);
                        }
                    }
                }
            }
        }
    }
    
    private String getMatParamNameForDefineId(TechniqueDef techniqueDef, Collection<MatParam> matParams, int defineId) {
        for (MatParam matParam : matParams) {
            Integer id = techniqueDef.getShaderParamDefineId(matParam.getName());
            if (id != null && id == defineId) {
                return matParam.getName();
            }
        }
        return null;
    }

    /**
     */
    private void loadMaterial() {
        JsonMaterialKey key = new JsonMaterialKey("MatDefs/Material.json");
        Material mat = assetManager.loadAsset(key);
        
        System.out.println(mat);
        System.out.println("materialParameters:");
        for (MatParam param : mat.getParams()) {
            System.out.printf("    %s %n", param);
        }
        System.out.println(mat.getAdditionalRenderState());
    }
    
}
