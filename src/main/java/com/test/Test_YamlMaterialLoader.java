package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.YamlMaterialKey;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
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

        YamlMaterialKey key = new YamlMaterialKey("MatDefs/New/Material.yaml");
        Material mat = assetManager.loadAsset(key);
        printDebug(mat);
        
        stop();
    }

    /**
     * @param mat
     */
    private void printDebug(Material mat) {
        System.out.println(mat);
        System.out.println("materialParameters:");
        for (MatParam param : mat.getParams()) {
            System.out.printf("    %s %n", param);
        }
        System.out.println(mat.getAdditionalRenderState());
    }

}
