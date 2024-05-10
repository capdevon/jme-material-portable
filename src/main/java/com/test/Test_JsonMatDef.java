package com.test;

import java.io.IOException;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.material.MaterialDef;
import com.jme3.material.exporter.JsonMatDefExporter;
import com.jme3.system.JmeContext;

public class Test_JsonMatDef extends SimpleApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Test_JsonMatDef app = new Test_JsonMatDef();
        app.start(JmeContext.Type.Headless);
    }
    
    @Override
    public void simpleInitApp() {
        MaterialDef mat = assetManager.loadAsset(new AssetKey<MaterialDef>("MatDefs/PBRLighting.j3md"));
        JsonMatDefExporter exporter = new JsonMatDefExporter();
        try {
            exporter.save(mat, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }

}
