package com.test;

import java.io.IOException;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.exporter.YamlMaterialExporter;
import com.jme3.system.JmeContext;

/**
 * 
 * @author capdevon
 */
public class Test_YamlMaterial extends SimpleApplication {
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_YamlMaterial app = new Test_YamlMaterial();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        Material mat = assetManager.loadMaterial("Models/Ferrari/Car.j3m");
        YamlMaterialExporter exporter = new YamlMaterialExporter();
        try {
            exporter.save(mat, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }

}