package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.YamlMaterialKey;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.plugins.YamlMaterialLoader;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;

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
        app.start();
//        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(YamlMaterialLoader.class, "yaml");

//        YamlMaterialKey key = new YamlMaterialKey("MatDefs/New/Material.yaml");
//        Material mat = assetManager.loadAsset(key);
        
        YamlMaterialLoader loader = new YamlMaterialLoader();
        Material mat = loader.loadMaterial(assetManager, "MatDefs/New/Material.yaml");
        
        MaterialDebug.print(mat);
        
        loadModel(mat);
//        stop();
    }

    /**
     * @param mat
     */
    private void loadModel(Material mat) {
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        flyCam.setMoveSpeed(20);
        
        Spatial model = assetManager.loadModel("Models/Ferrari/Car.j3o");
        model.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                geom.setMaterial(mat);
            }
        });
        
        rootNode.attachChild(model);
        rootNode.addLight(new AmbientLight());
        rootNode.addLight(new DirectionalLight(new Vector3f(0, -1, 0)));
    }

}
