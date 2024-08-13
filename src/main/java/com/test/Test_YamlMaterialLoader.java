package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.YamlMaterialKey;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.plugins.YamlMaterialLoader;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
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
        YamlMaterialLoader loader = new YamlMaterialLoader();
//        MaterialDef matDef = loader.loadMaterialDef(assetManager, "MatDefs/New/PBRLighting2.yaml");
        MaterialDef matDef = assetManager.loadAsset(new AssetKey<MaterialDef>("MatDefs/New/PBRLighting2.yaml"));
//        MaterialDef def = assetManager.loadAsset(new JsonMaterialDefKey("MatDefs/New/PBRLighting.yaml"));
        
        MaterialDebug.print(matDef);
    }

    /**
     */
    private void loadMaterial() {
        YamlMaterialKey key = new YamlMaterialKey("Materials/Material.yaml");
        Material mat = assetManager.loadAsset(key);
        
//        YamlMaterialLoader loader = new YamlMaterialLoader();
//        Material mat = loader.loadMaterial(assetManager, "Materials/Material.yaml");
        
        MaterialDebug.print(mat);
    }

    /**
     * @param mat
     */
    private void loadModel(Material mat) {
        Spatial model = assetManager.loadModel("Models/Ferrari/Car.j3o");
        model.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                geom.setMaterial(mat);
            }
        });
        
        rootNode.attachChild(model);
    }
    
    private void setupScene() {
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        flyCam.setMoveSpeed(20);
        
        rootNode.addLight(new AmbientLight());
        rootNode.addLight(new DirectionalLight(new Vector3f(0, -1, 0)));
    }

}
